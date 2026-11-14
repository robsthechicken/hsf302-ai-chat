package demo.web.api;
import demo.dto.ChatReply;
import demo.dto.ChatRequest;
import demo.service.CustomerChatService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/customer/api")
public class CustomerChatApiController {
    private final CustomerChatService chat;
    public CustomerChatApiController(CustomerChatService chat){this.chat=chat;}
    @GetMapping("/chat") public List<ChatHistory.Entry> history(HttpSession session){return ChatHistory.get(session);}
    @PostMapping("/chat") public ChatReply chat(@RequestBody ChatRequest request,HttpSession session){
        ChatReply reply=chat.reply(request==null?null:request.message());
        ChatHistory.add(session,request.message(),reply);
        return reply;
    }
}
