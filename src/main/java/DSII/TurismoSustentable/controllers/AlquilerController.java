package DSII.TurismoSustentable.controllers;

import DSII.TurismoSustentable.dto.AlquilerDTO;
import DSII.TurismoSustentable.dto.AlquilerRequestDTO;
import DSII.TurismoSustentable.models.Usuario;
import DSII.TurismoSustentable.repositories.AlquilerRepository;
import DSII.TurismoSustentable.repositories.UsuarioRepository;
import DSII.TurismoSustentable.services.AlquilerService; // <-- 1. IMPORTA EL SERVICIO
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class AlquilerController {

    // --- Inyectamos el Servicio ---
    @Autowired
    private AlquilerService alquilerService;

    // --- Mantenemos estos Repos solo para el GET (está bien por ahora) ---
    @Autowired
    private AlquilerRepository alquilerRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    // TU MÉTODO GET (SIN CAMBIOS, es solo lectura y funciona bien)
    @GetMapping("/alquileres")
    public List<AlquilerDTO> getAlquiler(Principal principal) {
        Usuario usuario = usuarioRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado o sesión expirada"));
        return alquilerRepository.findByUsuarioAndBorradoAlquilerFalse(usuario)
                .stream()
                .map(AlquilerDTO:: new)
                .collect(Collectors.toList());
    }

    /**
     * CREAR ALQUILER
     * Ahora solo llama al servicio y maneja la respuesta.
     */
    @PostMapping("/alquileres")
    public ResponseEntity<Object> crearAlquiler(@RequestBody AlquilerRequestDTO alquilerRequest) {
        try {
            // 2. Llama al servicio (que es transaccional)
            AlquilerDTO alquilerGuardado = alquilerService.crearAlquiler(alquilerRequest);

            // 3. Devuelve la respuesta OK
            return new ResponseEntity<>(alquilerGuardado, HttpStatus.CREATED);

        } catch (Exception e) {
            // 4. Si el servicio lanzó un error, devuélvelo como 400 Bad Request
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * BORRAR ALQUILER
     * Ahora solo llama al servicio y maneja la respuesta.
     */
    @DeleteMapping("/alquileres/{id}")
    public ResponseEntity<Object> borrarAlquilerLogico(@PathVariable Integer id) {
        try {
            // 1. Llama al servicio (que es transaccional)
            alquilerService.borrarAlquilerLogico(id);

            // 2. Devuelve éxito
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);

        } catch (Exception e) {
            // 3. Si el servicio lanzó un error (ej: 404), devuélvelo
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * FINALIZAR ALQUILER
     * Ahora solo llama al servicio y maneja la respuesta.
     */
    @PatchMapping("/alquileres/finalizar/{id}")
    public ResponseEntity<Object> finalizarAlquiler(@PathVariable Integer id) {
        try {
            // 1. Llama al servicio (que es transaccional)
            AlquilerDTO alquilerFinalizado = alquilerService.finalizarAlquiler(id);

            // 2. Devuelve la respuesta OK
            return ResponseEntity.ok(alquilerFinalizado);

        } catch (Exception e) {
            // 3. Si el servicio lanzó un error (404 o 400), devuélvelo
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}