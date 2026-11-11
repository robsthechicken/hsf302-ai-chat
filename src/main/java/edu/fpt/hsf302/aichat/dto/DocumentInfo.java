package edu.fpt.hsf302.aichat.dto;

/** Thông tin một tài liệu đã upload, để hiển thị lên trang. */
public class DocumentInfo {

    private final String name;
    private final int chunks;

    public DocumentInfo(String name, int chunks) {
        this.name = name;
        this.chunks = chunks;
    }

    public String getName() { return name; }
    public int getChunks() { return chunks; }
}
