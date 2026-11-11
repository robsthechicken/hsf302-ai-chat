package edu.fpt.hsf302.aichat.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** Một tin nhắn hiển thị trong khung chat (lưu trong HttpSession nên phải Serializable). */
public class ChatMessage implements Serializable {

    private final boolean fromUser;
    private final String text;
    private final List<String> sources; // chỉ AI RAG mới có
    private final boolean error;

    private ChatMessage(boolean fromUser, String text, List<String> sources, boolean error) {
        this.fromUser = fromUser;
        this.text = text;
        this.sources = sources == null ? new ArrayList<>() : new ArrayList<>(sources);
        this.error = error;
    }

    public static ChatMessage user(String text) {
        return new ChatMessage(true, text, null, false);
    }

    public static ChatMessage ai(String text, List<String> sources) {
        return new ChatMessage(false, text, sources, false);
    }

    public static ChatMessage error(String text) {
        return new ChatMessage(false, text, null, true);
    }

    public boolean isFromUser() { return fromUser; }
    public String getText() { return text; }
    public List<String> getSources() { return sources; }
    public boolean isError() { return error; }
}
