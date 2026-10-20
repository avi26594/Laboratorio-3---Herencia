public class Cuadrilla {
    private final String id;
    private final double capacidadHoras;
    private final boolean certRefrigeracion;

    public Cuadrilla(String id, double capacidadHoras, boolean certRefrigeracion) {
        this.id = Validaciones.textoRequerido(id, "El código de cuadrilla");
        this.capacidadHoras = Validaciones.numeroPositivo(capacidadHoras, "La capacidad de horas");
        this.certRefrigeracion = certRefrigeracion;
    }

    public String getId() {
        return id;
    }

    public double getCapacidadHoras() {
        return capacidadHoras;
    }

    public boolean isCertificadaRefrigeracion() {
        return certRefrigeracion;
    }

    @Override
    public String toString() {
        return String.format("%s | capacidad: %.2f h | certificada en refrigeración: %s",
                id, capacidadHoras, certRefrigeracion ? "Sí" : "No");
    }
}
