package es.michaelmarquez.biblioteca.ui;

import es.michaelmarquez.biblioteca.modelo.Genero;
import es.michaelmarquez.biblioteca.modelo.Libro;
import es.michaelmarquez.biblioteca.modelo.Prestamo;
import es.michaelmarquez.biblioteca.modelo.Socio;
import es.michaelmarquez.biblioteca.servicio.Biblioteca;
import es.michaelmarquez.biblioteca.servicio.BibliotecaException;

import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MenuConsola {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Biblioteca biblioteca;
    private final Entrada entrada;

    public MenuConsola(Biblioteca biblioteca, Entrada entrada) {
        this.biblioteca = biblioteca;
        this.entrada = entrada;
    }

    public void iniciar() {
        System.out.println("GESTOR DE BIBLIOTECA");
        int opcion;
        do {
            System.out.println();
            System.out.println("1. Libros");
            System.out.println("2. Socios");
            System.out.println("3. Préstamos");
            System.out.println("0. Guardar y salir");
            opcion = entrada.leerEntero("Opción: ", 0, 3);
            switch (opcion) {
                case 1 -> menuLibros();
                case 2 -> menuSocios();
                case 3 -> menuPrestamos();
                default -> { }
            }
        } while (opcion != 0);
    }

    private void menuLibros() {
        int opcion;
        do {
            System.out.println();
            System.out.println("LIBROS");
            System.out.println("1. Listar todos");
            System.out.println("2. Buscar por título, autor o ISBN");
            System.out.println("3. Buscar por género");
            System.out.println("4. Buscar por año de publicación");
            System.out.println("5. Alta de libro");
            System.out.println("6. Baja de libro");
            System.out.println("0. Volver");
            opcion = entrada.leerEntero("Opción: ", 0, 6);
            try {
                switch (opcion) {
                    case 1 -> mostrarLibros(biblioteca.getLibros());
                    case 2 -> mostrarLibros(biblioteca.buscarLibros(entrada.leerTexto("Texto a buscar: ")));
                    case 3 -> mostrarLibros(biblioteca.buscarPorGenero(elegirGenero()));
                    case 4 -> buscarPorAnio();
                    case 5 -> altaLibro();
                    case 6 -> bajaLibro();
                    default -> { }
                }
            } catch (BibliotecaException | IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } while (opcion != 0);
    }

    private void menuSocios() {
        int opcion;
        do {
            System.out.println();
            System.out.println("SOCIOS");
            System.out.println("1. Listar todos");
            System.out.println("2. Buscar por nombre");
            System.out.println("3. Alta de socio");
            System.out.println("4. Baja de socio");
            System.out.println("0. Volver");
            opcion = entrada.leerEntero("Opción: ", 0, 4);
            try {
                switch (opcion) {
                    case 1 -> mostrarSocios(biblioteca.getSocios());
                    case 2 -> mostrarSocios(biblioteca.buscarSocios(entrada.leerTexto("Nombre a buscar: ")));
                    case 3 -> altaSocio();
                    case 4 -> bajaSocio();
                    default -> { }
                }
            } catch (BibliotecaException | IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } while (opcion != 0);
    }

    private void menuPrestamos() {
        int opcion;
        do {
            System.out.println();
            System.out.println("PRÉSTAMOS");
            System.out.println("1. Prestar libro");
            System.out.println("2. Devolver libro");
            System.out.println("3. Préstamos activos");
            System.out.println("4. Préstamos vencidos");
            System.out.println("0. Volver");
            opcion = entrada.leerEntero("Opción: ", 0, 4);
            LocalDate hoy = LocalDate.now();
            try {
                switch (opcion) {
                    case 1 -> prestar(hoy);
                    case 2 -> devolver(hoy);
                    case 3 -> mostrarPrestamos(biblioteca.getPrestamosActivos(), hoy);
                    case 4 -> mostrarPrestamos(biblioteca.getPrestamosVencidos(hoy), hoy);
                    default -> { }
                }
            } catch (BibliotecaException | IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } while (opcion != 0);
    }

    private void buscarPorAnio() {
        int anioActual = Year.now().getValue();
        int desde = entrada.leerEntero("Desde el año: ", Libro.ANIO_MINIMO, anioActual);
        int hasta = entrada.leerEntero("Hasta el año: ", desde, anioActual);
        mostrarLibros(biblioteca.buscarPorAnio(desde, hasta));
    }

    private void altaLibro() throws BibliotecaException {
        String isbn = entrada.leerTexto("ISBN (13 dígitos): ");
        // El ISBN se comprueba antes de pedir el resto para no hacer escribir todo para nada
        if (!Libro.esIsbnValido(isbn)) {
            System.out.println("Error: el ISBN no es válido, revisa los dígitos.");
            return;
        }
        if (biblioteca.buscarPorIsbn(isbn).isPresent()) {
            System.out.println("Error: ya hay un libro con ese ISBN.");
            return;
        }
        String titulo = entrada.leerTexto("Título: ");
        String autor = entrada.leerTexto("Autor: ");
        int anio = entrada.leerEntero("Año: ", Libro.ANIO_MINIMO, Year.now().getValue());
        Genero genero = elegirGenero();
        Libro libro = new Libro(isbn, titulo, autor, anio, genero);
        biblioteca.altaLibro(libro);
        System.out.println("Libro añadido: " + libro);
    }

    private void bajaLibro() throws BibliotecaException {
        Libro libro = biblioteca.getLibro(entrada.leerTexto("ISBN del libro: "));
        // El servicio también lo comprueba, pero así no se pide confirmar algo que va a fallar
        if (biblioteca.estaPrestado(libro.getIsbn())) {
            System.out.println("Error: el libro está prestado, no se puede dar de baja.");
            return;
        }
        if (entrada.confirmar("¿Dar de baja \"" + libro.getTitulo() + "\"?")) {
            biblioteca.bajaLibro(libro.getIsbn());
            System.out.println("Libro dado de baja.");
        }
    }

    private void altaSocio() throws BibliotecaException {
        String nombre = entrada.leerTexto("Nombre y apellidos: ");
        String email = entrada.leerTexto("Email: ");
        Socio socio = biblioteca.altaSocio(nombre, email);
        System.out.println("Socio dado de alta con el número " + socio.getNumero());
    }

    private void bajaSocio() throws BibliotecaException {
        Socio socio = biblioteca.getSocio(entrada.leerEntero("Número de socio: "));
        if (!biblioteca.prestamosActivosDe(socio.getNumero()).isEmpty()) {
            System.out.println("Error: el socio tiene libros sin devolver, no se puede dar de baja.");
            return;
        }
        if (entrada.confirmar("¿Dar de baja a " + socio.getNombre() + "?")) {
            biblioteca.bajaSocio(socio.getNumero());
            System.out.println("Socio dado de baja.");
        }
    }

    private void prestar(LocalDate hoy) throws BibliotecaException {
        String isbn = entrada.leerTexto("ISBN del libro: ");
        int numeroSocio = entrada.leerEntero("Número de socio: ");
        Prestamo prestamo = biblioteca.prestar(isbn, numeroSocio, hoy);
        System.out.println("Préstamo registrado. Fecha límite de devolución: "
                + prestamo.getFechaLimite().format(FORMATO_FECHA));
    }

    private void devolver(LocalDate hoy) throws BibliotecaException {
        Prestamo prestamo = biblioteca.devolver(entrada.leerTexto("ISBN del libro: "), hoy);
        long retraso = prestamo.diasDeRetraso(hoy);
        if (retraso > 0) {
            System.out.println("Devuelto con " + retraso + " día(s) de retraso.");
        } else {
            System.out.println("Devuelto en plazo. Gracias.");
        }
    }

    private Genero elegirGenero() {
        Genero[] generos = Genero.values();
        for (int i = 0; i < generos.length; i++) {
            System.out.println("  " + (i + 1) + ". " + generos[i].getNombre());
        }
        int elegido = entrada.leerEntero("Género: ", 1, generos.length);
        return generos[elegido - 1];
    }

    private void mostrarLibros(List<Libro> libros) {
        if (libros.isEmpty()) {
            System.out.println("No hay libros que mostrar.");
            return;
        }
        System.out.printf("%-13s  %-26s  %-22s  %4s  %-15s  %s%n",
                "ISBN", "Título", "Autor", "Año", "Género", "Estado");
        for (Libro l : libros) {
            String estado = biblioteca.estaPrestado(l.getIsbn()) ? "Prestado" : "Disponible";
            System.out.printf("%-13s  %-26s  %-22s  %4d  %-15s  %s%n", l.getIsbn(),
                    recortar(l.getTitulo(), 26), recortar(l.getAutor(), 22),
                    l.getAnio(), l.getGenero().getNombre(), estado);
        }
        System.out.println(libros.size() + " libro(s).");
    }

    private void mostrarSocios(List<Socio> socios) {
        if (socios.isEmpty()) {
            System.out.println("No hay socios que mostrar.");
            return;
        }
        System.out.printf("%4s  %-26s  %-30s  %s%n", "Nº", "Nombre", "Email", "Préstamos");
        for (Socio s : socios) {
            int activos = biblioteca.prestamosActivosDe(s.getNumero()).size();
            System.out.printf("%4d  %-26s  %-30s  %d/%d%n", s.getNumero(), recortar(s.getNombre(), 26),
                    recortar(s.getEmail(), 30), activos, Biblioteca.MAX_PRESTAMOS_POR_SOCIO);
        }
    }

    private void mostrarPrestamos(List<Prestamo> prestamos, LocalDate hoy) {
        if (prestamos.isEmpty()) {
            System.out.println("No hay préstamos que mostrar.");
            return;
        }
        System.out.printf("%-26s  %-22s  %-10s  %s%n", "Libro", "Socio", "Límite", "Retraso");
        for (Prestamo p : prestamos) {
            String titulo = biblioteca.buscarPorIsbn(p.getIsbn())
                    .map(Libro::getTitulo)
                    .orElse(p.getIsbn() + " (de baja)");
            String socio;
            try {
                socio = biblioteca.getSocio(p.getNumeroSocio()).getNombre();
            } catch (BibliotecaException e) {
                socio = "Socio " + p.getNumeroSocio();
            }
            long retraso = p.diasDeRetraso(hoy);
            System.out.printf("%-26s  %-22s  %-10s  %s%n", recortar(titulo, 26), recortar(socio, 22),
                    p.getFechaLimite().format(FORMATO_FECHA), retraso > 0 ? retraso + " días" : "-");
        }
    }

    private String recortar(String texto, int maximo) {
        if (texto.length() <= maximo) {
            return texto;
        }
        return texto.substring(0, maximo - 3) + "...";
    }
}
