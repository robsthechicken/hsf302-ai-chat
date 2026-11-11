package edu.fpt.hsf302.aichat.controller;

import edu.fpt.hsf302.aichat.dto.ChatMessage;
import edu.fpt.hsf302.aichat.dto.ChatMode;
import edu.fpt.hsf302.aichat.dto.RagAnswer;
import edu.fpt.hsf302.aichat.service.DocumentService;
import edu.fpt.hsf302.aichat.service.NormalChatService;
import edu.fpt.hsf302.aichat.service.RagChatService;
import dev.langchain4j.exception.AuthenticationException;
import dev.langchain4j.exception.ModelNotFoundException;
import dev.langchain4j.exception.RateLimitException;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller chỉ điều phối: nhận request -> gọi đúng service theo "mode" -> trả về trang chat.
 * Mọi logic AI nằm ở service.
 */
@Controller
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private NormalChatService normalChatService;

    @Autowired
    private RagChatService ragChatService;

    @Autowired
    private DocumentService documentService;

    @GetMapping("/")
    public String home() {
        return "redirect:/chat";
    }

    @GetMapping("/chat")
    public String chatPage(@RequestParam(defaultValue = "NORMAL") ChatMode mode,
                           HttpSession session, Model bag) {
        bag.addAttribute("mode", mode);
        bag.addAttribute("messages", getHistory(session, mode));   // mỗi chế độ một lịch sử riêng
        bag.addAttribute("docs", documentService.getDocuments());
        return "chat";
    }

    @PostMapping("/chat/ask")
    public String ask(@RequestParam ChatMode mode,
                      @RequestParam String question,
                      HttpSession session) {
        String q = question.trim();
        if (!q.isEmpty()) {
            List<ChatMessage> history = getHistory(session, mode);
            history.add(ChatMessage.user(q));
            try {
                if (mode == ChatMode.RAG) {
                    RagAnswer r = ragChatService.ask(q);
                    history.add(ChatMessage.ai(r.getAnswer(), r.getSources()));
                } else {
                    history.add(ChatMessage.ai(normalChatService.ask(q), null));
                }
            } catch (Exception e) {
                log.warn("Hỏi AI thất bại (mode={})", mode, e);
                history.add(ChatMessage.error(aiErrorMessage(e)));
            }
        }
        return backToChat(mode);
    }

    @PostMapping("/chat/upload")
    public String upload(@RequestParam("file") MultipartFile file, RedirectAttributes flash) {
        String name = file.getOriginalFilename();
        if (name == null || name.isBlank()) {
            flash.addFlashAttribute("error", "Chưa chọn file.");
        } else if (!name.toLowerCase().endsWith(".txt")) {
            flash.addFlashAttribute("error", "Hiện chỉ hỗ trợ file .txt.");
        } else {
            try {
                String content = new String(file.getBytes(), StandardCharsets.UTF_8);
                if (content.isBlank()) {
                    flash.addFlashAttribute("error", "File không có nội dung.");
                } else {
                    int chunks = documentService.addDocument(name, content);
                    flash.addFlashAttribute("notice", "Đã thêm \"" + name + "\" (" + chunks + " đoạn).");
                }
            } catch (Exception e) {
                log.warn("Upload \"{}\" thất bại", name, e);
                flash.addFlashAttribute("error", "Không thêm được tài liệu. " + aiErrorMessage(e));
            }
        }
        return backToChat(ChatMode.RAG);
    }

    @PostMapping("/chat/clear-docs")
    public String clearDocs() {
        documentService.clearAll();
        return backToChat(ChatMode.RAG);
    }

    @PostMapping("/chat/clear-history")
    public String clearHistory(@RequestParam ChatMode mode, HttpSession session) {
        getHistory(session, mode).clear();
        return backToChat(mode);
    }

    // ---------- helper ----------

    /** Câu ngắn cho người dùng; chi tiết lỗi (JSON của Google/Groq) chỉ nằm trong log. */
    private String aiErrorMessage(Exception e) {
        if (e instanceof RateLimitException) {
            return "AI đang hết hạn mức miễn phí, đợi khoảng 1 phút rồi thử lại.";
        }
        if (e instanceof AuthenticationException) {
            return "API key không hợp lệ, kiểm tra GROQ_API_KEY / GEMINI_API_KEY.";
        }
        if (e instanceof ModelNotFoundException) {
            return "Không tìm thấy model, kiểm tra tên model trong application.properties.";
        }
        return "Có lỗi khi gọi AI, thử lại sau.";
    }

    /** redirect để F5 không gửi lại form; #end để trang cuộn xuống tin nhắn mới nhất. */
    private String backToChat(ChatMode mode) {
        return "redirect:/chat?mode=" + mode.name() + "#end";
    }

    @SuppressWarnings("unchecked")
    private List<ChatMessage> getHistory(HttpSession session, ChatMode mode) {
        String key = "history_" + mode.name();
        List<ChatMessage> history = (List<ChatMessage>) session.getAttribute(key);
        if (history == null) {
            history = new ArrayList<>();
            session.setAttribute(key, history);
        }
        return history;
    }
}
