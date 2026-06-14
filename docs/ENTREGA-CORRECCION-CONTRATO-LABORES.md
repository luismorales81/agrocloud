# Entrega: Corrección contrato labores y bug LotesManagement

## Archivos modificados

| Archivo | Cambio |
|---------|--------|
| `agrogestion-backend/src/main/java/com/agrocloud/cultivos/application/LaborService.java` | Nuevo método `getLaboresDetalladasConFiltros`; GET /api/labores siempre devuelve DTO |
| `agrogestion-backend/src/main/java/com/agrocloud/controller/LaborController.java` | Unificación: ambas ramas devuelven `List<LaborDetalladoDTO>` |
| `agrogestion-frontend/src/components/LotesManagement.tsx` | Fix mapeo `lote_nombre`; import y uso de `LaborDetalladoDTO` |
| `agrogestion-frontend/src/types/labor.types.ts` | **Nuevo** – interface `LaborDetalladoDTO` compartida |
| `agrogestion-frontend/src/services/apiServices.ts` | Tipado `listar()` y `listarConFiltros()` → `Promise<LaborDetalladoDTO[]>` |
| `agrogestion-frontend/src/services/OfflineService.ts` | Tipado `getLabores()` → `Promise<LaborDetalladoDTO[]>` |
| `agrogestion-frontend/src/components/LaboresManagement.tsx` | Import y uso de `LaborDetalladoDTO` en mapeo |

---

## Diff por archivo

### 1. LaborService.java

```diff
+    /**
+     * Listar labores del usuario con filtros, devolviendo siempre DTO (contrato unificado).
+     * GET /api/labores devuelve siempre List<LaborDetalladoDTO> con o sin filtros.
+     */
+    public List<LaborDetalladoDTO> getLaboresDetalladasConFiltros(User user, LocalDate fechaDesde, LocalDate fechaHasta,
+                                                                  Long loteId, String estado, Boolean soloVencidas) {
+        List<Labor> labores = getLaboresConFiltros(user, fechaDesde, fechaHasta, loteId, estado, soloVencidas);
+        return labores.stream()
+                .map(this::convertirADetalladoDTO)
+                .collect(Collectors.toList());
+    }
+
     /**
     * Eliminar labor según su estado.
```

### 2. LaborController.java

```diff
-    /**
-     * Obtener labores con filtros opcionales (spec SDD): fecha_desde, fecha_hasta, lote_id, estado, overdue.
-     * Sin parámetros devuelve todas las labores del usuario (con costos detallados si no hay filtros de lista simple).
-     */
-    @GetMapping
-    public ResponseEntity<?> getAllLabores(...) {
-        ...
-        if (fecha_desde != null || ...) {
-            List<Labor> labores = laborService.getLaboresConFiltros(...);
-            return ResponseEntity.ok(labores);
-        }
-        List<LaborDetalladoDTO> labores = laborService.getLaboresDetalladasByUser(user);
-        return ResponseEntity.ok(labores);
+    /**
+     * Obtener labores con filtros opcionales (spec SDD): fecha_desde, fecha_hasta, lote_id, estado, overdue.
+     * Contrato unificado: siempre devuelve List<LaborDetalladoDTO> (con o sin filtros).
+     */
+    @GetMapping
+    public ResponseEntity<List<LaborDetalladoDTO>> getAllLabores(...) {
+        ...
+        List<LaborDetalladoDTO> labores = (fecha_desde != null || ...)
+                ? laborService.getLaboresDetalladasConFiltros(...)
+                : laborService.getLaboresDetalladasByUser(user);
+        return ResponseEntity.ok(labores);
```

### 3. LotesManagement.tsx

```diff
+ import type { LaborDetalladoDTO } from '../types/labor.types';

-      const data = await laboresService.listar();
-      const laboresMapeadas: Labor[] = data.map((labor: any) => ({
+      const data: LaborDetalladoDTO[] = await laboresService.listar();
+      const laboresMapeadas: Labor[] = data.map((labor: LaborDetalladoDTO) => ({
          ...
-         lote_nombre: labor.lote?.nombre || '',
+         lote_nombre: labor.loteNombre || labor.lote?.nombre || '',
```

### 4. labor.types.ts (nuevo)

```typescript
export interface LaborDetalladoDTO {
  id: number;
  tipo?: string;
  loteId: number;
  loteNombre: string;
  fechaRealizacion?: string;
  cultivoId?: number;
  overdue?: boolean;
  // ... resto de campos usados
}
```

### 5. apiServices.ts

```diff
+ import type { LaborDetalladoDTO } from '../types/labor.types';

-  async listar() {
-    const response = await api.get(API_ENDPOINTS.LABORES.LISTAR);
+  async listar(): Promise<LaborDetalladoDTO[]> {
+    const response = await api.get<LaborDetalladoDTO[]>(API_ENDPOINTS.LABORES.LISTAR);
     return response.data;
   }

-  async listarConFiltros(...) {
+  async listarConFiltros(...): Promise<LaborDetalladoDTO[]> {
     ...
-    const response = await api.get(url);
+    const response = await api.get<LaborDetalladoDTO[]>(url);
```

### 6. OfflineService.ts

```diff
+ import type { LaborDetalladoDTO } from '../types/labor.types';

-  public async getLabores(): Promise<any[]> {
+  public async getLabores(): Promise<LaborDetalladoDTO[]> {
     return this.get('labores', async () => {
-      const response = await api.get('/labores');
+      const response = await api.get<LaborDetalladoDTO[]>('/labores');
```

### 7. LaboresManagement.tsx

```diff
+ import type { LaborDetalladoDTO } from '../types/labor.types';

-          const laboresActivas = laboresData.filter((labor: any) => labor.activo !== false);
-          const laboresMapeadas: Labor[] = laboresActivas.map((labor: any) => ({
+          const laboresActivas = laboresData.filter((labor: LaborDetalladoDTO) => labor.activo !== false);
+          const laboresMapeadas: Labor[] = laboresActivas.map((labor: LaborDetalladoDTO) => ({
```

---

## Por qué el contrato es estable ahora

1. **Antes:** GET /api/labores sin filtros devolvía `List<LaborDetalladoDTO>` y con filtros `List<Labor>`, con estructuras distintas (`loteId`/`loteNombre` vs `lote`).
2. **Ahora:** GET /api/labores devuelve siempre `List<LaborDetalladoDTO>` (con o sin filtros). Se reutiliza `convertirADetalladoDTO` para transformar `Labor` → `LaborDetalladoDTO` cuando hay filtros.
3. **Frontend:** Se usa `labor.loteNombre` como fuente principal y `labor.lote?.nombre` como respaldo, compatible con ambos formatos.
4. **Tipos:** `LaborDetalladoDTO` centralizado en `labor.types.ts` y usado en servicios y componentes, evitando `any` y desalineaciones.

---

## Confirmación

| Verificación | Estado |
|--------------|--------|
| GET /api/labores devuelve siempre `List<LaborDetalladoDTO>` | Sí |
| Nombre de lote en LotesManagement | Corregido (`loteNombre` como fuente principal) |
| Filtros siguen funcionando | Sí (misma lógica, respuesta convertida a DTO) |
| Sin ruptura de contrato | Sí (mismo DTO en todos los casos) |
| Linter frontend | Sin errores |

---

## Cómo validar manualmente

1. **Backend:** `mvn compile` en `agrogestion-backend`
2. **Frontend:** `npm run build` en `agrogestion-frontend`
3. **Funcional:** Listar labores en LotesManagement y LaboresManagement; comprobar que el nombre del lote se muestra bien y que los filtros funcionan.
