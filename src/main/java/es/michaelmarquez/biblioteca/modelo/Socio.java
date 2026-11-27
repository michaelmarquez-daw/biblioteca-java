package es.michaelmarquez.biblioteca.modelo;

public class Socio {

    private final int numero;
    private final String nombre;
    private final String email;

    public Socio(int numero, String nombre, String email) {
        if (numero <= 0) {
            throw new IllegalArgumentException("El número de socio debe ser positivo");
        }
        String emailLimpio = Validacion.textoObligatorio(email, "email");
        if (!emailLimpio.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new IllegalArgumentException("El email " + email + " no tiene un formato válido");
        }
        this.numero = numero;
        this.nombre = Validacion.textoObligatorio(nombre, "nombre");
        this.email = emailLimpio.toLowerCase();
    }

    public int getNumero() {
        return numero;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return nombre + " (socio " + numero + ")";
    }
}
