package es.michaelmarquez.biblioteca.modelo;

public enum Genero {
    NOVELA("Novela"),
    POESIA("Poesía"),
    TEATRO("Teatro"),
    ENSAYO("Ensayo"),
    CIENCIA_FICCION("Ciencia ficción"),
    HISTORIA("Historia"),
    DIVULGACION("Divulgación"),
    INFANTIL("Infantil");

    private final String nombre;

    Genero(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
