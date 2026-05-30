package DSII.TurismoSustentable.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import DSII.TurismoSustentable.models.Usuario;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    List<Usuario> findByActivoTrue();
    Optional<Usuario> findByEmail(String email);
}
