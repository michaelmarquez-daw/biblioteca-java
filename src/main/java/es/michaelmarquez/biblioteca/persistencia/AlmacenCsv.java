package es.michaelmarquez.biblioteca.persistencia;

import es.michaelmarquez.biblioteca.modelo.Genero;
import es.michaelmarquez.biblioteca.modelo.Libro;
import es.michaelmarquez.biblioteca.modelo.Prestamo;
import es.michaelmarquez.biblioteca.modelo.Socio;
import es.michaelmarquez.biblioteca.servicio.Biblioteca;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Guarda la biblioteca en tres ficheros CSV separados por ";" dentro de una carpeta.
 * La primera línea de cada fichero es la cabecera y se ignora al leer.
 */
public class AlmacenCsv {

    private static final String SEPARADOR = ";";
    private static final String LIBROS = "libros.csv";
    private static final String SOCIOS = "socios.csv";
    private static final String PRESTAMOS = "prestamos.csv";

    private final Path carpeta;

    public AlmacenCsv(Path carpeta) {
        this.carpeta = carpeta;
    }

    public Biblioteca cargar() throws IOException {
        List<Libro> libros = leer(LIBROS, this::leerLibro);
        List<Socio> socios = leer(SOCIOS, this::leerSocio);
        List<Prestamo> prestamos = leer(PRESTAMOS, this::leerPrestamo);
        try {
            return new Biblioteca(libros, socios, prestamos);
        } catch (IllegalArgumentException e) {
            throw new IOException("Datos incoherentes: " + e.getMessage(), e);
        }
    }

    public void guardar(Biblioteca biblioteca) throws IOException {
        Files.createDirectories(carpeta);

        List<String> lineas = new ArrayList<>();
        lineas.add("isbn;titulo;autor;anio;genero");
        for (Libro l : biblioteca.getLibros()) {
            lineas.add(String.join(SEPARADOR, l.getIsbn(), l.getTitulo(), l.getAutor(),
                    String.valueOf(l.getAnio()), l.getGenero().name()));
        }
        Files.write(carpeta.resolve(LIBROS), lineas, StandardCharsets.UTF_8);

        lineas = new ArrayList<>();
        lineas.add("numero;nombre;email");
        for (Socio s : biblioteca.getSocios()) {
            lineas.add(String.join(SEPARADOR, String.valueOf(s.getNumero()), s.getNombre(), s.getEmail()));
        }
        Files.write(carpeta.resolve(SOCIOS), lineas, StandardCharsets.UTF_8);

        lineas = new ArrayList<>();
        lineas.add("id;isbn;numeroSocio;fechaPrestamo;fechaLimite;fechaDevolucion");
        for (Prestamo p : biblioteca.getPrestamos()) {
            String devolucion = p.getFechaDevolucion() == null ? "" : p.getFechaDevolucion().toString();
            lineas.add(String.join(SEPARADOR, String.valueOf(p.getId()), p.getIsbn(),
                    String.valueOf(p.getNumeroSocio()), p.getFechaPrestamo().toString(),
                    p.getFechaLimite().toString(), devolucion));
        }
        Files.write(carpeta.resolve(PRESTAMOS), lineas, StandardCharsets.UTF_8);
    }

    /**
     * Lee un fichero y convierte cada línea con el conversor que se le pasa.
     * Si falta el fichero devuelve una lista vacía (primera vez que se arranca).
     * Si una línea está mal se para todo: es mejor avisar que cargar datos a medias
     * y luego machacar el fichero al guardar.
     */
    private <T> List<T> leer(String nombreFichero, Function<String[], T> conversor) throws IOException {
        Path ruta = carpeta.resolve(nombreFichero);
        List<T> resultado = new ArrayList<>();
        if (Files.notExists(ruta)) {
            return resultado;
        }
        List<String> lineas = Files.readAllLines(ruta, StandardCharsets.UTF_8);
        for (int i = 1; i < lineas.size(); i++) {
            String linea = lineas.get(i);
            if (linea.isBlank()) {
                continue;
            }
            try {
                resultado.add(conversor.apply(linea.split(SEPARADOR, -1)));
            } catch (RuntimeException e) {
                throw new IOException(nombreFichero + ", línea " + (i + 1) + ": " + e.getMessage(), e);
            }
        }
        return resultado;
    }

    private Libro leerLibro(String[] campos) {
        comprobarCampos(campos, 5);
        return new Libro(campos[0], campos[1], campos[2], Integer.parseInt(campos[3].trim()),
                Genero.valueOf(campos[4].trim()));
    }

    private Socio leerSocio(String[] campos) {
        comprobarCampos(campos, 3);
        return new Socio(Integer.parseInt(campos[0].trim()), campos[1], campos[2]);
    }

    private Prestamo leerPrestamo(String[] campos) {
        comprobarCampos(campos, 6);
        LocalDate devolucion = campos[5].isBlank() ? null : LocalDate.parse(campos[5].trim());
        return new Prestamo(Integer.parseInt(campos[0].trim()), campos[1].trim(),
                Integer.parseInt(campos[2].trim()), LocalDate.parse(campos[3].trim()),
                LocalDate.parse(campos[4].trim()), devolucion);
    }

    private void comprobarCampos(String[] campos, int esperados) {
        if (campos.length != esperados) {
            throw new IllegalArgumentException("se esperaban " + esperados + " campos y hay " + campos.length);
        }
    }
}
