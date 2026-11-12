package demo.service;
import demo.config.AiModelProvider;
import demo.dto.NoteInfo;
import demo.model.*;
import demo.repository.KnowledgeNoteRepository;
import demo.web.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
@Service
public class NoteService {
    private static final Logger log=LoggerFactory.getLogger(NoteService.class);
    private final KnowledgeNoteRepository notes;
    private final DocumentTextReader reader;
    private final RagService rag;
    private final AiModelProvider models;
    public NoteService(KnowledgeNoteRepository notes,DocumentTextReader reader,RagService rag,AiModelProvider models){
        this.notes=notes;this.reader=reader;this.rag=rag;this.models=models;
    }
    private String title(String title){String v=title==null?"":title.trim();if(v.isEmpty()||v.length()>150)throw new IllegalArgumentException(Messages.NOTE_TITLE_INVALID);return v;}
    private String content(String content){String v=content==null?"":content.trim();if(v.isEmpty()||v.length()>50000)throw new IllegalArgumentException(Messages.NOTE_CONTENT_INVALID);return v;}
    public KnowledgeNote create(String title,String content){
        var note=notes.save(new KnowledgeNote(title(title),content(content),NoteOrigin.WRITTEN,null));
        indexSafely(note);return note;
    }
    public KnowledgeNote update(Long id,String title,String content){
        String cleanTitle=title(title),cleanContent=content(content);
        var n=notes.findById(id).orElseThrow(()->new IllegalArgumentException(Messages.NOTE_NOT_FOUND));
        n.change(cleanTitle,cleanContent);
        n=notes.save(n);indexSafely(n);return n;
    }
    public void delete(Long id){
        if(!notes.existsById(id))throw new IllegalArgumentException(Messages.NOTE_NOT_FOUND);
        notes.deleteById(id);rag.remove(SourceType.NOTE,id);
    }
    public KnowledgeNote importFile(MultipartFile file){
        String text=reader.read(file),name=DocumentTextReader.cleanName(file.getOriginalFilename());
        int dot=name.lastIndexOf('.');String title=dot>0?name.substring(0,dot):name;
        if(title.length()>150)title=title.substring(0,150);
        var n=notes.save(new KnowledgeNote(title(title),content(text),NoteOrigin.IMPORTED,name));
        indexSafely(n);return n;
    }
    private void indexSafely(KnowledgeNote note){
        if(!models.hasEmbeddingKey()){log.warn("Không có key embedding; ghi chú #{} chưa được index",note.getId());return;}
        try{rag.index(SourceType.NOTE,note.getId(),note.getTitle(),note.getTitle()+"\n"+note.getContent());}
        catch(RuntimeException e){log.warn("Ghi chú #{} đã lưu nhưng chưa index: {}",note.getId(),e.getMessage());}
    }
    public List<NoteInfo> list(){return notes.findAllByOrderByCreatedAtDescIdDesc().stream().map(n->{
        String preview=n.getContent().replace('\n',' ').replace('\r',' ');
        if(preview.length()>120)preview=preview.substring(0,120)+"...";
        return new NoteInfo(n.getId(),n.getTitle(),n.getOrigin(),n.getSourceFileName(),n.getCreatedAt(),n.getContent(),preview);
    }).toList();}
}
