package demo.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="persona")
public class Persona {
    public static final long SINGLETON_ID = 1L;
    @Id private Long id;
    @Column(nullable=false,length=2000) private String content;
    @Column(nullable=false) private LocalDateTime updatedAt;
    protected Persona() {}
    public Persona(String content) {id=SINGLETON_ID; setContent(content);}
    public Long getId(){return id;} public String getContent(){return content;}
    public void setContent(String content){this.content=content;this.updatedAt=LocalDateTime.now();}
    public LocalDateTime getUpdatedAt(){return updatedAt;}
}
