package demo.dto;
public record RetrievedChunk(demo.model.SourceType type, Long refId, String displayName, String text, double score) {}
