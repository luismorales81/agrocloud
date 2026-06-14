# Resumen de correcciones de contención de invariantes (I1, I3)

**Fecha:** 2026-01-25  
**Alcance:** Cierre de fugas sin rediseño, sin cambios de API ni de esquema.

---

## Summary of changes

1. **SiembraService (LEAK 1 – I1):** En los cuatro flujos que crean una labor con `estado = COMPLETADA` y la guardan, se asigna `fechaRealizacion` antes del primer `save`: siembra (fecha de siembra o hoy), cosecha (fecha de cosecha o hoy), abandono y limpieza (hoy).
2. **PlotService.resetearLote (LEAK 2 – I3):** En el segundo bucle solo se hace `setActivo(false)` y `save` cuando `estado` es CANCELADA o ANULADA (guard condicional mínimo).
3. **LaborService.deleteLabor (LEAK 3 – I3):** Antes de `setActivo(false)` se lanza `IllegalStateException` si `estado` no es CANCELADA ni ANULADA.
4. **LaborService (I1 adicional):** En `confirmarLaborSiembra` y `confirmarLaborCosecha` se asigna `fechaRealizacion` (fechaInicio o hoy) antes de guardar la labor como COMPLETADA, para que ningún camino con COMPLETADA viole I1.

No se ha cambiado ningún contrato de API, ruta, DTO ni esquema de base de datos.

---

## Code diff — SiembraService

**Cambio 1 – laborSiembra (registrarSiembra):**  
Tras `setEstado(EstadoLabor.COMPLETADA)` se añade una línea:

```java
laborSiembra.setFechaRealizacion(request.getFechaSiembra() != null ? request.getFechaSiembra() : java.time.LocalDate.now());
```

**Cambio 2 – laborCosecha (registrarCosecha):**  
Tras `setEstado(EstadoLabor.COMPLETADA)` se añade:

```java
laborCosecha.setFechaRealizacion(request.getFechaCosecha() != null ? request.getFechaCosecha() : java.time.LocalDate.now());
```

**Cambio 3 – laborAbandono (abandonarCultivo):**  
Tras `setEstado(EstadoLabor.COMPLETADA)` se añade:

```java
laborAbandono.setFechaRealizacion(java.time.LocalDate.now());
```

**Cambio 4 – laborLimpieza (limpiarCultivo):**  
Tras `setEstado(EstadoLabor.COMPLETADA)` se añade:

```java
laborLimpieza.setFechaRealizacion(java.time.LocalDate.now());
```

---

## Code diff — PlotService.resetearLote

**Antes (segundo bucle):**
```java
for (com.agrocloud.model.entity.Labor labor : labores) {
    if (labor.getActivo() == null || labor.getActivo()) {
        labor.setActivo(false);
        labor.setObservaciones(...);
        laborRepository.save(labor);
    }
}
```

**Después:**
```java
for (com.agrocloud.model.entity.Labor labor : labores) {
    if (labor.getActivo() == null || labor.getActivo()) {
        if (labor.getEstado() == com.agrocloud.model.entity.Labor.EstadoLabor.CANCELADA
                || labor.getEstado() == com.agrocloud.model.entity.Labor.EstadoLabor.ANULADA) {
            labor.setActivo(false);
            labor.setObservaciones(...);
            laborRepository.save(labor);
        }
    }
}
```

---

## Code diff — LaborService.deleteLabor

**Antes:** Tras las comprobaciones de permisos se hacía directamente:
```java
labor.setActivo(false);
laborRepository.save(labor);
```

**Después:** Se añade la guarda I3 antes de desactivar:
```java
if (labor.getEstado() != Labor.EstadoLabor.CANCELADA && labor.getEstado() != Labor.EstadoLabor.ANULADA) {
    throw new IllegalStateException("Cannot deactivate labor unless it is CANCELADA or ANULADA");
}
labor.setActivo(false);
laborRepository.save(labor);
```

---

## LaborService — confirmarLaborSiembra / confirmarLaborCosecha (I1)

En ambos métodos, antes de `laborRepository.save(labor)` tras `setEstado(COMPLETADA)` se añade:

```java
labor.setFechaRealizacion(labor.getFechaInicio() != null ? labor.getFechaInicio() : LocalDate.now());
```

---

## Search audit result

### setEstado(COMPLETADA) en entidad Labor

| Ubicación | ¿Asigna fechaRealizacion antes de save? | Cumple I1 |
|-----------|----------------------------------------|-----------|
| SiembraService ~98  (laborSiembra)     | Sí (request.getFechaSiembra() o now)   | Sí       |
| SiembraService ~278 (laborCosecha)     | Sí (request.getFechaCosecha() o now)  | Sí       |
| SiembraService ~483 (laborAbandono)    | Sí (LocalDate.now())                  | Sí       |
| SiembraService ~525 (laborLimpieza)    | Sí (LocalDate.now())                  | Sí       |
| LaborService ~808 (confirmarLaborSiembra)   | Sí (fechaInicio o now) | Sí       |
| LaborService ~834 (confirmarLaborCosecha)   | Sí (fechaInicio o now) | Sí       |
| LaborService.aplicarTransicionDeEstado      | Sí (interno al método)               | Sí       |

No hay más asignaciones directas a `setEstado(COMPLETADA)` que persistan Labor sin fechaRealizacion.

### setActivo(false) sobre Labor (entidad com.agrocloud.model.entity.Labor)

| Ubicación | Contexto | Cumple I3 |
|-----------|----------|-----------|
| PlotService ~235  | Primer bucle resetearLote: antes se hace setEstado(CANCELADA) | Sí (estado = CANCELADA) |
| PlotService ~243  | Segundo bucle: solo si estado CANCELADA o ANULADA             | Sí (guard añadido)     |
| LaborService ~1343 | eliminarLabor: PLANIFICADA → setEstado(CANCELADA) luego setActivo(false) | Sí |
| LaborService ~1357 | eliminarLabor: caso “ya CANCELADA o ANULADA” → setActivo(false)          | Sí |
| LaborService ~1418 | anularLabor: setEstado(ANULADA) luego setActivo(false)                   | Sí |
| LaborService ~1820 | deleteLabor: solo tras comprobar estado CANCELADA o ANULADA (throw si no) | Sí |

El resto de `setActivo(false)` en el proyecto se aplican a otras entidades (Plot, Insumo, DosisAgroquimico, Recria, AplicacionAgroquimica, etc.), no a Labor; I3 solo aplica a la entidad Labor.

---

## Confirmation that no API contract changed

- No se ha modificado ningún endpoint, ruta, método HTTP ni firma de controlador.
- No se ha cambiado ningún DTO, request ni response.
- No se ha tocado el esquema de base de datos ni migraciones.
- Comportamiento observable:  
  - Siembra/cosecha/abandono/limpieza siguen creando labores COMPLETADA; ahora siempre con fechaRealizacion.  
  - resetearLote solo archiva (activo=false) labores que ya están CANCELADA o ANULADA.  
  - deleteLabor ahora falla con IllegalStateException si la labor no está CANCELADA ni ANULADA; para “eliminar” una labor planificada o completada debe usarse el flujo existente (eliminarLabor / anularLabor).  
- Contrato de API (rutas, verbos, cuerpos, códigos de respuesta) se mantiene; solo se refuerzan invariantes en backend.

---

**Fin del resumen.** Cambios mínimos y acotados a la contención de I1 e I3.
