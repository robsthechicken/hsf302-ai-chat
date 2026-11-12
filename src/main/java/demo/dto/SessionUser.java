package demo.dto;
public record SessionUser(Long id, String username, demo.model.Role role) implements java.io.Serializable {}
