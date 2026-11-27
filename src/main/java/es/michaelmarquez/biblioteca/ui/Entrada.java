package es.michaelmarquez.biblioteca.ui;

import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Lectura de datos por teclado. Siempre se lee la línea entera con nextLine()
 * y luego se convierte, así no quedan saltos de línea colgando en el Scanner
 * como pasa al mezclar nextInt() y nextLine().
 */
public class Entrada {

    private final Scanner scanner;

    public Entrada(Scanner scanner) {
        this.scanner = scanner;
    }

    public String leerTexto(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String texto = leerLinea().trim();
            if (!texto.isEmpty()) {
                return texto;
            }
            System.out.println("  No puede quedar vacío.");
        }
    }

    public int leerEntero(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String texto = leerLinea().trim();
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                System.out.println("  \"" + texto + "\" no es un número entero.");
            }
        }
    }

    public int leerEntero(String mensaje, int minimo, int maximo) {
        while (true) {
            int numero = leerEntero(mensaje);
            if (numero >= minimo && numero <= maximo) {
                return numero;
            }
            System.out.println("  Tiene que estar entre " + minimo + " y " + maximo + ".");
        }
    }

    public boolean confirmar(String mensaje) {
        while (true) {
            String respuesta = leerTexto(mensaje + " (s/n): ").toLowerCase();
            if (respuesta.equals("s")) {
                return true;
            }
            if (respuesta.equals("n")) {
                return false;
            }
            System.out.println("  Responde s o n.");
        }
    }

    // Si se cierra la entrada (Ctrl+Z en Windows o fin de un fichero redirigido)
    // lanzamos la excepción para que App guarde y termine en vez de quedarse en bucle.
    private String leerLinea() {
        if (!scanner.hasNextLine()) {
            throw new NoSuchElementException("No hay más entrada");
        }
        return scanner.nextLine();
    }
}
