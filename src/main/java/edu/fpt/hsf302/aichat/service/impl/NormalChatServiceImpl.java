package edu.fpt.hsf302.aichat.service.impl;

import edu.fpt.hsf302.aichat.service.NormalChatService;
import edu.fpt.hsf302.aichat.service.PromptRules;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NormalChatServiceImpl implements NormalChatService {

    @Autowired
    private ChatModel chatModel;

    @Override
    public String ask(String question) {
        // Gửi thẳng câu hỏi cho model chat (Groq). Không embedding, không tài liệu.
        ChatResponse response = chatModel.chat(SystemMessage.from(PromptRules.PLAIN_TEXT), UserMessage.from(question));
        return response.aiMessage().text();
    }
}
