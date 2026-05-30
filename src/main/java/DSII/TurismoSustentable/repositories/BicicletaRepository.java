package DSII.TurismoSustentable.repositories;

import DSII.TurismoSustentable.models.Bicicleta;
import DSII.TurismoSustentable.models.EstadoBicicleta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.List;

public interface BicicletaRepository extends JpaRepository<Bicicleta, Integer> {

    /**
     * 3. ESTADOS DE LAS BICICLETAS (Preferencias/Estados de Bicis en general)
     * * Consulta que utiliza la tabla Bicicleta (no Alquiler) para contar cuántas bicicletas
     * están en cada EstadoBicicleta.
     * * Resultado: List<Object[]> donde cada Object[] contiene [EstadoBicicleta, Long Cantidad].
     */

    @Query("SELECT b.estado, COUNT(b.id) FROM Bicicleta b GROUP BY b.estado")
    List<Object[]> contarBicicletasPorEstado();

    // ----------------------------------------------------------------------------------------------------

    Optional<Bicicleta> findByCodigoBicicleta(String codigoBicicleta);
    List<Bicicleta> findByBorradoBiciFalse();
    List<Bicicleta> findByBorradoBiciFalseAndEstado(EstadoBicicleta estado);
}
