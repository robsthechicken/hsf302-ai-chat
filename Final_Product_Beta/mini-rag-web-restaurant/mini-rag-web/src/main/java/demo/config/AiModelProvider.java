package demo.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import org.springframework.stereotype.Component;

/**
 * Nơi DUY NHẤT tạo model AI. Đổi nền tảng chỉ cần sửa application.properties:
 *  - chat:      provider = groq | openai | gemini
 *  - embedding: provider = gemini | openai | groq
 * "groq" và "openai" dùng chung kiểu API của OpenAI, khác nhau ở địa chỉ gốc (base-url).
 *
 * Model được tạo "lười" (lúc dùng lần đầu) để app vẫn chạy được khi chưa có API key
 * và hiện thông báo thân thiện thay vì sập lúc khởi động.
 */
@Component
public class AiModelProvider {

    private static final String GROQ_BASE_URL = "https://api.groq.com/openai/v1";

    private final AiProperties.Chat chatCfg;
    private final AiProperties.Embedding embCfg;
    private ChatModel chatModel;
    private EmbeddingModel embeddingModel;

    public AiModelProvider(AiProperties props) {
        this.chatCfg = props.chat();
        this.embCfg = props.embedding();
    }

    public boolean hasChatKey() {
        return notBlank(chatCfg.apiKey());
    }

    public boolean hasEmbeddingKey() {
        return notBlank(embCfg.apiKey());
    }

    /** Đủ key cho cả chat lẫn embedding (chế độ RAG cần cả hai). */
    public boolean hasApiKey() {
        return hasChatKey() && hasEmbeddingKey();
    }

    public String providerName() {
        return chatCfg.provider().toLowerCase();
    }

    public String chatModelName() {
        return chatCfg.model();
    }

    public synchronized ChatModel chatModel() {
        if (!hasChatKey()) {
            throw new IllegalStateException(
                    "Chưa cấu hình API key cho chat. Hãy đặt biến môi trường GROQ_API_KEY (hoặc sửa ai.chat.api-key) rồi chạy lại.");
        }
        if (chatModel == null) {
            chatModel = switch (providerName()) {
                case "gemini" -> GoogleAiGeminiChatModel.builder()
                        .apiKey(chatCfg.apiKey())
                        .modelName(chatCfg.model())
                        .temperature(chatCfg.temperature())
                        .build();
                case "openai", "groq" -> openAiCompatibleChat();
                default -> throw unknownProvider("ai.chat.provider", chatCfg.provider());
            };
        }
        return chatModel;
    }

    public synchronized EmbeddingModel embeddingModel() {
        if (!hasEmbeddingKey()) {
            throw new IllegalStateException(
                    "Chưa cấu hình API key cho embedding (dùng cho chế độ RAG). Hãy đặt biến môi trường GEMINI_API_KEY (hoặc sửa ai.embedding.api-key) rồi chạy lại.");
        }
        if (embeddingModel == null) {
            String provider = embCfg.provider().toLowerCase();
            embeddingModel = switch (provider) {
                case "gemini" -> GoogleAiEmbeddingModel.builder()
                        .apiKey(embCfg.apiKey())
                        .modelName(embCfg.model())
                        .build();
                case "openai", "groq" -> openAiCompatibleEmbedding(provider);
                default -> throw unknownProvider("ai.embedding.provider", embCfg.provider());
            };
        }
        return embeddingModel;
    }

    /** Cô lập API của model để có thể mock trong test. */
    public String chat(String systemPrompt, String userMessage) {
        return chatModel().chat(SystemMessage.from(systemPrompt), UserMessage.from(userMessage)).aiMessage().text();
    }

    // ---------- helper ----------

    private ChatModel openAiCompatibleChat() {
        var builder = OpenAiChatModel.builder()
                .apiKey(chatCfg.apiKey())
                .modelName(chatCfg.model())
                .temperature(chatCfg.temperature());
        String url = resolveBaseUrl(providerName(), chatCfg.baseUrl());
        if (url != null) builder.baseUrl(url);
        return builder.build();
    }

    private EmbeddingModel openAiCompatibleEmbedding(String provider) {
        var builder = OpenAiEmbeddingModel.builder()
                .apiKey(embCfg.apiKey())
                .modelName(embCfg.model());
        String url = resolveBaseUrl(provider, embCfg.baseUrl());
        if (url != null) builder.baseUrl(url);
        return builder.build();
    }

    /** base-url tự điền trong properties thì dùng nó; không thì groq có địa chỉ riêng, openai dùng mặc định. */
    private String resolveBaseUrl(String provider, String configured) {
        if (notBlank(configured)) return configured.trim();
        if ("groq".equals(provider)) return GROQ_BASE_URL;
        return null;
    }

    private IllegalStateException unknownProvider(String key, String value) {
        return new IllegalStateException(key + " không hợp lệ: '" + value + "'. Chỉ hỗ trợ: groq, openai, gemini.");
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}
