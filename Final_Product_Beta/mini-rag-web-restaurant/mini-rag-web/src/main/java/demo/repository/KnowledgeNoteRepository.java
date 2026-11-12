package demo.repository;
import demo.model.KnowledgeNote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface KnowledgeNoteRepository extends JpaRepository<KnowledgeNote,Long> {List<KnowledgeNote> findAllByOrderByCreatedAtDescIdDesc();}
