package es.michaelmarquez.biblioteca.servicio;

import es.michaelmarquez.biblioteca.modelo.Genero;
import es.michaelmarquez.biblioteca.modelo.Libro;
import es.michaelmarquez.biblioteca.modelo.Prestamo;
import es.michaelmarquez.biblioteca.modelo.Socio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * Aquí están todas las reglas de la biblioteca. No lee ni escribe en consola
 * ni en ficheros, así se puede probar con JUnit sin nada más.
 * Los métodos que dependen del día reciben la fecha como parámetro por el mismo motivo.
 */
public class Biblioteca {

    public static final int MAX_PRESTAMOS_POR_SOCIO = 3;
    public static final int DIAS_DE_PRESTAMO = 14;

    private final Map<String, Libro> libros = new LinkedHashMap<>();
    private final Map<Integer, Socio> socios = new TreeMap<>();
    private final List<Prestamo> prestamos = new ArrayList<>();
    private int siguienteIdPrestamo = 1;
    private int siguienteNumeroSocio = 1;

    public Biblioteca() {
    }

    /**
     * Crea la biblioteca con datos ya existentes (los que carga AlmacenCsv).
     * Lanza IllegalArgumentException si los datos no cuadran entre sí.
     */
    public Biblioteca(List<Libro> libros, List<Socio> socios, List<Prestamo> prestamos) {
        for (Libro libro : libros) {
            if (this.libros.put(libro.getIsbn(), libro) != null) {
                throw new IllegalArgumentException("el ISBN " + libro.getIsbn() + " está repetido");
            }
        }
        for (Socio socio : socios) {
            if (this.socios.put(socio.getNumero(), socio) != null) {
                throw new IllegalArgumentException("el socio número " + socio.getNumero() + " está repetido");
            }
        }
        comprobarPrestamosActivos(prestamos);
        this.prestamos.addAll(prestamos);

        siguienteIdPrestamo = prestamos.stream()
                .mapToInt(Prestamo::getId)
                .max()
                .orElse(0) + 1;
        // También se miran los préstamos: un socio dado de baja sigue en el historial
        // y su número no se puede volver a dar a otra persona.
        siguienteNumeroSocio = Stream.concat(
                        socios.stream().map(Socio::getNumero),
                        prestamos.stream().map(Prestamo::getNumeroSocio))
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0) + 1;
    }

    // Los préstamos devueltos pueden ser de libros o socios ya dados de baja,
    // pero uno activo tiene que apuntar a un libro y un socio que existan.
    private void comprobarPrestamosActivos(List<Prestamo> prestamos) {
        Set<String> librosFuera = new HashSet<>();
        for (Prestamo p : prestamos) {
            if (!p.estaActivo()) {
                continue;
            }
            if (!libros.containsKey(p.getIsbn())) {
                throw new IllegalArgumentException("el préstamo " + p.getId() + " es de un libro que no existe");
            }
            if (!socios.containsKey(p.getNumeroSocio())) {
                throw new IllegalArgumentException("el préstamo " + p.getId() + " es de un socio que no existe");
            }
            if (!librosFuera.add(p.getIsbn())) {
                throw new IllegalArgumentException("el libro " + p.getIsbn() + " tiene dos préstamos activos");
            }
        }
    }

    public void altaLibro(Libro libro) throws BibliotecaException {
        if (libros.containsKey(libro.getIsbn())) {
            throw new BibliotecaException("Ya existe un libro con el ISBN " + libro.getIsbn());
        }
        libros.put(libro.getIsbn(), libro);
    }

    public Libro bajaLibro(String isbn) throws BibliotecaException {
        Libro libro = getLibro(isbn);
        if (estaPrestado(libro.getIsbn())) {
            throw new BibliotecaException("No se puede dar de baja: el libro está prestado");
        }
        return libros.remove(libro.getIsbn());
    }

    public Optional<Libro> buscarPorIsbn(String isbn) {
        return Optional.ofNullable(libros.get(Libro.normalizarIsbn(isbn)));
    }

    public Libro getLibro(String isbn) throws NoEncontradoException {
        return buscarPorIsbn(isbn)
                .orElseThrow(() -> new NoEncontradoException("No hay ningún libro con el ISBN " + isbn));
    }

    public List<Libro> getLibros() {
        return libros.values().stream()
                .sorted(Comparator.comparing(Libro::getTitulo))
                .toList();
    }

    /** Busca el texto en el título, el autor o el ISBN, sin distinguir mayúsculas. */
    public List<Libro> buscarLibros(String texto) {
        String buscado = texto.trim().toLowerCase();
        // Para el ISBN se quitan los guiones, así "978-84" también encuentra
        String isbnBuscado = Libro.normalizarIsbn(buscado);
        return getLibros().stream()
                .filter(l -> l.getTitulo().toLowerCase().contains(buscado)
                        || l.getAutor().toLowerCase().contains(buscado)
                        || (!isbnBuscado.isEmpty() && l.getIsbn().contains(isbnBuscado)))
                .toList();
    }

    public List<Libro> buscarPorGenero(Genero genero) {
        return getLibros().stream()
                .filter(l -> l.getGenero() == genero)
                .toList();
    }

    public List<Libro> buscarPorAnio(int desde, int hasta) {
        return libros.values().stream()
                .filter(l -> l.getAnio() >= desde && l.getAnio() <= hasta)
                .sorted(Comparator.comparingInt(Libro::getAnio))
                .toList();
    }

    public Socio altaSocio(String nombre, String email) throws BibliotecaException {
        boolean emailRepetido = socios.values().stream()
                .anyMatch(s -> s.getEmail().equalsIgnoreCase(email.trim()));
        if (emailRepetido) {
            throw new BibliotecaException("Ya hay un socio con el email " + email);
        }
        Socio socio = new Socio(siguienteNumeroSocio, nombre, email);
        socios.put(socio.getNumero(), socio);
        // Se incrementa después de crear el socio: si el nombre o el email no valen, no se gasta el número
        siguienteNumeroSocio++;
        return socio;
    }

    public Socio bajaSocio(int numero) throws BibliotecaException {
        Socio socio = getSocio(numero);
        if (!prestamosActivosDe(numero).isEmpty()) {
            throw new BibliotecaException("No se puede dar de baja: el socio tiene libros sin devolver");
        }
        return socios.remove(socio.getNumero());
    }

    public Socio getSocio(int numero) throws NoEncontradoException {
        Socio socio = socios.get(numero);
        if (socio == null) {
            throw new NoEncontradoException("No existe el socio número " + numero);
        }
        return socio;
    }

    public List<Socio> getSocios() {
        return List.copyOf(socios.values());
    }

    public List<Socio> buscarSocios(String texto) {
        String buscado = texto.trim().toLowerCase();
        return socios.values().stream()
                .filter(s -> s.getNombre().toLowerCase().contains(buscado))
                .toList();
    }

    public Prestamo prestar(String isbn, int numeroSocio, LocalDate hoy) throws BibliotecaException {
        Libro libro = getLibro(isbn);
        Socio socio = getSocio(numeroSocio);

        if (estaPrestado(libro.getIsbn())) {
            throw new PrestamoNoPermitidoException("\"" + libro.getTitulo() + "\" ya está prestado");
        }
        List<Prestamo> activos = prestamosActivosDe(socio.getNumero());
        if (activos.size() >= MAX_PRESTAMOS_POR_SOCIO) {
            throw new PrestamoNoPermitidoException(socio.getNombre() + " ya tiene "
                    + MAX_PRESTAMOS_POR_SOCIO + " libros prestados, que es el máximo");
        }
        boolean tieneVencidos = activos.stream().anyMatch(p -> p.estaVencido(hoy));
        if (tieneVencidos) {
            throw new PrestamoNoPermitidoException(socio.getNombre()
                    + " tiene préstamos vencidos y debe devolverlos antes de pedir otro");
        }

        Prestamo prestamo = new Prestamo(siguienteIdPrestamo++, libro.getIsbn(), socio.getNumero(),
                hoy, hoy.plusDays(DIAS_DE_PRESTAMO));
        prestamos.add(prestamo);
        return prestamo;
    }

    public Prestamo devolver(String isbn, LocalDate hoy) throws BibliotecaException {
        String isbnLimpio = Libro.normalizarIsbn(isbn);
        Prestamo prestamo = prestamos.stream()
                .filter(p -> p.estaActivo() && p.getIsbn().equals(isbnLimpio))
                .findFirst()
                .orElseThrow(() -> new NoEncontradoException("Ese libro no está prestado ahora mismo"));
        prestamo.registrarDevolucion(hoy);
        return prestamo;
    }

    public boolean estaPrestado(String isbn) {
        String limpio = Libro.normalizarIsbn(isbn);
        return prestamos.stream()
                .anyMatch(p -> p.estaActivo() && p.getIsbn().equals(limpio));
    }

    public List<Prestamo> prestamosActivosDe(int numeroSocio) {
        return prestamos.stream()
                .filter(p -> p.estaActivo() && p.getNumeroSocio() == numeroSocio)
                .toList();
    }

    public List<Prestamo> getPrestamosActivos() {
        return prestamos.stream()
                .filter(Prestamo::estaActivo)
                .sorted(Comparator.comparing(Prestamo::getFechaLimite))
                .toList();
    }

    public List<Prestamo> getPrestamosVencidos(LocalDate hoy) {
        return prestamos.stream()
                .filter(p -> p.estaVencido(hoy))
                .sorted(Comparator.comparing(Prestamo::getFechaLimite))
                .toList();
    }

    /** Todos los préstamos, también los devueltos (para guardarlos). */
    public List<Prestamo> getPrestamos() {
        return Collections.unmodifiableList(prestamos);
    }
}
