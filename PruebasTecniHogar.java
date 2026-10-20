import java.util.Map;

public class PruebasTecniHogar {
    private static int pruebasEjecutadas;

    private PruebasTecniHogar() {
    }

    public static void main(String[] args) {
        probarReglasDeCostoYTiempo();
        probarValidaciones();
        probarDatosInicialesYCotizacion();
        probarAsignacionesYJornadas();
        probarPoliticasDeVisitas();
        probarRetiroSinIngresos();
        System.out.println("PRUEBAS APROBADAS: " + pruebasEjecutadas);
    }

    private static void probarReglasDeCostoYTiempo() {
        Diagnostico diagnosticoMetro = new Diagnostico("T-D1", "A", "Zona 1", 1.0,
                ZonaTrabajo.METROPOLITANA);
        Diagnostico diagnosticoPeriferico = new Diagnostico("T-D2", "A", "Mixco", 1.5,
                ZonaTrabajo.PERIFERICA);
        Instalacion instalacionMetro = new Instalacion("T-I1", "A", "Zona 1", 2.5,
                ZonaTrabajo.METROPOLITANA, 5);
        Instalacion instalacionPeriferica = new Instalacion("T-I2", "A", "Mixco", 2.0,
                ZonaTrabajo.PERIFERICA, 2);
        InstalacionCertificada certificada = new InstalacionCertificada("T-E1", "A", "Zona 1",
                2.5, ZonaTrabajo.METROPOLITANA, 5, 2000.0);
        MantenimientoAC mantenimiento = new MantenimientoAC("T-A1", "A", "Zona 1",
                1.5, ZonaTrabajo.METROPOLITANA, 18000);

        verificarCasi(150.0, diagnosticoMetro.quote(), "Diagnóstico metropolitano");
        verificarCasi(225.0, diagnosticoPeriferico.quote(), "Diagnóstico periférico");
        verificarCasi(460.0, instalacionMetro.quote(), "Instalación y hora por fracción");
        verificarCasi(355.0, instalacionPeriferica.quote(), "Instalación periférica");
        verificarCasi(3.75, 5 * 0.75, "Horas de puntos");
        verificarCasi(680.0, certificada.quote(), "Certificado y garantía del 5%");
        verificarCasi(345.0, mantenimiento.quote(), "Mantenimiento AC con base Q100");
        verificar(1 == diagnosticoMetro.obtenerDiasAtencion()
                && 2 == diagnosticoPeriferico.obtenerDiasAtencion()
                && 3 == instalacionMetro.obtenerDiasAtencion()
                && 5 == instalacionPeriferica.obtenerDiasAtencion()
                && 5 == certificada.obtenerDiasAtencion()
                && 2 == mantenimiento.obtenerDiasAtencion(), "Tiempos de atención por categoría");
    }

    private static void probarValidaciones() {
        esperarRechazo(() -> new Diagnostico("X1", "A", "B", 1.51,
                ZonaTrabajo.METROPOLITANA), "Diagnóstico sobre 1.5 horas");
        esperarRechazo(() -> new Instalacion("X2", "A", "B", 2,
                ZonaTrabajo.METROPOLITANA, 0), "Puntos no positivos");
        esperarRechazo(() -> new MantenimientoAC("X3", "A", "B", 1,
                ZonaTrabajo.METROPOLITANA, 8999), "BTU fuera de rango");
        esperarRechazo(() -> new MantenimientoAC("X4", "A", "B", 1,
                ZonaTrabajo.PERIFERICA, 12000), "AC periférico");
        esperarRechazo(() -> new MantenimientoAC("X5", "A", "B", Double.MAX_VALUE,
                ZonaTrabajo.METROPOLITANA, 12000).quote(), "Costo no finito");
        esperarRechazo(() -> new InstalacionCertificada("X6", "A", "B", 1,
                ZonaTrabajo.METROPOLITANA, 1, 0), "Materiales no positivos");
        esperarRechazo(() -> new Instalacion("  ", "A", "B", 1,
                ZonaTrabajo.METROPOLITANA, 1), "Código vacío");
    }

    private static void probarDatosInicialesYCotizacion() {
        TecniHogar sistema = new TecniHogar();
        sistema.cargarDatosIniciales();
        verificar(sistema.getOrdenes().size() == 8, "Ocho órdenes iniciales");
        verificar(sistema.getCuadrillas().size() == 3, "Tres cuadrillas iniciales");
        for (Orden orden : sistema.getOrdenes()) {
            verificar(orden.getEstado() == EstadoOrden.PENDIENTE, "Órdenes iniciales pendientes");
        }
        verificarCasi(0, sistema.getIngresosTotales(), "Ingresos iniciales cero");
        Orden cotizacion = new Diagnostico("T-COT", "A", "Zona 1", 1,
                ZonaTrabajo.METROPOLITANA);
        sistema.cotizar(cotizacion);
        verificarCasi(0, sistema.getIngresosTotales(), "Cotizar no genera ingresos");
        sistema.registrarOrden(cotizacion);
        verificarCasi(150, sistema.getIngresosRegistro(), "Registrar cobra el monto cotizado");
        esperarRechazo(() -> sistema.registrarOrden(new Diagnostico("T-COT", "B", "Zona 2", 1,
                ZonaTrabajo.METROPOLITANA)), "Código repetido");
        verificarCasi(150, sistema.getIngresosRegistro(), "Rechazo no modifica ingresos");
    }

    private static void probarAsignacionesYJornadas() {
        TecniHogar sistema = new TecniHogar();
        Cuadrilla certificada = new Cuadrilla("Q1", 2.0, true);
        Cuadrilla noCertificada = new Cuadrilla("Q2", 8.0, false);
        sistema.registrarCuadrilla(certificada);
        sistema.registrarCuadrilla(noCertificada);
        Orden aire = new MantenimientoAC("A1", "A", "Zona 1", 1,
                ZonaTrabajo.METROPOLITANA, 12000);
        Orden instalacion = new Instalacion("I1", "A", "Zona 1", 1.5,
                ZonaTrabajo.METROPOLITANA, 1);
        sistema.registrarOrden(aire);
        sistema.registrarOrden(instalacion);
        double ingresoInicial = sistema.getIngresosTotales();
        Jornada jornada = sistema.crearJornada("Q2");
        esperarRechazo(() -> sistema.asignarOrden(jornada.getId(), aire.getId()),
                "AC sin certificación");
        verificar(jornada.getOrdenes().isEmpty(), "Asignación incompatible no modifica la jornada");
        Jornada jornadaCertificada = sistema.crearJornada("Q1");
        sistema.asignarOrden(jornadaCertificada.getId(), aire.getId());
        esperarRechazo(() -> sistema.asignarOrden(jornadaCertificada.getId(), instalacion.getId()),
                "Capacidad excedida");
        verificar(jornadaCertificada.getOrdenes().size() == 1, "Rechazo de capacidad es atómico");
        esperarRechazo(() -> sistema.crearJornada("Q1"), "Cuadrilla con jornada abierta");
        verificarCasi(ingresoInicial, sistema.getIngresosTotales(), "Asignaciones rechazadas sin ingreso");
        esperarRechazo(() -> sistema.iniciarJornada(jornada.getId()), "Jornada vacía");
        verificar(jornada.getEstado() == EstadoJornada.EN_PREPARACION,
                "Jornada vacía sigue en preparación");
        sistema.iniciarJornada(jornadaCertificada.getId());
        verificar(aire.getEstado() == EstadoOrden.EN_ATENCION, "Iniciar pasa orden a atención");
        sistema.registrarResultado(jornadaCertificada.getId(), aire.getId(), true);
        verificar(aire.getEstado() == EstadoOrden.COMPLETA, "Visita exitosa completa la orden");
        verificar(jornadaCertificada.getEstado() == EstadoJornada.CERRADA,
                "Jornada se cierra después del último resultado");
        verificar(sistema.crearJornada("Q1").getEstado() == EstadoJornada.EN_PREPARACION,
                "Cuadrilla libre después del cierre");
    }

    private static void probarPoliticasDeVisitas() {
        probarCancelacion(new Diagnostico("V-D", "A", "B", 1, ZonaTrabajo.METROPOLITANA), 3);
        probarCancelacion(new Instalacion("V-I", "A", "B", 1,
                ZonaTrabajo.METROPOLITANA, 1), 3);
        probarCancelacion(new InstalacionCertificada("V-E", "A", "B", 1,
                ZonaTrabajo.METROPOLITANA, 1, 100), 2);
        probarCancelacion(new MantenimientoAC("V-A", "A", "B", 1,
                ZonaTrabajo.METROPOLITANA, 12000), 1);
    }

    private static void probarCancelacion(Orden orden, int maxVisitas) {
        TecniHogar sistema = new TecniHogar();
        sistema.registrarCuadrilla(new Cuadrilla("Q1", 10, true));
        sistema.registrarOrden(orden);
        for (int visita = 1; visita <= maxVisitas; visita++) {
            Jornada jornada = sistema.crearJornada("Q1");
            sistema.asignarOrden(jornada.getId(), orden.getId());
            double ingresosAntesDeIniciar = sistema.getIngresosVisitasAdicionales();
            sistema.iniciarJornada(jornada.getId());
            verificar(orden.getVisitasRealizadas() == visita, "Contador visita " + visita);
            verificarCasi(visita > 1 ? 40 : 0,
                    sistema.getIngresosVisitasAdicionales() - ingresosAntesDeIniciar,
                    "Cargo de visita adicional " + visita);
            sistema.registrarResultado(jornada.getId(), orden.getId(), false);
        }
        verificar(orden.getEstado() == EstadoOrden.CANCELADA, "Agotar visitas cancela la orden");
        verificar(orden.getVisitasRealizadas() == maxVisitas, "Límite de visitas por categoría");
        verificarCasi(40.0 * (maxVisitas - 1), sistema.getIngresosVisitasAdicionales(),
                "Ingresos acumulados por visitas adicionales");
    }

    private static void probarRetiroSinIngresos() {
        TecniHogar sistema = new TecniHogar();
        sistema.registrarCuadrilla(new Cuadrilla("Q1", 8, false));
        Orden orden = new Instalacion("R1", "A", "Zona 1", 1,
                ZonaTrabajo.METROPOLITANA, 1);
        sistema.registrarOrden(orden);
        double ingresos = sistema.getIngresosTotales();
        Jornada jornada = sistema.crearJornada("Q1");
        sistema.asignarOrden(jornada.getId(), orden.getId());
        sistema.retirarOrden(jornada.getId(), orden.getId());
        verificar(orden.getEstado() == EstadoOrden.PENDIENTE
                && orden.getJornadaAsignada() == null, "Retirar libera la orden");
        verificarCasi(ingresos, sistema.getIngresosTotales(), "Retirar no modifica ingresos");
        Map<EstadoOrden, Integer> estados = sistema.contarOrdenesPorCategoriaYEstado()
                .get(orden.obtenerCategoria());
        verificar(estados.get(EstadoOrden.PENDIENTE) == 1, "Reporte de órdenes por estado");
    }

    private static void verificar(boolean condicion, String nombre) {
        pruebasEjecutadas++;
        if (!condicion) {
            throw new AssertionError("Falló: " + nombre);
        }
        System.out.println("OK " + pruebasEjecutadas + " - " + nombre);
    }

    private static void verificarCasi(double esperado, double obtenido, String nombre) {
        verificar(Math.abs(esperado - obtenido) < 0.000001,
                nombre + " (esperado " + esperado + ", obtenido " + obtenido + ")");
    }

    private static void esperarRechazo(Runnable operacion, String nombre) {
        pruebasEjecutadas++;
        try {
            operacion.run();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            System.out.println("OK " + pruebasEjecutadas + " - " + nombre);
            return;
        }
        throw new AssertionError("Debió rechazarse: " + nombre);
    }
}
