public class InstalacionCertificada extends Instalacion {
    private static final double COSTO_CERTIFICADO = 120.0;
    private static final double PORCENTAJE_GARANTIA = 0.05;
    private final double costoMateriales;

    public InstalacionCertificada(String id, String nombreCliente, String direccion,
            double horasEstimadasTrabajo, ZonaTrabajo zona, int puntosAInstalar,
            double costoMateriales) {
        super(id, nombreCliente, direccion, horasEstimadasTrabajo, zona, puntosAInstalar);
        this.costoMateriales = Validaciones.numeroPositivo(
                costoMateriales, "El costo de materiales");
    }

    public double getCostoMateriales() {
        return costoMateriales;
    }

    @Override
    public double calcularCosto() {
        return super.calcularCosto() + COSTO_CERTIFICADO
                + costoMateriales * PORCENTAJE_GARANTIA;
    }

    @Override
    public int obtenerDiasAtencion() {
        return super.obtenerDiasAtencion() + 2;
    }

    @Override
    public int obtenerMaxVisitas() {
        return 2;
    }

    @Override
    public String obtenerCategoria() {
        return "Instalación eléctrica certificada";
    }
}
