package es.michaelmarquez.biblioteca.persistencia;

import es.michaelmarquez.biblioteca.modelo.Genero;
import es.michaelmarquez.biblioteca.modelo.Libro;
import es.michaelmarquez.biblioteca.modelo.Prestamo;
import es.michaelmarquez.biblioteca.modelo.Socio;
import es.michaelmarquez.biblioteca.servicio.Biblioteca;
import es.michaelmarquez.biblioteca.servicio.BibliotecaException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlmacenCsvTest {

    @TempDir
    Path carpeta;

    @Test
    void loQueSeGuardaSeVuelveACargarIgual() throws IOException, BibliotecaException {
        Biblioteca original = new Biblioteca();
        original.altaLibro(new Libro("9788497595049", "La sombra del viento", "Carlos Ruiz Zafón",
                2001, Genero.NOVELA));
        original.altaLibro(new Libro("9788484509400", "Campos de Castilla", "Antonio Machado",
                1912, Genero.POESIA));
        int socio = original.altaSocio("Sara Benítez Ruiz", "sara.benitez@example.com").getNumero();
        LocalDate dia = LocalDate.of(2026, 1, 10);
        original.prestar("9788497595049", socio, dia);
        original.prestar("9788484509400", socio, dia);
        original.devolver("9788484509400", dia.plusDays(20));

        AlmacenCsv almacen = new AlmacenCsv(carpeta);
        almacen.guardar(original);
        Biblioteca cargada = almacen.cargar();

        Libro libro = cargada.getLibro("9788497595049");
        assertEquals("La sombra del viento", libro.getTitulo());
        assertEquals("Carlos Ruiz Zafón", libro.getAutor());
        assertEquals(Genero.NOVELA, libro.getGenero());

        Socio sara = cargada.getSocio(socio);
        assertEquals("Sara Benítez Ruiz", sara.getNombre());

        List<Prestamo> prestamos = cargada.getPrestamos();
        assertEquals(2, prestamos.size());
        assertNull(prestamos.get(0).getFechaDevolucion());
        assertEquals(dia.plusDays(20), prestamos.get(1).getFechaDevolucion());
        assertEquals(6, prestamos.get(1).diasDeRetraso(dia.plusDays(30)));
        assertTrue(cargada.estaPrestado("9788497595049"));
    }

    @Test
    void losIdsDePrestamoSiguenDespuesDeCargar() throws IOException, BibliotecaException {
        Biblioteca original = new Biblioteca();
        original.altaLibro(new Libro("9788497595049", "La sombra del viento", "Carlos Ruiz Zafón",
                2001, Genero.NOVELA));
        original.altaLibro(new Libro("9788484509400", "Campos de Castilla", "Antonio Machado",
                1912, Genero.POESIA));
        int socio = original.altaSocio("Sara Benítez Ruiz", "sara.benitez@example.com").getNumero();
        original.prestar("9788497595049", socio, LocalDate.of(2026, 1, 10));

        AlmacenCsv almacen = new AlmacenCsv(carpeta);
        almacen.guardar(original);
        Biblioteca cargada = almacen.cargar();

        Prestamo nuevo = cargada.prestar("9788484509400", socio, LocalDate.of(2026, 1, 11));
        assertEquals(2, nuevo.getId());
    }

    @Test
    void siNoHayFicherosEmpiezaVacia() throws IOException {
        Biblioteca biblioteca = new AlmacenCsv(carpeta.resolve("no-existe")).cargar();

        assertTrue(biblioteca.getLibros().isEmpty());
        assertTrue(biblioteca.getSocios().isEmpty());
    }

    @Test
    void unaLineaMalFormadaDaErrorConElNumeroDeLinea() throws IOException {
        Files.writeString(carpeta.resolve("libros.csv"),
                "isbn;titulo;autor;anio;genero\n"
                        + "9788437602547;Nada;Carmen Laforet;1945;NOVELA\n"
                        + "9788466307666;Patria;Fernando Aramburu;dos mil;NOVELA\n",
                StandardCharsets.UTF_8);

        IOException e = assertThrows(IOException.class, () -> new AlmacenCsv(carpeta).cargar());
        assertTrue(e.getMessage().contains("línea 3"));
    }

    @Test
    void despuesDeCargarNoSeRepiteElNumeroDeUnSocioBorrado() throws IOException, BibliotecaException {
        Biblioteca original = new Biblioteca();
        original.altaLibro(new Libro("9788497595049", "La sombra del viento", "Carlos Ruiz Zafón",
                2001, Genero.NOVELA));
        original.altaSocio("Sara Benítez Ruiz", "sara.benitez@example.com");
        int pablo = original.altaSocio("Pablo Ortega León", "pablo.ortega@example.com").getNumero();
        original.prestar("9788497595049", pablo, LocalDate.of(2026, 1, 10));
        original.devolver("9788497595049", LocalDate.of(2026, 1, 15));
        original.bajaSocio(pablo);

        AlmacenCsv almacen = new AlmacenCsv(carpeta);
        almacen.guardar(original);
        Biblioteca cargada = almacen.cargar();

        int nuevo = cargada.altaSocio("Inés Molina Vidal", "ines.molina@example.com").getNumero();
        assertEquals(pablo + 1, nuevo);
    }

    @Test
    void unIsbnRepetidoNoSeCarga() throws IOException {
        Files.writeString(carpeta.resolve("libros.csv"),
                "isbn;titulo;autor;anio;genero\n"
                        + "9788437602547;Nada;Carmen Laforet;1945;NOVELA\n"
                        + "9788437602547;Nada (otra edición);Carmen Laforet;2010;NOVELA\n",
                StandardCharsets.UTF_8);

        IOException e = assertThrows(IOException.class, () -> new AlmacenCsv(carpeta).cargar());
        assertTrue(e.getMessage().contains("repetido"));
    }
}
