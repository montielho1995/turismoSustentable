package DSII.TurismoSustentable.models;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Alquiler {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // Relaciones para el análisis de estadísticas (Género del Usuario, Tipo de Bicicleta)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false) // Aseguramos que el usuario no sea nulo
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bicicleta_id", nullable = false) // Aseguramos que la bicicleta no sea nula
    private Bicicleta bicicleta;

    // --- CAMPOS DE UBICACIÓN Y TIEMPO ---

    @NotNull(message = "La fecha de inicio es obligatoria.")
    private LocalDateTime fechaInicio;

    private LocalDateTime fechaFin;
    private BigDecimal costoTotal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estacion_retiro_id")
    @NotNull(message = "La ubicación de retiro es obligatoria.")
    private Estacion ubicacionRetiro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estacion_devolucion_id")
    private Estacion ubicacionDevolucion;

    // ------------------------------------

    @Enumerated(EnumType.STRING)
    private EstadoAlquiler estado;

    private boolean borradoAlquiler = false;

    // CONSTRUCTORES
    public Alquiler() {
    }

    // Constructor completo para INICIO del alquiler
    public Alquiler(Usuario usuario, Bicicleta bicicleta, Estacion ubicacionRetiro) {
        this.usuario = usuario;
        this.bicicleta = bicicleta;
        this.fechaInicio = LocalDateTime.now();
        this.ubicacionRetiro = ubicacionRetiro;
        this.estado = EstadoAlquiler.INICIADO; // <-- Uso directo del Enum
        this.costoTotal = BigDecimal.ZERO;
        this.borradoAlquiler = false;
    }

    public Alquiler(Usuario usuario, Bicicleta bicicleta, LocalDateTime fechaInicio,
                    LocalDateTime fechaFin, BigDecimal costoTotal, EstadoAlquiler estado,
                    Estacion ubicacionRetiro, Estacion ubicacionDevolucion) {
        this.usuario = usuario;
        this.bicicleta = bicicleta;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.costoTotal = costoTotal;
        this.estado = estado; // <-- Asignación directa del Enum
        this.ubicacionRetiro = ubicacionRetiro;
        this.ubicacionDevolucion = ubicacionDevolucion;
    }

    // GETTERS Y SETTERS

    public int getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Bicicleta getBicicleta() {
        return bicicleta;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public BigDecimal getCostoTotal() {
        return costoTotal;
    }

    public EstadoAlquiler getEstado() {
        return estado;
    }

    public Estacion getUbicacionRetiro() {
        return ubicacionRetiro;
    }

    public Estacion getUbicacionDevolucion() {
        return ubicacionDevolucion;
    }

    public boolean isBorradoAlquiler() {
        return borradoAlquiler;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public void setBicicleta(Bicicleta bicicleta) {
        this.bicicleta = bicicleta;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public void setCostoTotal(BigDecimal costoTotal) {
        this.costoTotal = costoTotal;
    }

    public void setEstado(EstadoAlquiler estado) {
        this.estado = estado;
    }

    public void setUbicacionRetiro(Estacion ubicacionRetiro) {
        this.ubicacionRetiro = ubicacionRetiro;
    }

    public void setUbicacionDevolucion(Estacion ubicacionDevolucion) {
        this.ubicacionDevolucion = ubicacionDevolucion;
    }

    public void setBorradoAlquiler(boolean borradoAlquiler) {
        this.borradoAlquiler = borradoAlquiler;
    }
}