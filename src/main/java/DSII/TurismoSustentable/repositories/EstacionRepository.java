package DSII.TurismoSustentable.repositories;

import DSII.TurismoSustentable.models.Estacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EstacionRepository extends JpaRepository<Estacion, Integer> {

    // Útil para buscar una estación por su nombre
    Optional<Estacion> findByNombre(String nombre);
    List<Estacion> findByBorradoEstacionFalse();

}