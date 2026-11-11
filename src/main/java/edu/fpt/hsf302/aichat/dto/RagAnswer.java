package edu.fpt.hsf302.aichat.dto;

import java.util.List;

/** Kết quả của AI RAG: câu trả lời + các đoạn tài liệu đã dùng để trả lời. */
public class RagAnswer {

    private final String answer;
    private final List<String> sources;

    public RagAnswer(String answer, List<String> sources) {
        this.answer = answer;
        this.sources = sources;
    }

    public String getAnswer() { return answer; }
    public List<String> getSources() { return sources; }
}
