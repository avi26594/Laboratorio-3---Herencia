import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

public class TecniHogarApp {
    private static final Scanner ENTRADA = new Scanner(System.in);
    private static final TecniHogar SISTEMA = new TecniHogar();

    private TecniHogarApp() {
    }

    public static void main(String[] args) {
        SISTEMA.cargarDatosIniciales();
        System.out.println("TecniHogar - Administración de servicios técnicos");
        System.out.println("Datos iniciales cargados. Los ingresos comienzan en Q0.00.");

        menu:
        while (true) {
            try {
                mostrarMenu();
                String opcion = leerLinea();
                if (opcion.isEmpty()) {
                    System.out.println("La opción no puede estar vacía. Ingrese un número del 0 al 10.");
                    System.out.println();
                    continue;
                }
                if ("0".equals(opcion)) {
                    System.out.println("Programa finalizado.");
                    break;
                }
                ejecutarOpcion(opcion);
            } catch (FinDeEntrada ex) {
                System.out.println("Fin de la entrada. Programa finalizado sin procesar datos incompletos.");
                break menu;
            } catch (IllegalArgumentException | IllegalStateException ex) {
                System.out.println("Operación rechazada: " + ex.getMessage());
            }
            System.out.println();
        }
    }

    private static void mostrarMenu() {
        System.out.println("1. Cotizar y registrar orden");
        System.out.println("2. Registrar cuadrilla");
        System.out.println("3. Crear jornada");
        System.out.println("4. Asignar orden a jornada");
        System.out.println("5. Retirar orden de jornada");
        System.out.println("6. Iniciar jornada");
        System.out.println("7. Registrar resultado de visita");
        System.out.println("8. Consultar órdenes y cuadrillas");
        System.out.println("9. Consultar reportes");
        System.out.println("10. Consultar jornadas abiertas");
        System.out.println("0. Salir");
        mostrarPrompt("Seleccione una opción: ");
    }

    private static void ejecutarOpcion(String opcion) {
        switch (opcion) {
            case "1":
                cotizarYRegistrarOrden();
                break;
            case "2":
                registrarCuadrilla();
                break;
            case "3":
                crearJornada();
                break;
            case "4":
                asignarOrden();
                break;
            case "5":
                retirarOrden();
                break;
            case "6":
                iniciarJornada();
                break;
            case "7":
                registrarResultado();
                break;
            case "8":
                consultarDatos();
                break;
            case "9":
                mostrarReportes();
                break;
            case "10":
                mostrarJornadasAbiertas();
                break;
            default:
                throw new IllegalArgumentException("Seleccione una opción del menú.");
        }
    }

    private static void cotizarYRegistrarOrden() {
        System.out.println("Categorías disponibles:");
        System.out.println("1. Diagnóstico");
        System.out.println("2. Instalación");
        System.out.println("3. Instalación eléctrica certificada");
        System.out.println("4. Mantenimiento de aire acondicionado");
        int categoria = leerEntero("Seleccione categoría: ", 1, 4);
        String id = leerTexto("Código de orden: ");
        String cliente = leerTexto("Nombre del cliente: ");
        String direccion = leerTexto("Dirección: ");
        double horas = leerDoublePositivo("Horas estimadas: ");
        ZonaTrabajo zona = leerZona();
        Orden orden;
        switch (categoria) {
            case 1:
                orden = new Diagnostico(id, cliente, direccion, horas, zona);
                break;
            case 2:
                orden = new Instalacion(id, cliente, direccion, horas, zona,
                        leerEnteroPositivo("Puntos a instalar: "));
                break;
            case 3:
                orden = new InstalacionCertificada(id, cliente, direccion, horas, zona,
                        leerEnteroPositivo("Puntos a instalar: "),
                        leerDoublePositivo("Valor declarado de materiales: "));
                break;
            case 4:
                orden = new MantenimientoAC(id, cliente, direccion, horas, zona,
                        leerEnteroEnRango("Capacidad del equipo en BTU: ", 9000, 60000));
                break;
            default:
                throw new IllegalArgumentException("Categoría no válida.");
        }
        SISTEMA.cotizar(orden);
        System.out.printf(Locale.ROOT, "Cotización: Q%.2f | atención estimada: %d día(s) hábil(es).%n",
                orden.quote(), orden.obtenerDiasAtencion());
        if (leerSiNo("¿El cliente acepta y desea registrar la orden? (s/n): ")) {
            SISTEMA.registrarOrden(orden);
            System.out.println("Orden registrada como PENDIENTE; ingreso registrado: "
                    + dinero(orden.quote()) + ".");
        } else {
            System.out.println("Cotización no registrada; los ingresos no cambiaron.");
        }
    }

    private static void registrarCuadrilla() {
        String id = leerTexto("Código de cuadrilla: ");
        double capacidad = leerDoublePositivo("Capacidad máxima de horas: ");
        boolean certificada = leerSiNo("¿Certificada en refrigeración? (s/n): ");
        SISTEMA.registrarCuadrilla(new Cuadrilla(id, capacidad, certificada));
        System.out.println("Cuadrilla registrada.");
    }

    private static void crearJornada() {
        mostrarCuadrillas();
        Jornada jornada = SISTEMA.crearJornada(leerTexto("Código de la cuadrilla: "));
        System.out.println("Jornada creada: " + jornada.getId() + " (EN_PREPARACION).");
    }

    private static void asignarOrden() {
        mostrarJornadas(SISTEMA.obtenerJornadasAbiertas());
        String idJornada = leerTexto("Código de jornada: ");
        mostrarOrdenes();
        String idOrden = leerTexto("Código de orden: ");
        SISTEMA.asignarOrden(idJornada, idOrden);
        Jornada jornada = SISTEMA.buscarJornada(idJornada);
        System.out.printf(Locale.ROOT, "Orden asignada. Horas: %.2f; capacidad disponible: %.2f.%n",
                jornada.getHorasAsignadas(), jornada.getHorasDisponibles());
    }

    private static void retirarOrden() {
        mostrarJornadas(SISTEMA.obtenerJornadasAbiertas());
        String idJornada = leerTexto("Código de jornada: ");
        String idOrden = leerTexto("Código de orden a retirar: ");
        SISTEMA.retirarOrden(idJornada, idOrden);
        System.out.println("Orden retirada y devuelta a PENDIENTE; los ingresos no cambiaron.");
    }

    private static void iniciarJornada() {
        mostrarJornadas(SISTEMA.obtenerJornadasAbiertas());
        String idJornada = leerTexto("Código de jornada: ");
        double antes = SISTEMA.getIngresosVisitasAdicionales();
        SISTEMA.iniciarJornada(idJornada);
        System.out.println("Jornada iniciada; todas sus órdenes están EN_ATENCION.");
        System.out.println("Cargos por visitas adicionales registrados: "
                + dinero(SISTEMA.getIngresosVisitasAdicionales() - antes) + ".");
    }

    private static void registrarResultado() {
        mostrarJornadas(SISTEMA.obtenerJornadasAbiertas());
        String idJornada = leerTexto("Código de jornada: ");
        Jornada jornada = SISTEMA.buscarJornada(idJornada);
        if (jornada.getEstado() != EstadoJornada.EN_ATENCION) {
            throw new IllegalStateException("La jornada debe estar EN_ATENCION.");
        }
        System.out.println("Órdenes pendientes de resultado:");
        for (Orden orden : jornada.getOrdenes()) {
            if (!jornada.tieneResultadoRegistrado(orden.getId())) {
                System.out.println(orden.getId() + " | " + orden.obtenerCategoria()
                        + " | " + orden.getNombreCliente());
            }
        }
        String idOrden = leerTexto("Código de orden: ");
        boolean completada = leerSiNo("¿Se completó el servicio? (s/n; n = visita fallida): ");
        SISTEMA.registrarResultado(idJornada, idOrden, completada);
        Orden orden = SISTEMA.buscarOrden(idOrden);
        System.out.println("Resultado registrado. Estado de la orden: " + orden.getEstado() + ".");
        System.out.println("Estado de la jornada: " + jornada.getEstado() + ".");
    }

    private static void consultarDatos() {
        mostrarOrdenes();
        mostrarCuadrillas();
    }

    private static void mostrarOrdenes() {
        System.out.println("Órdenes:");
        for (Orden orden : SISTEMA.getOrdenes()) {
            System.out.printf(Locale.ROOT,
                    "%s | %s | %s | %s | %.2f h | Q%.2f | %d día(s) | visitas %d/%d | %s%n",
                    orden.getId(), orden.obtenerCategoria(), orden.getNombreCliente(),
                    orden.getZona(), orden.getHorasEstimadasTrabajo(), orden.quote(),
                    orden.obtenerDiasAtencion(), orden.getVisitasRealizadas(),
                    orden.obtenerMaxVisitas(), orden.getEstado());
        }
    }

    private static void mostrarCuadrillas() {
        System.out.println("Cuadrillas:");
        for (Cuadrilla cuadrilla : SISTEMA.getCuadrillas()) {
            System.out.println(cuadrilla);
        }
    }

    private static void mostrarReportes() {
        Map<String, Map<EstadoOrden, Integer>> conteos =
                SISTEMA.contarOrdenesPorCategoriaYEstado();
        System.out.println("Órdenes por categoría y estado");
        System.out.println("(incluye las órdenes de ejemplo cargadas inicialmente como PENDIENTE):");
        System.out.printf("%-40s | %10s | %12s | %10s | %10s | %7s%n",
                "Categoría", "Pendientes", "En atención", "Completas", "Canceladas", "Total");
        System.out.println("--------------------------------------------------------------------------------------------------------");

        int totalPendientes = 0;
        int totalEnAtencion = 0;
        int totalCompletas = 0;
        int totalCanceladas = 0;
        for (Map.Entry<String, Map<EstadoOrden, Integer>> categoria : conteos.entrySet()) {
            Map<EstadoOrden, Integer> estados = categoria.getValue();
            int pendientes = estados.get(EstadoOrden.PENDIENTE);
            int enAtencion = estados.get(EstadoOrden.EN_ATENCION);
            int completas = estados.get(EstadoOrden.COMPLETA);
            int canceladas = estados.get(EstadoOrden.CANCELADA);
            totalPendientes += pendientes;
            totalEnAtencion += enAtencion;
            totalCompletas += completas;
            totalCanceladas += canceladas;
            System.out.printf(Locale.ROOT, "%-40s | %10d | %12d | %10d | %10d | %7d%n",
                    categoria.getKey(), pendientes, enAtencion, completas, canceladas,
                    pendientes + enAtencion + completas + canceladas);
        }
        System.out.println("--------------------------------------------------------------------------------------------------------");
        System.out.printf(Locale.ROOT, "%-40s | %10d | %12d | %10d | %10d | %7d%n",
                "TOTAL", totalPendientes, totalEnAtencion, totalCompletas, totalCanceladas,
                totalPendientes + totalEnAtencion + totalCompletas + totalCanceladas);

        System.out.println("Ingresos por registro: " + dinero(SISTEMA.getIngresosRegistro()));
        System.out.println("Ingresos por visitas adicionales: "
                + dinero(SISTEMA.getIngresosVisitasAdicionales()));
        System.out.println("Ingresos totales: " + dinero(SISTEMA.getIngresosTotales()));
        System.out.println("Ingresos por categoría (registro + visitas adicionales):");
        for (String categoria : conteos.keySet()) {
            double registros = SISTEMA.getIngresosRegistroPorCategoria()
                    .getOrDefault(categoria, 0.0);
            double visitas = SISTEMA.getIngresosVisitasPorCategoria().getOrDefault(categoria, 0.0);
            System.out.printf(Locale.ROOT, "%s: Q%.2f + Q%.2f = Q%.2f%n",
                    categoria, registros, visitas, registros + visitas);
        }
        mostrarJornadasAbiertas();
    }

    private static void mostrarJornadasAbiertas() {
        System.out.println("Jornadas no cerradas:");
        List<Jornada> jornadas = SISTEMA.obtenerJornadasAbiertas();
        if (jornadas.isEmpty()) {
            System.out.println("No hay jornadas abiertas.");
            return;
        }
        for (Jornada jornada : jornadas) {
            System.out.println(jornada);
        }
    }

    private static void mostrarJornadas(List<Jornada> jornadas) {
        System.out.println("Jornadas:");
        if (jornadas.isEmpty()) {
            System.out.println("No hay jornadas abiertas.");
            return;
        }
        for (Jornada jornada : jornadas) {
            System.out.println(jornada);
        }
    }

    private static ZonaTrabajo leerZona() {
        System.out.println("Zona: 1) Metropolitana  2) Periférica");
        return leerEntero("Seleccione zona: ", 1, 2) == 1
                ? ZonaTrabajo.METROPOLITANA : ZonaTrabajo.PERIFERICA;
    }

    private static int leerEntero(String mensaje, int minimo, int maximo) {
        while (true) {
            mostrarPrompt(mensaje);
            String valor = leerLinea();
            if (valor.isEmpty()) {
                System.out.println("La entrada no puede estar vacía. Ingrese un entero entre "
                        + minimo + " y " + maximo + ".");
                continue;
            }
            try {
                int numero = Integer.parseInt(valor);
                if (numero < minimo || numero > maximo) {
                    System.out.println("Ingrese un entero entre " + minimo + " y " + maximo + ".");
                } else {
                    return numero;
                }
            } catch (NumberFormatException ex) {
                System.out.println("Entrada inválida: se requiere un número entero.");
            }
        }
    }

    private static int leerEnteroPositivo(String mensaje) {
        while (true) {
            mostrarPrompt(mensaje);
            String valor = leerLinea();
            if (valor.isEmpty()) {
                System.out.println("La entrada no puede estar vacía. Ingrese un entero positivo.");
                continue;
            }
            try {
                int numero = Integer.parseInt(valor);
                if (numero > 0) {
                    return numero;
                }
                System.out.println("El valor debe ser un entero positivo.");
            } catch (NumberFormatException ex) {
                System.out.println("Entrada inválida: se requiere un número entero.");
            }
        }
    }

    private static int leerEnteroEnRango(String mensaje, int minimo, int maximo) {
        while (true) {
            mostrarPrompt(mensaje);
            String valor = leerLinea();
            if (valor.isEmpty()) {
                System.out.println("La entrada no puede estar vacía. Ingrese un entero entre "
                        + minimo + " y " + maximo + ".");
                continue;
            }
            try {
                int numero = Integer.parseInt(valor);
                if (numero >= minimo && numero <= maximo) {
                    return numero;
                }
                System.out.println("El valor debe estar entre " + minimo + " y " + maximo + ".");
            } catch (NumberFormatException ex) {
                System.out.println("Entrada inválida: se requiere un número entero.");
            }
        }
    }

    private static double leerDoublePositivo(String mensaje) {
        while (true) {
            mostrarPrompt(mensaje);
            String valor = leerLinea();
            if (valor.isEmpty()) {
                System.out.println("La entrada no puede estar vacía. Ingrese un número positivo.");
                continue;
            }
            try {
                double numero = Double.parseDouble(valor.replace(',', '.'));
                if (Double.isFinite(numero) && numero > 0) {
                    return numero;
                }
                System.out.println("El valor debe ser un número positivo.");
            } catch (NumberFormatException ex) {
                System.out.println("Entrada inválida: se requiere un número.");
            }
        }
    }

    private static boolean leerSiNo(String mensaje) {
        while (true) {
            mostrarPrompt(mensaje);
            String valor = leerLinea();
            if (valor.isEmpty()) {
                System.out.println("La respuesta no puede estar vacía. Responda s o n.");
                continue;
            }
            if ("s".equalsIgnoreCase(valor) || "si".equalsIgnoreCase(valor)
                    || "sí".equalsIgnoreCase(valor)) {
                return true;
            }
            if ("n".equalsIgnoreCase(valor) || "no".equalsIgnoreCase(valor)) {
                return false;
            }
            System.out.println("Responda s o n.");
        }
    }

    private static String leerTexto(String mensaje) {
        while (true) {
            mostrarPrompt(mensaje);
            String valor = leerLinea();
            if (!valor.isEmpty()) {
                return valor;
            }
            System.out.println("La entrada no puede estar vacía. Intente de nuevo.");
        }
    }

    private static void mostrarPrompt(String mensaje) {
        System.out.println(mensaje);
        System.out.print("> ");
        System.out.flush();
    }

    private static String leerLinea() {
        if (!ENTRADA.hasNextLine()) {
            throw new FinDeEntrada();
        }
        return ENTRADA.nextLine().trim();
    }

    private static String dinero(double monto) {
        return String.format(Locale.ROOT, "Q%.2f", monto);
    }

    private static final class FinDeEntrada extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
}
