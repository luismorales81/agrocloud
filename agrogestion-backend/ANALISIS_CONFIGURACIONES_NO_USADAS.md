# Análisis de Configuraciones No Usadas

## Resumen

Este documento identifica qué configuraciones del script `INSERTAR_CONFIGURACIONES_GENERALES.sql` **NO se están usando** actualmente en el código, ni para control ni para generar mensajes.

## Estado de las Configuraciones

### ✅ Configuraciones QUE SE USAN (desde Parámetros Productivos):

1. **DIAS_CACHORRA** (Configuraciones Generales)
   - Se usa en: `MadreService.calcularEstadoSegunEdad()`
   - ✅ Correcto - Es la única que debe estar en Configuraciones Generales

2. **DIAS_ALERTA_PARTOS** (Parámetros Productivos)
   - Se usa en: `AlertasPorcinosService.calcularPartosProgramados()` y `DashboardPorcinosService.obtenerAlertas()`
   - ✅ Correcto - Está en Parámetros Productivos, NO debe estar en Configuraciones Generales

3. **DIAS_ALERTA_ECOGRAFIAS** (Parámetros Productivos)
   - Se usa en: `AlertasPorcinosService.calcularEcografiasProgramadas()`
   - ✅ Correcto - Está en Parámetros Productivos

4. **DIAS_ALERTA_DESTETES** (Parámetros Productivos)
   - Se usa en: `AlertasPorcinosService.calcularDestetesProgramados()`
   - ✅ Correcto - Está en Parámetros Productivos

5. **UMBRAL_MORTALIDAD_LACTANCIA** (Parámetros Productivos)
   - Se usa en: `DashboardPorcinosService.obtenerAlertas()` para alertar mortalidad alta
   - ✅ Correcto - Está en Parámetros Productivos

6. **LECHONES_VIVOS_PARTO_OBJETIVO** (Parámetros Productivos)
   - Se usa en: `ReportesPorcinoService` para comparar con objetivos en reportes
   - ✅ Correcto - Está en Parámetros Productivos

7. **PARTOS_MADRE_ANIO_OBJETIVO** (Parámetros Productivos)
   - Se usa en: `ReportesPorcinoService` para comparar con objetivos en reportes
   - ✅ Correcto - Está en Parámetros Productivos

### ❌ Configuraciones DUPLICADAS en Configuraciones Generales (NO se usan desde ahí):

Todas estas están duplicadas en **Parámetros Productivos** y el código usa los valores de **Parámetros Productivos**, NO de Configuraciones Generales:

1. **DIAS_ALERTA_PARTOS** → Duplicado de `ParametrosProductivos.diasAntelacionAlertarPartos`
2. **DIAS_ALERTA_ECOGRAFIAS** → Duplicado de `ParametrosProductivos.diasAntelacionAlertarEcografias`
3. **DIAS_ALERTA_DESTETES** → Duplicado de `ParametrosProductivos.diasAntelacionAlertarDestetes`
4. **DIAS_ALERTA_REVISIONES** → Duplicado de `ParametrosProductivos.diasAntelacionAlertarRevisionesSanitarias`
5. **UMBRAL_MORTALIDAD_LACTANCIA** → Duplicado de `ParametrosProductivos.umbralMortalidadLactanciaPorcentaje`
6. **UMBRAL_MORTALIDAD_RECRIA** → Duplicado de `ParametrosProductivos.umbralMortalidadRecriaPorcentaje`
7. **UMBRAL_MINIMO_PREÑEZ** → Duplicado de `ParametrosProductivos.porcentajeMinimoPrenezAntesAdvertencia`
8. **PESO_PROMEDIO_NACIMIENTO** → Duplicado de `ParametrosProductivos.pesoPromedioNacimiento`
9. **PESO_DESTETE_OBJETIVO** → Duplicado de `ParametrosProductivos.pesoDesteteObjetivo`
10. **PESO_VENTA_OBJETIVO** → Duplicado de `ParametrosProductivos.pesoVentaObjetivo`
11. **LECHONES_VIVOS_PARTO_OBJETIVO** → Duplicado de `ParametrosProductivos.lechonesVivosPartoObjetivo`
12. **LECHONES_DESTETADOS_OBJETIVO** → Duplicado de `ParametrosProductivos.lechonesDestetadosObjetivo`
13. **PARTOS_MADRE_ANIO_OBJETIVO** → Duplicado de `ParametrosProductivos.partosMadreAnioObjetivo`

### ⚠️ Configuraciones que NO se usan en NINGÚN lugar (ni en Parámetros Productivos ni en Configuraciones Generales):

1. **UMBRAL_MORTALIDAD_RECRIA** (ParametrosProductivos)
   - Existe en la entidad pero NO se usa en el código para generar alertas
   - ⚠️ **DEBE IMPLEMENTARSE** o eliminarse

2. **PORCENTAJE_MINIMO_PREÑEZ** (ParametrosProductivos)
   - Existe en la entidad como `porcentajeMinimoPrenezAntesAdvertencia`
   - ⚠️ **NO SE USA** - No hay validación ni alerta por porcentaje mínimo de preñez

3. **PESO_PROMEDIO_NACIMIENTO** (ParametrosProductivos)
   - Existe en la entidad pero NO se usa en validaciones ni reportes
   - ⚠️ **NO SE USA** - Podría usarse para validar pesos al nacer

4. **PESO_DESTETE_OBJETIVO** (ParametrosProductivos)
   - Existe en la entidad pero NO se usa en validaciones ni reportes
   - ⚠️ **NO SE USA** - Podría usarse para comparar pesos reales vs objetivo

5. **PESO_VENTA_OBJETIVO** (ParametrosProductivos)
   - Existe en la entidad pero NO se usa en validaciones ni reportes
   - ⚠️ **NO SE USA** - Podría usarse para comparar pesos reales vs objetivo

6. **LECHONES_DESTETADOS_OBJETIVO** (ParametrosProductivos)
   - Existe en la entidad pero NO se usa en reportes (solo se usa `lechonesVivosPartoObjetivo`)
   - ⚠️ **NO SE USA** - Podría usarse para comparar lechones destetados vs objetivo

7. **HABILITAR_NOTIFICACIONES** (Configuraciones Generales)
   - Existe en el script pero NO se usa en el código
   - ⚠️ **NO SE USA** - No hay lógica para habilitar/deshabilitar notificaciones

8. **FORMATO_FECHA** (Configuraciones Generales)
   - Existe en el script pero NO se usa en el código
   - ⚠️ **NO SE USA** - El formato de fecha está hardcodeado en el frontend

9. **IDIOMA_INTERFAZ** (Configuraciones Generales)
   - Existe en el script pero NO se usa en el código
   - ⚠️ **NO SE USA** - No hay sistema de internacionalización implementado

10. **DIAS_ALERTA_REVISIONES** (Parámetros Productivos)
    - Existe en la entidad como `diasAntelacionAlertarRevisionesSanitarias`
    - ⚠️ **NO SE USA** - No hay generación de alertas de revisiones sanitarias

## Recomendaciones

### 1. Eliminar Configuraciones Duplicadas del Script

El script `INSERTAR_CONFIGURACIONES_GENERALES.sql` debe **SOLO** insertar:
- `DIAS_CACHORRA` (porque es la única que se usa desde Configuraciones Generales)
- `HABILITAR_NOTIFICACIONES`, `FORMATO_FECHA`, `IDIOMA_INTERFAZ` (si se van a implementar en el futuro)

Todas las demás deben eliminarse del script porque están duplicadas en Parámetros Productivos.

### 2. Implementar o Eliminar Parámetros No Usados

Los siguientes parámetros de Parámetros Productivos deben **implementarse** o **eliminarse**:

#### A. Implementar Alertas:
- `diasAntelacionAlertarRevisionesSanitarias` - Generar alertas de revisiones sanitarias programadas
- `umbralMortalidadRecriaPorcentaje` - Alertar si la mortalidad en recría supera el umbral
- `porcentajeMinimoPrenezAntesAdvertencia` - Alertar si el % de preñez está por debajo del umbral

#### B. Implementar Validaciones/Comparaciones:
- `pesoPromedioNacimiento` - Validar pesos al nacer o comparar con el promedio
- `pesoDesteteObjetivo` - Comparar pesos reales vs objetivo en destetes
- `pesoVentaObjetivo` - Comparar pesos reales vs objetivo en ventas
- `lechonesDestetadosObjetivo` - Comparar lechones destetados vs objetivo en reportes

#### C. Implementar Funcionalidades del Sistema:
- `HABILITAR_NOTIFICACIONES` - Lógica para habilitar/deshabilitar notificaciones
- `FORMATO_FECHA` - Sistema de configuración de formato de fecha
- `IDIOMA_INTERFAZ` - Sistema de internacionalización

#### D. O Eliminar:
Si no se van a implementar, estos parámetros deben eliminarse de la entidad y de los scripts de inicialización para evitar confusión.

## Conclusión

**Configuraciones que NO se están usando actualmente:**

1. **En Configuraciones Generales (duplicadas)** - 13 configuraciones duplicadas en Parámetros Productivos
2. **En Parámetros Productivos (no implementadas)** - 7 parámetros que existen pero no se usan
3. **En Configuraciones Generales (no implementadas)** - 3 configuraciones del sistema que no se usan

**Total: 23 configuraciones que deben limpiarse o implementarse**
