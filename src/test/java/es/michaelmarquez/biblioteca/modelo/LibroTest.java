package es.michaelmarquez.biblioteca.modelo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LibroTest {

    @Test
    void aceptaIsbnConOSinGuiones() {
        assertTrue(Libro.esIsbnValido("9788437602547"));
        assertTrue(Libro.esIsbnValido("978-84-376-0254-7"));
    }

    @Test
    void rechazaIsbnConDigitoDeControlMalOLongitudIncorrecta() {
        assertFalse(Libro.esIsbnValido("9788437602548"));
        assertFalse(Libro.esIsbnValido("978843760254"));
        assertFalse(Libro.esIsbnValido("97884376025AB"));
        assertFalse(Libro.esIsbnValido(null));
    }

    @Test
    void guardaElIsbnSinGuiones() {
        Libro libro = new Libro("978-84-376-0254-7", "Nada", "Carmen Laforet", 1945, Genero.NOVELA);

        assertEquals("9788437602547", libro.getIsbn());
    }

    @Test
    void noPermiteTituloVacioNiPuntoYComa() {
        assertThrows(IllegalArgumentException.class,
                () -> new Libro("9788437602547", "  ", "Carmen Laforet", 1945, Genero.NOVELA));
        assertThrows(IllegalArgumentException.class,
                () -> new Libro("9788437602547", "Nada; edición especial", "Carmen Laforet", 1945, Genero.NOVELA));
    }

    @Test
    void noPermiteAniosImposibles() {
        assertThrows(IllegalArgumentException.class,
                () -> new Libro("9788437602547", "Nada", "Carmen Laforet", 1200, Genero.NOVELA));
        assertThrows(IllegalArgumentException.class,
                () -> new Libro("9788437602547", "Nada", "Carmen Laforet", 3000, Genero.NOVELA));
    }
}
