package es.michaelmarquez.biblioteca.modelo;

final class Validacion {

    private Validacion() {
    }

    static String textoObligatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo " + campo + " es obligatorio");
        }
        // El ";" es el separador de los ficheros CSV, si se cuela rompe la línea al guardar
        if (valor.contains(";")) {
            throw new IllegalArgumentException("El campo " + campo + " no puede contener ';'");
        }
        return valor.trim();
    }
}
