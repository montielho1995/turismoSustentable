package DSII.TurismoSustentable.dto;

import DSII.TurismoSustentable.models.Estacion;
import DSII.TurismoSustentable.models.EstadoBicicleta; // <-- Importante para el filtro

public class EstacionDTO {

    private int id;
    private String nombre;
    private String direccion;
    private int capacidad;

    // --- NUEVOS CAMPOS CALCULADOS ---
    private int bicicletasDisponibles;
    private int espaciosLibres;

    public EstacionDTO(Estacion estacion) {
        this.id = estacion.getId();
        this.nombre = estacion.getNombre();
        this.direccion = estacion.getDireccion();
        this.capacidad = estacion.getCapacidad();

        // --- CÁLCULO CON STREAMS (OPCIÓN 1) ---
        if (estacion.getBicicletasEnEstacion() != null) {
            this.bicicletasDisponibles = (int) estacion.getBicicletasEnEstacion().stream()
                    .filter(b -> b.getEstado() == EstadoBicicleta.DISPONIBLE)
                    .count();
        } else {
            this.bicicletasDisponibles = 0;
        }

        // Calculamos espacios libres (Capacidad - bicis disponibles)
        this.espaciosLibres = this.capacidad - this.bicicletasDisponibles;
    }

    // GETTERS
    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public int getBicicletasDisponibles() {
        return bicicletasDisponibles;
    }

    public int getEspaciosLibres() {
        return espaciosLibres;
    }
}
