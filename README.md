# Laboratorio 3 - Herencia

Aplicación de consola Java para cotizaciones, órdenes de servicio, cuadrillas, jornadas, visitas e ingresos de TecniHogar.

## Requisitos

- JDK 8 o posterior.
- No utiliza dependencias externas ni guarda datos en archivos.

## Compilar y ejecutar

Abra una terminal en esta carpeta y ejecute:

```powershell
javac -encoding UTF-8 *.java
java TecniHogarApp
```

Ejecuta `java TecniHogarApp` desde una terminal interactiva (por ejemplo, la terminal integrada de VS Code), no desde una acción de ejecución que no permita escribir en la consola. La aplicación utiliza un solo `Scanner` para leer `System.in`, una línea completa por pregunta; no mezcla `Scanner` y `BufferedReader`. Cada pregunta aparece en su propia línea y la respuesta se escribe después de `>`. Presiona Enter una vez por cada respuesta. Si se presiona Enter con un campo vacío, el programa muestra un mensaje y vuelve a pedir ese mismo dato. Si se termina la entrada (EOF) durante una operación, el programa finaliza sin sustituir los datos faltantes por valores predeterminados.

El menú se mantiene activo hasta elegir `0`. Los códigos iniciales de órdenes son `D01`, `D02`, `I01`, `I02`, `E01`, `E02`, `A01` y `A02`; las cuadrillas son `C01`, `C02` y `C03`.
Al aparecer `Seleccione una opción:`, ingrese el número de una opción (`1` a `10`) y presione Enter; use `0` para salir. Presionar Enter sin escribir una opción vuelve a mostrar el menú con un mensaje.

Las órdenes de ejemplo se cargan pendientes con ingresos en Q0.00, conforme a la instrucción de iniciar sin ingresos registrados. Por eso el reporte inicial muestra dos órdenes PENDIENTES por categoría y cero en los demás estados; la tabla cambia al asignar y completar/fallar visitas. Las nuevas órdenes generan el ingreso de registro solo si el cliente acepta la cotización. Los cargos de Q40.00 por visita adicional se registran al iniciar cada visita posterior a la primera.

## Ejecutar pruebas

```powershell
javac -encoding UTF-8 *.java
java PruebasTecniHogar
```

Las pruebas cubren cálculos y tiempos por categoría, validaciones, capacidad y certificación, transiciones de jornadas, límites de visitas e invariantes de ingresos. La ejecución finaliza con `PRUEBAS APROBADAS` si todos los casos pasan.

### Evidencia y salidas verificadas

| Caso | Datos principales | Resultado esperado | Resultado obtenido |
|---|---|---|---|
| Diagnóstico metropolitano | 1.0 h | Q150.00; 1 día hábil | Coincide |
| Diagnóstico periférico | 1.5 h | Q225.00; 2 días hábiles | Coincide |
| Instalación metropolitana | 2.5 h; 5 puntos | max(2.5, 3.75) = 3.75; se facturan 4 h; Q460.00; 3 días | Coincide |
| Instalación periférica | 2.0 h; 2 puntos | 2 h facturables; Q355.00; 5 días | Coincide |
| Instalación certificada | 2.5 h; 5 puntos; materiales Q2,000.00 | Q460.00 + Q120.00 + Q100.00 = Q680.00; 5 días | Coincide |
| Mantenimiento AC | 1.5 h; 18,000 BTU | Q100.00 + Q165.00 + Q80.00 = Q345.00; 2 días | Coincide |
| Visitas fallidas | Diagnóstico/instalación: 3; certificada: 2; AC: 1 | Se cancelan al alcanzar el límite; cargos adicionales acumulados: Q80.00, Q40.00 y Q0.00 | Coincide |
| Cotización no aceptada | Diagnóstico metropolitano | Ingresos permanecen en Q0.00 | Coincide |
| Orden registrada | Diagnóstico metropolitano | Ingreso por registro Q150.00 | Coincide |
| Retiro/rechazo | Retirar orden; asignar AC a cuadrilla no certificada; exceder capacidad | Orden vuelve a pendiente; ninguna operación rechazada cambia ingresos ni asignaciones previas | Coincide |
| Entrada malformada en el menú | Texto donde se espera una opción o número | Se informa el error y el menú continúa | Coincide |

La última ejecución automatizada reportó `PRUEBAS APROBADAS: 75`. En la prueba de consola también se confirmó que las órdenes de ejemplo aparecen en los cuatro grupos de categoría y que los ingresos iniciales son Q0.00.

## Reglas aplicadas

- Diagnóstico: Q150.00 metropolitano o Q225.00 periférico; máximo 1.5 horas.
- Instalación: tarifa base Q100.00 metropolitana o Q175.00 periférica, más Q90.00 por cada hora facturable o fracción. Las horas facturables son `ceil(max(horas estimadas, puntos * 0.75))`.
- Instalación certificada: costo de instalación más Q120.00 de certificado y 5% del valor declarado de materiales.
- Mantenimiento de aire acondicionado: se asumió tarifa base metropolitana de Q100.00, más Q110.00 por hora estimada y Q80.00 de refrigerante; únicamente se admite zona metropolitana y equipos de 9,000 a 60,000 BTU. La guía indica que usa una tarifa base según zona, pero no especifica su monto; se eligió Q100.00 como supuesto explícito.
- Capacidad de la cuadrilla: se calcula con horas estimadas, no horas facturables.
- Los reportes de ingresos separan registro y visitas adicionales y desglosan ambos por categoría.
