package demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Đọc các dòng "ai.chat.*" và "ai.embedding.*" trong application.properties.
 * Chat và embedding cấu hình RIÊNG vì có thể dùng 2 nền tảng khác nhau
 * (vd chat bằng Groq, embedding bằng Gemini).
 */
@ConfigurationProperties(prefix = "ai")
public record AiProperties(@DefaultValue Chat chat, @DefaultValue Embedding embedding) {

    public record Chat(
            @DefaultValue("groq") String provider,
            String apiKey,
            @DefaultValue("llama-3.3-70b-versatile") String model,
            String baseUrl,
            @DefaultValue("0.2") double temperature) {
    }

    public record Embedding(
            @DefaultValue("gemini") String provider,
            String apiKey,
            @DefaultValue("gemini-embedding-001") String model,
            String baseUrl) {
    }
}
