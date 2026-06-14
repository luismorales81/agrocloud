# Ejecución alineada con Spec — Módulo Porcinos

**Fuente de verdad:** `SOLUCION-ARQUITECTONICA-PORCINOS.md`  
**Objetivo:** Auditoría contra código real, refactors concretos, migraciones Flyway, concurrencia, eliminación escritura Faena, casos de prueba y plan de rollout.

---

# PASO 1 — Divergencias detectadas

## 1.1 Resumen por archivo

| Archivo | Método / Ubicación | Regla esperada según spec | Implementación actual | Problema | Impacto funcional | Severidad |
|---------|--------------------|---------------------------|------------------------|----------|--------------------|-----------|
| **Recria.java** | Campo `origen` | Origen persistido: `@Column` + `@Enumerated(EnumType.STRING)`, sin @Transient | `@Transient` + `@JsonProperty("origen")` | El valor no se persiste ni se recupera de BD | Trazabilidad perdida; reportes por origen incorrectos | **CRÍTICA** |
| **Recria.java** | — | Opcional: @Version para concurrencia | No existe campo version | Sin control de concurrencia en ventas | Ventas concurrentes pueden exceder stock | **ALTA** |
| **VentaPorcinoService** | `calcularAnimalesDisponibles` | Fórmula única: `disponibles = cantidadAnimales - Σ(muertes)`; no restar ventas | `cantidadInicial - muertes - ventasPrevias` | Doble descuento: ventas ya están en cantidadAnimales | Tras 2ª venta, disponibles en 0 o negativo; bloqueos erróneos | **CRÍTICA** |
| **VentaPorcinoService** | `registrarVenta` | Usar un único componente de dominio para disponibles; bloqueo (versión o lock) | Sin lock; usa método privado con ventasPrevias | Concurrencia y doble descuento | Stock negativo o venta en exceso | **CRÍTICA** |
| **MuerteRecriaService** | `calcularAnimalesDisponibles` | Misma fórmula oficial; no ventasPrevias | `cantidadInicial - totalMuertes - ventasPrevias` | Resta ventas; incoherente con spec (cantidadAnimales ya las refleja) | Validación de muerte demasiado restrictiva o incoherente con venta | **CRÍTICA** |
| **MovimientoEtapaService** | `calcularAnimalesDisponibles` | Usar componente único de dominio (repo muertes), no colección en entidad | `cantidadInicial - muertes` con `recria.getMuertesRecria()` (colección lazy) | Fórmula correcta pero fuente de muertes es la colección (puede no estar cargada); no usa componente único | Riesgo de N+1 o datos incorrectos si colección no cargada | **ALTA** |
| **RecriaService** | `calcularAnimalesDisponibles` | Usar componente único de dominio | `cantidadInicial - muertes` con `recria.getMuertesRecria()`; comentario "ventas ya descontaron" | Misma fórmula que spec pero implementación duplicada y usa colección en entidad | Cierre de recría puede fallar si muertes no cargadas; no hay fuente única | **ALTA** |
| **FaenaService** | `registrarFaena` | No registrar nuevas faenas; lanzar UnsupportedOperationException | Escribe en porcinos_faena y actualiza recria.cantidadAnimales | Sigue siendo vía de escritura; compite con VentaPorcino | Doble vía de salida; stock incoherente si se usan ambos | **CRÍTICA** |
| **FaenaService** | `calcularAnimalesDisponibles` | No aplica (endpoint deshabilitado) | `cantidadInicial - muertes - faenasPrevias` | Fórmula distinta; no actualiza cantidadAnimales en faena | Inconsistencia si se llama | **ALTA** |
| **FaenaController** | `registrarFaena` (POST /{recriaId}) | Devolver 410 Gone con mensaje de uso de VentaPorcino tipo FAENA | 200 + llama a faenaService.registrarFaena | Sigue aceptando escritura de faenas | Clientes pueden seguir escribiendo por Faena | **CRÍTICA** |
| **DesteteService** | `registrarDestete` | Si parto.fechaFin != null validar parto.fechaFin >= fechaDestete; si null setear parto.fechaFin = fechaDestete | Solo setea parto.fechaFin si null; no valida si ya estaba cerrado | Parto cerrado con fecha anterior al destete no se valida | Inconsistencia temporal en reportes | **MEDIA** |
| **DesteteService** | `crearRecriaDesdeDestete` | Recria con origen DESTETE persistido | setOrigen(DESTETE) pero entidad tiene origen @Transient | Origen no se guarda en BD | Mismo impacto que Recria.origen | **CRÍTICA** |
| **Parto.java** | — | Parto tiene gestacion_id (nullable) | No existe campo gestacion_id | No hay FK Parto → Gestación | No se puede auditar qué gestación generó el parto | **ALTA** |
| **PartoService** | `registrarParto` | Setear parto.gestacion_id con la gestación usada en la validación | No setea gestacion en partoData | Parto guardado sin vínculo a gestación | Mismo impacto que falta de gestacion_id | **ALTA** |
| **GestacionService** | `registrarAborto` | Cierre de gestación y actualización madre a ADULTA en la misma transacción | Llama madreService.actualizarEstadoMadre(madre, ADULTA, ...) y save(gestacion) | Cumple spec | — | — |
| **GestacionService** | `finalizarGestacion` | Si no hay parto, actualizar madre a ADULTA en la misma transacción | Comprueba parto y llama actualizarEstadoMadre si partoOpt.isEmpty() | Cumple spec | — | — |
| **ServicioService** | Método privado `actualizarEstadoMadre` | Un único lugar: MadreService.actualizarEstadoMadre; eliminar duplicado | Tiene copia privada de actualizarEstadoMadre (historial + madreRepository.save) | Lógica duplicada; cambios en regla de historial no se reflejan en un solo lugar | Comportamiento divergente posible | **ALTA** |
| **ServicioService** | `marcarServicioComoFallido` | Al cerrar gestación (ABORTO) actualizar madre en misma transacción vía MadreService | Cierra gestación y llama actualizarEstadoMadre (propio) | Usa su propio actualizarEstadoMadre en lugar de MadreService | Mismo que arriba | **ALTA** |
| **MadreMuerteService** | `cerrarGestacionActiva` | Al poner gestación ABORTO, actualizar madre a ADULTA en la misma transacción | Solo pone gestacion.setEstado(ABORTO) y save(gestacion); no actualiza madre | Madre puede quedar en GESTACION sin gestación activa | Invariante Madre–Gestación roto | **ALTA** |
| **RecriaRepository** | — | Soporte para lock en venta (findByIdForUpdate o @Version) | Solo findByIdAndActivoTrue; sin lock | Sin bloqueo en registrarVenta | Concurrencia no controlada | **ALTA** |
| **BD** | porcinos_destetes | UNIQUE(parto_id) | No existe constraint | Puede haber más de un destete por parto | Doble recría por parto; datos incoherentes | **CRÍTICA** |
| **BD** | porcinos_recria | CHECK (cantidad_animales >= 0) | No existe | No se impide cantidad negativa en BD | Stock negativo posible por bug o acceso directo | **ALTA** |
| **BD** | porcinos_partos | Columna gestacion_id (nullable) | No existe | Parto sin referencia a gestación | Trazabilidad | **ALTA** |
| **BD** | porcinos_recria | origen NULL → 'EXTERNO' para datos existentes | — | Datos históricos con origen NULL | Reportes/filtros por origen incompletos | **MEDIA** |

---

# PASO 2 — Refactors exactos por archivo

## 2.1 Componente de dominio: RecriaStockService (nuevo)

**Spec:** Un único componente que calcule `disponibles(recria) = cantidadAnimales - Σ(muertes)`.

**Crear archivo:** `agrogestion-backend/src/main/java/com/agrocloud/porcinos/application/RecriaStockService.java`

```java
package com.agrocloud.porcinos.application;

import com.agrocloud.porcinos.domain.Recria;
import com.agrocloud.porcinos.infrastructure.MuerteRecriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Servicio de dominio: única fuente de verdad para el cálculo de animales disponibles en recría.
 * Fórmula: disponibles = cantidadAnimales - Σ(muertes activas).
 * Las ventas/faenas ya están reflejadas en cantidadAnimales (no se restan).
 */
@Service
public class RecriaStockService {

    @Autowired
    private MuerteRecriaRepository muerteRecriaRepository;

    /**
     * Calcula los animales disponibles en la recría según la fórmula oficial.
     * Usado por VentaPorcinoService, MuerteRecriaService, MovimientoEtapaService, RecriaService.
     */
    public int animalesDisponibles(Recria recria) {
        int cantidad = recria.getCantidadAnimales() != null ? recria.getCantidadAnimales() : 0;
        int muertes = muerteRecriaRepository.findByRecriaAndActivoTrue(recria).stream()
            .mapToInt(m -> m.getCantidad() != null ? m.getCantidad() : 0)
            .sum();
        return Math.max(0, cantidad - muertes);
    }
}
```

---

## 2.2 Entidad Recria: persistir origen y añadir @Version

**Archivo:** `agrogestion-backend/src/main/java/com/agrocloud/porcinos/domain/Recria.java`

**Cambios:**

1) Sustituir el bloque del campo `origen` (quitar @Transient, mapear a BD):

```java
// ANTES:
    @Transient
    @JsonProperty("origen")
    private OrigenRecria origen;

// DESPUÉS:
    @Enumerated(EnumType.STRING)
    @Column(name = "origen", length = 20)
    @JsonProperty("origen")
    private OrigenRecria origen;
```

2) Añadir campo de versión para concurrencia (tras el campo `activo`, antes de `empresa`):

```java
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Version
    @Column(name = "version")
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
```

3) Añadir getter/setter para version:

```java
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
```

**Imports:** ya existen `jakarta.persistence.*` y `EnumType`. Añadir ninguno si `@Version` está en `jakarta.persistence`.

**Por qué cumple la spec:** Origen se persiste y se lee de BD; un solo lugar (RecriaStockService) calcula disponibles; @Version permite control de concurrencia en venta.

---

## 2.3 VentaPorcinoService: usar RecriaStockService y eliminar ventasPrevias

**Archivo:** `agrogestion-backend/src/main/java/com/agrocloud/porcinos/application/VentaPorcinoService.java`

**Inyección:** Añadir dependencia de RecriaStockService y opcionalmente quitar MuerteRecriaRepository si solo se usaba para calcular (el cálculo pasa a RecriaStockService).

**Código actual relevante (fragmentos):**

```java
    @Autowired
    private MuerteRecriaRepository muerteRecriaRepository;
    // ...
            int animalesDisponibles = calcularAnimalesDisponibles(recria);
    // ...
    private int calcularAnimalesDisponibles(Recria recria) {
        int cantidadInicial = recria.getCantidadAnimales() != null ? recria.getCantidadAnimales() : 0;
        int muertes = muerteRecriaRepository.findByRecriaAndActivoTrue(recria).stream() ...
        int ventasPrevias = ventaPorcinoRepository.findByEmpresaAndActivoTrue(recria.getEmpresa()).stream() ...
        return cantidadInicial - muertes - ventasPrevias;
    }
```

**Código corregido:**

1) Añadir import y campo:

```java
import com.agrocloud.porcinos.application.RecriaStockService;
// ...
    @Autowired
    private RecriaStockService recriaStockService;
```

2) Eliminar la inyección de MuerteRecriaRepository solo si no se usa en otro método (en el código actual no se usa fuera de calcularAnimalesDisponibles). Se puede dejar inyectado por si otros métodos lo usan; si no, eliminarlo.

3) Reemplazar el uso de calcularAnimalesDisponibles por recriaStockService y validar cantidad >= 0 tras restar:

```java
            // VALIDACIÓN: Verificar animales disponibles (fórmula oficial: cantidadAnimales - muertes)
            int animalesDisponibles = recriaStockService.animalesDisponibles(recria);
            if (ventaData.getCantidad() > animalesDisponibles) {
                throw new IllegalArgumentException(
                    String.format("La cantidad a vender (%d) excede los animales disponibles (%d)",
                        ventaData.getCantidad(), animalesDisponibles));
            }

            // AUTOMATISMO: Descontar animales del lote/recría (spec: cantidadAnimales se actualiza en venta)
            int nuevaCantidad = recria.getCantidadAnimales() - ventaData.getCantidad();
            if (nuevaCantidad < 0) {
                throw new IllegalArgumentException("La cantidad resultante no puede ser negativa");
            }
            recria.setCantidadAnimales(nuevaCantidad);
```

4) Eliminar por completo el método privado `calcularAnimalesDisponibles` (todo el bloque desde `private int calcularAnimalesDisponibles` hasta el cierre de llave antes de `public List<VentaPorcino>`).

**Concurrencia:** Con @Version en Recria, al hacer `recriaRepository.save(recria)` JPA actualizará con WHERE version = X; si otra transacción modificó la fila, se lanzará OptimisticLockException. No hace falta cambiar el repository; el flujo ya es transaccional. Si se desea manejar explícitamente:

```java
// Opcional: capturar OptimisticLockException y relanzar con mensaje de dominio
return ventaPorcinoRepository.save(ventaData);
```

No es necesario findByIdWithLock si se usa @Version.

**Por qué cumple la spec:** Un solo lugar (RecriaStockService) para disponibles; no se restan ventas; cantidadAnimales se actualiza en venta; concurrencia vía @Version.

---

## 2.4 MuerteRecriaService: usar RecriaStockService y no modificar cantidadAnimales

**Archivo:** `agrogestion-backend/src/main/java/com/agrocloud/porcinos/application/MuerteRecriaService.java`

1) Añadir import y campo:

```java
import com.agrocloud.porcinos.application.RecriaStockService;
// ...
    @Autowired
    private RecriaStockService recriaStockService;
```

2) Sustituir la validación que usa calcularAnimalesDisponibles:

```java
        // REGLA DE NEGOCIO: disponibles = cantidadAnimales - Σ(muertes); no restar ventas
        int animalesDisponibles = recriaStockService.animalesDisponibles(recria);
        int nuevaCantidadDespuesMuerte = animalesDisponibles - muerteData.getCantidad();

        if (nuevaCantidadDespuesMuerte < 0) {
            throw new IllegalArgumentException(
                String.format("No se puede registrar la muerte: quedarían %d animales (stock negativo). " +
                    "Animales disponibles: %d, cantidad a registrar: %d",
                    nuevaCantidadDespuesMuerte, animalesDisponibles, muerteData.getCantidad()));
        }
```

3) Eliminar la inyección de VentaPorcinoRepository si no se usa en ningún otro método (en el código actual solo se usa en calcularAnimalesDisponibles).

4) Eliminar por completo el método privado `calcularAnimalesDisponibles`.

**Por qué cumple la spec:** Usa la fórmula oficial vía RecriaStockService; no modifica cantidadAnimales al registrar muerte; no resta ventas.

---

## 2.5 MovimientoEtapaService: usar RecriaStockService

**Archivo:** `agrogestion-backend/src/main/java/com/agrocloud/porcinos/application/MovimientoEtapaService.java`

1) Añadir:

```java
import com.agrocloud.porcinos.application.RecriaStockService;
// ...
    @Autowired
    private RecriaStockService recriaStockService;
```

2) Reemplazar la llamada y eliminar el método privado:

- Donde está:
  `int animalesDisponibles = calcularAnimalesDisponibles(recria);`
  sustituir por:
  `int animalesDisponibles = recriaStockService.animalesDisponibles(recria);`

- Eliminar el método privado `calcularAnimalesDisponibles(Recria recria)` completo (el que usa getMuertesRecria()).

**Por qué cumple la spec:** Un único componente de dominio para disponibles; no usa la colección de la entidad.

---

## 2.6 RecriaService: usar RecriaStockService en cierre y crearRecria origen

**Archivo:** `agrogestion-backend/src/main/java/com/agrocloud/porcinos/application/RecriaService.java`

1) Añadir:

```java
import com.agrocloud.porcinos.application.RecriaStockService;
// ...
    @Autowired
    private RecriaStockService recriaStockService;
```

2) En `cerrarRecria`, sustituir:
  `int animalesDisponibles = calcularAnimalesDisponibles(recria);`
  por:
  `int animalesDisponibles = recriaStockService.animalesDisponibles(recria);`

3) Eliminar el método privado `calcularAnimalesDisponibles(Recria recria)`.

4) En `crearRecria`, ya se hace `if (recriaData.getOrigen() == null) recriaData.setOrigen(Recria.OrigenRecria.EXTERNO);`. Una vez persistido el campo origen en la entidad, no hay cambio adicional; solo asegurar que la entidad Recria ya no tenga @Transient en origen (refactor 2.2).

**Por qué cumple la spec:** Cierre usa la fórmula oficial; origen EXTERNO por defecto en alta manual y se persiste.

---

## 2.7 DesteteService: validar parto.fechaFin >= fechaDestete

**Archivo:** `agrogestion-backend/src/main/java/com/agrocloud/porcinos/application/DesteteService.java`

En `registrarDestete`, donde se hace el automatismo de cerrar parto, reemplazar por:

```java
        // AUTOMATISMO: Cerrar parto si no está cerrado; si ya está cerrado, validar fecha
        if (parto.getFechaFin() == null && desteteData.getFechaDestete() != null) {
            parto.setFechaFin(desteteData.getFechaDestete().atTime(
                parto.getFechaInicio().getHour(), parto.getFechaInicio().getMinute()));
            partoRepository.save(parto);
        } else if (parto.getFechaFin() != null && desteteData.getFechaDestete() != null) {
            if (parto.getFechaFin().toLocalDate().isBefore(desteteData.getFechaDestete())) {
                throw new IllegalArgumentException(
                    "La fecha de fin del parto no puede ser anterior a la fecha del destete");
            }
        }
```

Origen DESTETE en la recría ya se setea; al persistir origen en Recria (refactor 2.2), se guardará solo con este cambio.

**Por qué cumple la spec:** parto.fechaFin >= fechaDestete validado; origen DESTETE persistido vía entidad corregida.

---

## 2.8 Parto: añadir gestacion_id; PartoService: setear gestacion

**Archivo:** `agrogestion-backend/src/main/java/com/agrocloud/porcinos/domain/Parto.java`

1) Añadir campo (por ejemplo tras `madre`):

```java
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "madre_id", nullable = false)
    @JsonIgnore
    private Madre madre;

    @Column(name = "gestacion_id")
    private Long gestacionId;

    @Column(name = "fecha_inicio", nullable = false)
```

2) Getter/setter:

```java
    public Long getGestacionId() { return gestacionId; }
    public void setGestacionId(Long gestacionId) { this.gestacionId = gestacionId; }
```

**Archivo:** `agrogestion-backend/src/main/java/com/agrocloud/porcinos/application/PartoService.java`

En `registrarParto`, justo antes de `Parto partoGuardado = partoRepository.save(partoData);`:

```java
        partoData.setGestacionId(gestacion.getId());
        Parto partoGuardado = partoRepository.save(partoData);
```

**Por qué cumple la spec:** Parto guarda la gestación usada en la validación; trazabilidad formal.

---

## 2.9 ServicioService: usar MadreService.actualizarEstadoMadre

**Archivo:** `agrogestion-backend/src/main/java/com/agrocloud/porcinos/application/ServicioService.java`

1) Asegurar inyección de MadreService (si no está):

```java
    @Autowired
    private MadreService madreService;
```

2) Reemplazar todas las llamadas a `actualizarEstadoMadre(madre, nuevoEstado, fecha, observacion)` por:

`madreService.actualizarEstadoMadre(madre, nuevoEstado, fecha, observacion);`

3) Eliminar por completo el método privado `actualizarEstadoMadre` (aproximadamente líneas 534–557).

**Por qué cumple la spec:** Un único punto de actualización de estado de madre; sin duplicado de lógica.

---

## 2.10 MadreMuerteService: actualizar madre al cerrar gestación

**Archivo:** `agrogestion-backend/src/main/java/com/agrocloud/porcinos/application/MadreMuerteService.java`

1) Inyectar MadreService si no está:

```java
    @Autowired
    private MadreService madreService;
```

2) En `cerrarGestacionActiva`, después de guardar la gestación, actualizar la madre a ADULTA en la misma transacción:

```java
    private void cerrarGestacionActiva(Madre madre, LocalDate fechaMuerte, String causa) {
        Optional<Gestacion> gestacionOpt = gestacionRepository.findActivaByMadre(madre);
        if (gestacionOpt.isPresent()) {
            Gestacion gestacion = gestacionOpt.get();
            gestacion.setEstado(Gestacion.EstadoGestacion.ABORTO);
            gestacion.setFechaAborto(fechaMuerte);
            gestacion.setCausaAborto("Muerte de la madre: " + causa);
            gestacionRepository.save(gestacion);
            madreService.actualizarEstadoMadre(madre, Madre.EstadoMadre.ADULTA,
                fechaMuerte, "Gestación cerrada por muerte de la madre: " + causa);
        }
    }
```

**Por qué cumple la spec:** Al cerrar gestación (ABORTO) se actualiza madre a ADULTA en la misma transacción.

---

# PASO 3 — Migraciones SQL (Flyway)

**Convención:** `V1_XXX__descripcion_corta.sql`. Se asume que la última migración existente es V1_127; nuevas a partir de V1_128.

---

## V1_128__Porcinos_Limpieza_Destetes_Duplicados_Y_Unique_Parto.sql

```sql
-- ============================================================================
-- Spec: Un destete por parto. Limpieza de duplicados y UNIQUE(parto_id).
-- Ejecutar en entorno con datos: revisar duplicados antes de añadir constraint.
-- ============================================================================

-- Diagnóstico (solo consulta; no modifica): partos con más de un destete activo
-- SELECT parto_id, COUNT(*) AS cnt FROM porcinos_destetes WHERE activo = 1 GROUP BY parto_id HAVING cnt > 1;

-- Para cada parto_id duplicado, dejar un destete (el de id mayor) y marcar el resto inactivos
UPDATE porcinos_destetes d1
INNER JOIN (
    SELECT parto_id, MAX(id) AS id_keep
    FROM porcinos_destetes
    WHERE activo = 1
    GROUP BY parto_id
    HAVING COUNT(*) > 1
) dup ON d1.parto_id = dup.parto_id AND d1.id <> dup.id_keep
SET d1.activo = 0;

-- Segunda pasada: si aún hubiera duplicados (mismo parto_id, varios activo=1), dejar solo uno
UPDATE porcinos_destetes d1
INNER JOIN (
    SELECT parto_id, MIN(id) AS id_keep
    FROM porcinos_destetes
    WHERE activo = 1
    GROUP BY parto_id
) one ON d1.parto_id = one.parto_id AND d1.id <> one.id_keep AND d1.activo = 1
SET d1.activo = 0;

-- Añadir constraint único
ALTER TABLE porcinos_destetes ADD CONSTRAINT uk_destetes_parto UNIQUE (parto_id);
```

---

## V1_129__Porcinos_Parto_Gestacion_Id_Recria_Check_Origen.sql

```sql
-- ============================================================================
-- Spec: Parto.gestacion_id, CHECK cantidad_animales >= 0, actualizar origen NULL.
-- ============================================================================

-- Parto: columna gestacion_id (nullable)
SET @dbname = DATABASE();
SET @tablename = 'porcinos_partos';
SET @columnname = 'gestacion_id';

SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
  'SELECT 1',
  'ALTER TABLE porcinos_partos ADD COLUMN gestacion_id BIGINT NULL AFTER madre_id'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- FK opcional (solo si la tabla porcinos_gestacion existe y no rompe datos)
-- ALTER TABLE porcinos_partos ADD CONSTRAINT fk_partos_gestacion FOREIGN KEY (gestacion_id) REFERENCES porcinos_gestacion(id) ON DELETE SET NULL;

-- Índice para consultas
SET @idx = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
   WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'porcinos_partos' AND INDEX_NAME = 'idx_partos_gestacion') > 0,
  'SELECT 1',
  'CREATE INDEX idx_partos_gestacion ON porcinos_partos(gestacion_id)'
));
PREPARE stmt FROM @idx;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Recría: corregir negativos antes del CHECK (invariante de spec)
UPDATE porcinos_recria SET cantidad_animales = 0 WHERE cantidad_animales < 0;
ALTER TABLE porcinos_recria ADD CONSTRAINT chk_recria_cantidad_animales CHECK (cantidad_animales >= 0);

-- Recría: actualizar origen NULL a EXTERNO (datos existentes)
UPDATE porcinos_recria SET origen = 'EXTERNO' WHERE origen IS NULL;
```

**Nota:** Si en tu motor `ALTER TABLE ... ADD CONSTRAINT chk_...` falla porque ya existe el constraint o por sintaxis (p. ej. MySQL 8.0 acepta CHECK), mantener solo la parte que aplique. Si `chk_recria_cantidad_animales` ya existe, omitir esa línea o usar un IF equivalente.

---

## V1_130__Porcinos_Recria_Version.sql

```sql
-- ============================================================================
-- Spec: Concurrencia optimista en Recria (campo version).
-- ============================================================================

SET @dbname = DATABASE();
SET @tablename = 'porcinos_recria';
SET @columnname = 'version';

SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
  'SELECT 1',
  'ALTER TABLE porcinos_recria ADD COLUMN version BIGINT NULL DEFAULT 0'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE porcinos_recria SET version = 0 WHERE version IS NULL;
```

---

# PASO 4 — Concurrencia en ventas

**Decisión:** Usar **@Version en Recria** (Opción A). Coherente con el stack JPA existente, sin métodos custom en el repository y con manejo estándar de OptimisticLockException.

- **Entidad:** Ya contemplada en refactor 2.2 (campo `version` con `@Version`).
- **Repository:** No requiere cambios; no se usa `findByIdWithLock`.
- **registrarVenta:** Flujo transaccional actual:
  1. `recriaRepository.findByIdAndActivoTrue(ventaData.getRecria().getId())` → carga Recria con version.
  2. `recriaStockService.animalesDisponibles(recria)` (solo lectura).
  3. Validar `ventaData.getCantidad() <= animalesDisponibles`.
  4. `recria.setCantidadAnimales(nuevaCantidad)` (JPA incrementa version en flush).
  5. `recriaRepository.save(recria)` → UPDATE ... SET cantidad_animales=..., version=version+1 WHERE id=? AND version=?.
  6. `ventaPorcinoRepository.save(ventaData)`.

Si otra transacción actualizó la misma recría, el UPDATE no afectará filas y JPA lanzará `OptimisticLockException`. Se puede capturar en el controller o en un `@ControllerAdvice` y devolver 409 Conflict con mensaje: "La recría fue modificada por otra operación; intente de nuevo."

Ejemplo en el servicio (opcional):

```java
    @Transactional
    public VentaPorcino registrarVenta(VentaPorcino ventaData, User user) {
        // ... validaciones y lógica existente ...
        try {
            recriaRepository.save(recria);
            ventaData.setRecria(recria);
            return ventaPorcinoRepository.save(ventaData);
        } catch (jakarta.persistence.OptimisticLockException e) {
            throw new IllegalStateException("La recría fue modificada por otra operación. Intente de nuevo.", e);
        }
    }
```

---

# PASO 5 — Eliminación de Faena como vía de escritura

## 5.1 FaenaController: POST devuelve 410 Gone

**Archivo:** `agrogestion-backend/src/main/java/com/agrocloud/controller/FaenaController.java`

**Código actual del endpoint POST:**

```java
    @PostMapping("/{recriaId}")
    public ResponseEntity<Faena> registrarFaena(
            @PathVariable Long recriaId,
            @RequestBody Faena faenaData,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            Faena faena = faenaService.registrarFaena(recriaId, faenaData, user);
            return ResponseEntity.ok(faena);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            ...
        }
    }
```

**Código corregido (410 Gone, sin llamar al servicio de escritura):**

```java
    @PostMapping("/{recriaId}")
    public ResponseEntity<?> registrarFaena(
            @PathVariable Long recriaId,
            @RequestBody Faena faenaData,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.GONE)
            .body(Map.of(
                "mensaje", "Este endpoint ha sido deshabilitado. Use POST /api/v1/porcinos/ventas con tipo FAENA para registrar faenas.",
                "endpointAlternativo", "/api/v1/porcinos/ventas",
                "tipoRequerido", "FAENA"
            ));
    }
```

**Import añadido:** `org.springframework.http.HttpStatus` y `java.util.Map`.

---

## 5.2 FaenaService.registrarFaena → UnsupportedOperationException

**Archivo:** `agrogestion-backend/src/main/java/com/agrocloud/porcinos/application/FaenaService.java`

**Reemplazar el cuerpo completo del método `registrarFaena`** por:

```java
    @Transactional
    public Faena registrarFaena(Long recriaId, Faena faenaData, User user) {
        throw new UnsupportedOperationException(
            "Registro de faenas unificado con VentaPorcino. Use VentaPorcinoService.registrarVenta() con tipo FAENA.");
    }
```

Se mantienen los métodos de solo lectura (obtenerFaenasPorRecria, obtenerTodasLasFaenas, obtenerIngresosTotales) para historial.

---

## 5.3 VentaPorcinoService y tipo FAENA

El código actual ya valida y procesa `ventaData.getTipo() == VentaPorcino.TipoVenta.FAENA` (peso envío, recría obligatoria, etc.). No requiere cambios adicionales para soportar FAENA.

---

# PASO 6 — Casos de prueba necesarios

| Caso | Escenario | Qué validar |
|-----|-----------|--------------|
| **Venta parcial múltiple** | Recría con N animales; registrar venta 1 de A, venta 2 de B, con A+B ≤ N. | Disponibles después de venta 1 = N - A - muertes; después de venta 2 = N - A - B - muertes; cantidadAnimales tras venta 2 = N - A - B; no se resta dos veces las ventas. |
| **Venta concurrente** | Dos hilos registran venta sobre la misma recría con stock 10; cada uno intenta vender 6. | Una venta tiene éxito (cantidadAnimales = 4); la otra falla por disponibles insuficientes o por OptimisticLockException; nunca cantidadAnimales &lt; 0 ni 12 vendidos. |
| **Muerte después de venta** | Recría: cantidadAnimales = 8 (ya con ventas descontadas); 2 muertes. Disponibles = 6. Registrar 1 muerte. | Éxito; disponibles pasan a 5; cantidadAnimales sigue 8 (no se modifica en muerte). |
| **Cierre recría con muertes** | Recría con cantidadAnimales = 5 y 5 muertes (disponibles = 0). | cerrarRecria permite cierre; si hay 1 muerte menos (disponibles = 1), cerrarRecria lanza con mensaje de animales vivos. |
| **Intento doble destete** | Parto con un destete ya registrado (activo). Intentar registrar otro destete para el mismo parto. | DesteteService lanza "Ya existe un destete registrado para este parto"; si se saltara la app, BD rechaza por UNIQUE(parto_id). |
| **Cierre gestación actualiza madre** | Registrar aborto en gestación activa; finalizar gestación sin parto; marcar servicio fallido con gestación; muerte de madre con gestación activa. | En todos los casos madre.estadoActual pasa a ADULTA (o el que corresponda) en la misma transacción que el cierre de gestación. |
| **Parto guarda gestacion_id** | Registrar parto para madre con gestación activa. | partoGuardado.getGestacionId() equals gestacion.getId(); en BD porcinos_partos.gestacion_id no null para ese parto. |
| **Origen persistido** | Crear recría desde destete; crear recría manual; leer recría por ID desde BD. | Recría desde destete tiene origen DESTETE y se persiste; recría manual tiene EXTERNO; al recargar entidad desde BD, getOrigen() devuelve el valor guardado. |
| **Faena POST 410** | POST /api/v1/porcinos/faena/{recriaId} con body válido. | Respuesta 410 Gone; cuerpo con mensaje y endpointAlternativo; no se inserta fila en porcinos_faena ni se modifica recria.cantidadAnimales. |
| **FaenaService.registrarFaena** | Llamada directa a faenaService.registrarFaena(...). | Lanza UnsupportedOperationException con mensaje indicando usar VentaPorcinoService. |

---

# PASO 7 — Plan de rollout en producción

## Orden de deploy

1. **Migraciones (en orden)**  
   - Ejecutar Flyway en este orden: V1_128 → V1_129 → V1_130.  
   - V1_128: limpieza destetes duplicados + UNIQUE(parto_id).  
   - V1_129: gestacion_id en partos, CHECK recría, UPDATE origen.  
   - V1_130: columna version en porcinos_recria.

2. **Despliegue de aplicación**  
   - Desplegar backend con: RecriaStockService; entidad Recria (origen persistido + @Version); Parto (gestacion_id); refactors en VentaPorcinoService, MuerteRecriaService, MovimientoEtapaService, RecriaService, DesteteService, PartoService, ServicioService, MadreMuerteService; FaenaController POST → 410; FaenaService.registrarFaena → UnsupportedOperationException.

## Orden recomendado de migraciones

- Primero V1_128 (evitar nuevos duplicados y garantizar integridad).  
- Luego V1_129 (gestacion_id, CHECK, datos origen).  
- Por último V1_130 (version para concurrencia).

## Riesgos

- **Destetes duplicados:** La UPDATE en V1_128 marca inactivos los duplicados; decidir si los registros inactivos se conservan para auditoría.  
- **CHECK cantidad_animales:** Si ya existen filas con cantidad_animales &lt; 0, la migración fallará; corregir datos antes o hacer UPDATE correctivo previo.  
- **Clientes que llaman POST faena:** Recibirán 410; el front debe usar POST /api/v1/porcinos/ventas con tipo FAENA (coordinar con front antes o en la misma ventana).

## Cómo validar en base real

- Consultar destetes por parto: `SELECT parto_id, COUNT(*) FROM porcinos_destetes WHERE activo = 1 GROUP BY parto_id HAVING COUNT(*) > 1` → debe devolver 0 filas.  
- Comprobar origen: `SELECT id, origen FROM porcinos_recria WHERE origen IS NULL` → 0 filas tras V1_129.  
- Partos nuevos: `SELECT id, gestacion_id FROM porcinos_partos ORDER BY id DESC LIMIT 10` → gestacion_id no null cuando el parto se registró con la app actualizada.  
- Recría: `SELECT id, cantidad_animales, version FROM porcinos_recria WHERE activo = 1 LIMIT 10` → version presente y coherente.

## Qué monitorear post-deploy

- Errores 410 en `/api/v1/porcinos/faena` (esperado si el front sigue llamando; confirmar migración de clientes a ventas con tipo FAENA).  
- Excepciones `OptimisticLockException` o mensaje "La recría fue modificada por otra operación" (indica concurrencia; comportamiento esperado).  
- Fallos de constraint en INSERT/UPDATE (UNIQUE destete, CHECK cantidad_animales) para detectar datos o flujos no contemplados.

---

**Documento de ejecución cerrado. Todas las decisiones siguen la spec sin alternativas ni rediseños.**
