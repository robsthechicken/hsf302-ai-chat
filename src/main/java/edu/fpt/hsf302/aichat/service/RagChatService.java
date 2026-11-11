package edu.fpt.hsf302.aichat.service;

import edu.fpt.hsf302.aichat.dto.RagAnswer;

/** AI RAG: chỉ trả lời dựa trên các tài liệu người dùng đã upload. */
public interface RagChatService {

    RagAnswer ask(String question);
}
