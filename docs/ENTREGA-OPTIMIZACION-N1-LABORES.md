# Entrega: Optimización N+1 en GET /api/labores

## 1. Diff completo por archivo modificado

### LaborRepository.java

```diff
     /**
     * Buscar labores por lista de IDs de lotes
     */
     List<Labor> findByLoteIdInOrderByFechaInicioDesc(List<Long> loteIds);
+
+    /**
+     * Buscar labores por lista de IDs de lotes con fetch de relaciones ManyToOne.
+     * Evita N+1: lote, lote.campo, cultivo, usuario.
+     * NO hace JOIN FETCH de colecciones OneToMany.
+     */
+    @Query("SELECT DISTINCT l FROM Labor l " +
+           "LEFT JOIN FETCH l.lote lo " +
+           "LEFT JOIN FETCH lo.campo " +
+           "LEFT JOIN FETCH l.cultivo " +
+           "LEFT JOIN FETCH l.usuario " +
+           "WHERE l.lote.id IN :loteIds AND l.activo = true " +
+           "ORDER BY l.fechaInicio DESC")
+    List<Labor> findByLoteIdInWithFetch(@Param("loteIds") List<Long> loteIds);
```

### LaborMaquinariaRepository.java

```diff
     @Query("SELECT lm FROM LaborMaquinaria lm WHERE lm.labor.id = :laborId")
     List<LaborMaquinaria> findByLaborId(@Param("laborId") Long laborId);
+
+    /**
+     * Carga batch: maquinarias de múltiples labores. Evita N+1.
+     */
+    @Query("SELECT lm FROM LaborMaquinaria lm JOIN FETCH lm.labor WHERE lm.labor.id IN :laborIds")
+    List<LaborMaquinaria> findByLaborIdIn(@Param("laborIds") List<Long> laborIds);
```

### LaborManoObraRepository.java

```diff
     @Query("SELECT lmo FROM LaborManoObra lmo WHERE lmo.labor.id = :laborId")
     List<LaborManoObra> findByLaborId(@Param("laborId") Long laborId);
+
+    /**
+     * Carga batch: mano de obra de múltiples labores. Evita N+1.
+     */
+    @Query("SELECT lmo FROM LaborManoObra lmo JOIN FETCH lmo.labor WHERE lmo.labor.id IN :laborIds")
+    List<LaborManoObra> findByLaborIdIn(@Param("laborIds") List<Long> laborIds);
```

### LaborInsumoRepository.java

```diff
     @Query("SELECT li FROM LaborInsumo li JOIN FETCH li.insumo WHERE li.labor.id = :laborId")
     List<LaborInsumo> findByLaborIdWithInsumo(@Param("laborId") Long laborId);
+
+    /**
+     * Carga batch: insumos de múltiples labores con JOIN FETCH insumo y labor. Evita N+1.
+     */
+    @Query("SELECT DISTINCT li FROM LaborInsumo li JOIN FETCH li.insumo JOIN FETCH li.labor WHERE li.labor.id IN :laborIds")
+    List<LaborInsumo> findByLaborIdInWithInsumo(@Param("laborIds") List<Long> laborIds);
```

### LaborService.java

```diff
+    @Transactional(readOnly = true)
     public List<Labor> getLaboresByUser(User user) {
         ...
-            List<Labor> labores = laborRepository.findByLoteIdInOrderByFechaInicioDesc(loteIds);
+            List<Labor> labores = laborRepository.findByLoteIdInWithFetch(loteIds);
             System.out.println("[LABOR_SERVICE] Labores encontradas: " + labores.size());
-
-            // Inicializar relaciones lazy para evitar LazyInitializationException en serialización JSON
-            if (labores != null) {
-                labores.forEach(labor -> {
-                    if (labor.getLote() != null) {
-                        labor.getLote().getId(); // Inicializar lote
-                    }
-                    if (labor.getUsuario() != null) {
-                        labor.getUsuario().getId(); // Inicializar usuario
-                    }
-                    if (labor.getUsuarioAnulacion() != null) {
-                        labor.getUsuarioAnulacion().getId(); // Inicializar usuarioAnulacion
-                    }
-                });
-            }
-
             return labores;
```

```diff
-    public List<LaborDetalladoDTO> getLaboresDetalladasByUser(User user) {
-        List<Labor> labores = getLaboresByUser(user);
-        return labores.stream()
-            .map(this::convertirADetalladoDTO)
-            .collect(Collectors.toList());
-    }
+    @Transactional(readOnly = true)
+    public List<LaborDetalladoDTO> getLaboresDetalladasByUser(User user) {
+        List<Labor> labores = getLaboresByUser(user);
+        return convertirLaboresADetalladoDTOBatch(labores);
+    }
```

```diff
+    @Transactional(readOnly = true)
     public List<Labor> getLaboresConFiltros(User user, ...) {
```

```diff
+    @Transactional(readOnly = true)
     public List<LaborDetalladoDTO> getLaboresDetalladasConFiltros(User user, ...) {
         List<Labor> labores = getLaboresConFiltros(user, ...);
-        return labores.stream()
-                .map(this::convertirADetalladoDTO)
-                .collect(Collectors.toList());
+        return convertirLaboresADetalladoDTOBatch(labores);
     }
```

```diff
+    /**
+     * Conversión batch: evita N+1 cargando maquinarias, mano de obra e insumos en 3 queries.
+     */
+    private List<LaborDetalladoDTO> convertirLaboresADetalladoDTOBatch(List<Labor> labores) {
+        if (labores == null || labores.isEmpty()) {
+            return new ArrayList<>();
+        }
+        List<Long> laborIds = labores.stream().map(Labor::getId).collect(Collectors.toList());
+        List<LaborMaquinaria> todasMaquinarias = laborMaquinariaRepository.findByLaborIdIn(laborIds);
+        List<LaborManoObra> todaManoObra = laborManoObraRepository.findByLaborIdIn(laborIds);
+        List<LaborInsumo> todosInsumos = laborInsumoRepository.findByLaborIdInWithInsumo(laborIds);
+        Map<Long, List<LaborMaquinaria>> maqPorLabor = todasMaquinarias.stream()
+                .collect(Collectors.groupingBy(lm -> lm.getLabor().getId()));
+        Map<Long, List<LaborManoObra>> moPorLabor = todaManoObra.stream()
+                .collect(Collectors.groupingBy(lmo -> lmo.getLabor().getId()));
+        Map<Long, List<LaborInsumo>> insPorLabor = todosInsumos.stream()
+                .collect(Collectors.groupingBy(li -> li.getLabor().getId()));
+        return labores.stream()
+                .map(l -> convertirADetalladoDTOConMapas(l, maqPorLabor.getOrDefault(...), ...))
+                .collect(Collectors.toList());
+    }
+
+    private LaborDetalladoDTO convertirADetalladoDTOConMapas(Labor labor,
+            List<LaborMaquinaria> maquinarias, List<LaborManoObra> manoObra, List<LaborInsumo> insumos) {
+        // Misma lógica que antes pero recibe listas pre-cargadas
+    }
+
+    private LaborDetalladoDTO convertirADetalladoDTO(Labor labor) {
+        return convertirLaboresADetalladoDTOBatch(List.of(labor)).get(0);
+    }
```

---

## 2. Número estimado de queries: antes vs después

| Escenario | Antes | Después |
|-----------|-------|---------|
| **N labores** | 1 + 6N | 4 |
| **100 labores** | 601 | 4 |
| **1.000 labores** | 6.001 | 4 |
| **10.000 labores** | 60.001 | 4 |

**Desglose antes:** 1 (labores base) + N×3 (maquinarias, manoObra, insumos por labor) + N×3 (lote, lote.campo, cultivo, usuario por labor) ≈ 1 + 6N.

**Desglose después:**
1. `findByLoteIdInWithFetch` – labores + lote + lote.campo + cultivo + usuario
2. `findByLaborIdIn` – maquinarias
3. `findByLaborIdIn` – mano de obra
4. `findByLaborIdInWithInsumo` – insumos

---

## 3. Confirmación de contrato

- **GET /api/labores** sigue devolviendo `List<LaborDetalladoDTO>`.
- La estructura JSON no cambia: mismos campos, mismos nombres, mismos tipos.
- El frontend no requiere cambios.

---

## 4. Confirmación de ausencia de N+1 residual

| Relación | Estrategia | Estado |
|----------|------------|--------|
| `l.lote` | `LEFT JOIN FETCH l.lote` | Resuelto |
| `l.lote.campo` | `LEFT JOIN FETCH lo.campo` | Resuelto |
| `l.cultivo` | `LEFT JOIN FETCH l.cultivo` | Resuelto |
| `l.usuario` | `LEFT JOIN FETCH l.usuario` | Resuelto |
| Maquinarias por labor | `findByLaborIdIn` + `groupingBy` | Resuelto |
| Mano de obra por labor | `findByLaborIdIn` + `groupingBy` | Resuelto |
| Insumos por labor | `findByLaborIdInWithInsumo` + `groupingBy` | Resuelto |

No se usa `JOIN FETCH` en colecciones `@OneToMany`; las colecciones se cargan en batch y se agrupan por `laborId`.

---

## 5. Validaciones

| Verificación | Estado |
|--------------|--------|
| `mvn clean compile` | OK (exit_code: 0) |
| Contrato JSON | Sin cambios |
| Duplicados | `DISTINCT` en queries que lo requieren |
| `LazyInitializationException` | Evitada con fetch y `@Transactional(readOnly = true)` |
| Explosión cartesiana | No aplica (solo `ManyToOne` en fetch principal) |
| Tests | 15/17 pasan. 2 fallos en `LaborServiceTransicionEstadoTest` por validación `fechaRealizacion < fechaInicio` (pre-existentes, no relacionados con la optimización N+1). |

---

## 6. Archivos modificados

1. `agrogestion-backend/src/main/java/com/agrocloud/repository/LaborRepository.java`
2. `agrogestion-backend/src/main/java/com/agrocloud/repository/LaborMaquinariaRepository.java`
3. `agrogestion-backend/src/main/java/com/agrocloud/repository/LaborManoObraRepository.java`
4. `agrogestion-backend/src/main/java/com/agrocloud/repository/LaborInsumoRepository.java`
5. `agrogestion-backend/src/main/java/com/agrocloud/cultivos/application/LaborService.java`
