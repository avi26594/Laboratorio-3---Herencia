public class Diagnostico extends Orden {
    public Diagnostico(String id, String nombreCliente, String direccion,
            double horasEstimadasTrabajo, ZonaTrabajo zona) {
        super(id, nombreCliente, direccion, horasEstimadasTrabajo, zona);
        if (horasEstimadasTrabajo > 1.5) {
            throw new IllegalArgumentException("Un diagnóstico no puede superar 1.5 horas.");
        }
    }

    @Override
    public double calcularCosto() {
        return getZona() == ZonaTrabajo.METROPOLITANA ? 150.0 : 225.0;
    }

    @Override
    public int obtenerDiasAtencion() {
        return getZona() == ZonaTrabajo.METROPOLITANA ? 1 : 2;
    }

    @Override
    public int obtenerMaxVisitas() {
        return 3;
    }

    @Override
    public boolean esCompatibleCon(Cuadrilla cuadrilla) {
        return cuadrilla != null;
    }

    @Override
    public String obtenerCategoria() {
        return "Diagnóstico";
    }
}
