package edu.fpt.hsf302.aichat.service.impl;

import edu.fpt.hsf302.aichat.dto.RagAnswer;
import edu.fpt.hsf302.aichat.service.DocumentService;
import edu.fpt.hsf302.aichat.service.PromptRules;
import edu.fpt.hsf302.aichat.service.RagChatService;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RagChatServiceImpl implements RagChatService {

    @Autowired
    private EmbeddingModel embeddingModel;   // Gemini

    @Autowired
    private ChatModel chatModel;             // Groq

    @Autowired
    private EmbeddingStore<TextSegment> store;

    @Autowired
    private DocumentService documentService;

    @Value("${app.rag.top-k}")
    private int topK;

    @Value("${app.rag.min-score}")
    private double minScore;

    @Override
    public RagAnswer ask(String question) {
        if (documentService.getDocuments().isEmpty()) {
            return new RagAnswer("Chưa có tài liệu nào. Hãy upload file .txt ở khung bên trên rồi hỏi lại.",
                    new ArrayList<>());
        }

        // 1. Embed câu hỏi (Gemini) và tìm các đoạn gần nghĩa nhất
        Embedding queryEmbedding = embeddingModel.embed(question).content();
        List<EmbeddingMatch<TextSegment>> matches = store.search(
                EmbeddingSearchRequest.builder()
                        .queryEmbedding(queryEmbedding)
                        .maxResults(topK)
                        .minScore(minScore)
                        .build()
        ).matches();

        if (matches.isEmpty()) {
            return new RagAnswer("Không tìm thấy thông tin liên quan trong tài liệu.", new ArrayList<>());
        }

        // 2. Ghép các đoạn tìm được thành context
        StringBuilder context = new StringBuilder();
        for (EmbeddingMatch<TextSegment> m : matches) {
            context.append(m.embedded().text()).append("\n\n");
        }

        // 3. Gửi context + câu hỏi cho model chat (Groq)

        String rules = """
        Chỉ dùng thông tin trong phần <context> để trả lời bằng tiếng Việt.
        Nếu context không có thông tin, hãy trả lời: "Tôi không tìm thấy thông tin này trong tài liệu."
        """ + PromptRules.PLAIN_TEXT;

        String userPrompt = """
        <context>
        %s
        </context>

        Câu hỏi: %s
        """.formatted(context, question);

        String answer = chatModel.chat(SystemMessage.from(rules), UserMessage.from(userPrompt))
                .aiMessage().text();

        // 4. Danh sách nguồn để hiển thị
        List<String> sources = new ArrayList<>();
        for (EmbeddingMatch<TextSegment> m : matches) {
            String src = m.embedded().metadata().getString("source");
            String preview = m.embedded().text().replace("\n", " ");
            if (preview.length() > 100) preview = preview.substring(0, 100) + "...";
            sources.add(String.format("[%.2f] %s: %s", m.score(), src, preview));
        }
        return new RagAnswer(answer, sources);
    }
}
