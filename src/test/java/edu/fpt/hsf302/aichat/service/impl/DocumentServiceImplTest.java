package edu.fpt.hsf302.aichat.service.impl;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit test không cần Spring, không gọi Gemini: dùng EmbeddingModel giả + kho vector thật trong RAM.
 */
class DocumentServiceImplTest {

    // Mọi đoạn đều nhận cùng một vector -> search sẽ trả về tất cả các đoạn trong kho.
    private static final EmbeddingModel FAKE_GEMINI = segments -> Response.from(
            segments.stream().map(s -> Embedding.from(new float[]{1f, 0f})).toList());

    private static final EmbeddingModel BROKEN_GEMINI = segments -> {
        throw new RuntimeException("giả lập Gemini 429");
    };

    // ~1200 ký tự -> vài đoạn với chunk-size 500
    private static final String CONTENT = "Cửa hàng mở cửa từ 8 giờ đến 21 giờ. ".repeat(35);

    private InMemoryEmbeddingStore<TextSegment> store;
    private DocumentServiceImpl service;

    @BeforeEach
    void setUp() {
        store = new InMemoryEmbeddingStore<>();
        service = new DocumentServiceImpl();
        ReflectionTestUtils.setField(service, "embeddingModel", FAKE_GEMINI);
        ReflectionTestUtils.setField(service, "store", store);
        ReflectionTestUtils.setField(service, "chunkSize", 500);
        ReflectionTestUtils.setField(service, "chunkOverlap", 50);
    }

    @Test
    void reUploadSameFile_replacesChunksInsteadOfDoubling() {
        int chunks = service.addDocument("a.txt", CONTENT);
        service.addDocument("a.txt", CONTENT);

        assertThat(chunks).isGreaterThan(1);
        assertThat(chunksInStore()).isEqualTo(chunks);
        assertThat(service.getDocuments()).hasSize(1);
    }

    @Test
    void reUploadFailsAtGemini_keepsOldVersion() {
        int chunks = service.addDocument("a.txt", CONTENT);

        ReflectionTestUtils.setField(service, "embeddingModel", BROKEN_GEMINI);
        assertThatThrownBy(() -> service.addDocument("a.txt", CONTENT + " bản mới"))
                .hasMessageContaining("429");

        assertThat(chunksInStore()).isEqualTo(chunks);
        assertThat(service.getDocuments()).singleElement()
                .satisfies(d -> assertThat(d.getChunks()).isEqualTo(chunks));
    }

    private int chunksInStore() {
        return store.search(EmbeddingSearchRequest.builder()
                .queryEmbedding(Embedding.from(new float[]{1f, 0f}))
                .maxResults(1000)
                .minScore(0.0)
                .build()).matches().size();
    }
}
