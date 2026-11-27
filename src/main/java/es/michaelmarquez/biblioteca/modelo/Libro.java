package es.michaelmarquez.biblioteca.modelo;

import java.time.Year;

public class Libro {

    public static final int ANIO_MINIMO = 1450;

    private final String isbn;
    private final String titulo;
    private final String autor;
    private final int anio;
    private final Genero genero;

    public Libro(String isbn, String titulo, String autor, int anio, Genero genero) {
        if (!esIsbnValido(isbn)) {
            throw new IllegalArgumentException("El ISBN " + isbn + " no es válido");
        }
        int anioActual = Year.now().getValue();
        if (anio < ANIO_MINIMO || anio > anioActual) {
            throw new IllegalArgumentException("El año debe estar entre " + ANIO_MINIMO + " y " + anioActual);
        }
        if (genero == null) {
            throw new IllegalArgumentException("El género es obligatorio");
        }
        this.isbn = normalizarIsbn(isbn);
        this.titulo = Validacion.textoObligatorio(titulo, "título");
        this.autor = Validacion.textoObligatorio(autor, "autor");
        this.anio = anio;
        this.genero = genero;
    }

    /** Quita guiones y espacios para poder comparar ISBN escritos de distinta forma. */
    public static String normalizarIsbn(String isbn) {
        if (isbn == null) {
            return "";
        }
        return isbn.replace("-", "").replace(" ", "");
    }

    /**
     * Comprueba un ISBN-13: los 12 primeros dígitos se multiplican
     * alternando por 1 y por 3, y el último es el dígito de control.
     */
    public static boolean esIsbnValido(String isbn) {
        String digitos = normalizarIsbn(isbn);
        if (!digitos.matches("\\d{13}")) {
            return false;
        }
        int suma = 0;
        for (int i = 0; i < 12; i++) {
            int digito = digitos.charAt(i) - '0';
            suma += (i % 2 == 0) ? digito : digito * 3;
        }
        int control = (10 - suma % 10) % 10;
        return control == digitos.charAt(12) - '0';
    }

    public String getIsbn() {
        return isbn;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getAutor() {
        return autor;
    }

    public int getAnio() {
        return anio;
    }

    public Genero getGenero() {
        return genero;
    }

    @Override
    public String toString() {
        return titulo + " (" + autor + ", " + anio + ")";
    }
}
