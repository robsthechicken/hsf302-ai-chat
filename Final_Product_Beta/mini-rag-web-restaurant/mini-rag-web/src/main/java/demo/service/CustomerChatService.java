package demo.service;
import demo.config.AiModelProvider;
import demo.dto.ChatReply;
import demo.model.Dish;
import demo.web.Messages;
import org.springframework.stereotype.Service;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
@Service
public class CustomerChatService {
    private final DishService dishes;
    private final PersonaService personas;
    private final AiModelProvider models;
    private final PromptLoader prompts;
    public CustomerChatService(DishService dishes, PersonaService personas, AiModelProvider models, PromptLoader prompts){
        this.dishes=dishes;this.personas=personas;this.models=models;this.prompts=prompts;
    }
    public ChatReply reply(String message){
        String input=message==null?"":message.trim();
        if(input.isEmpty())throw new IllegalArgumentException(Messages.EMPTY_MESSAGE);
        if(input.length()>4000)throw new IllegalArgumentException(Messages.MESSAGE_TOO_LONG);
        List<Dish> menu=dishes.listAvailable();
        if(menu.isEmpty())return new ChatReply(Messages.MENU_EMPTY,List.of());
        var vi=NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN"));
        StringBuilder menuText=new StringBuilder();
        for(Dish d:menu){
            menuText.append("- ").append(d.getName()).append(" | ").append(vi.format(d.getPriceVnd())).append(" đồng");
            if(d.getDescription()!=null&&!d.getDescription().isBlank())menuText.append(" | ").append(d.getDescription());
            menuText.append("\n");
        }
        String system=prompts.load(PromptLoader.CUSTOMER_RULES)
                +"\n\n=== PERSONA (chỉ quyết định giọng điệu) ===\n"+personas.get()
                +"\n\n=== THỰC ĐƠN ===\n"+menuText;
        String answer=models.chat(system,input);
        return new ChatReply(answer==null||answer.isBlank()?Messages.AI_EMPTY_ANSWER:answer.trim(),List.of());
    }
}
