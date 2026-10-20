public class MantenimientoAC extends Orden {
    private static final double TARIFA_BASE_METROPOLITANA = 100.0;
    private static final double COSTO_POR_HORA = 110.0;
    private static final double CARGO_REFRIGERANTE = 80.0;
    private final int cantidadBTU;

    public MantenimientoAC(String id, String nombreCliente, String direccion,
            double horasEstimadasTrabajo, ZonaTrabajo zona, int cantidadBTU) {
        super(id, nombreCliente, direccion, horasEstimadasTrabajo, zona);
        if (zona != ZonaTrabajo.METROPOLITANA) {
            throw new IllegalArgumentException(
                    "El mantenimiento de aire acondicionado solo se permite en zona metropolitana.");
        }
        if (cantidadBTU < 9000 || cantidadBTU > 60000) {
            throw new IllegalArgumentException("La capacidad debe estar entre 9,000 y 60,000 BTU.");
        }
        this.cantidadBTU = cantidadBTU;
    }

    public int getCantidadBTU() {
        return cantidadBTU;
    }

    @Override
    public double calcularCosto() {
        return TARIFA_BASE_METROPOLITANA
                + COSTO_POR_HORA * getHorasEstimadasTrabajo()
                + CARGO_REFRIGERANTE;
    }

    @Override
    public int obtenerDiasAtencion() {
        return 2;
    }

    @Override
    public int obtenerMaxVisitas() {
        return 1;
    }

    @Override
    public boolean esCompatibleCon(Cuadrilla cuadrilla) {
        return cuadrilla != null && cuadrilla.isCertificadaRefrigeracion();
    }

    @Override
    public String obtenerCategoria() {
        return "Mantenimiento de aire acondicionado";
    }
}
