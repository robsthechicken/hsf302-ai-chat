package edu.fpt.hsf302.aichat.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * File quá lớn bị Spring chặn lúc đọc multipart, TRƯỚC khi vào ChatController,
 * nên @ExceptionHandler đặt trong controller không bắt được -> phải dùng @ControllerAdvice.
 */
@ControllerAdvice
public class UploadExceptionHandler {

    @Value("${spring.servlet.multipart.max-file-size}")
    private DataSize maxFileSize;

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String tooLarge(HttpServletRequest request) {
        RequestContextUtils.getOutputFlashMap(request)
                .put("error", "File quá lớn, tối đa " + maxFileSize.toKilobytes() + "KB.");
        return "redirect:/chat?mode=RAG";
    }
}
