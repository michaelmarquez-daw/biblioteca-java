package es.michaelmarquez.biblioteca;

import es.michaelmarquez.biblioteca.persistencia.AlmacenCsv;
import es.michaelmarquez.biblioteca.servicio.Biblioteca;
import es.michaelmarquez.biblioteca.ui.Entrada;
import es.michaelmarquez.biblioteca.ui.MenuConsola;

import java.io.Console;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class App {

    public static void main(String[] args) {
        // Por defecto usa la carpeta datos/ del directorio desde el que se lanza
        Path carpetaDatos = Path.of(args.length > 0 ? args[0] : "datos");
        AlmacenCsv almacen = new AlmacenCsv(carpetaDatos);

        Biblioteca biblioteca;
        try {
            biblioteca = almacen.cargar();
        } catch (IOException e) {
            // No seguimos: si arrancara vacía, al salir sobrescribiría los ficheros buenos
            System.err.println("No se han podido cargar los datos (" + e.getMessage() + ").");
            System.err.println("Revisa los ficheros de " + carpetaDatos.toAbsolutePath());
            System.exit(1);
            return;
        }

        Scanner scanner = new Scanner(System.in, charsetDeEntrada());
        MenuConsola menu = new MenuConsola(biblioteca, new Entrada(scanner));
        try {
            menu.iniciar();
        } catch (NoSuchElementException e) {
            System.out.println();
            System.out.println("Se ha cerrado la entrada.");
        }

        try {
            almacen.guardar(biblioteca);
            System.out.println("Datos guardados en " + carpetaDatos.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error al guardar los datos: " + e.getMessage());
        }
    }

    // En la consola de Windows (cmd) el teclado no va en UTF-8, y sin esto
    // las tildes y la ñ se leían mal.
    private static Charset charsetDeEntrada() {
        Console consola = System.console();
        return consola != null ? consola.charset() : Charset.defaultCharset();
    }
}
