package demo.config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
@ConfigurationProperties(prefix = "rag")
public record RagProperties(@DefaultValue("500") int chunkSize,
                            @DefaultValue("50") int chunkOverlap,
                            @DefaultValue FeedbackRetrieval feedback,
                            @DefaultValue NoteRetrieval note) {
    public record FeedbackRetrieval(@DefaultValue("5") int topK, @DefaultValue("0.4") double minScore) {}
    public record NoteRetrieval(@DefaultValue("3") int topK, @DefaultValue("0.4") double minScore) {}
}
