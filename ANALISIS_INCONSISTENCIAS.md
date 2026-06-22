# Análisis de Inconsistencias en la Lógica del Sistema

Este documento identifica inconsistencias similares a la encontrada entre `finalizarGestacion()` y `registrarParto()`, donde acciones manuales pueden impedir operaciones posteriores.

---

## 1. **Cierre Manual de Parto → Imposibilita Registrar Destete**

### Ubicación
- `DesteteService.registrarDestete()` - Línea 64-67
- `PartoService.cerrarParto()` - Línea 244-258

### Problema
`DesteteService.registrarDestete()` requiere que el parto **NO esté cerrado** (`fechaFin == null`). Si alguien cierra manualmente un parto con `PartoService.cerrarParto()` antes de registrar el destete, la validación fallará.

### Código Problemático
```java
// DesteteService.java - Línea 64-67
if (parto.getFechaFin() != null) {
    throw new IllegalArgumentException("El parto ya está cerrado");
}
```

### Impacto
**ALTO** - Impide registrar destetes si el parto fue cerrado manualmente prematuramente.

### Solución Sugerida
Modificar `registrarDestete()` para permitir destetes incluso si el parto está cerrado (similar a como se resolvió con gestaciones finalizadas). Alternativamente, `cerrarParto()` podría validar que no haya destetes pendientes antes de cerrar.

---

## 2. **Finalización Manual de Gestación → No Actualiza Estado de la Madre**

### Ubicación
- `GestacionService.finalizarGestacion()` - Línea 116-129

### Problema
`finalizarGestacion()` cambia el estado de la gestación a `FINALIZADA` pero **NO actualiza el estado de la madre**. Si la madre está en `GESTACION` y se finaliza manualmente la gestación, la madre queda en estado inconsistente (GESTACION sin gestación activa).

### Código Problemático
```java
// GestacionService.java - Línea 116-129
@Transactional
public Gestacion finalizarGestacion(Long gestacionId, User user) {
    // ... validaciones ...
    gestacion.setEstado(Gestacion.EstadoGestacion.FINALIZADA);
    return gestacionRepository.save(gestacion);
    // ❌ NO actualiza estado de la madre
}
```

### Impacto
**MEDIO** - La madre queda en estado inconsistente. Sin embargo, ya se resolvió parcialmente permitiendo buscar gestaciones `FINALIZADA` en `registrarParto()`.

### Solución Sugerida
`finalizarGestacion()` debería actualizar el estado de la madre (a `LACTANCIA` si hay parto, o a `ADULTA` si no), o al menos validar que no haya inconsistencias. También podría registrar el cambio en el historial de estados.

---

## 3. **Registrar Aborto → No Actualiza Historial de Estados**

### Ubicación
- `GestacionService.registrarAborto()` - Línea 75-95

### Problema
`registrarAborto()` actualiza el estado de la madre a `ADULTA` **directamente** sin usar el método `actualizarEstadoMadre()` que actualiza el historial. Otros servicios (PartoService, DesteteService, ChequeoGestacionService) sí actualizan el historial correctamente.

### Código Problemático
```java
// GestacionService.java - Línea 91-93
// Actualizar estado de la madre
gestacion.getMadre().setEstadoActual(Madre.EstadoMadre.ADULTA);
madreRepository.save(gestacion.getMadre());
// ❌ NO actualiza el historial de estados
```

### Impacto
**MEDIO** - El historial de estados de la madre queda incompleto, lo que afecta reportes y trazabilidad.

### Solución Sugerida
Refactorizar `registrarAborto()` para usar un método `actualizarEstadoMadre()` similar al usado en otros servicios, o crear un método compartido en un servicio común.

---

## 4. **Cerrar Parto Manualmente → No Valida Destetes Pendientes**

### Ubicación
- `PartoService.cerrarParto()` - Línea 244-258

### Problema
`cerrarParto()` permite cerrar un parto sin validar si hay destetes pendientes. Si se cierra un parto antes de registrar el destete, luego no se podrá registrar (ver inconsistencia #1).

### Código Problemático
```java
// PartoService.java - Línea 244-258
@Transactional
public Parto cerrarParto(Long partoId, LocalDateTime fechaFin, User user) {
    // ... validaciones ...
    parto.setFechaFin(fechaFin != null ? fechaFin : LocalDateTime.now());
    return partoRepository.save(parto);
    // ❌ NO valida si hay destetes pendientes
    // ❌ NO actualiza estado de la madre
}
```

### Impacto
**ALTO** - Permite crear situaciones inconsistentes que impiden operaciones posteriores.

### Solución Sugerida
Agregar validación para verificar si existe un destete registrado. Si no existe, advertir al usuario o requerir confirmación. También considerar actualizar el estado de la madre si corresponde.

---

## 5. **Finalizar Gestación Manualmente → Imposibilita Registrar Chequeos**

### Ubicación
- `ChequeoGestacionService.registrarChequeo()` - Línea 63-65
- `GestacionService.finalizarGestacion()` - Línea 116-129

### Problema
`ChequeoGestacionService.registrarChequeo()` requiere que la gestación esté en estado `EN_CURSO`. Si alguien finaliza manualmente la gestación antes de registrar un chequeo, no podrá hacerlo.

### Código Problemático
```java
// ChequeoGestacionService.java - Línea 63-65
if (gestacion.getEstado() != Gestacion.EstadoGestacion.EN_CURSO) {
    throw new IllegalArgumentException("Solo se pueden registrar chequeos en gestaciones en curso");
}
```

### Impacto
**BAJO** - Si una gestación está finalizada, probablemente no debería recibir chequeos. Sin embargo, podría haber casos donde se necesite registrar un chequeo retrasado.

### Solución Sugerida
Evaluar si es necesario permitir chequeos en gestaciones finalizadas (probablemente no). Si se decide mantener la restricción, está bien como está.

---

## 6. **Finalizar Gestación Manualmente → No Valida Partos Registrados**

### Ubicación
- `GestacionService.finalizarGestacion()` - Línea 116-129

### Problema
`finalizarGestacion()` no valida si ya hay un parto registrado para esa gestación. Si se finaliza una gestación después de registrar el parto, podría haber inconsistencias (aunque no bloquea funcionalidad).

### Impacto
**BAJO** - Probablemente no causa errores, pero es una validación que podría prevenir inconsistencias de datos.

### Solución Sugerida
Agregar validación para verificar si existe un parto asociado a la gestación antes de finalizarla. Si existe, podría advertir al usuario o prevenir la finalización.

---

## 7. **Cerrar Parto Manualmente → No Actualiza Estado de la Madre**

### Ubicación
- `PartoService.cerrarParto()` - Línea 244-258

### Problema
`cerrarParto()` cierra el parto pero **NO actualiza el estado de la madre**. Si la madre está en `LACTANCIA` y se cierra el parto, la madre podría quedar en estado inconsistente si no hay destete registrado.

### Impacto
**MEDIO** - La madre podría quedar en estado `LACTANCIA` sin parto abierto, aunque esto se corregiría al registrar el destete.

### Solución Sugerida
Considerar actualizar el estado de la madre si corresponde, o al menos validar que la situación sea consistente.

---

## 8. **Inconsistencia en Actualización de Estado de la Madre**

### Ubicación
- Múltiples servicios tienen métodos `actualizarEstadoMadre()` duplicados

### Problema
Los métodos `actualizarEstadoMadre()` están **duplicados** en varios servicios:
- `PartoService.actualizarEstadoMadre()` - Línea 214-238
- `DesteteService.actualizarEstadoMadre()` - Línea 179-203
- `ChequeoGestacionService.actualizarEstadoMadre()` - Línea 141-165

Mientras que `GestacionService.registrarAborto()` actualiza el estado directamente sin usar ningún método auxiliar.

### Impacto
**MEDIO** - Código duplicado dificulta el mantenimiento. Si se necesita cambiar la lógica de actualización de estados, hay que hacerlo en múltiples lugares.

### Solución Sugerida
Extraer `actualizarEstadoMadre()` a un servicio común (ej: `MadreEstadoService`) o agregarlo a `MadreService` para centralizar esta lógica.

---

## Resumen de Prioridades

| # | Inconsistencia | Impacto | Prioridad |
|---|----------------|---------|-----------|
| 1 | Cierre Manual de Parto → Destete | ALTO | 🔴 **ALTA** |
| 4 | Cerrar Parto sin Validar Destetes | ALTO | 🔴 **ALTA** |
| 2 | Finalizar Gestación → Estado Madre | MEDIO | 🟡 **MEDIA** |
| 3 | Registrar Aborto → Historial | MEDIO | 🟡 **MEDIA** |
| 7 | Cerrar Parto → Estado Madre | MEDIO | 🟡 **MEDIA** |
| 8 | Código Duplicado → actualizarEstadoMadre | MEDIO | 🟡 **MEDIA** |
| 5 | Finalizar Gestación → Chequeos | BAJO | 🟢 **BAJA** |
| 6 | Finalizar Gestación → Partos | BAJO | 🟢 **BAJA** |

---

## Patrón General Observado

Las inconsistencias siguen un patrón común:

1. **Métodos que permiten acciones manuales** (cerrar, finalizar) sin validar dependencias
2. **Métodos que requieren estados específicos** sin flexibilidad para estados alternativos válidos
3. **Falta de actualización de estados relacionados** (madre, historial) cuando se realizan acciones manuales
4. **Inconsistencia en el uso de métodos auxiliares** (algunos usan `actualizarEstadoMadre()`, otros no)

### Recomendaciones Generales

1. **Unificar el manejo de estados**: Crear métodos compartidos para actualizar estados de madres y gestionar historiales
2. **Agregar validaciones preventivas**: Antes de cerrar/finalizar, validar dependencias y advertir al usuario
3. **Flexibilizar validaciones**: Permitir estados alternativos cuando sea lógicamente válido (como se hizo con gestaciones FINALIZADA)
4. **Actualizar estados relacionados**: Asegurar que los estados de entidades relacionadas se actualicen consistentemente

---

*Documento generado mediante análisis del código base*

