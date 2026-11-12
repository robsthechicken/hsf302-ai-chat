package demo.service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import demo.model.*;
import demo.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
@Component @Order(1)
public class DataSeeder implements ApplicationRunner {
    private static final Logger log=LoggerFactory.getLogger(DataSeeder.class);
    private final AppUserRepository users;private final DishRepository dishes;
    private final PersonaRepository personas;private final FeedbackRepository feedbacks;
    private final KnowledgeNoteRepository notes;private final PasswordEncoder encoder;
    private final TransactionTemplate transactions;private final ObjectMapper mapper;
    @Value("${app.seed.enabled:true}") private boolean enabled;
    public DataSeeder(AppUserRepository users,DishRepository dishes,PersonaRepository personas,FeedbackRepository feedbacks,
        KnowledgeNoteRepository notes,PasswordEncoder encoder,TransactionTemplate transactions,ObjectMapper mapper){
        this.users=users;this.dishes=dishes;this.personas=personas;this.feedbacks=feedbacks;
        this.notes=notes;this.encoder=encoder;this.transactions=transactions;this.mapper=mapper;
    }
    @Override public void run(ApplicationArguments args){
        try{
            if(!enabled||users.count()>0)return;
            try(var input=new ClassPathResource("seed/seed.json").getInputStream()){
                JsonNode seed=mapper.readTree(input);
                transactions.executeWithoutResult(tx->{
                    Map<String,AppUser> byName=new HashMap<>();
                    for(var u:seed.path("users")){
                        var user=users.save(new AppUser(u.path("username").asText(),encoder.encode(u.path("password").asText()),Role.valueOf(u.path("role").asText())));
                        byName.put(user.getUsername(),user);
                    }
                    personas.save(new Persona(seed.path("persona").asText()));
                    for(var d:seed.path("dishes"))dishes.save(new Dish(d.path("name").asText(),d.path("description").asText(),d.path("priceVnd").asInt(),d.path("available").asBoolean()));
                    int n=0;
                    for(var f:seed.path("feedbacks")){
                        var feedback=new Feedback(byName.get(f.path("author").asText()),f.path("content").asText());
                        n++;feedback.setCreatedAt(LocalDateTime.now().minusHours(16-n));
                        Relevance rel=Relevance.valueOf(f.path("relevance").asText());
                        Topic topic=f.path("topic").isNull()?null:Topic.valueOf(f.path("topic").asText());
                        LabelSource label=f.path("labelSource").isNull()?null:LabelSource.valueOf(f.path("labelSource").asText());
                        feedback.setLabel(rel,topic,label);feedbacks.save(feedback);
                    }
                    for(var item:seed.path("notes"))notes.save(new KnowledgeNote(item.path("title").asText(),item.path("content").asText(),NoteOrigin.WRITTEN,null));
                    log.info("Đã nạp dữ liệu mẫu: {} tài khoản, {} món, {} góp ý, {} ghi chú",users.count(),dishes.count(),feedbacks.count(),notes.count());
                });
            }
        }catch(Exception e){log.warn("Không thể nạp dữ liệu mẫu: {}",e.getMessage());}
    }
}
