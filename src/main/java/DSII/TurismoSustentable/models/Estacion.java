package DSII.TurismoSustentable.models;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Estacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(unique = true) // No permitimos dos estaciones con el mismo nombre
    private String nombre; // Ej: "Plaza Cien Años"
    @NotBlank(message = "La dirección es obligatoria")
    private String direccion; // Ej: "Av. San Martín 123"
    @Min(value = 1, message = "La capacidad debe ser al menos 1")
    private int capacidad; // Ej: 20 (cuántas bicis entran)
    private boolean borradoEstacion = false;

    // Relación: Una Estación puede tener muchas bicicletas "estacionadas" aquí.
    // Esta es la "otra cara" del @ManyToOne que pondremos en Bicicleta.
    @OneToMany(mappedBy = "ubicacionActual", fetch = FetchType.LAZY)
    private Set<Bicicleta> bicicletasEnEstacion = new HashSet<>();

    // CONSTRUCTORES
    public Estacion() {
    }

    public Estacion(String nombre, String direccion, int capacidad) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.capacidad = capacidad;
        this.borradoEstacion = false;
    }

    // GETTERS Y SETTERS (Básicos)
    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    public Set<Bicicleta> getBicicletasEnEstacion() {
        return bicicletasEnEstacion;
    }

    public boolean isBorradoEstacion() {
        return borradoEstacion;
    }
    public void setBorradoEstacion(boolean borradoEstacion) {
        this.borradoEstacion = borradoEstacion;
    }

    // --- equals() y hashCode() ---
    // Importante para que JPA maneje bien las relaciones.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Estacion estacion = (Estacion) o;
        return nombre != null ? nombre.equals(estacion.nombre) : estacion.nombre == null;
    }

    @Override
    public int hashCode() {
        return nombre != null ? nombre.hashCode() : 0;
    }
}
