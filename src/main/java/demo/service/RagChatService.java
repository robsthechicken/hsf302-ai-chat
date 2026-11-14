package demo.service;
import demo.config.AiModelProvider;
import demo.dto.*;
import demo.model.*;
import demo.repository.FeedbackRepository;
import demo.web.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.*;
@Service
public class RagChatService {
    private static final Logger log=LoggerFactory.getLogger(RagChatService.class);
    private final RagService rag;
    private final FeedbackRepository feedbacks;
    private final PromptLoader prompts;
    private final AiModelProvider models;
    public RagChatService(RagService rag,FeedbackRepository feedbacks,PromptLoader prompts,AiModelProvider models){
        this.rag=rag;this.feedbacks=feedbacks;this.prompts=prompts;this.models=models;
    }
    public ChatReply ask(String question){
        String input=question==null?"":question.trim();
        if(input.isEmpty())throw new IllegalArgumentException(Messages.EMPTY_MESSAGE);
        if(input.length()>4000)throw new IllegalArgumentException(Messages.MESSAGE_TOO_LONG);
        var chunks=rag.isEmpty()?List.<RetrievedChunk>of():rag.search(input);
        // Câu hỏi kiểu "góp ý mới nhất", "khách vừa góp ý gì" không khớp ngữ nghĩa với nội dung góp ý,
        // nên luôn kèm vài góp ý mới nhất từ DB (cũng giúp khi góp ý chưa index được).
        var recent=feedbacks.findTop5ByOrderByCreatedAtDescIdDesc();
        if(chunks.isEmpty()&&recent.isEmpty())
            return new ChatReply(rag.isEmpty()?Messages.RAG_EMPTY_STORE:Messages.RAG_NOT_FOUND,List.of());
        var context=new StringBuilder();
        Map<String,SourceInfo> sources=new LinkedHashMap<>();
        Set<Long> feedbackIds=new HashSet<>();
        for(RetrievedChunk c:chunks){
            String line;
            if(c.type()==SourceType.FEEDBACK){
                var found=feedbacks.findById(c.refId());
                if(found.isEmpty()){log.warn("Không tìm thấy góp ý #{} trong DB",c.refId());continue;}
                feedbackIds.add(c.refId());
                line=feedbackLine(found.get(),c.text());
            }else line="[Ghi chú: "+c.displayName()+"] "+c.text();
            if(context.length()>0)context.append("\n\n");
            context.append(line.replace("</context>",""));
            String sourceKey=c.type()+":"+c.refId();
            String preview=c.text().replace('\n',' ').replace('\r',' ');
            if(preview.length()>120)preview=preview.substring(0,120)+"...";
            SourceInfo previous=sources.get(sourceKey);
            if(previous==null||previous.score()<c.score())
                sources.put(sourceKey,new SourceInfo(c.displayName(),c.score(),preview,c.type(),c.refId()));
        }
        for(Feedback f:recent){
            if(!feedbackIds.add(f.getId()))continue;
            if(context.length()>0)context.append("\n\n");
            context.append(feedbackLine(f,f.getContent()).replace("</context>",""));
        }
        if(context.isEmpty())return new ChatReply(Messages.RAG_NOT_FOUND,List.of());
        String system=prompts.load(PromptLoader.RAG).replace("{context}",context);
        String answer=models.chat(system,input);
        String output=answer==null||answer.isBlank()?Messages.AI_EMPTY_ANSWER:answer.trim();
        if(output.replaceFirst("\\.+$","").trim().equals(Messages.RAG_NOT_FOUND.replaceFirst("\\.+$","").trim()))
            return new ChatReply(output,List.of());
        return new ChatReply(output,List.copyOf(sources.values()));
    }
    private String feedbackLine(Feedback f,String text){
        String labels=f.getRelevance()==Relevance.UNCLASSIFIED?"UNCLASSIFIED":f.getRelevance()+" | "+f.getTopic();
        return "[Góp ý #"+f.getId()+" | "+labels+" | gửi lúc "+f.getCreatedAt().truncatedTo(java.time.temporal.ChronoUnit.MINUTES)+"] "+text;
    }
}
