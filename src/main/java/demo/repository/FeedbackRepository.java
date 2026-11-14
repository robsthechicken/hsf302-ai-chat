package demo.repository;
import demo.model.Feedback;
import demo.model.Relevance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
public interface FeedbackRepository extends JpaRepository<Feedback,Long> {
    @Query("select f from Feedback f join fetch f.author order by f.createdAt desc, f.id desc")
    List<Feedback> findAllWithAuthor();
    @Query("select f from Feedback f join fetch f.author where f.relevance = :rel order by f.createdAt desc, f.id desc")
    List<Feedback> findByRelevanceWithAuthor(@Param("rel") Relevance rel);
    List<Feedback> findByRelevanceOrderByIdAsc(Relevance relevance);
    long countByRelevance(Relevance relevance);
    List<Feedback> findTop5ByOrderByCreatedAtDescIdDesc();
}
