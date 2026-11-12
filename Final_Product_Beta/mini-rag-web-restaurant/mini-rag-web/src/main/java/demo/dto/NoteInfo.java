package demo.dto;
public record NoteInfo(Long id, String title, demo.model.NoteOrigin origin, String sourceFileName, java.time.LocalDateTime createdAt, String content, String preview) {}
