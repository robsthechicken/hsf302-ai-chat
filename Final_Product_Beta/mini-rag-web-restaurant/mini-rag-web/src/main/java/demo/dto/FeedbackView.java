package demo.dto;
public record FeedbackView(Long id, String authorName, String content, java.time.LocalDateTime createdAt, demo.model.Relevance relevance, demo.model.Topic topic, demo.model.LabelSource labelSource) {}
