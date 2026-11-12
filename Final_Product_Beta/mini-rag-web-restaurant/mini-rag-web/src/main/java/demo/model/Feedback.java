package demo.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="feedback")
public class Feedback {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="author_id",nullable=false) private AppUser author;
    @Column(nullable=false,length=1000) private String content;
    @Column(nullable=false) private LocalDateTime createdAt;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private Relevance relevance=Relevance.UNCLASSIFIED;
    @Enumerated(EnumType.STRING) @Column(length=20) private Topic topic;
    @Enumerated(EnumType.STRING) @Column(length=20) private LabelSource labelSource;
    protected Feedback() {}
    public Feedback(AppUser author, String content){this.author=author;this.content=content;}
    @PrePersist protected void prePersist(){if(createdAt==null)createdAt=LocalDateTime.now();}
    public Long getId(){return id;} public AppUser getAuthor(){return author;}
    public String getContent(){return content;} public LocalDateTime getCreatedAt(){return createdAt;}
    public Relevance getRelevance(){return relevance;} public Topic getTopic(){return topic;}
    public LabelSource getLabelSource(){return labelSource;}
    public void setCreatedAt(LocalDateTime date){this.createdAt=date;}
    public void setLabel(Relevance relevance, Topic topic, LabelSource source){
        this.relevance=relevance;
        this.topic=relevance==Relevance.UNCLASSIFIED?null:topic;
        this.labelSource=relevance==Relevance.UNCLASSIFIED?null:source;
    }
}
