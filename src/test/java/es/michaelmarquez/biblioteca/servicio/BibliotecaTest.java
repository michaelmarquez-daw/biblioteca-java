package es.michaelmarquez.biblioteca.servicio;

import es.michaelmarquez.biblioteca.modelo.Genero;
import es.michaelmarquez.biblioteca.modelo.Libro;
import es.michaelmarquez.biblioteca.modelo.Prestamo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BibliotecaTest {

    private static final String NADA = "9788437602547";
    private static final String PATRIA = "9788466307666";
    private static final String BODAS = "9788413351490";
    private static final String CAMPOS = "9788484509400";
    private static final String FAHRENHEIT = "9788490626481";

    private static final LocalDate HOY = LocalDate.of(2026, 3, 2);

    private Biblioteca biblioteca;
    private int lucia;
    private int daniel;

    @BeforeEach
    void prepararDatos() throws BibliotecaException {
        biblioteca = new Biblioteca();
        biblioteca.altaLibro(new Libro(NADA, "Nada", "Carmen Laforet", 1945, Genero.NOVELA));
        biblioteca.altaLibro(new Libro(PATRIA, "Patria", "Fernando Aramburu", 2016, Genero.NOVELA));
        biblioteca.altaLibro(new Libro(BODAS, "Bodas de sangre", "Federico García Lorca", 1933, Genero.TEATRO));
        biblioteca.altaLibro(new Libro(CAMPOS, "Campos de Castilla", "Antonio Machado", 1912, Genero.POESIA));
        biblioteca.altaLibro(new Libro(FAHRENHEIT, "Fahrenheit 451", "Ray Bradbury", 1953, Genero.CIENCIA_FICCION));
        lucia = biblioteca.altaSocio("Lucía Romero Sanz", "lucia.romero@example.com").getNumero();
        daniel = biblioteca.altaSocio("Daniel Herrero Gil", "daniel.herrero@example.com").getNumero();
    }

    @Test
    void elPrestamoTieneCatorceDiasDePlazo() throws BibliotecaException {
        Prestamo prestamo = biblioteca.prestar(NADA, lucia, HOY);

        assertEquals(LocalDate.of(2026, 3, 16), prestamo.getFechaLimite());
        assertTrue(biblioteca.estaPrestado(NADA));
    }

    @Test
    void noSePuedePrestarUnLibroQueYaEstaPrestado() throws BibliotecaException {
        biblioteca.prestar(NADA, lucia, HOY);

        assertThrows(PrestamoNoPermitidoException.class, () -> biblioteca.prestar(NADA, daniel, HOY));
    }

    @Test
    void unSocioNoPuedeTenerMasDeTresPrestamos() throws BibliotecaException {
        biblioteca.prestar(NADA, lucia, HOY);
        biblioteca.prestar(PATRIA, lucia, HOY);
        biblioteca.prestar(BODAS, lucia, HOY);

        PrestamoNoPermitidoException e = assertThrows(PrestamoNoPermitidoException.class,
                () -> biblioteca.prestar(CAMPOS, lucia, HOY));
        assertTrue(e.getMessage().contains("máximo"));
        assertFalse(biblioteca.estaPrestado(CAMPOS));
    }

    @Test
    void alDevolverUnoPuedeVolverAPedir() throws BibliotecaException {
        biblioteca.prestar(NADA, lucia, HOY);
        biblioteca.prestar(PATRIA, lucia, HOY);
        biblioteca.prestar(BODAS, lucia, HOY);
        biblioteca.devolver(PATRIA, HOY.plusDays(3));

        biblioteca.prestar(CAMPOS, lucia, HOY.plusDays(3));

        assertEquals(3, biblioteca.prestamosActivosDe(lucia).size());
    }

    @Test
    void unSocioConPrestamosVencidosNoPuedePedirOtro() throws BibliotecaException {
        biblioteca.prestar(NADA, lucia, HOY);
        LocalDate dentroDeUnMes = HOY.plusDays(30);

        assertThrows(PrestamoNoPermitidoException.class,
                () -> biblioteca.prestar(PATRIA, lucia, dentroDeUnMes));
    }

    @Test
    void devolucionConRetrasoCuentaLosDiasPasadosDeLaFechaLimite() throws BibliotecaException {
        biblioteca.prestar(NADA, lucia, HOY);

        Prestamo prestamo = biblioteca.devolver(NADA, HOY.plusDays(14 + 5));

        assertEquals(5, prestamo.diasDeRetraso(HOY.plusDays(40)));
        assertFalse(prestamo.estaActivo());
        assertFalse(biblioteca.estaPrestado(NADA));
    }

    @Test
    void devolucionEnPlazoNoTieneRetraso() throws BibliotecaException {
        biblioteca.prestar(NADA, lucia, HOY);

        Prestamo prestamo = biblioteca.devolver(NADA, HOY.plusDays(14));

        assertEquals(0, prestamo.diasDeRetraso(HOY.plusDays(14)));
    }

    @Test
    void devolverUnLibroQueNoEstaPrestadoLanzaExcepcion() {
        assertThrows(NoEncontradoException.class, () -> biblioteca.devolver(NADA, HOY));
    }

    @Test
    void listadoDeVencidosSoloTieneLosQueHanPasadoLaFecha() throws BibliotecaException {
        biblioteca.prestar(NADA, lucia, HOY);
        biblioteca.prestar(PATRIA, daniel, HOY.plusDays(10));
        biblioteca.prestar(BODAS, daniel, HOY.plusDays(10));
        biblioteca.devolver(BODAS, HOY.plusDays(11));

        List<Prestamo> vencidos = biblioteca.getPrestamosVencidos(HOY.plusDays(20));

        assertEquals(1, vencidos.size());
        assertEquals(NADA, vencidos.get(0).getIsbn());
    }

    @Test
    void noSePuedeDarDeBajaUnLibroPrestadoNiASuSocio() throws BibliotecaException {
        biblioteca.prestar(NADA, lucia, HOY);

        assertThrows(BibliotecaException.class, () -> biblioteca.bajaLibro(NADA));
        assertThrows(BibliotecaException.class, () -> biblioteca.bajaSocio(lucia));
    }

    @Test
    void noSeAdmitenDosLibrosConElMismoIsbn() {
        Libro repetido = new Libro("978-84-376-0254-7", "Otro título", "Otra autora", 2000, Genero.ENSAYO);

        assertThrows(BibliotecaException.class, () -> biblioteca.altaLibro(repetido));
    }

    @Test
    void laBusquedaNoDistingueMayusculasYMiraAutorYTitulo() {
        assertEquals(1, biblioteca.buscarLibros("lorca").size());
        assertEquals(1, biblioteca.buscarLibros("CAMPOS").size());
        assertEquals(2, biblioteca.buscarPorGenero(Genero.NOVELA).size());
        assertEquals(3, biblioteca.buscarPorAnio(1900, 1950).size());
    }

    @Test
    void losNumerosDeSocioSonCorrelativos() throws BibliotecaException {
        int nuevo = biblioteca.altaSocio("Marta Iglesias Prieto", "marta.iglesias@example.com").getNumero();

        assertEquals(daniel + 1, nuevo);
    }

    @Test
    void elNumeroDeUnSocioDadoDeBajaNoSeReutiliza() throws BibliotecaException {
        biblioteca.prestar(NADA, daniel, HOY);
        biblioteca.devolver(NADA, HOY.plusDays(2));
        biblioteca.bajaSocio(daniel);

        int nuevo = biblioteca.altaSocio("Marta Iglesias Prieto", "marta.iglesias@example.com").getNumero();

        assertNotEquals(daniel, nuevo);
        assertTrue(biblioteca.getPrestamos().stream().noneMatch(p -> p.getNumeroSocio() == nuevo));
    }

    @Test
    void elIsbnConGuionesSirveParaComprobarYBuscar() throws BibliotecaException {
        biblioteca.prestar(NADA, lucia, HOY);

        assertTrue(biblioteca.estaPrestado("978-84-376-0254-7"));
        assertEquals(1, biblioteca.buscarLibros("978-84-376").size());
    }
}
