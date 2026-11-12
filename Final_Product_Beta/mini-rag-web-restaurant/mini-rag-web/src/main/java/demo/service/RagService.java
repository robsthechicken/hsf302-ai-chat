package demo.service;
import demo.config.AiModelProvider;
import demo.config.RagProperties;
import demo.dto.IndexStatus;
import demo.dto.RetrievedChunk;
import demo.model.*;
import demo.web.Messages;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.*;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.*;
@Service
public class RagService {
    private static final Logger log=LoggerFactory.getLogger(RagService.class);
    private static final int EMBED_BATCH=50;
    private record Item(SourceType type,Long refId,String displayName,String text,
                        List<TextSegment> segments,List<Embedding> embeddings) {}
    private final AiModelProvider models;
    private final RagProperties props;
    private final Map<String,Item> items=new LinkedHashMap<>();
    private volatile EmbeddingStore<TextSegment> feedbackStore=new InMemoryEmbeddingStore<>();
    private volatile EmbeddingStore<TextSegment> noteStore=new InMemoryEmbeddingStore<>();
    public RagService(AiModelProvider models,RagProperties props){this.models=models;this.props=props;}
    private String key(SourceType type,Long id){return type.name()+":"+id;}
    private Item makeItem(SourceType type, Long id, String name, String text){
        if(text==null||text.isBlank())throw new IllegalArgumentException(Messages.EMPTY_CONTENT);
        Metadata meta=Metadata.from(Map.of("type",type.name(),"refId",String.valueOf(id),"source",name));
        List<TextSegment> segments=DocumentSplitters.recursive(props.chunkSize(),props.chunkOverlap())
                .split(Document.from(text,meta));
        if(segments.isEmpty())throw new IllegalArgumentException(Messages.EMPTY_CONTENT);
        List<Embedding> embeddings=new ArrayList<>();
        for(int i=0;i<segments.size();i+=EMBED_BATCH){
            var batch=segments.subList(i,Math.min(i+EMBED_BATCH,segments.size()));
            embeddings.addAll(models.embeddingModel().embedAll(batch).content());
        }
        return new Item(type,id,name,text,List.copyOf(segments),List.copyOf(embeddings));
    }
    public synchronized int index(SourceType type,Long refId,String displayName,String text){
        Item next=makeItem(type,refId,displayName,text); // Không sửa kho trước khi embedding hoàn tất.
        Map<String,Item> future=new LinkedHashMap<>(items);
        future.put(key(type,refId),next);
        EmbeddingStore<TextSegment> fresh=buildStore(future,type);
        items.clear();items.putAll(future);
        if(type==SourceType.FEEDBACK)feedbackStore=fresh;else noteStore=fresh;
        return next.segments().size();
    }
    public synchronized boolean remove(SourceType type,Long refId){
        if(!items.containsKey(key(type,refId)))return false;
        Map<String,Item> future=new LinkedHashMap<>(items);
        future.remove(key(type,refId));
        EmbeddingStore<TextSegment> fresh=buildStore(future,type);
        items.clear();items.putAll(future);
        if(type==SourceType.FEEDBACK)feedbackStore=fresh;else noteStore=fresh;
        return true;
    }
    private EmbeddingStore<TextSegment> buildStore(Map<String,Item> map,SourceType type){
        EmbeddingStore<TextSegment> store=new InMemoryEmbeddingStore<>();
        for(Item d:map.values())if(d.type()==type)store.addAll(d.embeddings(),d.segments());
        return store;
    }
    public synchronized IndexStatus rebuild(List<Feedback> feedbacks,List<KnowledgeNote> notes){
        Map<String,Item> future=new LinkedHashMap<>();
        int total=feedbacks.size()+notes.size();
        boolean quota=false;
        for(Feedback f:feedbacks){
            if(quota)break;
            quota=addOrReuse(future,SourceType.FEEDBACK,f.getId(),"Góp ý #"+f.getId(),f.getContent());
        }
        if(!quota)for(KnowledgeNote n:notes){
            quota=addOrReuse(future,SourceType.NOTE,n.getId(),n.getTitle(),n.getTitle()+"\n"+n.getContent());
            if(quota)break;
        }
        var newFeedbackStore=buildStore(future,SourceType.FEEDBACK);
        var newNoteStore=buildStore(future,SourceType.NOTE);
        items.clear();items.putAll(future);feedbackStore=newFeedbackStore;noteStore=newNoteStore;
        return new IndexStatus(total,future.size());
    }
    /** true nghĩa là gặp hạn mức 429, cần dừng các mục còn lại. */
    private boolean addOrReuse(Map<String,Item> future,SourceType type,Long id,String name,String text){
        try {
            Item previous=items.get(key(type,id));
            Item next=previous!=null&&previous.text().equals(text)&&previous.displayName().equals(name)
                    ? previous:makeItem(type,id,name,text);
            future.put(key(type,id),next);
            return false;
        }catch(RuntimeException e){
            log.warn("Không thể index {} #{}: {}",type,id,e.getMessage());
            return String.valueOf(e.getMessage()).contains("429");
        }
    }
    public List<RetrievedChunk> search(String question){
        if(isEmpty())return List.of();
        Embedding embedding=models.embeddingModel().embed(question).content();
        var result=new ArrayList<RetrievedChunk>();
        queryStore(feedbackStore,SourceType.FEEDBACK,embedding,props.feedback().topK(),props.feedback().minScore(),result);
        queryStore(noteStore,SourceType.NOTE,embedding,props.note().topK(),props.note().minScore(),result);
        result.sort(Comparator.comparingDouble(RetrievedChunk::score).reversed());
        return result;
    }
    private void queryStore(EmbeddingStore<TextSegment> store,SourceType type,Embedding embedding,int topK,double minScore,List<RetrievedChunk> out){
        var request=EmbeddingSearchRequest.builder().queryEmbedding(embedding).maxResults(topK).minScore(minScore).build();
        for(EmbeddingMatch<TextSegment> m:store.search(request).matches()){
            var meta=m.embedded().metadata();
            out.add(new RetrievedChunk(type,Long.valueOf(meta.getString("refId")),meta.getString("source"),m.embedded().text(),m.score()));
        }
    }
    public synchronized int size(){return items.size();}
    public synchronized boolean isEmpty(){return items.isEmpty();}
}
