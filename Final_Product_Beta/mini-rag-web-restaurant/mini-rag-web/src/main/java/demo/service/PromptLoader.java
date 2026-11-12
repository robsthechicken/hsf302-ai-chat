package demo.service;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
@Component
public class PromptLoader {
    public static final String CUSTOMER_RULES="customer-rules.txt";
    public static final String RAG="rag.txt";
    public static final String CLASSIFY="classify.txt";
    private final Map<String,String> cache=new ConcurrentHashMap<>();
    public String load(String fileName){return cache.computeIfAbsent(fileName, name->{
        try (var stream = new ClassPathResource("prompts/"+name).getInputStream()){
            return new String(stream.readAllBytes(),StandardCharsets.UTF_8);
        } catch(IOException e){throw new IllegalStateException("Thiếu file prompt: "+name,e);}
    });}
}
