package demo.repository;
import demo.model.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PersonaRepository extends JpaRepository<Persona,Long> {}
