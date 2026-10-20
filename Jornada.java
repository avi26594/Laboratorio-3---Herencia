import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Jornada {
    private final String id;
    private final Cuadrilla cuadrilla;
    private final List<Orden> ordenes;
    private final Set<String> resultadosRegistrados;
    private EstadoJornada estado;

    public Jornada(String id, Cuadrilla cuadrilla) {
        this.id = Validaciones.textoRequerido(id, "El código de jornada");
        if (cuadrilla == null) {
            throw new IllegalArgumentException("La cuadrilla es obligatoria.");
        }
        this.cuadrilla = cuadrilla;
        this.ordenes = new ArrayList<>();
        this.resultadosRegistrados = new LinkedHashSet<>();
        this.estado = EstadoJornada.EN_PREPARACION;
    }

    public void asignarOrden(Orden orden) {
        if (estado != EstadoJornada.EN_PREPARACION) {
            throw new IllegalStateException("Solo se asignan órdenes en jornadas en preparación.");
        }
        if (orden == null) {
            throw new IllegalArgumentException("La orden es obligatoria.");
        }
        if (orden.getEstado() != EstadoOrden.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden asignar órdenes pendientes.");
        }
        if (orden.getJornadaAsignada() != null) {
            throw new IllegalStateException("La orden ya está asignada a otra jornada.");
        }
        if (!orden.esCompatibleCon(cuadrilla)) {
            throw new IllegalArgumentException(
                    "La orden no es compatible con la certificación de la cuadrilla.");
        }
        if (getHorasAsignadas() + orden.getHorasEstimadasTrabajo() > cuadrilla.getCapacidadHoras()) {
            throw new IllegalArgumentException("La asignación excede la capacidad disponible de la cuadrilla.");
        }
        orden.asignarA(this);
        ordenes.add(orden);
    }

    public void retirarOrden(String idOrden) {
        if (estado != EstadoJornada.EN_PREPARACION) {
            throw new IllegalStateException("Solo se retiran órdenes de jornadas en preparación.");
        }
        Orden orden = buscarOrden(idOrden);
        if (orden.getEstado() != EstadoOrden.PENDIENTE
                || resultadosRegistrados.contains(orden.getId())) {
            throw new IllegalStateException("La orden no se puede retirar en su estado actual.");
        }
        orden.retirarDe(this);
        ordenes.remove(orden);
    }

    public void iniciar() {
        if (estado != EstadoJornada.EN_PREPARACION) {
            throw new IllegalStateException("La jornada no está en preparación.");
        }
        if (ordenes.isEmpty()) {
            throw new IllegalStateException("No se puede iniciar una jornada vacía.");
        }
        for (Orden orden : ordenes) {
            if (orden.getEstado() != EstadoOrden.PENDIENTE
                    || orden.getJornadaAsignada() != this
                    || !orden.esCompatibleCon(cuadrilla)
                    || orden.getVisitasRealizadas() >= orden.obtenerMaxVisitas()) {
                throw new IllegalStateException("La orden " + orden.getId()
                        + " ya no cumple las condiciones para iniciar.");
            }
        }
        for (Orden orden : ordenes) {
            orden.iniciarVisita(this);
        }
        estado = EstadoJornada.EN_ATENCION;
    }

    public void registrarResultado(String idOrden, boolean completada) {
        if (estado != EstadoJornada.EN_ATENCION) {
            throw new IllegalStateException("Los resultados solo se registran durante la atención.");
        }
        Orden orden = buscarOrden(idOrden);
        if (resultadosRegistrados.contains(orden.getId())) {
            throw new IllegalStateException("El resultado de esta orden ya fue registrado.");
        }
        orden.registrarResultado(this, completada);
        resultadosRegistrados.add(orden.getId());
        if (resultadosRegistrados.size() == ordenes.size()) {
            estado = EstadoJornada.CERRADA;
        }
    }

    private Orden buscarOrden(String idOrden) {
        for (Orden orden : ordenes) {
            if (orden.getId().equalsIgnoreCase(idOrden)) {
                return orden;
            }
        }
        throw new IllegalArgumentException("La orden no pertenece a esta jornada.");
    }

    public String getId() {
        return id;
    }

    public Cuadrilla getCuadrilla() {
        return cuadrilla;
    }

    public List<Orden> getOrdenes() {
        return Collections.unmodifiableList(ordenes);
    }

    public EstadoJornada getEstado() {
        return estado;
    }

    public double getHorasAsignadas() {
        double horas = 0.0;
        for (Orden orden : ordenes) {
            horas += orden.getHorasEstimadasTrabajo();
        }
        return horas;
    }

    public double getHorasDisponibles() {
        return cuadrilla.getCapacidadHoras() - getHorasAsignadas();
    }

    public boolean tieneResultadoRegistrado(String idOrden) {
        return resultadosRegistrados.contains(idOrden);
    }

    @Override
    public String toString() {
        return String.format("%s | cuadrilla %s | %s | horas asignadas: %.2f | disponibles: %.2f",
                id, cuadrilla.getId(), estado, getHorasAsignadas(), getHorasDisponibles());
    }
}
