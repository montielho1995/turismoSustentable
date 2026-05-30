package DSII.TurismoSustentable.controllers;

import DSII.TurismoSustentable.dto.EstacionDTO;
import DSII.TurismoSustentable.models.Estacion;
import DSII.TurismoSustentable.repositories.EstacionRepository;
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
public class EstacionController {

    @Autowired
    private EstacionRepository estacionRepository;

    // OBTENER TODAS LAS ESTACIONES (Solo las no borradas)
    @GetMapping("/estaciones")
    public List<EstacionDTO> getEstaciones() {
        // Filtra solo por borrado lógico
        return estacionRepository.findByBorradoEstacionFalse()
                .stream()
                .map(EstacionDTO::new)
                .collect(Collectors.toList());
    }

    // GET por ID
    @GetMapping("/estaciones/{id}")
    public ResponseEntity<EstacionDTO> getEstacionPorId(@PathVariable Integer id) {

        Optional<Estacion> estacionOpt = estacionRepository.findById(id);

        if (estacionOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Estacion estacion = estacionOpt.get();

        // Filtro de seguridad: Si está borrada lógicamente, devuelve 404
        if (estacion.isBorradoEstacion()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(new EstacionDTO(estacion));
    }

    // POST /api/estaciones: Crear nueva estación (Solo ADMIN)
    @PostMapping("/estaciones")
    public ResponseEntity<Object> crearEstacion(@Valid @RequestBody Estacion estacion) {

        // Validación extra: Nombre único
        if (estacionRepository.findByNombre(estacion.getNombre()).isPresent()) {
            return new ResponseEntity<>("Ya existe una estación con ese nombre.", HttpStatus.BAD_REQUEST);
        }

        Estacion nuevaEstacion = estacionRepository.save(estacion);
        // Devuelve un DTO
        return new ResponseEntity<>(new EstacionDTO(nuevaEstacion), HttpStatus.CREATED);
    }

    // DELETE /api/estaciones/{id}: Borrado Lógico (Solo ADMIN)
    @DeleteMapping("/estaciones/{id}")
    public ResponseEntity<Object> eliminarEstacionLogico(@PathVariable Integer id) {

        Optional<Estacion> estacionOptional = estacionRepository.findById(id);

        if (estacionOptional.isEmpty()) {
            return new ResponseEntity<>("La estación con ID " + id + " no existe.", HttpStatus.NOT_FOUND);
        }

        Estacion estacion = estacionOptional.get();

        // --- VALIDACIÓN DE BICICLETAS ASIGNADAS ---
        // Si la estación tiene bicicletas (la lista no está vacía), impedimos el borrado.
        if (!estacion.getBicicletasEnEstacion().isEmpty()) {
            return new ResponseEntity<>(
                    "No se puede eliminar la estación porque tiene bicicletas asignadas. Por favor, mueva las bicicletas a otra estación primero.",
                    HttpStatus.CONFLICT // Retorna error 409
            );
        }
        // ------------------------------------------

        // Borrado lógico
        estacion.setBorradoEstacion(true); // Asegúrate que el nombre del método coincida con tu Entidad (setBorrada o setBorradoEstacion)

        // Guardar la entidad actualizada
        estacionRepository.save(estacion);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content
    }

    // PUT /api/estaciones/{id}: Actualizar estación (Solo ADMIN)
    @PutMapping("/estaciones/{id}")
    public ResponseEntity<Object> actualizarEstacion(@PathVariable Integer id, @Valid @RequestBody Estacion estacionDetalles) {

        Optional<Estacion> estacionOpt = estacionRepository.findById(id);

        // Manejo de error 404 y filtro de borrado lógico
        if (estacionOpt.isEmpty() || estacionOpt.get().isBorradoEstacion()) {
            return new ResponseEntity<>("La estación no existe o está inactiva.", HttpStatus.NOT_FOUND);
        }

        Estacion estacionExistente = estacionOpt.get();

        // Aplicar los nuevos datos
        // Verificamos nulls para no borrar datos accidentalmente, o puedes asignar directo si confías en el @Valid
        if(estacionDetalles.getNombre() != null)
            estacionExistente.setNombre(estacionDetalles.getNombre());

        if(estacionDetalles.getDireccion() != null)
            estacionExistente.setDireccion(estacionDetalles.getDireccion());

        if(estacionDetalles.getCapacidad() > 0)
            estacionExistente.setCapacidad(estacionDetalles.getCapacidad());

        Estacion estacionActualizada = estacionRepository.save(estacionExistente);
        return ResponseEntity.ok(new EstacionDTO(estacionActualizada));
    }
}
