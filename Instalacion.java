public class Instalacion extends Orden {
    private final int puntosAInstalar;

    public Instalacion(String id, String nombreCliente, String direccion,
            double horasEstimadasTrabajo, ZonaTrabajo zona, int puntosAInstalar) {
        super(id, nombreCliente, direccion, horasEstimadasTrabajo, zona);
        this.puntosAInstalar = Validaciones.enteroPositivo(puntosAInstalar, "Los puntos a instalar");
    }

    public int getPuntosAInstalar() {
        return puntosAInstalar;
    }

    public double obtenerHorasFacturables() {
        return Math.ceil(Math.max(getHorasEstimadasTrabajo(), puntosAInstalar * 0.75));
    }

    @Override
    public double calcularCosto() {
        double tarifaBase = getZona() == ZonaTrabajo.METROPOLITANA ? 100.0 : 175.0;
        return tarifaBase + 90.0 * obtenerHorasFacturables();
    }

    @Override
    public int obtenerDiasAtencion() {
        return getZona() == ZonaTrabajo.METROPOLITANA ? 3 : 5;
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
        return "Instalación";
    }
}
