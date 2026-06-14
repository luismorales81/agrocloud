# Auditoría arquitectónica: contrato GET /api/labores

**Alcance:** agrogestion-backend + agrogestion-frontend  
**Enfoque:** Estabilidad de contratos API, performance JPA, consistencia tipada end-to-end  
**Escenario:** Producción con ~1.000 usuarios concurrentes

---

## 🟢 1. Estado arquitectónico general

**Aceptable con riesgos**

- El contrato GET /api/labores está unificado y devuelve siempre `List<LaborDetalladoDTO>`.
- Hay exposición de entidades en otros endpoints (POST/PUT/PATCH).
- Existe N+1 crítico en la conversión Labor → DTO.
- No hay paginación; el endpoint no escala bien con muchos registros.
- El frontend tiene tipado parcial y varios `any` residuales.

---

## 🟡 2. Riesgos detectados

### Críticos

| ID | Riesgo | Ubicación | Detalle |
|----|--------|-----------|---------|
| C1 | **N+1 en convertirADetalladoDTO** | `LaborService.java` líneas 1669-1748 | Por cada `Labor` se ejecutan: `findByLabor` (maquinarias), `findByLabor` (manoObra), `findByLaborIdWithInsumo` (insumos). Además se accede a `labor.getLote()`, `lote.getCampo()`, `labor.getCultivo()`, `labor.getUsuario()` (LAZY). Para N labores: ~3N + 4N = **7N consultas**. Con 1.000 labores ≈ 7.000 queries. |
| C2 | **Sin paginación** | `LaborController` + `LaborRepository` | `findByLoteIdInOrderByFechaInicioDesc` devuelve todas las labores. Con 10.000 labores se cargan todas en memoria, se convierten a DTO y se serializan. Riesgo de OOM y latencia alta. |
| C3 | **Exposición de entidad Labor** | `LaborController` líneas 100, 124, 144 | `createLabor`, `updateLabor`, `patchLabor` devuelven `ResponseEntity<Labor>` o `ResponseEntity<?>` con entidad. Se serializa la entidad JPA (incluyendo `lote` sin `@JsonIgnore`). Contrato inestable y posible fuga de datos. |

### Moderados

| ID | Riesgo | Ubicación | Detalle |
|----|--------|-----------|---------|
| M1 | **LazyInitializationException en Plot.campo** | `LaborService.convertirADetalladoDTO` líneas 1734-1736 | `lote.getCampo().getNombre()` accede a `Plot.campo` (LAZY). Dentro de `@Transactional` funciona, pero si se extrae la conversión fuera de transacción o se usa en otro contexto, falla. |
| M2 | **Cache sin invalidación explícita** | `OfflineService.ts` | Al crear/actualizar/eliminar labores no se invalida la clave `labores`. TTL 2 min; durante ese tiempo se sirven datos obsoletos. |
| M3 | **ResponseEntity<?> con wildcard** | `LaborController` líneas 137, 152, 169 | `patchLabor`, `deleteLabor`, `anularLabor` usan `ResponseEntity<?>`. Tipado débil y posible retorno inconsistente. |
| M4 | **Posible null en observaciones** | `LaboresManagement.tsx` línea 1516 | `labor.observaciones.substring(0, 50)` puede lanzar si `observaciones` es `undefined` (p. ej. si el mapeo no cubre todos los caminos). |

### Menores

| ID | Riesgo | Ubicación | Detalle |
|----|--------|-----------|---------|
| L1 | **Uso extensivo de `any`** | `LaboresManagement`, `LotesManagement`, `apiServices` | Mapeos con `(x: any)` y parámetros `data: any` reducen seguridad de tipos. |
| L2 | **Duplicación lote / loteNombre** | `labor.types.ts` | Se mantiene `lote?: { id?, nombre? }` por compatibilidad. Redundante con el contrato unificado. |
| L3 | **Console.log en producción** | `LaborService.getLaboresByUser` | Varios `System.out.println` que deberían pasar a logger con nivel configurable. |

---

## 🔵 3. Problemas de performance

### 3.1 N+1 en conversión Labor → DTO

**Flujo actual:**

1. `getLaboresByUser` → 1 query (`findByLoteIdInOrderByFechaInicioDesc`) + N inicializaciones manuales de `lote`, `usuario`, `usuarioAnulacion`.
2. Para cada labor en `convertirADetalladoDTO`:
   - `laborMaquinariaRepository.findByLabor(labor)` → 1 query
   - `laborManoObraRepository.findByLabor(labor)` → 1 query
   - `laborInsumoRepository.findByLaborIdWithInsumo(labor.getId())` → 1 query
   - `labor.getLote()` → ya inicializado
   - `lote.getCampo()` → 1 query (LAZY en Plot)
   - `labor.getCultivo()` → 1 query (LAZY)
   - `labor.getUsuario()` → 1 query (LAZY)
   - En `convertirMaquinariaADTO`: `maquinaria.getLabor().getId()` → puede disparar proxy (normalmente en sesión)

**Total aproximado:** 1 + N × (3 + 3) = **1 + 6N** queries para N labores.

**Ejemplo:** 1.000 labores → ~6.001 queries.

### 3.2 Sin JOIN FETCH en repositorio de labores

`LaborRepository.findByLoteIdInOrderByFechaInicioDesc` es un método derivado sin `@Query` ni `@EntityGraph`. No se hace fetch de `lote`, `cultivo`, `usuario`, por lo que cada acceso lazy genera una query adicional.

### 3.3 Sin paginación

- No hay `Pageable` en el endpoint.
- Se cargan todas las labores del usuario/empresa.
- Con 10.000 labores: alto uso de memoria y tiempo de respuesta elevado.

### 3.4 DTO con datos pesados

`LaborDetalladoDTO` incluye `maquinarias`, `manoObra`, `insumosUsados` completos. Para listados podría bastar un resumen; el detalle podría ir en GET /labores/{id}.

---

## 🟣 4. Problemas de tipado en frontend

| Archivo | Línea | Problema |
|---------|-------|----------|
| `LaboresManagement.tsx` | 117 | `puedeModificarLabor = (labor: any)` |
| `LaboresManagement.tsx` | 295 | `useState<any[]>([])` para dosisDisponibles |
| `LaboresManagement.tsx` | 339, 354, 370, 383 | Mapeos con `(lote: any)`, `(insumo: any)`, etc. |
| `LaboresManagement.tsx` | 422, 432 | `(mo: any)`, `(ins: any)` en mapeo de manoObra/insumos |
| `LaboresManagement.tsx` | 566, 580, 590, 598 | `(d: any)`, `(ins: any)`, `(m: any)`, `(mo: any)` |
| `LaboresManagement.tsx` | 693 | `calcularCantidadDesdeDosis = (dosis: any, ...)` |
| `LaboresManagement.tsx` | 767, 780, 791 | Mapeos con `any` en insumos/maquinarias/manoObra |
| `LaboresManagement.tsx` | 949 | `handleInputChange = (field, value: any)` |
| `LaboresManagement.tsx` | 2637 | `dosisDisponibles.map((dosis: any) => ...)` |
| `LotesManagement.tsx` | 51-52 | `insumos_usados?: any[]`, `maquinaria_asignada?: any[]` |
| `LotesManagement.tsx` | 248, 300, 398 | `(field: any)`, `(lote: any)`, `(cultivo: any)` |
| `LotesManagement.tsx` | 407 | `handleInputChange = (field, value: any)` |
| `apiServices.ts` | múltiples | Parámetros `crear/actualizar` con `*Data: any` |

`labor.types.ts` y `LaborDetalladoDTO` están bien definidos; el problema es el uso residual de `any` en mapeos y handlers.

---

## 🔴 5. Recomendaciones de mejora

### 5.1 Backend: eliminar N+1 (crítico)

**Opción A – Query con JOIN FETCH**

```java
// LaborRepository.java
@Query("SELECT DISTINCT l FROM Labor l " +
       "LEFT JOIN FETCH l.lote " +
       "LEFT JOIN FETCH l.lote.campo " +
       "LEFT JOIN FETCH l.cultivo " +
       "LEFT JOIN FETCH l.usuario " +
       "WHERE l.lote.id IN :loteIds AND l.activo = true " +
       "ORDER BY l.fechaInicio DESC")
List<Labor> findByLoteIdInWithFetch(@Param("loteIds") List<Long> loteIds);
```

Usar este método en `getLaboresByUser` en lugar de `findByLoteIdInOrderByFechaInicioDesc`.

**Opción B – Batch fetch de maquinaria, mano de obra e insumos**

```java
// En LaborService, antes del stream de conversión:
List<Long> laborIds = labores.stream().map(Labor::getId).toList();
Map<Long, List<LaborMaquinaria>> maqPorLabor = laborMaquinariaRepository.findByLaborIdIn(laborIds)
    .stream().collect(Collectors.groupingBy(lm -> lm.getLabor().getId()));
// Similar para manoObra e insumos
```

Requiere métodos `findByLaborIdIn` en los repositorios correspondientes.

### 5.2 Backend: paginación

```java
// LaborController
@GetMapping
public ResponseEntity<Page<LaborDetalladoDTO>> getAllLabores(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size,
    ...) {
    Page<LaborDetalladoDTO> labores = laborService.getLaboresDetalladasPaginadas(user, page, size, ...);
    return ResponseEntity.ok(labores);
}
```

### 5.3 Backend: no exponer entidad Labor en POST/PUT/PATCH

```diff
// LaborController.java
- public ResponseEntity<Labor> createLabor(...) {
-     Labor laborCreada = laborService.crearLaborDesdeRequest(request, user);
-     return ResponseEntity.ok(laborCreada);
+ public ResponseEntity<LaborDetalladoDTO> createLabor(...) {
+     Labor laborCreada = laborService.crearLaborDesdeRequest(request, user);
+     LaborDetalladoDTO dto = laborService.convertirADetalladoDTO(laborCreada);
+     return ResponseEntity.ok(dto);
  }
```

Hacer `convertirADetalladoDTO` público o exponer un método equivalente en el servicio. Aplicar el mismo criterio a `updateLabor` y `patchLabor`.

### 5.4 Backend: reemplazar ResponseEntity<?>

```diff
- public ResponseEntity<?> patchLabor(...) {
+ public ResponseEntity<LaborDetalladoDTO> patchLabor(...) {
      Labor labor = laborService.actualizarParcialLabor(...);
-     return ResponseEntity.ok(labor);
+     return ResponseEntity.ok(laborService.convertirADetalladoDTO(labor));
  }
```

### 5.5 Frontend: evitar crash por observaciones null

```diff
// LaboresManagement.tsx línea 1516
- {labor.observaciones.substring(0, 50)}...
+ {(labor.observaciones || '').substring(0, 50)}{(labor.observaciones?.length ?? 0) > 50 ? '...' : ''}
```

O más simple: `{(labor.observaciones || '').slice(0, 50)}{(labor.observaciones?.length || 0) > 50 ? '...' : ''}`

### 5.6 Frontend: invalidar caché al mutar labores

```typescript
// En laboresService.crear, actualizar, eliminar, anular:
import { offlineService } from './OfflineService';
// Tras éxito:
offlineService.remove('labores');
```

O encapsular en un wrapper que invalide tras operaciones de escritura.

### 5.7 Frontend: tipado en LaboresManagement

Sustituir `(labor: any)` por `Labor` o `LaborDetalladoDTO` según el origen de los datos, y definir interfaces para `LaborMaquinaria`, `LaborManoObra`, `LaborInsumo` en lugar de `any`.

---

## 🏁 6. Conclusión final

### ¿La arquitectura está sólida para escalar?

**No en el estado actual.** El N+1 y la ausencia de paginación hacen que el endpoint no escale bien con muchos registros. Con pocas labores (< 100) puede ser aceptable; con miles, el rendimiento se degrada de forma importante.

### ¿El contrato está blindado contra regresiones?

**Parcialmente.** GET /api/labores está unificado y estable. Los endpoints de escritura (POST/PUT/PATCH) siguen exponiendo la entidad `Labor`, lo que mantiene un contrato frágil y acoplado al modelo JPA.

### ¿Hay deuda técnica acumulándose?

**Sí.** Se identifica:

- N+1 sin mitigación.
- Falta de paginación.
- Exposición de entidades en varios endpoints.
- Uso extensivo de `any` en frontend.
- Cache sin invalidación explícita.
- Posibles null/undefined en render (p. ej. `observaciones`).

### Prioridad sugerida

1. **Inmediato:** Mitigar N+1 (JOIN FETCH o batch fetch).
2. **Corto plazo:** Paginación en GET /api/labores.
3. **Corto plazo:** Dejar de exponer `Labor` en POST/PUT/PATCH; devolver DTO.
4. **Medio plazo:** Invalidar caché al mutar labores.
5. **Medio plazo:** Reducir `any` y reforzar tipado en frontend.
6. **Opcional:** Revisar si el listado necesita `maquinarias`, `manoObra`, `insumosUsados` completos o solo un resumen.
