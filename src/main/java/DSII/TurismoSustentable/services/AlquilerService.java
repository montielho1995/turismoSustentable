package DSII.TurismoSustentable.services;

import DSII.TurismoSustentable.dto.AlquilerDTO;
import DSII.TurismoSustentable.dto.AlquilerRequestDTO;
import DSII.TurismoSustentable.models.*;
import DSII.TurismoSustentable.repositories.AlquilerRepository;
import DSII.TurismoSustentable.repositories.BicicletaRepository;
import DSII.TurismoSustentable.repositories.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class AlquilerService {

    // Inyectamos los repositorios que necesitamos
    @Autowired
    private AlquilerRepository alquilerRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private BicicletaRepository bicicletaRepository;

    // Constante para cálculos
    private static final BigDecimal SEGUNDOS_EN_HORA = new BigDecimal("3600");

    /**
     * CREAR UN ALQUILER
     * Transaccional: O se guardan los 2 (alquiler y bici), o no se guarda ninguno.
     */
    @Transactional
    public AlquilerDTO crearAlquiler(AlquilerRequestDTO alquilerRequest) throws Exception {

        // 1. OBTENER EL USUARIO DE LA SESIÓN
        String emailUsuario = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new Exception("Usuario (sesión no válida) no encontrado."));

        // 2. Verificar existencia de Bicicleta
        Bicicleta bicicleta = bicicletaRepository.findById(alquilerRequest.getBicicletaId())
                .orElseThrow(() -> new Exception("Bicicleta no encontrada."));

        // 3. Verificar disponibilidad
        if (bicicleta.getEstado() != EstadoBicicleta.DISPONIBLE) {
            throw new Exception("Bicicleta No Disponible.");
        }

        // 4. Verificar duración
        if (alquilerRequest.getDuracionEnHoras() <= 0) {
            throw new Exception("Duración inválida.");
        }

        // 5. Lógica de cálculo (AHORA CON BIGDECIMAL)
        LocalDateTime fechaInicio = LocalDateTime.now();
        LocalDateTime fechaFin = fechaInicio.plusHours(alquilerRequest.getDuracionEnHoras());

        BigDecimal precioPorHora = bicicleta.getPrecioPorHora();
        BigDecimal duracion = new BigDecimal(alquilerRequest.getDuracionEnHoras());
        BigDecimal costoTotal = precioPorHora.multiply(duracion); // Cálculo preciso

        Alquiler nuevoAlquiler = new Alquiler(
                usuario,
                bicicleta,
                fechaInicio,
                fechaFin,
                costoTotal,
                EstadoAlquiler.INICIADO,
                bicicleta.getUbicacionActual(),
                null
        );

        // --- INICIO DE LA TRANSACCIÓN ---
        // 6. Actualizamos estado de Bicicleta
        bicicleta.setEstado(EstadoBicicleta.ALQUILADA);
        bicicletaRepository.save(bicicleta); // <-- Guardado 1

        // 7. Guardamos el alquiler
        Alquiler alquilerGuardado = alquilerRepository.save(nuevoAlquiler); // <-- Guardado 2
        // --- FIN DE LA TRANSACCIÓN ---
        // (Si el Guardado 2 falla, el Guardado 1 se revierte automáticamente)

        // 8. Retornar DTO
        return new AlquilerDTO(alquilerGuardado);
    }

    /**
     * BORRAR (CANCELAR) UN ALQUILER
     * Transaccional: O se actualizan los 2, o ninguno.
     */
    @Transactional
    public void borrarAlquilerLogico(Integer id) throws Exception {

        // 1. Buscar el alquiler
        Alquiler alquiler = alquilerRepository.findById(id)
                .orElseThrow(() -> new Exception("El alquiler con ID " + id + " no existe."));

        // 2. LÓGICA DE NEGOCIO: Si estaba INICIADO, liberamos la bicicleta.
        if (alquiler.getEstado() == EstadoAlquiler.INICIADO) {
            Bicicleta bicicleta = alquiler.getBicicleta();
            bicicleta.setEstado(EstadoBicicleta.DISPONIBLE);
            bicicletaRepository.save(bicicleta); // <-- Guardado 1
        }

        // 3. Borrado lógico
        alquiler.setBorradoAlquiler(true);
        alquilerRepository.save(alquiler); // <-- Guardado 2
    }

    /**
     * FINALIZAR UN ALQUILER
     * Transaccional: O se actualizan los 2, o ninguno.
     */
    @Transactional
    public AlquilerDTO finalizarAlquiler(Integer id) throws Exception {

        // 1. Buscar el alquiler
        Alquiler alquiler = alquilerRepository.findById(id)
                .orElseThrow(() -> new Exception("El alquiler no existe."));

        // 2. VALIDACIÓN CRÍTICA
        if (alquiler.getEstado() != EstadoAlquiler.INICIADO) {
            throw new Exception("El alquiler no se puede finalizar (ya está finalizado o cancelado).");
        }

        // 3. Establecer la hora actual como fecha de fin
        LocalDateTime fechaDevolucion = LocalDateTime.now();
        alquiler.setFechaFin(fechaDevolucion);
        alquiler.setEstado(EstadoAlquiler.FINALIZADO);

        // 5. Recalcular costo final (AHORA CON BIGDECIMAL)
        long segundosReales = ChronoUnit.SECONDS.between(alquiler.getFechaInicio(), fechaDevolucion);
        if (segundosReales < 0) segundosReales = 0;

        BigDecimal precioPorHora = alquiler.getBicicleta().getPrecioPorHora();

        // División precisa: (precioPorHora / 3600)
        BigDecimal precioPorSegundo = precioPorHora.divide(SEGUNDOS_EN_HORA, 10, RoundingMode.HALF_UP);

        // Multiplicación precisa: (precioPorSegundo * segundosReales)
        BigDecimal costoCalculado = precioPorSegundo.multiply(new BigDecimal(segundosReales));

        // Redondeo final a 2 decimales para dinero
        BigDecimal costoFinal = costoCalculado.setScale(2, RoundingMode.HALF_UP);

        alquiler.setCostoTotal(costoFinal);

        // 6. Liberar la bici
        Bicicleta bicicleta = alquiler.getBicicleta();
        bicicleta.setEstado(EstadoBicicleta.DISPONIBLE);
        bicicletaRepository.save(bicicleta); // <-- Guardado 1

        // 7. Guardar y devolver
        Alquiler alquilerFinalizado = alquilerRepository.save(alquiler); // <-- Guardado 2

        return new AlquilerDTO(alquilerFinalizado);
    }
}
