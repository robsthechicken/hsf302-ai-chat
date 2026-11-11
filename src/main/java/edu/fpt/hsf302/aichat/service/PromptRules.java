package edu.fpt.hsf302.aichat.service;

public final class PromptRules {
    public static final String PLAIN_TEXT = """
        Trả lời bằng văn bản thuần, không dùng markdown.
        Không dùng các ký tự *, #, ` và không kẻ bảng.
        Nếu cần tiêu đề, viết tiêu đề trên một dòng riêng và kết thúc bằng dấu hai chấm.
        Nếu cần liệt kê, mỗi ý một dòng, bắt đầu bằng dấu "-".""";

    private PromptRules() {}
}
