package demo.web.api;
import demo.dto.ChatReply;
import demo.dto.ChatRequest;
import demo.service.RagChatService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/owner/api")
public class OwnerChatApiController {
    private final RagChatService chat;
    public OwnerChatApiController(RagChatService chat){this.chat=chat;}
    @GetMapping("/chat") public List<ChatHistory.Entry> history(HttpSession session){return ChatHistory.get(session);}
    @PostMapping("/chat") public ChatReply chat(@RequestBody ChatRequest request,HttpSession session){
        ChatReply reply=chat.ask(request==null?null:request.message());
        ChatHistory.add(session,request.message(),reply);
        return reply;
    }
}
