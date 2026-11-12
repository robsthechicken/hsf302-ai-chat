package demo.web.api;
import demo.dto.ChatReply;
import demo.dto.ChatRequest;
import demo.service.CustomerChatService;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/customer/api")
public class CustomerChatApiController {
    private final CustomerChatService chat;
    public CustomerChatApiController(CustomerChatService chat){this.chat=chat;}
    @PostMapping("/chat") public ChatReply chat(@RequestBody ChatRequest request){return chat.reply(request==null?null:request.message());}
}
