package edu.fpt.hsf302.aichat.service.impl;

import edu.fpt.hsf302.aichat.dto.DocumentInfo;
import edu.fpt.hsf302.aichat.service.DocumentService;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

@Service
public class DocumentServiceImpl implements DocumentService {

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private EmbeddingStore<TextSegment> store;

    @Value("${app.rag.chunk-size}")
    private int chunkSize;

    @Value("${app.rag.chunk-overlap}")
    private int chunkOverlap;

    // tên file -> số đoạn (để hiển thị danh sách tài liệu)
    private final Map<String, Integer> documents = new LinkedHashMap<>();

    @Override
    public int addDocument(String fileName, String content) {
        // Không khoá phần split + gọi Gemini (chậm), để upload không chặn các request khác
        Document doc = Document.from(content, Metadata.from("source", fileName));

        List<TextSegment> segments = DocumentSplitters.recursive(chunkSize, chunkOverlap).split(doc);

        List<Embedding> embeddings = embeddingModel.embedAll(segments).content(); // gọi Gemini

        // chỉ xoá sau khi embed thành công, để khi lỗi Gemini không làm mất data.
        // Khoá cả 3 bước ghi để không ai thấy trạng thái nửa vời (đã xoá cũ, chưa thêm mới).
        synchronized (this) {
            store.removeAll(metadataKey("source").isEqualTo(fileName));
            store.addAll(embeddings, segments);
            documents.put(fileName, segments.size()); // put ghi đè nếu đã có
        }
        return segments.size();
    }

    @Override
    public synchronized List<DocumentInfo> getDocuments() {
        List<DocumentInfo> list = new ArrayList<>();
        documents.forEach((name, chunks) -> list.add(new DocumentInfo(name, chunks)));
        return list;
    }

    @Override
    public synchronized void clearAll() {
        store.removeAll();
        documents.clear();
    }
}
