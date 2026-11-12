package demo.service;
import demo.config.AiModelProvider;
import demo.config.FeedbackProperties;
import demo.dto.FeedbackView;
import demo.model.*;
import demo.repository.*;
import demo.web.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;
@Service
public class FeedbackService {
    private static final Logger log=LoggerFactory.getLogger(FeedbackService.class);
    private final FeedbackRepository feedbacks;
    private final AppUserRepository users;
    private final FeedbackProperties props;
    private final AiModelProvider models;
    private final RagService rag;
    private final Map<Long,Deque<Instant>> windows=new HashMap<>();
    public FeedbackService(FeedbackRepository feedbacks,AppUserRepository users,FeedbackProperties props,AiModelProvider models,RagService rag){
        this.feedbacks=feedbacks;this.users=users;this.props=props;this.models=models;this.rag=rag;
    }
    public synchronized Feedback submit(Long authorId,String content){
        String value=content==null?"":content.trim();
        if(value.isEmpty())throw new IllegalArgumentException(Messages.FEEDBACK_EMPTY);
        if(value.length()>props.maxLength())throw new IllegalArgumentException(String.format(Messages.FEEDBACK_TOO_LONG,props.maxLength()));
        var q=windows.computeIfAbsent(authorId,k->new ArrayDeque<>());
        Instant now=Instant.now();
        while(!q.isEmpty()&&q.peekFirst().isBefore(now.minusSeconds(props.windowMinutes()*60L)))q.removeFirst();
        if(q.size()>=props.maxPerWindow())throw new IllegalArgumentException(Messages.FEEDBACK_RATE_LIMIT);
        AppUser author=users.findById(authorId).orElseThrow(()->new IllegalArgumentException(Messages.USER_NOT_FOUND));
        Feedback f=feedbacks.save(new Feedback(author,value));
        q.addLast(now);
        if(models.hasEmbeddingKey()){
            try{rag.index(SourceType.FEEDBACK,f.getId(),"Góp ý #"+f.getId(),value);}
            catch(RuntimeException e){log.warn("Góp ý #{} đã lưu nhưng chưa index: {}",f.getId(),e.getMessage());}
        }else log.warn("Không có key embedding; góp ý #{} chưa được index",f.getId());
        return f;
    }
    @Transactional(readOnly=true) public List<FeedbackView> list(Relevance filter){
        var records=filter==null?feedbacks.findAllWithAuthor():feedbacks.findByRelevanceWithAuthor(filter);
        return records.stream().map(f->new FeedbackView(f.getId(),f.getAuthor().getUsername(),f.getContent(),f.getCreatedAt(),f.getRelevance(),f.getTopic(),f.getLabelSource())).toList();
    }
    @Transactional public void updateLabel(Long id,Relevance relevance,Topic topic){
        if(relevance==null)throw new IllegalArgumentException(Messages.INVALID_LABEL);
        Feedback f=feedbacks.findById(id).orElseThrow(()->new IllegalArgumentException(Messages.FEEDBACK_NOT_FOUND));
        f.setLabel(relevance,relevance==Relevance.UNCLASSIFIED?null:(topic==null?Topic.OTHER:topic),relevance==Relevance.UNCLASSIFIED?null:LabelSource.OWNER);
        feedbacks.save(f);
    }
    public long unclassifiedCount(){return feedbacks.countByRelevance(Relevance.UNCLASSIFIED);}
}
