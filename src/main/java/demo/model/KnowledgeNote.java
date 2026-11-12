package demo.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="knowledge_note")
public class KnowledgeNote {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false,length=150) private String title;
    @Column(nullable=false,length=50000) private String content;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private NoteOrigin origin;
    @Column(length=255) private String sourceFileName;
    @Column(nullable=false) private LocalDateTime createdAt;
    protected KnowledgeNote() {}
    public KnowledgeNote(String title, String content, NoteOrigin origin, String sourceFileName){
        this.title=title;this.content=content;this.origin=origin;this.sourceFileName=sourceFileName;
    }
    @PrePersist protected void onCreate(){if(createdAt==null)createdAt=LocalDateTime.now();}
    public Long getId(){return id;} public String getTitle(){return title;} public String getContent(){return content;}
    public NoteOrigin getOrigin(){return origin;} public String getSourceFileName(){return sourceFileName;}
    public LocalDateTime getCreatedAt(){return createdAt;}
    public void change(String title,String content){this.title=title;this.content=content;}
}
