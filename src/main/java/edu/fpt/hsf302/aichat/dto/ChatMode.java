package edu.fpt.hsf302.aichat.dto;

/** Hai chế độ chat. Tên enum (NORMAL/RAG) chính là giá trị tham số "mode" trên URL. */
public enum ChatMode {
    NORMAL("AI thường"),
    RAG("AI RAG");

    private final String label;

    ChatMode(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
