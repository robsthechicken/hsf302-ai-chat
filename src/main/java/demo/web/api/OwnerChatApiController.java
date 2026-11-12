package demo.web.api;
import demo.dto.ChatReply;
import demo.dto.ChatRequest;
import demo.service.RagChatService;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/owner/api")
public class OwnerChatApiController {
    private final RagChatService chat;
    public OwnerChatApiController(RagChatService chat){this.chat=chat;}
    @PostMapping("/chat") public ChatReply chat(@RequestBody ChatRequest request){return chat.ask(request==null?null:request.message());}
}
