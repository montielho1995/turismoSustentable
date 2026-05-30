package DSII.TurismoSustentable.models;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private int id;

    @NotBlank(message = "El nombre no puede estar vacío.")
    private String nombre;

    @NotBlank(message = "El apellido no puede estar vacío.")
    private String apellido;

    @NotBlank(message = "El email no puede estar vacío.")
    @Email(message = "Debe ser una dirección de correo válida.")
    // Validación de Email: NO puede empezar con un dígito ([0-9]), seguida del @.
    @Pattern(regexp = "^[^0-9].*@.*\\..*$", message = "El email no puede comenzar con un número.")
    private String email;

    @NotBlank(message = "La clave es obligatoria.")
    @Size(min = 6, message = "La clave debe tener al menos 6 caracteres.")
    // Validación de Contraseña: Mínimo 6 caracteres, al menos 1 mayúscula, al menos 1 caracter especial.
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[!@#$&*])(?=\\S+$).{6,}$",
            message = "La clave debe tener al menos 6 caracteres, una mayúscula y un carácter especial.")
    private String password;    // Valor encriptado en la BD

    @NotNull(message = "La fecha de nacimiento es obligatoria.")
    @Past(message = "La fecha de nacimiento debe estar en el pasado.")
    private LocalDate fechaNacimiento;

    @NotNull(message = "El género es obligatorio.")
    @Enumerated(EnumType.STRING)
    private GeneroUsuario genero;

    // --- CAMPO ROL MODIFICADO ---
    @Enumerated(EnumType.STRING) // Almacena el Enum como String en la BD
    private Rol rol = Rol.CLIENTE; // Valor por defecto: CLIENTE
    // ----------------------------

    @CreationTimestamp
    private LocalDate fechaRegistro;
    private boolean activo = true;


    @OneToMany(mappedBy = "usuario", fetch = FetchType.EAGER)
    Set<Alquiler> alquileres = new HashSet<>();

    @OneToMany(mappedBy = "usuario", fetch = FetchType.EAGER)
    Set<Tarjeta> tarjetas = new HashSet<>();

    // CONSTRUCTORES
    public Usuario() {
    }

    // Constructor actualizado con los nuevos campos y el rol
    public Usuario(String nombre, String apellido, String email, String password, LocalDate fechaNacimiento, GeneroUsuario genero, boolean activo, Rol rol) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.password = password;
        this.fechaNacimiento = fechaNacimiento;
        this.genero = genero;
        this.fechaRegistro = LocalDate.now();
        this.activo = true;
        this.rol = rol; // Asignación del rol
        this.alquileres = new HashSet<>();
        this.tarjetas = new HashSet<>();
    }

    // Constructor de conveniencia para CLIENTE
    public Usuario(String nombre, String apellido, String email, String password, LocalDate fechaNacimiento, GeneroUsuario genero, boolean activo) {
        this(nombre, apellido, email, password, fechaNacimiento, genero, activo, Rol.CLIENTE);
    }

    // Metodo para añadir un Alquiler
    public void addAlquiler(Alquiler alquiler){
        alquiler.setUsuario(this);
        this.alquileres.add(alquiler);
    }

    // Metodo para añadir una Tarjeta
    public void addTarjeta(Tarjeta tarjeta){
        tarjeta.setUsuario(this);
        this.tarjetas.add(tarjeta);
    }

    // GETTERS Y SETTERS
    public int getId() {
        return id;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    // **METODO DE CONVENIENCIA PARA SABER SI ES ADMINISTRADOR**
    public boolean esAdministrador() {
        return this.rol == Rol.ADMIN;
    }

    // **METODO DE CONVENIENCIA PARA SABER SI PUEDE ALQUILAR**
    public boolean puedeAlquilar() {
        return this.rol == Rol.CLIENTE || this.rol == Rol.ADMIN;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public GeneroUsuario getGenero() {
        return genero;
    }

    public void setGenero(GeneroUsuario genero) {
        this.genero = genero;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Set<Alquiler> getAlquileres() {
        return alquileres;
    }

    public Set<Tarjeta> getTarjetas() {
        return tarjetas;
    }

    public void setAlquileres(Set<Alquiler> alquileres) {
        this.alquileres = alquileres;
    }

    public void setTarjetas(Set<Tarjeta> tarjetas) {
        this.tarjetas = tarjetas;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Usuario usuario = (Usuario) o;

        // Usamos el email como identificador único de negocio.
        // Asegura que tu email tenga una restricción "UNIQUE" en la BD.
        return email != null ? email.equals(usuario.email) : usuario.email == null;
    }

    @Override
    public int hashCode() {
        // Usamos el email para generar el hashCode.
        return email != null ? email.hashCode() : 0;
    }

}