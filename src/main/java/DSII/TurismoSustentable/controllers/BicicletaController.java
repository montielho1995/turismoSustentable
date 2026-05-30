package DSII.TurismoSustentable.controllers;

import DSII.TurismoSustentable.dto.BicicletaDTO;
import DSII.TurismoSustentable.models.Bicicleta;
import DSII.TurismoSustentable.models.EstadoBicicleta; // Importación de Enum
import DSII.TurismoSustentable.repositories.BicicletaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class BicicletaController {

    @Autowired
    private BicicletaRepository bicicletaRepository;

    // OBTENER TODAS LAS BICIS DE LA BD (Solo las no borradas)
    @GetMapping("/bicicletas")
    public List<BicicletaDTO> getBicicletas(){
        // Filtra solo por borrado lógico (muestra MANTENIMIENTO, ALQUILADA, etc.)
        return bicicletaRepository.findByBorradoBiciFalse()
                .stream()
                .map(BicicletaDTO:: new)
                .collect(Collectors.toList());
    }

    // OBTIENE LAS BICIS DISPONIBLES PARA ALQUILAR (Corrección de Enum)
    @GetMapping("/bicicletas/disponibles")
    public List<BicicletaDTO> getBicicletasDisponibles(){
        // Filtra por no borrada (borradoBici=false) Y estado = DISPONIBLE (Enum)
        return bicicletaRepository.findByBorradoBiciFalseAndEstado(EstadoBicicleta.DISPONIBLE)
                .stream()
                .map(BicicletaDTO:: new)
                .collect(Collectors.toList());
    }

    // GET por ID
    @GetMapping("/bicicletas/{id}")
    public ResponseEntity<BicicletaDTO> getBicicletaPorId(@PathVariable Integer id) {

        Optional<Bicicleta> bicicletaOpt = bicicletaRepository.findById(id);

        if (bicicletaOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Bicicleta bicicleta = bicicletaOpt.get();

        // Filtro de seguridad. Si la bici está borrada lógicamente, devuelve 404
        if (bicicleta.isBorradoBici()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(new BicicletaDTO(bicicleta));
    }

    // POST /api/bicicletas: Crear nueva bicicleta (Solo ADMIN)
    @PostMapping("/bicicletas")
    public ResponseEntity<BicicletaDTO> crearBicicleta(@Valid @RequestBody Bicicleta bicicleta) {
        // Spring convierte el JSON String a Enum y valida los campos (@Valid)
        Bicicleta nuevaBicicleta = bicicletaRepository.save(bicicleta);
        // Devuelve un DTO
        return new ResponseEntity<>(new BicicletaDTO(nuevaBicicleta), HttpStatus.CREATED);
    }

    // DELETE /api/bicicletas/{id}: Borrado Lógico (Solo ADMIN)
    @DeleteMapping("/bicicletas/{id}")
    public ResponseEntity<Object> eliminarBicicletaLogico(@PathVariable Integer id) {

        Optional<Bicicleta> bicicletaOptional = bicicletaRepository.findById(id);

        if (bicicletaOptional.isEmpty()) {
            return new ResponseEntity<>("La bicicleta no existe.", HttpStatus.NOT_FOUND);
        }

        Bicicleta bicicleta = bicicletaOptional.get();

        // --- ¡VERIFICACIÓN DE ESTADO! ---
        if (bicicleta.getEstado() == EstadoBicicleta.ALQUILADA) {
            return new ResponseEntity<>(
                    "No se puede eliminar una bicicleta que está actualmente alquilada.",
                    HttpStatus.CONFLICT // 409 Conflict es el código correcto
            );
        }
        // ---------------------------------

        // Borrado lógico (Tu sugerencia: INACTIVA)
        bicicleta.setBorradoBici(true);
        bicicleta.setEstado(EstadoBicicleta.INACTIVA); // La ponemos inactiva

        bicicletaRepository.save(bicicleta);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content
    }

    // PUT /api/bicicletas/{id}: Actualizar bicicleta (Solo ADMIN)
    @PutMapping("/bicicletas/{id}")
    public ResponseEntity<Object> actualizarBicicleta(@PathVariable Integer id, @RequestBody Bicicleta bicicletaDetalles) {

        Optional<Bicicleta> bicicletaOpt = bicicletaRepository.findById(id);

        // Manejo de error 404 y filtro de borrado lógico
        if (bicicletaOpt.isEmpty() || bicicletaOpt.get().isBorradoBici()) {
            return new ResponseEntity<>("La bicicleta no existe o está inactiva.", HttpStatus.NOT_FOUND);
        }

        Bicicleta bicicletaExistente = bicicletaOpt.get();

        // --- ACTUALIZACIÓN SEGURA (Validar Nulos) ---

        if (bicicletaDetalles.getCodigoBicicleta() != null) {
            bicicletaExistente.setCodigoBicicleta(bicicletaDetalles.getCodigoBicicleta());
        }

        if (bicicletaDetalles.getEstado() != null) {
            bicicletaExistente.setEstado(bicicletaDetalles.getEstado());
        }

        // Aquí actualizamos la relación con la Entidad Estación
        if (bicicletaDetalles.getUbicacionActual() != null) {
            bicicletaExistente.setUbicacionActual(bicicletaDetalles.getUbicacionActual());
        }

        if (bicicletaDetalles.getTipo() != null) {
            bicicletaExistente.setTipo(bicicletaDetalles.getTipo());
        }

        if (bicicletaDetalles.getPrecioPorHora() != null) {
            bicicletaExistente.setPrecioPorHora(bicicletaDetalles.getPrecioPorHora());
        }

        Bicicleta bicicletaActualizada = bicicletaRepository.save(bicicletaExistente);
        return ResponseEntity.ok(new BicicletaDTO(bicicletaActualizada));
    }
}
