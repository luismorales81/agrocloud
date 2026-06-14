# Suite de tests — Estado derivado del módulo agrícola

## Objetivo

Validar que el modelo de estado derivado sea consistente: el estado del lote se calcula exclusivamente desde `EstadoLoteCalculator` y la única escritura de `Plot.estado` ocurre en `EstadoLoteUpdater`. Orden de prioridad: **ABANDONADO → COSECHADO → DISPONIBLE → EN_COSECHA → LISTO_PARA_COSECHA → EN_FLORACION → EN_CRECIMIENTO → SEMBRADO**.

---

## Entregables

### 1. EstadoLoteCalculatorTest (unitarios)

**Ubicación:** `agrogestion-backend/src/test/java/com/agrocloud/cultivos/application/EstadoLoteCalculatorTest.java`

- **Sin Spring, sin BD.** Prueba solo `EstadoLoteCalculator.calcularEstado(Plot, List<Labor>, Optional<HistorialCosecha>)` con objetos construidos a mano.
- **No se mockea el Calculator:** es la clase bajo prueba.

| Test | Qué valida |
|------|------------|
| **1) Lote sin labores ni historial → DISPONIBLE** | `labores` vacía y `cosechaVigente` vacío → resultado DISPONIBLE. |
| **2) Labor SIEMBRA → SEMBRADO** | Una labor SIEMBRA activa y pocos días (o sin fecha siembra) → SEMBRADO. |
| **3) días >= 15 → EN_CRECIMIENTO** | Labor SIEMBRA activa + `fechaSiembra` = hoy − 20 días → EN_CRECIMIENTO. |
| **4) días >= 45 → EN_FLORACION** | Labor SIEMBRA activa + `fechaSiembra` = hoy − 50 días → EN_FLORACION. |
| **5) días >= 100 → LISTO_PARA_COSECHA** | Labor SIEMBRA activa + `fechaSiembra` = hoy − 110 días → LISTO_PARA_COSECHA. |
| **6) Cosecha vigente → COSECHADO** | `Optional.of(historial)` y sin abandono → COSECHADO. |
| **8) Labor ABANDONO → ABANDONADO** | Labor OTROS con descripción/observaciones "ABANDONO" activa → ABANDONADO. |
| **8b) ABANDONO prevalece sobre historial** | Misma labor abandono + cosecha vigente → ABANDONADO (prioridad 1). |
| **Labor COSECHA activa → EN_COSECHA** | Labor SIEMBRA + Labor COSECHA activas → EN_COSECHA. |
| **Labor cancelada/anulada no cuenta** | Labor SIEMBRA con estado CANCELADA o ANULADA → lote sin siembra activa → DISPONIBLE. |

---

### 2. EstadoDerivadoIntegracionTest (integración)

**Ubicación:** `agrogestion-backend/src/test/java/com/agrocloud/cultivos/application/EstadoDerivadoIntegracionTest.java`

- **@SpringBootTest** con **@ActiveProfiles("test")** y **@Transactional** (rollback por test).
- **Servicios reales:** `EstadoLoteUpdater`, `LaborRepository`, `PlotRepository`, `HistorialCosechaRepository`, `HistorialCosechaService`, `EstadoLoteService`. No se mockea `EstadoLoteCalculator` (se usa el real dentro del updater).
- **Setup:** En cada test se crean `User`, `Empresa`, `Field`, `Plot` (y cuando hace falta `Cultivo`, `Labor`, `HistorialCosecha`) y se persisten en H2.

| # | Test | Explicación breve |
|---|------|-------------------|
| **1** | Lote sin labores ni historial → DISPONIBLE | Lote recién creado; se llama `estadoLoteUpdater.recalcularEstado(lote.getId())`. Se verifica que el estado persistido sea DISPONIBLE. |
| **2** | Crear Labor SIEMBRA → SEMBRADO | Se persiste una Labor SIEMBRA activa y se actualiza `lote.fechaSiembra`. Tras `recalcularEstado`, el lote debe quedar en SEMBRADO. |
| **3** | Simular días >= 15 → EN_CRECIMIENTO | Lote con `fechaSiembra = hoy − 20` y Labor SIEMBRA activa. Tras recalcular, estado debe ser EN_CRECIMIENTO. |
| **4** | Simular días >= 45 → EN_FLORACION | Igual con `fechaSiembra = hoy − 50`. Estado esperado EN_FLORACION. |
| **5** | Simular días >= 100 → LISTO_PARA_COSECHA | Igual con `fechaSiembra = hoy − 110`. Estado esperado LISTO_PARA_COSECHA. |
| **6** | Ejecutar cosecha → COSECHADO | Se crea y persiste un `HistorialCosecha` para el lote (sin marcar liberado). Tras recalcular, estado debe ser COSECHADO. |
| **7** | Liberar lote → DISPONIBLE | Se marca `lote.liberadoParaSiembra = true` y se recalcula, o se llama `liberarLoteForzadamente`. Estado debe ser DISPONIBLE. |
| **8** | Labor ABANDONO → ABANDONADO | Se crea Labor OTROS con descripción de abandono. Tras recalcular, estado debe ser ABANDONADO. |
| **9** | Eliminar Labor SIEMBRA → DISPONIBLE | Se crea Labor SIEMBRA, se recalcula (SEMBRADO). Luego se marca la labor como CANCELADA e inactiva y se recalcula; estado debe pasar a DISPONIBLE. |
| **10** | Forzar SEMBRADO/COSECHADO manualmente → falla | Se llama `estadoLoteService.confirmarCambioEstado(confirmacion, usuario)` con `estadoPropuesto = SEMBRADO` o COSECHADO. Se espera `IllegalStateException` con mensaje que indique que esos estados son derivados y no pueden setearse manualmente. |

---

## Setup de datos (integración)

- **Perfil:** `application-test.properties` en `src/test/resources`: H2 en memoria, `spring.jpa.hibernate.ddl-auto=create-drop`, Flyway deshabilitado.
- **Dependencia:** `pom.xml`: `com.h2database:h2` con `scope=test`.
- **@BeforeEach:** Se crean y guardan en orden: `User`, `Empresa`, `Field`, `Plot`. Cada test que necesite `Cultivo`, `Labor` o `HistorialCosecha` los crea y persiste dentro del test.

---

## Requisitos cubiertos

- No se mockea `EstadoLoteCalculator`.
- Se usan servicios y repositorios reales en la suite de integración.
- Tests transaccionales: la clase de integración está anotada con `@Transactional` (rollback al finalizar cada test).
- No se modifica código productivo; solo se añaden tests y perfil/dependencia de test.

---

## Cómo ejecutar

```bash
cd agrogestion-backend
mvn test -Dtest=EstadoLoteCalculatorTest,EstadoDerivadoIntegracionTest
```

Para solo unitarios del calculator:

```bash
mvn test -Dtest=EstadoLoteCalculatorTest
```

Para solo integración (requiere H2 y perfil test):

```bash
mvn test -Dtest=EstadoDerivadoIntegracionTest
```
