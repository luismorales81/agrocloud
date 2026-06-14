# Auditoría Spec-Driven Development (DDD + Clean Architecture) — Módulo Porcinos

**Versión:** 1.0  
**Alcance:** Dominio Porcinos (ciclo madre, parto, destete, recría, ventas, padrillos, alimentación).  
**Objetivo:** Errores estructurales, invariantes, agregados, máquinas de estado, restricciones BD, bugs ocultos y plan de refactorización por fases.

---

# 1. Lista exhaustiva de errores estructurales

## 1.1 Críticos

---

**Error 1 — Doble descuento de ventas en cálculo de animales disponibles**

| Campo | Detalle |
|-------|---------|
| **Severidad** | Crítico |
| **Ubicación** | `VentaPorcinoService.calcularAnimalesDisponibles`, `MuerteRecriaService.calcularAnimalesDisponibles` |
| **Problema** | Se calcula `disponibles = cantidadAnimales - muertes - ventasPrevias`, pero en `registrarVenta` también se hace `recria.setCantidadAnimales(cantidadAnimales - cantidadVendida)`. Tras la primera venta, `cantidadAnimales` ya está reducida; al volver a calcular, `ventasPrevias` incluye esa venta → se resta dos veces. |
| **Riesgo real** | A partir de la segunda venta/faena sobre la misma recría, "animales disponibles" queda en 0 o negativo y el sistema rechaza ventas válidas o permite cerrar recría con "0 disponibles" cuando en realidad aún hay stock. |
| **Propuesta** | **Fuente única de verdad:** `cantidadAnimales` = cantidad actual tras ventas/movimientos. Definir `disponibles = cantidadAnimales - Σ(muertes)` y no restar ventas (ya están reflejadas en `cantidadAnimales`). En `VentaPorcinoService` y `MuerteRecriaService` reemplazar `calcularAnimalesDisponibles` por esta fórmula y eliminar `ventasPrevias`. |
| **Impacto** | Refactor de 2 servicios; pruebas de regresión sobre ventas parciales y cierre de recría. |

---

**Error 2 — Recria.origen no persistido**

| Campo | Detalle |
|-------|---------|
| **Severidad** | Crítico |
| **Ubicación** | Entidad `Recria` (campo `origen` @Transient), BD `porcinos_recria.origen` (V1_120) |
| **Problema** | La columna `origen` existe en BD pero la entidad declara el campo como `@Transient`. El valor DESTETE/EXTERNO no se persiste ni se recupera. |
| **Riesgo real** | Recrías creadas por destete pierden el origen al recargar; reportes y filtros por origen son incorrectos; migraciones futuras pueden asumir datos que no existen. |
| **Propuesta** | Mapear `origen` en la entidad: `@Enumerated(EnumType.STRING) @Column(name = "origen", length = 20)` y eliminar `@Transient`. Asegurar que `DesteteService.crearRecriaDesdeDestete` y `RecriaService.crearRecria` setean origen antes de `save`. Migración de datos: actualizar filas existentes con `origen` NULL según heurística (recría con lote vinculable a parto/destete → DESTETE, resto → EXTERNO) o dejar NULL y documentar. |
| **Impacto** | Cambio en entidad + migración de datos opcional; sin cambio de esquema BD. |

---

**Error 3 — Inconsistencia Faena vs VentaPorcino**

| Campo | Detalle |
|-------|---------|
| **Severidad** | Crítico |
| **Ubicación** | `FaenaService`, `VentaPorcinoService`, entidades `Faena` y `VentaPorcino` |
| **Problema** | V1_109 unificó faena en VentaPorcino (tipo FAENA), pero existe `FaenaService` y repositorio `FaenaRepository` que calculan disponibles como `cantidadInicial - muertes - faenasPrevias` (sin tocar `cantidadAnimales`). Si se usan ambos caminos (Faena antigua y VentaPorcino tipo FAENA), la misma recría tiene dos fuentes de bajas y dos políticas de actualización de cantidad. |
| **Riesgo real** | Doble registro de faenas, doble descuento o descuento en un solo lado; números de recría incoherentes. |
| **Propuesta** | Decisión explícita: (A) Deprecar `Faena` y `FaenaService` y que toda faena sea `VentaPorcino` tipo FAENA; o (B) Mantener solo Faena y migrar UI/API a Faena. Recomendado (A). Un solo cálculo de disponibles (ver Error 1) y una sola entidad de salida (VentaPorcino). |
| **Impacto** | Migración de datos si hay filas en `porcinos_faena`; refactor de controllers y front; posible eliminación de `FaenaService` y entidad `Faena`. |

---

**Error 4 — Madre en GESTACION sin gestación activa**

| Campo | Detalle |
|-------|---------|
| **Severidad** | Crítico |
| **Ubicación** | Entidad `Madre` (estado), `GestacionRepository`, ausencia de FK Madre→Gestacion activa |
| **Problema** | El estado `Madre.estadoActual` puede quedar en GESTACION si: (1) se elimina o desactiva la gestación y no se actualiza la madre; (2) error en transacción; (3) actualización manual de BD. No hay restricción en BD que impida madre.estado_actual = 'GESTACION' sin fila activa en `porcinos_gestacion`. |
| **Riesgo real** | Reportes y dashboards cuentan “madres gestantes” incorrectos; validación “madre debe estar en GESTACION para parto” puede fallar o permitir parto sin gestación; consumo diario asigna receta GESTACION a madres “fantasma”. |
| **Propuesta** | (1) Invariante de aplicación: al cerrar/eliminar gestación (ABORTO, FINALIZADA, activo=false), siempre actualizar madre a ADULTA en la misma transacción. (2) Job o query de consistencia: detectar madres con estado GESTACION sin gestación activa y corregir a ADULTA (o alertar). (3) A largo plazo: considerar trigger o check que exija existencia de gestación activa cuando estado_actual = 'GESTACION' (complejidad alta en SQL). |
| **Impacto** | Revisión de todos los puntos que cierran/desactivan gestación; opcional: job de saneamiento y documentación de invariante. |

---

**Error 5 — Un solo destete por parto no garantizado en BD**

| Campo | Detalle |
|-------|---------|
| **Severidad** | Crítico |
| **Ubicación** | Tabla `porcinos_destetes` (parto_id), sin UNIQUE(parto_id) |
| **Problema** | La regla “un parto tiene como máximo un destete” se valida solo en `DesteteService`. Cualquier inserción directa o otro servicio podría crear un segundo destete para el mismo parto. |
| **Riesgo real** | Doble creación de recrías para el mismo parto; incoherencia en “destete por parto” en reportes; doble descuento de lechones. |
| **Propuesta** | Añadir constraint único en BD: `UNIQUE(parto_id)` en `porcinos_destetes` (o `porcinos_destetes(parto_id)` donde activo=1 si se usa soft delete). Migración: antes de crear el índice, eliminar duplicados si los hubiera (quedarse con uno por parto). |
| **Impacto** | Migración Flyway con ADD UNIQUE; verificación de duplicados previa. |

---

## 1.2 Altos

---

**Error 6 — Una sola gestación activa por madre no garantizada en BD**

| Campo | Detalle |
|-------|---------|
| **Severidad** | Alto |
| **Ubicación** | Tabla `porcinos_gestacion`, sin índice/constraint que impida dos filas activas por madre |
| **Problema** | La aplicación evita crear dos gestaciones activas para la misma madre, pero la BD no lo impide. Un bug o acceso directo podría insertar una segunda gestación EN_CURSO para la misma madre. |
| **Riesgo real** | Cálculo de “gestación activa” ambiguo (cuál se usa); recordatorios duplicados; consumo diario podría tomar la incorrecta. |
| **Propuesta** | Índice único parcial: en MySQL 8.0 `CREATE UNIQUE INDEX idx_una_gestacion_activa_por_madre ON porcinos_gestacion (madre_id) WHERE estado = 'EN_CURSO' AND activo = 1` (o equivalente). Si el motor no soporta índices parciales, mantener la regla en servicio y añadir constraint de aplicación estricta + test de integración. |
| **Impacto** | Migración con índice único parcial (si soportado) o documentación + tests. |

---

**Error 7 — Parto sin referencia a Gestación**

| Campo | Detalle |
|-------|---------|
| **Severidad** | Alto |
| **Ubicación** | Entidad `Parto` (no tiene gestacion_id), `PartoService` |
| **Problema** | Parto se asocia solo a Madre. La relación Parto–Gestación se resuelve por lógica (última gestación activa o finalizada de la madre). No hay FK Parto → Gestacion ni invariante explícito “todo parto proviene de una gestación”. |
| **Riesgo real** | No se puede auditar qué gestación generó qué parto; si hay varias gestaciones finalizadas, la “asociación” es heurística; posibles partos huérfanos de gestación. |
| **Propuesta** | Añadir `gestacion_id` (nullable al inicio) en `Parto` y setearlo en `PartoService.registrarParto` con la gestación usada en la validación. Migración: añadir columna, opcionalmente rellenar por madre + fecha. No hacer FK NOT NULL hasta tener datos consistentes. |
| **Impacto** | Nueva columna en `porcinos_partos`; actualización de PartoService y DTOs. |

---

**Error 8 — Transferencia de lechones sin ajuste numérico**

| Campo | Detalle |
|-------|---------|
| **Severidad** | Alto |
| **Ubicación** | `TransferenciaLechon`, `TransferenciaLechonService` |
| **Problema** | Se registra cantidad de lechones transferidos de parto A a parto B, pero no se actualiza `Parto.nacidosVivos` (ni ningún contador) en origen ni destino. Los totales de parto dejan de ser consistentes con las transferencias. |
| **Riesgo real** | Nacidos vivos del parto origen no reflejan “los que se fueron”; parto destino no refleja “adoptados”; reportes y validaciones (ej. destete ≤ nacidos vivos) usan números incorrectos. |
| **Propuesta** | Definir invariante: “nacidosVivos = cantidad que permanece en la madre” o “nacidosVivos - transferidos_out + transferidos_in = cantidad actual”. Opción (A): mantener nacidosVivos como “nacidos en el parto” y añadir campos o tablas “transferidos desde este parto” / “transferidos a este parto” y que destete valide contra “nacidosVivos - transferidos_out”. Opción (B): descontar en parto origen y sumar en destino (requiere campos adicionales o tabla de detalle). Documentar la regla y aplicar en servicio. |
| **Impacto** | Refactor de modelo (campos o tablas) y de TransferenciaLechonService; validación en DesteteService. |

---

**Error 9 — MuerteRecria no actualiza cantidadAnimales**

| Campo | Detalle |
|-------|---------|
| **Severidad** | Alto |
| **Ubicación** | `MuerteRecriaService`, entidad `Recria` |
| **Problema** | Al registrar muerte en recría no se hace `recria.setCantidadAnimales(cantidad - muerte)`. “Disponibles” se calcula como cantidadAnimales - Σ(muertes) - …; por tanto la semántica es “cantidadAnimales = cabezas iniciales (o tras ventas/movimientos), muertes se restan solo en el cálculo”. Esto es coherente con “no tocar cantidadAnimales en muerte”, pero entonces VentaPorcino tampoco debería tocar cantidadAnimales y todo debería ser “cantidad inicial - muertes - ventas”. Hoy hay mezcla: ventas sí modifican cantidadAnimales, muertes no. |
| **Riesgo real** | Dos interpretaciones de `cantidadAnimales`: (1) “stock actual” → entonces muertes deberían descontar; (2) “stock inicial/tras movimientos” → entonces ventas no deberían descontar. La mezcla actual genera el doble descuento de ventas (Error 1). |
| **Propuesta** | Unificar política (ver invariantes Recría más abajo): **Opción recomendada:** `cantidadAnimales` = cantidad actual (se actualiza en venta y en movimiento; en muerte no, y disponibles = cantidadAnimales - Σ(muertes)). Coherente con Error 1. Dejar MuerteRecria solo como registro; no modificar cantidadAnimales. Asegurar que en todos los cálculos de disponibles se use la misma fórmula. |
| **Impacto** | Documentación de invariante y alinear VentaPorcinoService/MuerteRecriaService/RecriaService/MovimientoEtapaService a una sola definición. |

---

**Error 10 — Confirmación de destete y cierre de parto en borde**

| Campo | Detalle |
|-------|---------|
| **Severidad** | Alto |
| **Ubicación** | `DesteteService.registrarDestete` |
| **Problema** | Si el parto ya tiene `fechaFin` no null, no se actualiza al registrar destete. Si por algún motivo se registró un parto “cerrado” antes del destete, el destete se asocia al parto pero la fecha de cierre del parto puede ser anterior a la fecha del destete. No hay validación “fechaFin parto >= fechaDestete”. |
| **Riesgo real** | Inconsistencia temporal; reportes por fecha pueden mostrar parto cerrado antes del destete. |
| **Propuesta** | Al registrar destete, si parto.fechaFin != null, validar parto.fechaFin >= fechaDestete (o igual). Si parto.fechaFin es anterior, actualizar parto.fechaFin = fechaDestete (o rechazar). Dejar documentado que “el parto se cierra a más tardar en la fecha del destete”. |
| **Impacto** | Cambio en DesteteService (validación + posible actualización de fechaFin). |

---

## 1.3 Medios

---

**Error 11 — Estados de Madre no formalizados como máquina**

| Campo | Detalle |
|-------|---------|
| **Severidad** | Medio |
| **Ubicación** | Dominio Madre, varios servicios que llaman a `actualizarEstadoMadre` |
| **Problema** | Las transiciones (CACHORRA↔ADULTA, ADULTA→GESTACION→LACTANCIA→ADULTA, →DESCARTE) están dispersas en ServicioService, PartoService, DesteteService, GestacionService, MadreMuerteService. No hay un único lugar que defina las transiciones permitidas; una modificación futura podría introducir una transición inválida. |
| **Riesgo real** | Transiciones inconsistentes (ej. LACTANCIA→GESTACION sin destete); difícil onboarding y testing. |
| **Propuesta** | Introducir Máquina de Estados formal (ver sección 4): enum de transiciones permitidas o dominio “MadreEstadoMachine” que reciba (estadoActual, evento) y devuelva nuevoEstado o error. Todos los servicios delegan en él. |
| **Impacto** | Nuevo componente de dominio; refactor de llamadas a actualizarEstadoMadre. |

---

**Error 12 — Concurrencia en venta sobre la misma recría**

| Campo | Detalle |
|-------|---------|
| **Severidad** | Medio |
| **Ubicación** | `VentaPorcinoService.registrarVenta` |
| **Problema** | Dos requests simultáneos pueden leer la misma recría con cantidadAnimales = 10, ambos calculan disponibles = 10, ambos registran venta de 6 y actualizan a 4. Resultado: 12 vendidos y cantidadAnimales = 4 (o 10 - 6 = 4 en el último commit). |
| **Riesgo real** | Venta “en exceso” respecto al stock real; cantidadAnimales negativa o incoherente. |
| **Propuesta** | Bloqueo pesimista: `recriaRepository.findByIdWithLock(recriaId)` (SELECT FOR UPDATE) dentro de la transacción antes de calcular disponibles y actualizar. O versión optimista: campo `version` en Recria y actualizar con WHERE version = X; si no actualiza filas, reintentar o fallar. |
| **Impacto** | Cambio en repositorio (lock o @Version) y en servicio; pruebas de concurrencia. |

---

**Error 13 — Identificación de Madre/Padrillo unique sin scope empresa**

| Campo | Detalle |
|-------|---------|
| **Ubicación** | Entidad Madre, Padrillo (`identificacion` unique = true) |
| **Problema** | Si la BD tiene UNIQUE(identificacion) sin (empresa_id), dos empresas no podrían tener la misma identificación (ej. “M-001”). Si el unique es (empresa_id, identificacion), está bien. Revisar esquema real. |
| **Riesgo real** | Conflictos entre empresas o imposibilidad de multi-tenant por identificación. |
| **Propuesta** | Asegurar UNIQUE(empresa_id, identificacion) en porcinos_madres y porcinos_padrillos. Si hoy es solo UNIQUE(identificacion), migrar a UNIQUE(empresa_id, identificacion). |
| **Impacto** | Migración de índices; posible migración de datos si hay duplicados inter-empresa. |

---

**Error 14 — Parto abierto por madre no único en BD**

| Campo | Detalle |
|-------|---------|
| **Severidad** | Medio |
| **Ubicación** | Tabla porcinos_partos |
| **Problema** | La regla “una madre tiene como máximo un parto abierto (sin fechaFin)” se valida en PartoService; la BD no lo impide. |
| **Riesgo real** | Inserción directa o bug podría crear dos partos abiertos para la misma madre. |
| **Propuesta** | Índice único parcial: UNIQUE(madre_id) WHERE fecha_fin IS NULL (si el motor lo permite). Alternativa: job de consistencia y tests estrictos. |
| **Impacto** | Migración con índice parcial o reforzar aplicación. |

---

## 1.4 Bajos

---

**Error 15 — CACHORRA por edad con configuración por usuario**

| Campo | Detalle |
|-------|---------|
| **Severidad** | Bajo |
| **Ubicación** | `MadreService.calcularEstadoSegunEdad`, `ConfiguracionPorcinoService.obtenerValorInteger("DIAS_CACHORRA", 160, user)` |
| **Problema** | DIAS_CACHORRA se obtiene por usuario; debería ser por empresa para consistencia. |
| **Riesgo real** | Dos usuarios de la misma empresa con configuraciones distintas podrían ver estados distintos para la misma madre. |
| **Propuesta** | Obtener DIAS_CACHORRA por empresa (clave + empresa_id) o por configuración global del módulo. |
| **Impacto** | Cambio en ConfiguracionPorcino (scope empresa) y en MadreService. |

---

**Error 16 — Duplicación de lógica actualizarEstadoMadre**

| Campo | Detalle |
|-------|---------|
| **Severidad** | Bajo |
| **Ubicación** | MadreService.actualizarEstadoMadre vs ServicioService.actualizarEstadoMadre (método privado duplicado) |
| **Problema** | ServicioService tiene su propia copia de actualizarEstadoMadre en lugar de usar MadreService. Cualquier cambio en la regla de historial debe repetirse. |
| **Riesgo real** | Comportamiento divergente si se modifica uno y no el otro. |
| **Propuesta** | Eliminar el método privado de ServicioService y usar siempre MadreService.actualizarEstadoMadre. |
| **Impacto** | Refactor de ServicioService (inyección de MadreService si no está). |

---

# 2. Invariantes de dominio formales

## 2.1 Madre

**Invariantes de estado**

- `estadoActual` ∈ { CACHORRA, ADULTA, GESTACION, LACTANCIA, RECRIA, DESCARTE }.
- Si `estadoActual = GESTACION` entonces existe al menos una fila en `porcinos_gestacion` con `madre_id = id` y `estado = 'EN_CURSO'` y `activo = 1`.
- Si `estadoActual = LACTANCIA` entonces existe al menos un parto con `madre_id = id`, `activo = 1` y `fecha_fin IS NULL` (parto abierto).
- Si `estadoActual = DESCARTE` entonces `activo = false` y típicamente `fecha_baja` no nula.
- `estadoActual` solo puede cambiar mediante eventos definidos (servicio, control celo, parto, destete, aborto, muerte).

**Transiciones válidas**

- CACHORRA → ADULTA (por edad ≥ DIAS_CACHORRA).
- ADULTA → GESTACION (preñez confirmada en control de celo o gestación creada).
- GESTACION → LACTANCIA (parto registrado).
- GESTACION → ADULTA (aborto o servicio fallido).
- LACTANCIA → ADULTA (destete registrado).
- Cualquier estado → DESCARTE (muerte/baja de madre).

**Transiciones prohibidas**

- GESTACION → LACTANCIA sin parto registrado.
- LACTANCIA → GESTACION sin destete previo.
- ADULTA → LACTANCIA.
- CACHORRA → GESTACION (debe pasar por ADULTA).
- DESCARTE → cualquier otro estado.

**Reglas que deberían estar garantizadas en BD**

- No se puede eliminar físicamente una madre con gestación activa, parto abierto o servicio pendiente (o la BD debe tener ON DELETE RESTRICT en FKs desde gestacion/parto/servicio a madre).
- Un único registro de historial abierto (fecha_fin IS NULL) por madre por estado (o un único “actual”); depende del modelo de HistorialEstadoMadre.

---

## 2.2 Parto

**Invariantes numéricas**

- `nacidosVivos + nacidosMuertos + momias = totalNacidos`.
- `nacidosVivos >= 0`, `nacidosMuertos >= 0`, `momias >= 0`, `totalNacidos > 0`.
- Para cualquier destete asociado: `cantidad_destetados <= nacidosVivos` (del parto).

**Restricciones de unicidad**

- Por cada parto (id) existe como máximo un destete activo (parto_id → único en destetes con activo=1). En BD: UNIQUE(parto_id) o UNIQUE(parto_id, activo) según diseño.
- Por cada madre, como máximo un parto con `fecha_fin IS NULL` (parto abierto).

**Otras**

- `fecha_inicio` no nula; `fecha_fin` opcional hasta el destete (o cierre manual).
- Si existe destete, `fecha_fin` del parto debe ser >= fecha_destete (o igual).

---

## 2.3 Destete

**Restricciones obligatorias**

- Un solo destete activo por parto.
- `cantidad_destetados <= parto.nacidosVivos`.
- `peso_promedio_destete` dentro de rangos configurados (paramétrico).
- Parto pertenece a la misma empresa que el usuario.

**Reglas de consistencia con parto**

- Al registrar destete, si parto.fechaFin es null, setear parto.fechaFin = fechaDestete (o equivalente).
- La recría creada tiene cantidadAnimales = cantidad_destetados y fechaIngreso = fecha_destete; origen = DESTETE.

---

## 2.4 Recría

**Definición formal de cantidadAnimales**

- **Definición adoptada (recomendada):** `cantidadAnimales` = cantidad actual de cabezas en el lote en el momento del último evento que la modifica (venta, movimiento de etapa). No se disminuye al registrar muerte; las muertes se restan solo en el cálculo de “animales disponibles”.
- **Alternativa (no recomendada con el código actual):** cantidadAnimales = cantidad inicial fija; entonces ventas y muertes no deberían modificar cantidadAnimales y disponibles = cantidadAnimales - Σ(muertes) - Σ(ventas). Esto obligaría a no actualizar cantidadAnimales en VentaPorcino y MovimientoEtapa.

**Regla unificada de “animales disponibles”**

- `animales_disponibles(recria) = recria.cantidadAnimales - Σ(muerteRecria.cantidad WHERE recria_id = recria.id AND activo = 1)`.
- No restar ventas ni faenas porque ya están incorporadas en `cantidadAnimales` (cada venta y cada movimiento resta de cantidadAnimales).

**Política clara muertes vs ventas**

- **Muertes:** solo se registran en MuerteRecria; no modifican cantidadAnimales. Reducen “disponibles”.
- **Ventas/faenas:** actualizan cantidadAnimales (restan) y registran VentaPorcino. Reducen “disponibles” porque reducen cantidadAnimales.
- **Movimiento de etapa:** resta de recría origen y suma en destino (o crea nueva recría); ambos lados actualizan cantidadAnimales.

---

## 2.5 Venta (VentaPorcino)

**Validaciones obligatorias**

- cantidad > 0; pesoPromedio > 0.
- Si tipo ≠ FAENA: precioKg > 0.
- Si tipo = FAENA: recría obligatoria; establecimiento con realizaFaena = true; pesoEnvio > 0.
- animales_disponibles(recria) >= cantidad antes de registrar.

**Restricciones transaccionales**

- Actualización de Recria.cantidadAnimales y creación de VentaPorcino en la misma transacción.
- Bloqueo o versión optimista sobre Recria para evitar doble venta (ver Error 12).

---

# 3. Agregados correctos

## 3.1 ¿Madre es Aggregate Root?

**Decisión: Sí.**

- **Justificación:** Madre es la entidad que agrupa el ciclo reproductivo: Servicio, Gestacion, Parto y Destete son “hijos” de Madre en el sentido de que su existencia y validez dependen de la madre y de las reglas de estado de la madre. Las identidades de Servicio, Gestacion, Parto y Destete se crean y usan en el contexto de una Madre. El invariante “una sola gestación activa por madre” y “un solo parto abierto por madre” son invariantes del agregado Madre. Por tanto, Madre es la raíz; Servicio, Gestacion, Parto, Destete (y HistorialEstadoMadre) son entidades o value objects dentro del mismo agregado. Las operaciones que cambian estado (registrar servicio, control celo, parto, destete, aborto, muerte) deben respetar la raíz y no permitir estados inválidos.

**Precisión:** Destete crea Recría, que es otro agregado. La “salida” del agregado Madre es la creación de una Recría (por aplicación), no por referencia desde Madre. Por tanto el límite del agregado Madre termina en Parto/Destete; la recría se crea en una transacción que puede ser la misma que el destete, pero Recría es raíz de su propio agregado.

---

## 3.2 ¿Parto debería depender estrictamente de Gestación?

**Decisión: Sí, a nivel de invariante y de trazabilidad.**

- **Justificación:** En el dominio, un parto es la consecuencia de una gestación. Hoy Parto no tiene FK a Gestacion; la asociación es por madre + fecha. Para consistencia y auditoría, Parto debería tener `gestacion_id` (nullable en transición) y la aplicación debe garantizar que todo parto registrado tenga una gestación asociada (la que se usó para validar “madre en GESTACION”). No implica que Parto “pertenezca” al agregado Gestacion; Parto pertenece al agregado Madre, y Gestacion también. La dependencia es “todo Parto debe referenciar la Gestacion que lo originó”, no “Parto es hijo de Gestacion”. Por tanto: misma raíz (Madre), con referencia Parto → Gestacion para trazabilidad e invariantes.

---

## 3.3 ¿Recría es un agregado independiente?

**Decisión: Sí.**

- **Justificación:** Recría representa un lote de animales en una etapa; sus ciclos de vida (pesos, muertes, movimientos, ventas) se gestionan en torno a la recría. Madre no necesita conocer las recrías que “generó” (vía destete) para sus invariantes; la recría tiene su propio ciclo y sus propias reglas (cantidad, disponibles, cierre). Por tanto Recría es Aggregate Root independiente. MuerteRecria, RegistroPeso, MovimientoEtapa y VentaPorcino son entidades/eventos que pertenecen al agregado Recría en el sentido de que modifican o consultan el estado de la recría y deben respetar “animales disponibles” y “cantidadAnimales”.

---

## 3.4 ¿Venta debería estar dentro del agregado Recría?

**Decisión: Sí (VentaPorcino como entidad dentro del agregado Recría).**

- **Justificación:** Una venta/faena es un evento que modifica el estado de la recría (cantidadAnimales, posiblemente fechaSalida y destino). Para mantener el invariante “cantidadAnimales y ventas coherentes” y evitar doble descuento y condiciones de carrera, la operación “registrar venta” debe ser un caso de uso del agregado Recría: la raíz es Recria; VentaPorcino es una entidad o un evento de dominio dentro del mismo agregado. El repositorio podría ser RecriaRepository (y ventas como colección o como tabla con FK a recria); o VentaPorcinoRepository pero con transacción que bloquee Recria y actualice Recria + inserte VentaPorcino. Lo importante es una única transacción y una única fuente de verdad para “disponibles”.

---

# 4. Máquina de estados formal

## 4.1 Madre

**Estados válidos:** CACHORRA, ADULTA, GESTACION, LACTANCIA, RECRIA, DESCARTE.

**Eventos que disparan transición**

| Evento | Estado origen | Estado destino |
|--------|----------------|----------------|
| Edad alcanza DIAS_CACHORRA | CACHORRA | ADULTA |
| Servicio registrado | ADULTA | ADULTA (confirmación en historial) |
| Preñez confirmada (control celo) | ADULTA | GESTACION |
| Servicio fallido / Aborto | GESTACION | ADULTA |
| Parto registrado | GESTACION | LACTANCIA |
| Destete registrado | LACTANCIA | ADULTA |
| Muerte/baja madre | Cualquiera | DESCARTE |

**Transiciones inválidas que hoy el sistema podría permitir (por falta de máquina centralizada)**

- LACTANCIA → GESTACION sin destete (si alguien llamara actualizarEstadoMadre directamente).
- GESTACION → LACTANCIA sin parto (idem).
- CACHORRA → GESTACION (la validación de servicio exige no CACHORRA para servicio? Revisar: validarMadreParaServicio no exige ADULTA explícitamente; si CACHORRA puede recibir servicio, entonces sí podría pasar a GESTACION desde CACHORRA vía servicio + preñez. Definir si CACHORRA puede recibir servicio o no.)

**Propuesta de implementación**

- Clase o enum `MadreEstadoMachine`: método `transicion(EstadoMadre actual, EventoMadre evento, ...) returns EstadoMadre` que lanza si la transición no está permitida. Todos los servicios que hoy llaman a `actualizarEstadoMadre` en su lugar llaman a `MadreEstadoMachine.transicion(...)` y luego actualizan madre e historial.

---

## 4.2 Gestación

**Estados válidos:** EN_CURSO, ABORTO, FINALIZADA.

**Eventos**

| Evento | Estado origen | Estado destino |
|--------|----------------|----------------|
| Parto registrado | EN_CURSO | FINALIZADA |
| Aborto registrado / Servicio fallido | EN_CURSO | ABORTO |
| (No hay evento que salga de ABORTO o FINALIZADA) |

**Transiciones inválidas que hoy podría permitir**

- Registrar parto para una gestación ya FINALIZADA o ABORTO (la aplicación lo evita buscando gestación activa; si se pasara gestación_id incorrecto, podría haber incoherencia).
- Dos gestaciones EN_CURSO para la misma madre (evitado en aplicación, no en BD).

---

## 4.3 Recría

Recría no tiene “estado” explícito en el modelo (no hay enum EstadoRecria). Tiene `activo`, `fechaSalida`, `destino` y `etapa` (F1…TERMINACION). Una máquina de estados útil sería:

**Estados conceptuales:** ABIERTA (activo=true, fechaSalida=null), CERRADA (fechaSalida no null, destino definido).

**Eventos:** Cierre con destino (VENTA, FUTURA_MADRE, ENGORDE); venta total; movimiento total. No es obligatorio modelar una máquina compleja para Recría si con “ABIERTA/CERRADA” y reglas de negocio (no cerrar con disponibles > 0) basta. Opcional: formalizar “RecriaAbierta” vs “RecriaCerrada” como estados y un único evento “Cerrar(destino)” que valide disponibles = 0.

---

# 5. Restricciones de base de datos

## 5.1 Unique constraints

- **porcinos_destetes:** `UNIQUE(parto_id)` (o único por parto_id donde activo=1). Garantiza un destete por parto.
- **porcinos_gestacion:** Índice único parcial `(madre_id)` WHERE estado='EN_CURSO' AND activo=1. Una gestación activa por madre. *Nota: MySQL no soporta UNIQUE parcial hasta 8.0.13+ (expresiones); en versiones anteriores garantizar solo por aplicación o con trigger.*
- **porcinos_partos:** Índice único parcial `(madre_id)` WHERE fecha_fin IS NULL. Un parto abierto por madre. *Misma nota MySQL.*
- **porcinos_madres / porcinos_padrillos:** `UNIQUE(empresa_id, identificacion)` (ver Error 13).

## 5.2 Foreign keys restrictivas

- **porcinos_destetes.parto_id** → porcinos_partos(id) ON DELETE RESTRICT (no permitir borrar parto con destete).
- **porcinos_partos.madre_id** → porcinos_madres(id) ON DELETE RESTRICT.
- **porcinos_gestacion.madre_id** → porcinos_madres(id) ON DELETE RESTRICT.
- **porcinos_consumos_diarios_automaticos.recria_id** → porcinos_recria(id) ON DELETE SET NULL o RESTRICT según regla de negocio.

## 5.3 Check constraints

- **porcinos_partos:** `nacidos_vivos >= 0 AND nacidos_muertos >= 0 AND momias >= 0 AND total_nacidos = nacidos_vivos + nacidos_muertos + momias AND total_nacidos > 0`.
- **porcinos_destetes:** `cantidad_destetados > 0` (y a nivel aplicación: <= parto.nacidosVivos).
- **porcinos_recria:** `cantidad_animales >= 0`.
- **porcinos_ventas_porcinos:** `cantidad > 0`, `peso_promedio > 0`.

## 5.4 Índices recomendados

- porcinos_gestacion(madre_id, estado, activo) para “gestación activa por madre”.
- porcinos_partos(madre_id, fecha_fin) para “parto abierto por madre”.
- porcinos_destetes(parto_id) único.
- porcinos_muertes_recria(recria_id), porcinos_ventas_porcinos(recria_id) para cálculos de disponibles.
- porcinos_historial_estados_madres(madre_id, fecha_fin) para historial abierto.

---

# 6. Bugs potenciales ocultos

## 6.1 Doble descuento de ventas en recría

**Confirmado.** Ver Error 1. Tras la primera venta, `cantidadAnimales` se reduce pero `calcularAnimalesDisponibles` en VentaPorcinoService y MuerteRecriaService sigue restando `ventasPrevias`, por lo que disponibles queda 0 o negativo y bloquea ventas legítimas.

---

## 6.2 Transferencia de lechones sin ajuste numérico

**Confirmado.** Ver Error 8. Los nacidos vivos del parto no se actualizan; destete valida contra un total que no considera transferencias.

---

## 6.3 Recría.origen @Transient vs columna real

**Confirmado.** Ver Error 2. La columna existe; el valor no se persiste ni se lee.

---

## 6.4 Madre en GESTACION sin gestación activa

**Confirmado.** Ver Error 4. Posible por borrado/desactivación de gestación sin actualizar madre, o por inconsistencia previa.

---

## 6.5 Venta concurrente sobre misma recría

**Confirmado.** Ver Error 12. Sin bloqueo ni versión optimista, dos ventas simultáneas pueden exceder el stock.

---

## 6.6 Confirmación de destete sin cerrar parto

**Parcial.** El código cierra el parto si `parto.getFechaFin() == null` al registrar destete. El riesgo es si el parto llegara con fechaFin ya setada de forma incorrecta (anterior al destete); no hay validación fuerte de que fechaFin >= fechaDestete.

---

## 6.7 FaenaService vs VentaPorcino (faena)

**Confirmado.** Ver Error 3. Dos vías para “faena” y dos formas de calcular disponibles; FaenaService no actualiza cantidadAnimales. Riesgo de doble registro o números incoherentes.

---

## 6.8 RecriaService.calcularAnimalesDisponibles ignora ventas

RecriaService no resta ventas (“asumimos que las ventas ya descontaron de cantidadAnimales”). Eso es coherente con la política recomendada; el bug está solo en VentaPorcinoService y MuerteRecriaService que sí restan ventasPrevias. No es un bug adicional, pero refuerza la necesidad de unificar la fórmula en un solo lugar.

---

# 7. Plan de refactorización por fases

## Fase 1 — Correcciones críticas sin romper modelo

**Objetivo:** Eliminar bugs críticos y alinear política de “animales disponibles” sin cambiar agregados ni modelo de dominio.

| Tarea | Riesgo | Complejidad | Dependencias |
|-------|--------|-------------|--------------|
| Unificar cálculo de animales disponibles: definir `disponibles = cantidadAnimales - Σ(muertes)` y usarlo en VentaPorcinoService y MuerteRecriaService; eliminar ventasPrevias de ambos. | Bajo | Media | Ninguna |
| Mapear Recria.origen en entidad (quitar @Transient, @Column) y asegurar seteo en DesteteService y RecriaService. | Bajo | Baja | Ninguna |
| Decidir y aplicar deprecación de Faena/FaenaService o migración total a VentaPorcino tipo FAENA; un solo camino para faena. | Medio | Alta | Migración de datos si hay faenas en tabla antigua |
| Añadir UNIQUE(parto_id) en porcinos_destetes (con limpieza de duplicados si existen). | Bajo | Baja | Revisión de datos |
| Asegurar que al cerrar/desactivar gestación siempre se actualice madre a ADULTA en la misma transacción; revisar GestacionService, ServicioService. | Bajo | Media | Ninguna |
| Validar en DesteteService: si parto.fechaFin != null, exigir parto.fechaFin >= fechaDestete; si no, setear parto.fechaFin = fechaDestete. | Bajo | Baja | Ninguna |

**Entregables:** Código estable; documento de “política de cantidadAnimales y disponibles”; migración(es) Flyway si aplica.

---

## Fase 2 — Formalización de invariantes

**Objetivo:** Invariantes documentados y garantizados en código; restricciones BD donde sea posible.

| Tarea | Riesgo | Complejidad | Dependencias |
|-------|--------|-------------|--------------|
| Implementar máquina de estados de Madre (clase/enum de transiciones) y que todos los servicios la usen para cambiar estado. | Medio | Media | Fase 1 estable |
| Añadir Parto.gestacion_id (nullable), setearlo en PartoService; opcional rellenar históricos. | Bajo | Media | Ninguna |
| Añadir check constraints en BD (parto: total = vivos+muertos+momias; destete: cantidad > 0; recria: cantidad_animales >= 0). | Bajo | Baja | Fase 1 |
| Índice único parcial “una gestación activa por madre” (si el motor lo soporta). | Bajo | Baja | Ninguna |
| Índice único parcial “un parto abierto por madre” (si el motor lo soporta). | Bajo | Baja | Ninguna |
| Documento de invariantes (Madre, Parto, Destete, Recría, Venta) y ubicación en código donde se garantizan. | Bajo | Baja | Ninguna |

**Entregables:** Invariantes documentados; máquina de estados Madre; restricciones BD; Parto.gestacion_id.

---

## Fase 3 — Reorganización de agregados

**Objetivo:** Límites de agregados claros; venta como operación del agregado Recría; concurrencia controlada.

| Tarea | Riesgo | Complejidad | Dependencias |
|-------|--------|-------------|--------------|
| Introducir bloqueo pesimista o versión optimista en Recria para registrarVenta (y opcionalmente movimiento y muerte). | Medio | Media | Fase 1 |
| Refactor: “Registrar venta” como caso de uso que recibe RecriaRepository (o aplicación que carga Recria con lock), actualiza cantidadAnimales e inserta VentaPorcino en la misma transacción. | Medio | Media | Fase 1, 2 |
| Definir explícitamente agregados en documentación (Madre + Servicio + Gestacion + Parto + Destete; Recria + MuerteRecria + RegistroPeso + MovimientoEtapa + VentaPorcino) y revisar repositorios y transacciones. | Bajo | Media | Fase 2 |
| Eliminar duplicación de actualizarEstadoMadre (ServicioService debe usar MadreService). | Bajo | Baja | Fase 2 |

**Entregables:** Concurrencia segura en ventas; documentación de agregados; código alineado con agregados.

---

## Fase 4 — Limpieza arquitectónica

**Objetivo:** Desacoplar módulos; políticas en un solo lugar; opcional puerto para inventario/grano.

| Tarea | Riesgo | Complejidad | Dependencias |
|-------|--------|-------------|--------------|
| Extraer “calcularAnimalesDisponibles(Recria)” a un único componente (servicio de dominio o RecriaRepository/helper) y usarlo desde VentaPorcinoService, MuerteRecriaService, MovimientoEtapaService, RecriaService. | Bajo | Media | Fase 1, 2 |
| UNIQUE(empresa_id, identificacion) en madres y padrillos si aún no está. | Bajo | Baja | Revisión de datos |
| Definir y aplicar regla para transferencias de lechones (ajuste numérico en partos o campos “transferidos out/in”) y validación en destete. | Medio | Alta | Fase 2 |
| Revisar dependencia Porcinos → Cultivos (ConsumoDiarioAutomaticoService, ReportesPorcinoService) y proponer puerto en core para “grano” si se desea desacoplar. | Bajo | Alta | No bloqueante |

**Entregables:** Código sin duplicación de reglas críticas; transferencias con regla clara; opcionalmente menos acoplamiento a Cultivos.

---

# Resumen ejecutivo

- **Críticos (5):** Doble descuento ventas en disponibles, Recria.origen no persistido, inconsistencia Faena vs VentaPorcino, madre GESTACION sin gestación activa, un destete por parto no garantizado en BD.
- **Altos (5):** Una gestación activa por madre en BD, parto sin gestacion_id, transferencias sin ajuste numérico, política muertes vs ventas unificada, cierre de parto en destete.
- **Medios/Bajos (4):** Máquina de estados formal, concurrencia en venta, unique por empresa en madre/padrillo, parto abierto único por madre.
- **Invariantes:** Madre (estados y transiciones), Parto (numéricas y unicidad), Destete (uno por parto, ≤ nacidos vivos), Recría (cantidadAnimales = actual; disponibles = cantidadAnimales - muertes), Venta (misma transacción y bloqueo).
- **Agregados:** Madre (AR con Servicio, Gestacion, Parto, Destete); Recría (AR con MuerteRecria, RegistroPeso, MovimientoEtapa, VentaPorcino); Venta dentro del agregado Recría.
- **Plan en 4 fases:** Fase 1 correcciones críticas; Fase 2 invariantes y BD; Fase 3 agregados y concurrencia; Fase 4 limpieza y desacople.

Este documento sirve como **auditoría técnica**, **lista priorizada de correcciones** y **base para tareas en Notion** (cada ítem de las secciones 1–6 y cada tarea de la sección 7 puede convertirse en una tarea con criterios de aceptación y dependencias).
