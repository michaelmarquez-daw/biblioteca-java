package es.michaelmarquez.biblioteca.servicio;

/** Errores de reglas de la biblioteca que el usuario puede corregir (no son fallos del programa). */
public class BibliotecaException extends Exception {

    public BibliotecaException(String mensaje) {
        super(mensaje);
    }
}
