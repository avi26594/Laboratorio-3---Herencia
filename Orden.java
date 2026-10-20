public abstract class Orden implements Quotable {
    private final String id;
    private final String nombreCliente;
    private final String direccion;
    private final double horasEstimadasTrabajo;
    private final ZonaTrabajo zona;
    private EstadoOrden estado;
    private int visitasRealizadas;
    private Jornada jornadaAsignada;

    protected Orden(String id, String nombreCliente, String direccion,
            double horasEstimadasTrabajo, ZonaTrabajo zona) {
        this.id = Validaciones.textoRequerido(id, "El código de orden");
        this.nombreCliente = Validaciones.textoRequerido(nombreCliente, "El nombre del cliente");
        this.direccion = Validaciones.textoRequerido(direccion, "La dirección");
        this.horasEstimadasTrabajo = Validaciones.numeroPositivo(
                horasEstimadasTrabajo, "Las horas estimadas");
        if (zona == null) {
            throw new IllegalArgumentException("La zona es obligatoria.");
        }
        this.zona = zona;
        this.estado = EstadoOrden.PENDIENTE;
        this.visitasRealizadas = 0;
    }

    public abstract double calcularCosto();

    public abstract int obtenerDiasAtencion();

    public abstract int obtenerMaxVisitas();

    public abstract boolean esCompatibleCon(Cuadrilla cuadrilla);

    public abstract String obtenerCategoria();

    @Override
    public final double quote() {
        double costo = calcularCosto();
        if (!Double.isFinite(costo) || costo < 0) {
            throw new IllegalArgumentException("El costo calculado está fuera del rango permitido.");
        }
        return costo;
    }

    final void asignarA(Jornada jornada) {
        if (estado != EstadoOrden.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden asignar órdenes pendientes.");
        }
        if (jornadaAsignada != null) {
            throw new IllegalStateException("La orden ya está asignada a una jornada.");
        }
        jornadaAsignada = jornada;
    }

    final void retirarDe(Jornada jornada) {
        if (jornadaAsignada != jornada) {
            throw new IllegalStateException("La orden no pertenece a esta jornada.");
        }
        jornadaAsignada = null;
    }

    final void iniciarVisita(Jornada jornada) {
        if (jornadaAsignada != jornada || estado != EstadoOrden.PENDIENTE) {
            throw new IllegalStateException("La orden no está disponible para iniciar esta visita.");
        }
        if (visitasRealizadas >= obtenerMaxVisitas()) {
            throw new IllegalStateException("La orden agotó sus visitas permitidas.");
        }
        visitasRealizadas++;
        estado = EstadoOrden.EN_ATENCION;
    }

    final void registrarResultado(Jornada jornada, boolean completada) {
        if (jornadaAsignada != jornada || estado != EstadoOrden.EN_ATENCION) {
            throw new IllegalStateException("La orden no está en atención en esta jornada.");
        }
        if (completada) {
            estado = EstadoOrden.COMPLETA;
        } else if (visitasRealizadas >= obtenerMaxVisitas()) {
            estado = EstadoOrden.CANCELADA;
        } else {
            estado = EstadoOrden.PENDIENTE;
        }
        jornadaAsignada = null;
    }

    public final String getId() {
        return id;
    }

    public final String getNombreCliente() {
        return nombreCliente;
    }

    public final String getDireccion() {
        return direccion;
    }

    public final double getHorasEstimadasTrabajo() {
        return horasEstimadasTrabajo;
    }

    public final ZonaTrabajo getZona() {
        return zona;
    }

    public final EstadoOrden getEstado() {
        return estado;
    }

    public final int getVisitasRealizadas() {
        return visitasRealizadas;
    }

    public final Jornada getJornadaAsignada() {
        return jornadaAsignada;
    }
}
