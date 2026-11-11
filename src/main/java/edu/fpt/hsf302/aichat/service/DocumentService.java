package edu.fpt.hsf302.aichat.service;

import edu.fpt.hsf302.aichat.dto.DocumentInfo;

import java.util.List;

/** Quản lý tài liệu cho RAG: chia nhỏ -> embedding -> lưu vào kho vector. */
public interface DocumentService {

    /** Index một tài liệu, trả về số đoạn đã tạo. Upload lại cùng tên file thì thay thế bản cũ. */
    int addDocument(String fileName, String content);

    List<DocumentInfo> getDocuments();

    void clearAll();
}
