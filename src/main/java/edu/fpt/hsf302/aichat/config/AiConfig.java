package edu.fpt.hsf302.aichat.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Khai báo 3 bean dùng chung cho cả app:
 *  - ChatModel      : Groq   (trả lời)      -> dùng ở cả AI thường lẫn AI RAG
 *  - EmbeddingModel : Gemini (tạo vector)   -> chỉ dùng ở AI RAG
 *  - EmbeddingStore : kho vector lưu trong RAM (tắt app là mất)
 * Hai model dùng hai API key riêng nên tách thành hai bean riêng.
 */
@Configuration
public class AiConfig {

    @Value("${app.ai.groq.api-key:}")
    private String groqApiKey;

    @Value("${app.ai.groq.base-url}")
    private String groqBaseUrl;

    @Value("${app.ai.groq.model}")
    private String groqModel;

    @Value("${app.ai.gemini.api-key:}")
    private String geminiApiKey;

    @Value("${app.ai.gemini.embedding-model}")
    private String embeddingModelName;

    @Bean
    public ChatModel chatModel() {
        requireKey(groqApiKey, "GROQ_API_KEY");
        return OpenAiChatModel.builder()
                .baseUrl(groqBaseUrl)
                .apiKey(groqApiKey)
                .modelName(groqModel)
                .temperature(0.2)
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        requireKey(geminiApiKey, "GEMINI_API_KEY");
        return GoogleAiEmbeddingModel.builder()
                .apiKey(geminiApiKey)
                .modelName(embeddingModelName)
                .build();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    private void requireKey(String key, String envName) {
        if (key == null || key.isBlank()) {
            throw new IllegalStateException("Thiếu biến môi trường " + envName
                    + ". Đặt trong Run > Edit Configurations > Environment variables rồi chạy lại.");
        }
    }
}
