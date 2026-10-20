import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class TecniHogar {
    public static final double CARGO_VISITA_ADICIONAL = 40.0;

    private final Map<String, Orden> ordenes;
    private final Map<String, Cuadrilla> cuadrillas;
    private final List<Jornada> jornadas;
    private final Map<String, Double> ingresosRegistroPorCategoria;
    private final Map<String, Double> ingresosVisitasPorCategoria;
    private double ingresosRegistro;
    private double ingresosVisitasAdicionales;
    private int siguienteJornada;

    public TecniHogar() {
        ordenes = new LinkedHashMap<>();
        cuadrillas = new LinkedHashMap<>();
        jornadas = new ArrayList<>();
        ingresosRegistroPorCategoria = new TreeMap<>();
        ingresosVisitasPorCategoria = new TreeMap<>();
        ingresosRegistro = 0.0;
        ingresosVisitasAdicionales = 0.0;
        siguienteJornada = 1;
    }

    public Orden cotizar(Orden orden) {
        if (orden == null) {
            throw new IllegalArgumentException("La orden es obligatoria.");
        }
        orden.quote();
        return orden;
    }

    public void registrarOrden(Orden orden) {
        if (orden == null) {
            throw new IllegalArgumentException("La orden es obligatoria.");
        }
        validarCodigoUnico(ordenes, orden.getId(), "orden");
        if (orden.getEstado() != EstadoOrden.PENDIENTE || orden.getVisitasRealizadas() != 0) {
            throw new IllegalArgumentException("Solo se pueden registrar órdenes nuevas.");
        }
        double costo = orden.quote();
        if (!Double.isFinite(ingresosRegistro + costo)) {
            throw new IllegalArgumentException("El total de ingresos excede el rango permitido.");
        }
        ordenes.put(normalizarCodigo(orden.getId()), orden);
        ingresosRegistro += costo;
        sumarIngreso(ingresosRegistroPorCategoria, orden.obtenerCategoria(), costo);
    }

    private void agregarOrdenInicial(Orden orden) {
        validarCodigoUnico(ordenes, orden.getId(), "orden");
        ordenes.put(normalizarCodigo(orden.getId()), orden);
    }

    public void registrarCuadrilla(Cuadrilla cuadrilla) {
        if (cuadrilla == null) {
            throw new IllegalArgumentException("La cuadrilla es obligatoria.");
        }
        validarCodigoUnico(cuadrillas, cuadrilla.getId(), "cuadrilla");
        cuadrillas.put(normalizarCodigo(cuadrilla.getId()), cuadrilla);
    }

    public Jornada crearJornada(String idCuadrilla) {
        Cuadrilla cuadrilla = buscarCuadrilla(idCuadrilla);
        for (Jornada jornada : jornadas) {
            if (jornada.getCuadrilla() == cuadrilla && jornada.getEstado() != EstadoJornada.CERRADA) {
                throw new IllegalStateException("La cuadrilla ya tiene una jornada sin cerrar.");
            }
        }
        String id = String.format("J%03d", siguienteJornada++);
        Jornada jornada = new Jornada(id, cuadrilla);
        jornadas.add(jornada);
        return jornada;
    }

    public void asignarOrden(String idJornada, String idOrden) {
        Jornada jornada = buscarJornada(idJornada);
        Orden orden = buscarOrden(idOrden);
        jornada.asignarOrden(orden);
    }

    public void retirarOrden(String idJornada, String idOrden) {
        buscarJornada(idJornada).retirarOrden(idOrden);
    }

    public void iniciarJornada(String idJornada) {
        Jornada jornada = buscarJornada(idJornada);
        jornada.iniciar();
        for (Orden orden : jornada.getOrdenes()) {
            if (orden.getVisitasRealizadas() > 1) {
                ingresosVisitasAdicionales += CARGO_VISITA_ADICIONAL;
                sumarIngreso(ingresosVisitasPorCategoria,
                        orden.obtenerCategoria(), CARGO_VISITA_ADICIONAL);
            }
        }
    }

    public void registrarResultado(String idJornada, String idOrden, boolean completada) {
        buscarJornada(idJornada).registrarResultado(idOrden, completada);
    }

    private static void validarCodigoUnico(Map<String, ?> mapa, String codigo, String tipo) {
        if (mapa.containsKey(normalizarCodigo(codigo))) {
            throw new IllegalArgumentException("Ya existe una " + tipo + " con código " + codigo + ".");
        }
    }

    private static String normalizarCodigo(String codigo) {
        return codigo.trim().toUpperCase(Locale.ROOT);
    }

    private static void sumarIngreso(Map<String, Double> mapa, String categoria, double monto) {
        mapa.put(categoria, mapa.getOrDefault(categoria, 0.0) + monto);
    }

    public Orden buscarOrden(String id) {
        Orden orden = ordenes.get(normalizarCodigo(Validaciones.textoRequerido(id, "El código de orden")));
        if (orden == null) {
            throw new IllegalArgumentException("No existe una orden con código " + id + ".");
        }
        return orden;
    }

    public Cuadrilla buscarCuadrilla(String id) {
        Cuadrilla cuadrilla = cuadrillas.get(
                normalizarCodigo(Validaciones.textoRequerido(id, "El código de cuadrilla")));
        if (cuadrilla == null) {
            throw new IllegalArgumentException("No existe una cuadrilla con código " + id + ".");
        }
        return cuadrilla;
    }

    public Jornada buscarJornada(String id) {
        for (Jornada jornada : jornadas) {
            if (jornada.getId().equalsIgnoreCase(id)) {
                return jornada;
            }
        }
        throw new IllegalArgumentException("No existe una jornada con código " + id + ".");
    }

    public Map<String, Map<EstadoOrden, Integer>> contarOrdenesPorCategoriaYEstado() {
        Map<String, Map<EstadoOrden, Integer>> reporte = new TreeMap<>();
        for (Orden orden : ordenes.values()) {
            Map<EstadoOrden, Integer> porEstado = reporte.computeIfAbsent(
                    orden.obtenerCategoria(), categoria -> {
                        Map<EstadoOrden, Integer> estados = new EnumMap<>(EstadoOrden.class);
                        for (EstadoOrden estado : EstadoOrden.values()) {
                            estados.put(estado, 0);
                        }
                        return estados;
                    });
            porEstado.put(orden.getEstado(), porEstado.get(orden.getEstado()) + 1);
        }
        return reporte;
    }

    public List<Jornada> obtenerJornadasAbiertas() {
        List<Jornada> abiertas = new ArrayList<>();
        for (Jornada jornada : jornadas) {
            if (jornada.getEstado() != EstadoJornada.CERRADA) {
                abiertas.add(jornada);
            }
        }
        return Collections.unmodifiableList(abiertas);
    }

    public List<Orden> getOrdenes() {
        return Collections.unmodifiableList(new ArrayList<>(ordenes.values()));
    }

    public List<Cuadrilla> getCuadrillas() {
        return Collections.unmodifiableList(new ArrayList<>(cuadrillas.values()));
    }

    public List<Jornada> getJornadas() {
        return Collections.unmodifiableList(jornadas);
    }

    public double getIngresosRegistro() {
        return ingresosRegistro;
    }

    public double getIngresosVisitasAdicionales() {
        return ingresosVisitasAdicionales;
    }

    public double getIngresosTotales() {
        return ingresosRegistro + ingresosVisitasAdicionales;
    }

    public Map<String, Double> getIngresosRegistroPorCategoria() {
        return Collections.unmodifiableMap(ingresosRegistroPorCategoria);
    }

    public Map<String, Double> getIngresosVisitasPorCategoria() {
        return Collections.unmodifiableMap(ingresosVisitasPorCategoria);
    }

    public void cargarDatosIniciales() {
        if (!ordenes.isEmpty() || !cuadrillas.isEmpty() || !jornadas.isEmpty()) {
            throw new IllegalStateException("Los datos iniciales solo se cargan en un sistema vacío.");
        }
        registrarCuadrilla(new Cuadrilla("C01", 8.0, true));
        registrarCuadrilla(new Cuadrilla("C02", 6.0, false));
        registrarCuadrilla(new Cuadrilla("C03", 4.0, true));

        agregarOrdenInicial(new Diagnostico("D01", "Ana López", "Zona 10", 1.0,
                ZonaTrabajo.METROPOLITANA));
        agregarOrdenInicial(new Diagnostico("D02", "Luis Pérez", "Mixco", 1.5,
                ZonaTrabajo.PERIFERICA));
        agregarOrdenInicial(new Instalacion("I01", "María García", "Zona 1", 2.5,
                ZonaTrabajo.METROPOLITANA, 5));
        agregarOrdenInicial(new Instalacion("I02", "Pedro Díaz", "Villa Nueva", 2.0,
                ZonaTrabajo.PERIFERICA, 2));
        agregarOrdenInicial(new InstalacionCertificada("E01", "Carmen Ruiz", "Zona 4", 2.5,
                ZonaTrabajo.METROPOLITANA, 5, 2000.0));
        agregarOrdenInicial(new InstalacionCertificada("E02", "José Morales", "Zona 15", 3.0,
                ZonaTrabajo.METROPOLITANA, 3, 1000.0));
        agregarOrdenInicial(new MantenimientoAC("A01", "Sofía Gómez", "Zona 9", 1.5,
                ZonaTrabajo.METROPOLITANA, 18000));
        agregarOrdenInicial(new MantenimientoAC("A02", "Carlos Méndez", "Zona 14", 2.0,
                ZonaTrabajo.METROPOLITANA, 24000));
    }
}
