package es.michaelmarquez.biblioteca.modelo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Prestamo {

    private final int id;
    private final String isbn;
    private final int numeroSocio;
    private final LocalDate fechaPrestamo;
    private final LocalDate fechaLimite;
    private LocalDate fechaDevolucion;

    public Prestamo(int id, String isbn, int numeroSocio, LocalDate fechaPrestamo, LocalDate fechaLimite) {
        this(id, isbn, numeroSocio, fechaPrestamo, fechaLimite, null);
    }

    public Prestamo(int id, String isbn, int numeroSocio, LocalDate fechaPrestamo,
                    LocalDate fechaLimite, LocalDate fechaDevolucion) {
        if (fechaPrestamo == null || fechaLimite == null) {
            throw new IllegalArgumentException("Las fechas de préstamo y límite son obligatorias");
        }
        if (fechaLimite.isBefore(fechaPrestamo)) {
            throw new IllegalArgumentException("La fecha límite no puede ser anterior al préstamo");
        }
        this.id = id;
        this.isbn = isbn;
        this.numeroSocio = numeroSocio;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaLimite = fechaLimite;
        this.fechaDevolucion = fechaDevolucion;
    }

    public boolean estaActivo() {
        return fechaDevolucion == null;
    }

    public boolean estaVencido(LocalDate hoy) {
        return estaActivo() && hoy.isAfter(fechaLimite);
    }

    /**
     * Si ya se devolvió, cuenta hasta la fecha de devolución; si no, hasta hoy.
     * Nunca devuelve negativos: entregarlo antes de tiempo son 0 días de retraso.
     */
    public long diasDeRetraso(LocalDate hoy) {
        LocalDate fin = estaActivo() ? hoy : fechaDevolucion;
        long dias = ChronoUnit.DAYS.between(fechaLimite, fin);
        return Math.max(0, dias);
    }

    public void registrarDevolucion(LocalDate fecha) {
        if (!estaActivo()) {
            throw new IllegalStateException("Este préstamo ya se había devuelto");
        }
        if (fecha.isBefore(fechaPrestamo)) {
            throw new IllegalArgumentException("La devolución no puede ser anterior al préstamo");
        }
        this.fechaDevolucion = fecha;
    }

    public int getId() {
        return id;
    }

    public String getIsbn() {
        return isbn;
    }

    public int getNumeroSocio() {
        return numeroSocio;
    }

    public LocalDate getFechaPrestamo() {
        return fechaPrestamo;
    }

    public LocalDate getFechaLimite() {
        return fechaLimite;
    }

    public LocalDate getFechaDevolucion() {
        return fechaDevolucion;
    }
}
