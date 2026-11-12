package demo.service;
import demo.config.AiModelProvider;
import demo.dto.IndexStatus;
import demo.repository.FeedbackRepository;
import demo.repository.KnowledgeNoteRepository;
import demo.web.Messages;
import org.springframework.stereotype.Service;
@Service
public class IndexingService {
    private final RagService rag;
    private final AiModelProvider models;
    private final FeedbackRepository feedbacks;
    private final KnowledgeNoteRepository notes;
    public IndexingService(RagService rag,AiModelProvider models,FeedbackRepository feedbacks,KnowledgeNoteRepository notes){
        this.rag=rag;this.models=models;this.feedbacks=feedbacks;this.notes=notes;
    }
    public IndexStatus reindexAll(){
        if(!models.hasEmbeddingKey())throw new IllegalStateException(Messages.EMBEDDING_KEY_MISSING);
        return rag.rebuild(feedbacks.findAll(),notes.findAll());
    }
    public IndexStatus status(){return new IndexStatus((int)(feedbacks.count()+notes.count()),rag.size());}
}
