# SPEC — Módulo Avícola (histórico / crianza)

**Versión:** 1.0  
**Fecha:** Mayo 2026  
**Metodología:** Spec Driven Development (SDD)  
**Estado:** **Superseded** — el dominio se dividió en dos módulos activables y SPECs dedicadas:

- **Crianza / parrilleros:** `docs/specs/SPEC-MODULO-AVICOLA-CRIANZA.md` — tablas `avicola_*`, código de módulo `AVICOLA_CRIANZA`, paquete Java `com.agrocloud.avicola.crianza`.
- **Producción de huevos:** `docs/specs/SPEC-MODULO-AVICOLA-HUEVOS.md` — tablas `avicola_huevo_*`, código `AVICOLA_HUEVOS`, paquete `com.agrocloud.avicola.huevos`.

**Origen inventario CORE:** usar `ModuloOrigenInventarioAvicola.CRIANZA` o `.HUEVOS` según el flujo.

**Ubicación en el repo:** `docs/specs/SPEC-MODULO-AVICOLA.md` (referencia; nuevas implementaciones deben seguir las SPECs hijas).

---

## 1. Objetivo

Incorporar el rubro **avícola** a AgroGestion como módulo activable por empresa, siguiendo el patrón del módulo porcinos. Permite gestionar lotes de aves, ciclos productivos, consumo de alimento desde el inventario compartido (CORE), eventos sanitarios, pesadas y ventas/faena.

---

## 2. Alcance v1

### Incluido

- **Establecimientos avícolas propios** (`avicola_establecimiento`): independientes del módulo cultivos; la empresa puede usar cultivos y avícola en el mismo predio físico o en distintos, modelados por separado.
- **Catálogo de razas por empresa** (`avicola_raza`), configurable; los lotes referencian una raza del catálogo.
- Alta y gestión de lotes de aves (`avicola_lote`)
- Registro de ingresos de aves (origen externo)
- Etapas productivas: pollito, recría, terminación (por peso/edad, sin estado formal en v1)
- Pesadas y seguimiento de ganancia de peso
- Muertes (registro sin descontar `cantidadAnimales`)
- Consumo diario de alimento (manual; scheduler nocturno en v2)
- Eventos sanitarios (vacunación, tratamiento, diagnóstico)
- Ventas y faena (descuenta `cantidadAnimales`; cierra lote al llegar a cero); cada venta con monto **genera registro de ingreso económico** en el flujo de ingresos del CORE (tabla `cultivo_ingresos` / `Ingreso` según exista en el proyecto), sin duplicar lógica de stock.
- KPIs básicos: conversión alimenticia, mortalidad acumulada, rendimiento estimado

### Excluido en v1 (planificado para v2)

- Postura / producción de huevos
- Reproductoras propias y ciclo reproductivo
- Scheduler nocturno de consumo automático (en **v2** se alimentará con **gramos/ave/día** como parámetro fijo; **no** recetas por etapa como porcinos)
- Recetas de alimentación por etapa
- Integración con balanza IoT

---

## 3. Reglas transversales aplicables

> Ver `docs/CORE-Y-MODULOS.md` para detalle completo.

| Regla | Aplicación en este módulo |
|-------|--------------------------|
| Inventario único | `avicola_consumo.insumo_id` → `insumo.id` (CORE). Consumos generan `MovimientoInventario` con `moduloOrigen = "AVICOLA"` |
| Prefijo de tablas | Todas las tablas propias llevan `avicola_` |
| Multiempresa | `empresa_id` en toda entidad raíz; `Service` lo obtiene del `SecurityContext` |
| Módulo activable | Todos los endpoints llevan `@RequiresModule("AVICOLA")` |
| Paquete Java | `com.agrocloud.avicola` |
| Frontend | `src/modules/avicola/` |

---

## 4. Entidades y tablas

### 4.0 `avicola_establecimiento`

Establecimiento de explotación avícola **ajeno al modelo de establecimiento/lote de cultivos**. Permite convivencia de módulos con distinta granularidad geográfica.

```sql
CREATE TABLE IF NOT EXISTS avicola_establecimiento (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  empresa_id    BIGINT NOT NULL,
  nombre        VARCHAR(150) NOT NULL,
  observaciones TEXT,
  activo        BOOLEAN NOT NULL DEFAULT TRUE,
  created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_avicola_establecimiento_empresa (empresa_id)
);
```

### 4.0b `avicola_raza`

Catálogo de razas/líneas **por empresa**. El lote referencia `raza_id`; la especie comercial puede alinearse con el enum o derivarse de atributos de la raza (definir en implementación si `avicola_raza` incluye columna `especie` acorde a `AvicolaEspecie`).

```sql
CREATE TABLE IF NOT EXISTS avicola_raza (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  empresa_id    BIGINT NOT NULL,
  nombre        VARCHAR(120) NOT NULL,
  activo        BOOLEAN NOT NULL DEFAULT TRUE,
  created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_avicola_raza_empresa (empresa_id)
);
```

### 4.1 `avicola_lote`

```sql
CREATE TABLE IF NOT EXISTS avicola_lote (
  id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
  empresa_id              BIGINT NOT NULL,
  establecimiento_id      BIGINT NOT NULL,             -- FK avicola_establecimiento
  raza_id                 BIGINT NOT NULL,             -- FK avicola_raza
  nombre                  VARCHAR(100) NOT NULL,
  especie                 VARCHAR(50) NOT NULL,        -- ver enum AvicolaEspecie (puede redundar con raza según modelo final)
  origen                  VARCHAR(30) NOT NULL DEFAULT 'EXTERNO',
  fecha_ingreso           DATE NOT NULL,
  cantidad_inicial        INT NOT NULL,
  cantidad_animales       INT NOT NULL,                -- actualizado por ventas/faena
  peso_promedio_ingreso   DECIMAL(8,3),
  estado                  VARCHAR(30) NOT NULL DEFAULT 'ACTIVO', -- ACTIVO | CERRADO
  fecha_salida            DATE,
  observaciones           TEXT,
  created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_avicola_lote_empresa (empresa_id),
  INDEX idx_avicola_lote_estado (empresa_id, estado),
  INDEX idx_avicola_lote_establecimiento (establecimiento_id),
  INDEX idx_avicola_lote_raza (raza_id)
);
```

**Enum `AvicolaEspecie`:** `POLLO_PARRILLERO`, `GALLINA_PONEDORA`, `PAVO`, `PATO`, `OTRO`  
**Enum `AvicolaLoteEstado`:** `ACTIVO`, `CERRADO`  
**Enum `AvicolaLoteOrigen`:** `EXTERNO`, `PROPIO`

### 4.2 `avicola_pesada`

```sql
CREATE TABLE IF NOT EXISTS avicola_pesada (
  id               BIGINT AUTO_INCREMENT PRIMARY KEY,
  lote_id          BIGINT NOT NULL,
  empresa_id       BIGINT NOT NULL,
  fecha            DATE NOT NULL,
  peso_promedio    DECIMAL(8,3) NOT NULL,
  cantidad_pesada  INT,
  observaciones    TEXT,
  created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_avicola_pesada_lote (lote_id, empresa_id)
);
```

### 4.3 `avicola_muerte`

```sql
CREATE TABLE IF NOT EXISTS avicola_muerte (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  lote_id       BIGINT NOT NULL,
  empresa_id    BIGINT NOT NULL,
  fecha         DATE NOT NULL,
  cantidad      INT NOT NULL,
  causa         VARCHAR(100),
  observaciones TEXT,
  created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_avicola_muerte_lote (lote_id, empresa_id)
);
```

> **Regla de negocio:** las muertes NO restan de `avicola_lote.cantidad_animales`.  
> El disponible real se calcula como: `cantidadAnimales - SUM(avicola_muerte.cantidad)`

### 4.4 `avicola_venta`

```sql
CREATE TABLE IF NOT EXISTS avicola_venta (
  id              BIGINT AUTO_INCREMENT PRIMARY KEY,
  lote_id         BIGINT NOT NULL,
  empresa_id      BIGINT NOT NULL,
  fecha           DATE NOT NULL,
  tipo            VARCHAR(30) NOT NULL,   -- FAENA | VENTA_EN_PIE | DESCARTE
  cantidad        INT NOT NULL,
  peso_promedio   DECIMAL(8,3),
  precio_unitario DECIMAL(12,2),
  total           DECIMAL(14,2),
  comprador       VARCHAR(150),
  observaciones   TEXT,
  ingreso_id      BIGINT,                  -- FK cultivo_ingresos / Ingreso (CORE), si se registró ingreso económico
  created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_avicola_venta_lote (lote_id, empresa_id)
);
```

> **Regla de negocio:** las ventas SÍ restan de `avicola_lote.cantidad_animales`.  
> Si `cantidad_animales` llega a 0 → `estado = CERRADO` y `fecha_salida = hoy`.

> **Ingreso económico:** si `total` (o reglas definidas en diseño técnico) aplica, el servicio debe crear un **`Ingreso`** en el CORE vía el servicio correspondiente (`IngresoService` u homólogo) y persistir `ingreso_id` en la venta para trazabilidad.

**Enum `AvicolaVentaTipo`:** `FAENA`, `VENTA_EN_PIE`, `DESCARTE`

### 4.5 `avicola_consumo`

```sql
CREATE TABLE IF NOT EXISTS avicola_consumo (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  lote_id       BIGINT NOT NULL,
  empresa_id    BIGINT NOT NULL,
  insumo_id     BIGINT NOT NULL,          -- FK a insumo (CORE compartido)
  fecha         DATE NOT NULL,
  cantidad      DECIMAL(12,3) NOT NULL,
  tipo          VARCHAR(20) NOT NULL DEFAULT 'MANUAL',  -- MANUAL | AUTOMATICO
  observaciones TEXT,
  created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_avicola_consumo_lote (lote_id, empresa_id),
  INDEX idx_avicola_consumo_insumo (insumo_id)
);
```

> Al registrar un consumo, el servicio debe llamar a `MovimientoInventarioService` (CORE) con:
> - `tipo = EGRESO`
> - `moduloOrigen = "AVICOLA"`
> - `insumoId` y `cantidad` del consumo

### 4.6 `avicola_evento_sanitario`

```sql
CREATE TABLE IF NOT EXISTS avicola_evento_sanitario (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  lote_id       BIGINT NOT NULL,
  empresa_id    BIGINT NOT NULL,
  fecha         DATE NOT NULL,
  tipo          VARCHAR(80) NOT NULL,     -- VACUNACION | TRATAMIENTO | DIAGNOSTICO | OTRO
  descripcion   TEXT,
  insumo_id     BIGINT,                   -- FK a insumo (CORE), si aplica
  dosis         DECIMAL(10,3),
  observaciones TEXT,
  created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_avicola_sanitario_lote (lote_id, empresa_id)
);
```

---

## 5. Integración con módulos CORE

```
avicola_consumo ──── insumo_id ────► insumo         (CORE)
                                          │
                                          ▼
                              MovimientoInventario    (CORE)
                              moduloOrigen = "AVICOLA"
                              tipo = EGRESO
```

```
avicola_venta (total > 0 según reglas) ────► Ingreso / cultivo_ingresos (CORE)
                      ingreso_id ◄────────── id generado
```

- El módulo avícola **nunca escribe directamente** en tablas del CORE (insumos, movimientos, ingresos): siempre a través de servicios de aplicación expuestos para eso.
- Toda operación sobre **inventario de insumos** pasa por `MovimientoInventarioService`.
- Los **ingresos económicos** por venta pasan por el servicio de ingresos del CORE (detalle de campos obligatorios en diseño técnico).
- Ver `docs/CORE-Y-MODULOS.md` §Insumos para detalles de la interfaz del servicio de movimientos.

---

## 6. Endpoints REST

**Base:** `/api/avicola`  
**Seguridad:** JWT + `@RequiresModule("AVICOLA")` en todos los endpoints

| Método | Ruta | Descripción | HTTP response |
|--------|------|-------------|---------------|
| GET | `/establecimientos` | Listar establecimientos avícolas de la empresa | 200 |
| POST | `/establecimientos` | Crear establecimiento avícola | 201 |
| GET | `/razas` | Listar razas configuradas | 200 |
| POST | `/razas` | Alta de raza en el catálogo | 201 |
| PUT | `/razas/{id}` | Editar raza (nombre, activo) | 200 / 404 |
| GET | `/lotes` | Listar lotes de la empresa | 200 |
| POST | `/lotes` | Crear lote | 201 |
| GET | `/lotes/{id}` | Detalle de lote | 200 / 404 |
| PUT | `/lotes/{id}` | Editar lote | 200 / 404 |
| POST | `/lotes/{id}/pesadas` | Registrar pesada | 201 |
| GET | `/lotes/{id}/pesadas` | Historial de pesadas | 200 |
| POST | `/lotes/{id}/muertes` | Registrar muerte | 201 |
| GET | `/lotes/{id}/muertes` | Historial de muertes | 200 |
| POST | `/lotes/{id}/ventas` | Registrar venta/faena | 201 |
| GET | `/lotes/{id}/ventas` | Historial de ventas | 200 |
| POST | `/lotes/{id}/consumos` | Registrar consumo | 201 |
| GET | `/lotes/{id}/consumos` | Historial de consumos | 200 |
| POST | `/lotes/{id}/eventos-sanitarios` | Registrar evento sanitario | 201 |
| GET | `/lotes/{id}/eventos-sanitarios` | Historial sanitario | 200 |
| GET | `/lotes/{id}/resumen` | KPIs del lote | 200 |

---

## 7. Lógica de negocio

### 7.1 Ciclo de vida del lote

```
ACTIVO
  │
  ├── registrarPesada()       → solo registra, no cambia estado
  ├── registrarMuerte()       → solo registra, NO resta cantidadAnimales
  ├── registrarConsumo()      → registra + MovimientoInventario(EGRESO)
  ├── registrarEventoSan()    → solo registra
  │
  └── registrarVenta()
        ├── resta cantidad de cantidadAnimales
        └── si cantidadAnimales == 0 → estado = CERRADO, fechaSalida = hoy
              │
              ▼
           CERRADO (terminal)
```

### 7.2 KPIs del resumen

| KPI | Fórmula |
|-----|---------|
| Cantidad disponible | `cantidadAnimales - SUM(muertes.cantidad)` |
| Mortalidad acumulada % | `SUM(muertes.cantidad) / cantidadInicial * 100` |
| Conversión alimenticia | `SUM(consumos.cantidad en kg) / (pesoPromedioActual * cantidadDisponible)` |
| Días en producción | `today - fechaIngreso` |

### 7.3 Validaciones

- No se puede registrar ninguna operación sobre un lote en estado `CERRADO`.
- `cantidad` en venta no puede superar la **cantidad disponible** del lote: `cantidadAnimales - SUM(muertes.cantidad)` (coherente con §7.2).
- `insumo_id` en consumos debe existir en la tabla `insumo` del CORE.
- `establecimiento_id` debe existir en `avicola_establecimiento` y pertenecer a la empresa.
- `raza_id` debe existir en `avicola_raza` y pertenecer a la empresa.

---

## 8. Pantallas frontend

| Ruta | Componente | Descripción |
|------|------------|-------------|
| `/avicola` | `AvicolaDashboard` | KPIs globales + listado rápido de lotes activos |
| `/avicola/lotes` | `AvicolaListado` | Tabla de lotes con filtros |
| `/avicola/establecimientos` | `AvicolaEstablecimientos` | ABM establecimientos avícolas |
| `/avicola/razas` | `AvicolaRazas` | ABM catálogo de razas |
| `/avicola/lotes/nuevo` | `AvicolaFormularioLote` | Formulario nuevo lote |
| `/avicola/lotes/:id` | `AvicolaDetalleLote` | Detalle + tabs |
| `/avicola/lotes/:id` tab Pesadas | `TabPesadas` | Historial + formulario pesada |
| `/avicola/lotes/:id` tab Muertes | `TabMuertes` | Historial + formulario muerte |
| `/avicola/lotes/:id` tab Consumos | `TabConsumos` | Historial + formulario consumo |
| `/avicola/lotes/:id` tab Sanidad | `TabSanidad` | Historial + formulario evento sanitario |
| `/avicola/lotes/:id` tab Ventas | `TabVentas` | Historial + formulario venta/faena |

---

## 9. Estructura de paquetes Java

```
com.agrocloud.avicola/
  controller/
    AvicolaEstablecimientoController.java
    AvicolaRazaController.java
    AvicolaLoteController.java
  service/
    AvicolaEstablecimientoService.java
    AvicolaRazaService.java
    AvicolaLoteService.java
    AvicolaOperacionesService.java   ← pesadas, muertes, ventas, consumos, sanitarios
  repository/
    AvicolaEstablecimientoRepository.java
    AvicolaRazaRepository.java
    AvicolaLoteRepository.java
    AvicolaPesadaRepository.java
    AvicolaMuerteRepository.java
    AvicolaVentaRepository.java
    AvicolaConsumoRepository.java
    AvicolaEventoSanitarioRepository.java
  model/
    entity/
      AvicolaEstablecimiento.java
      AvicolaRaza.java
      AvicolaLote.java
      AvicolaPesada.java
      AvicolaMuerte.java
      AvicolaVenta.java
      AvicolaConsumo.java
      AvicolaEventoSanitario.java
    dto/
      AvicolaLoteRequest.java
      AvicolaLoteResponse.java
      AvicolaPesadaRequest.java
      ... (un par request/response por entidad)
      AvicolaResumenResponse.java
    enums/
      AvicolaLoteEstado.java
      AvicolaEspecie.java
      AvicolaVentaTipo.java
      AvicolaConsumoTipo.java
```

---

## 10. Decisiones resueltas (negocio)

| Tema | Decisión |
|------|----------|
| **Establecimiento** | Modelo **propio** del módulo avícola (`avicola_establecimiento`). Es independiente del establecimiento/lote de cultivos: una empresa puede usar ambos módulos con establecimientos distintos o coincidencias solo a nivel operativo, no obligatoria FK cruzada. |
| **Consumo automático v2** | **Gramos/ave/día** como parámetro fijo. **No** se usarán recetas por etapa al estilo porcinos. |
| **Ventas e ingreso económico** | **Sí:** cada venta que corresponda debe generar **ingreso económico** en el flujo de ingresos del CORE (`Ingreso` / `cultivo_ingresos`), vía servicio de aplicación; ver `ingreso_id` en `avicola_venta` y §5. |
| **Razas** | **Catálogo configurable por empresa** (`avicola_raza`); el lote referencia `raza_id`. El enum `AvicolaEspecie` puede mantenerse en lote para clasificación comercial o alinearse con atributos de la raza (detalle en diseño técnico). |

---

## 11. Criterio de aceptación v1

- [ ] CRUD de `avicola_establecimiento` y `avicola_raza` por empresa, con uso obligatorio al crear lotes
- [ ] Alta y cierre de lote funcionando
- [ ] Registro de muertes sin descontar `cantidadAnimales`
- [ ] Registro de ventas descontando `cantidadAnimales`; lote se cierra al llegar a 0; tope de venta según cantidad disponible (§7.3)
- [ ] Ventas con monto aplicable generan **Ingreso** en CORE y quedan vinculadas (`ingreso_id`)
- [ ] Consumos descontando stock del inventario compartido con `moduloOrigen = "AVICOLA"`
- [ ] Todos los endpoints protegidos por `@RequiresModule("AVICOLA")` y empresa
- [ ] Dashboard con KPIs: cantidad disponible, mortalidad %, conversión alimenticia
- [ ] Módulo deshabilitado por defecto; activable desde configuración de empresa
- [ ] Sin referencias cruzadas a tablas de otros módulos fuera del CORE (salvo uso explícito de servicios/tablas CORE acordados: insumo, movimiento inventario, ingreso)
