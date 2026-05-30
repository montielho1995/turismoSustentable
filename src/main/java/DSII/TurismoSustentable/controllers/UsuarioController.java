package DSII.TurismoSustentable.controllers;

import DSII.TurismoSustentable.dto.UsuarioDTO;
import DSII.TurismoSustentable.models.Rol;
import DSII.TurismoSustentable.models.Usuario;
import DSII.TurismoSustentable.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class UsuarioController {
    @Autowired
    private UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    // Constructor para inyección de dependencias
    public UsuarioController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/usuarios")
    public List<UsuarioDTO> getUsuarios(){
        // metodo que filtra por activo
        return usuarioRepository.findByActivoTrue()
                .stream()
                .map(UsuarioDTO:: new)
                .collect(Collectors.toList());
    }

    @GetMapping("/usuarios/{id}")
    public ResponseEntity<UsuarioDTO> getUsuarioPorId(@PathVariable Integer id) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);

        // Verificar el ID y devolver 404
        if (usuarioOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Usuario usuario = usuarioOpt.get();

        // Filtrado por activo/inactivo
        if (!usuario.isActivo()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Devolver si está activo
        return ResponseEntity.ok(new UsuarioDTO(usuario)); // Devuelve 200 OK con el DTO
    }

    // DEVUELVE EL NOMBRE DEL USUARIO ACTUAL
    @GetMapping("/usuarios/actual")
    public ResponseEntity<UsuarioDTO> getUsuarioActual(Principal principal) {

        // Spring Security inyecta el objeto Principal (que contiene el email)
        String email = principal.getName();

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado."));

        return ResponseEntity.ok(new UsuarioDTO(usuario));
    }

    @PostMapping("/usuarios")
    public ResponseEntity<UsuarioDTO> createUsuario(@Valid @RequestBody Usuario usuario) {

        // OBTENER la contraseña en texto plano
        String rawPassword = usuario.getPassword();

        // CIFRAR la contraseña y reasignarla al objeto
        String encodedPassword = passwordEncoder.encode(rawPassword);
        usuario.setPassword(encodedPassword);

        // Establecer el rol por defecto (que no se recibe del frontend)
        usuario.setRol(Rol.CLIENTE);

        // Guardar en la BD (la contraseña ya está cifrada)
        Usuario nuevoUsuario = usuarioRepository.save(usuario);

        return new ResponseEntity<>(new UsuarioDTO(nuevoUsuario), HttpStatus.CREATED);
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<Object> eliminarUsuarioLogico(@PathVariable Integer id) {

        Optional<Usuario> usuarioOptional = usuarioRepository.findById(id); // Buscar el usuario

        if (usuarioOptional.isEmpty()) { // Verificar existencia
            // Si no existe devuelve 404
            return new ResponseEntity<>("El usuario con ID " + id + " no existe.", HttpStatus.NOT_FOUND);
        }

        Usuario usuario = usuarioOptional.get();

        // --- NUEVA VALIDACIÓN DE ROL ---
        if (usuario.getRol() == Rol.ADMIN) {
            return new ResponseEntity<>(
                    "No se puede eliminar a un usuario Administrador.",
                    HttpStatus.FORBIDDEN // 403 Prohibido
            );
        }
        // -------------------------------

        // borrado logico
        if (!usuario.isActivo()) {
            // si ya estaba inactivo, devuelve un error 400
            return new ResponseEntity<>("El usuario ya se encuentra inactivo.", HttpStatus.BAD_REQUEST);
        }
        usuario.setActivo(false); // Inactiva la cuenta
        usuarioRepository.save(usuario); // UPDATE
        return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content
    }

    @PutMapping("/usuarios/{id}")
    public ResponseEntity<Object> actualizarUsuario(@PathVariable Integer id, @RequestBody Usuario usuarioDetalles) {

        // Buscar el usuario existente
        Optional<Usuario> usuarioOptional = usuarioRepository.findById(id);

        // 404 si el ID no existe
        if (usuarioOptional.isEmpty()) {
            return new ResponseEntity<>("El usuario con ID " + id + " no existe.", HttpStatus.NOT_FOUND);
        }

        Usuario usuarioExistente = usuarioOptional.get();

        // Filtro de seguridad por borrado lógico
        if (!usuarioExistente.isActivo()) {
            return new ResponseEntity<>("El usuario no está activo y no puede ser modificado.", HttpStatus.NOT_FOUND);
        }

        // --- MODIFICACIÓN: Validar Nulos antes de asignar (Permite edición parcial) ---

        if (usuarioDetalles.getNombre() != null) {
            usuarioExistente.setNombre(usuarioDetalles.getNombre());
        }

        if (usuarioDetalles.getApellido() != null) {
            usuarioExistente.setApellido(usuarioDetalles.getApellido());
        }

        if (usuarioDetalles.getEmail() != null) {
            usuarioExistente.setEmail(usuarioDetalles.getEmail());
        }

        if (usuarioDetalles.getGenero() != null) {
            usuarioExistente.setGenero(usuarioDetalles.getGenero());
        }

        if (usuarioDetalles.getFechaNacimiento() != null) {
            usuarioExistente.setFechaNacimiento(usuarioDetalles.getFechaNacimiento());
        }

        // NOTA: La contraseña se maneja con precaución. Si se envía una nueva, se aplica.
        if (usuarioDetalles.getPassword() != null && !usuarioDetalles.getPassword().isEmpty()) {
            String claveNuevaEncriptada = passwordEncoder.encode(usuarioDetalles.getPassword());
            usuarioExistente.setPassword(claveNuevaEncriptada);
        }

        // Guardar y devolver el objeto actualizado
        Usuario usuarioActualizado = usuarioRepository.save(usuarioExistente);

        // Devolver la respuesta mapeada a DTO / Status 200 OK
        return ResponseEntity.ok(new UsuarioDTO(usuarioActualizado));
    }
}
