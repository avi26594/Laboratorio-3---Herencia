final class Validaciones {
    private Validaciones() {
    }

    static String textoRequerido(String valor, String nombre) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(nombre + " no puede estar vacío.");
        }
        return valor.trim();
    }

    static double numeroPositivo(double valor, String nombre) {
        if (!Double.isFinite(valor) || valor <= 0) {
            throw new IllegalArgumentException(nombre + " debe ser un número positivo.");
        }
        return valor;
    }

    static int enteroPositivo(int valor, String nombre) {
        if (valor <= 0) {
            throw new IllegalArgumentException(nombre + " debe ser un entero positivo.");
        }
        return valor;
    }
}
