package demo.service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import demo.config.AiModelProvider;
import demo.dto.ClassifyResult;
import demo.model.*;
import demo.repository.FeedbackRepository;
import demo.web.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.Optional;
@Service
public class FeedbackClassificationService {
    private static final Logger log=LoggerFactory.getLogger(FeedbackClassificationService.class);
    private final FeedbackRepository feedbacks;
    private final AiModelProvider models;
    private final PromptLoader prompts;
    private static final ObjectMapper mapper=new ObjectMapper();
    record Label(Relevance relevance,Topic topic) {}
    public FeedbackClassificationService(FeedbackRepository feedbacks,AiModelProvider models,PromptLoader prompts){
        this.feedbacks=feedbacks;this.models=models;this.prompts=prompts;
    }
    public boolean classify(Long feedbackId){
        Feedback f=feedbacks.findById(feedbackId).orElseThrow(()->new IllegalArgumentException(Messages.FEEDBACK_NOT_FOUND));
        if(f.getLabelSource()==LabelSource.OWNER)return false;
        String request="<feedback>\n"+f.getContent().replace("</feedback>","")+"\n</feedback>";
        String raw=models.chat(prompts.load(PromptLoader.CLASSIFY),request);
        Optional<Label> parsed=parseLabel(raw);
        if(parsed.isEmpty()){log.warn("AI trả về nhãn không hợp lệ cho góp ý #{}",feedbackId);return false;}
        Label label=parsed.get();
        f.setLabel(label.relevance(),label.topic(),LabelSource.AI);
        feedbacks.save(f);
        return true;
    }
    public ClassifyResult classifyAllUnclassified(){
        var records=feedbacks.findByRelevanceOrderByIdAsc(Relevance.UNCLASSIFIED);
        if(records.isEmpty())return new ClassifyResult(0,0,0);
        if(!models.hasChatKey())throw new IllegalStateException(Messages.CHAT_KEY_MISSING);
        int labeled=0,failed=0;
        for(int i=0;i<records.size();i++){
            try{if(classify(records.get(i).getId()))labeled++;else failed++;}
            catch(RuntimeException e){
                log.warn("Không thể phân loại góp ý #{}: {}",records.get(i).getId(),e.getMessage());
                failed++;
                if(String.valueOf(e.getMessage()).contains("429")){failed+=records.size()-i-1;break;}
            }
        }
        return new ClassifyResult(records.size(),labeled,failed);
    }
    static Optional<Label> parseLabel(String raw){
        if(raw==null)return Optional.empty();
        int start=raw.indexOf('{'),end=raw.lastIndexOf('}');
        if(start<0||end<=start)return Optional.empty();
        try{
            JsonNode node=mapper.readTree(raw.substring(start,end+1));
            JsonNode relevant=node.path("relevant"),topic=node.path("topic");
            if(!relevant.isBoolean()||!topic.isTextual())return Optional.empty();
            return Optional.of(new Label(relevant.booleanValue()?Relevance.RELEVANT:Relevance.IRRELEVANT,Topic.valueOf(topic.asText().trim().toUpperCase(java.util.Locale.ROOT))));
        }catch(Exception e){return Optional.empty();}
    }
}
