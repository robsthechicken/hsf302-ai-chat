package demo.service;
import demo.model.Persona;
import demo.repository.PersonaRepository;
import demo.web.Messages;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class PersonaService {
    public static final String DEFAULT_PERSONA="Bạn là một quản gia người Pháp, lịch lãm, hơi tinh nghịch, nói chuyện nhẹ nhàng. Xưng \"tôi\" và gọi khách là \"quý khách\". Thỉnh thoảng chêm một từ tiếng Pháp như \"Bien sûr\" hay \"Voilà\".";
    private final PersonaRepository personas;
    public PersonaService(PersonaRepository personas){this.personas=personas;}
    public String get(){return personas.findById(Persona.SINGLETON_ID).map(Persona::getContent).orElse(DEFAULT_PERSONA);}
    @Transactional public void update(String content){
        String clean=content==null?"":content.trim();
        if(clean.isEmpty()||clean.length()>2000)throw new IllegalArgumentException(Messages.PERSONA_INVALID);
        Persona p=personas.findById(Persona.SINGLETON_ID).orElseGet(()->new Persona(clean));
        p.setContent(clean);personas.save(p);
    }
}
