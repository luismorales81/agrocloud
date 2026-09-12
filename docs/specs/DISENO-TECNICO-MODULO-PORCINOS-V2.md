# Diseño técnico — Módulo Porcinos v2

**Versión:** 1.0  
**Fecha:** Julio 2026  
**SPEC:** `SPEC-MODULO-PORCINOS-V2.md`  
**Estado:** Aprobado para implementación

---

## 1. Estructura de paquetes

```
com.agrocloud.porcinos/
├── controller/PorcinosController.java      @RequestMapping("/api/porcinos")
├── service/
│   ├── ServicioPorcinosCatalogos.java
│   ├── ServicioPorcinosReproduccion.java
│   ├── ServicioPorcinosLotes.java
│   ├── ServicioPorcinosOperaciones.java
│   ├── ServicioPorcinosDietas.java
│   ├── ServicioPorcinosPanel.java
│   └── ServicioPorcinosReportes.java
├── model/
│   ├── entity/     PorcinosEstablecimiento, PorcinosLote, ...
│   ├── dto/        *Solicitud, *Respuesta
│   └── enums/      PorcinosLoteEstado, PorcinosMadreEstado, ...
└── repository/     JpaRepository por entidad

Legacy (solo lectura / deprecación):
com.agrocloud.porcinos.domain.*  → tablas porcinos_legacy_*
```

---

## 2. Migraciones Flyway

| Versión | Archivo | Acción |
|---------|---------|--------|
| V1_162 | `V1_162__Porcinos_renombrar_legacy.sql` | RENAME `porcinos_*` → `porcinos_legacy_*` |
| V1_163 | `V1_163__Porcinos_v2_esquema.sql` | CREATE tablas v2 |
| V1_164 | `V1_164__Porcinos_v2_migrar_datos.sql` | INSERT desde legacy (idempotente) |

---

## 3. Esquema DDL v2 (resumen)

### porcinos_establecimiento
`id`, `empresa_id`, `nombre`, `ubicacion`, `coordenadas` TEXT, `dias_gestacion` INT DEFAULT 114, `dias_lactancia` INT DEFAULT 21, `dias_entre_celos` INT DEFAULT 21, `faena_habilitada` BOOLEAN DEFAULT TRUE, `capacidad_cabezas` INT, `activo`, auditoría

### porcinos_galpon
`id`, `establecimiento_id`, `nombre`, `capacidad_cabezas`, `estado` (DISPONIBLE|OCUPADO|INACTIVO), `activo`

### porcinos_madre
`id`, `empresa_id`, `caravana`, `raza_id`, `galpon_id`, `estado`, `fecha_ingreso`, `fecha_nacimiento`, `activo`

### porcinos_padrillo
`id`, `empresa_id`, `nombre`, `raza_id`, `activo`

### porcinos_servicio
`id`, `madre_id`, `padrillo_id`, `tipo_servicio_id`, `fecha`, `observaciones`

### porcinos_gestacion
`id`, `madre_id`, `servicio_id`, `fecha_inicio`, `fecha_probable_parto`, `estado` (EN_CURSO|FINALIZADA|ABORTO), `activo`  
UNIQUE INDEX `idx_porcinos_gestacion_activa_madre` (madre_id) WHERE estado='EN_CURSO' AND activo=1 — en MySQL: índice único + validación app

### porcinos_parto
`id`, `gestacion_id` NOT NULL, `madre_id`, `fecha`, `nacidos_vivos`, `nacidos_muertos`, `momificados`, `temperatura_ambiente`, `humedad_ambiente`, `observaciones`

### porcinos_destete
`id`, `parto_id` UNIQUE, `fecha`, `cantidad_destetados`, `peso_promedio_kg`, `lote_id` (FK lote creado)

### porcinos_lote
`id`, `empresa_id`, `galpon_id`, `campana_id`, `nombre`, `origen` (DESTETE|EXTERNO), `destete_id`, `fecha_ingreso`, `fecha_cierre`, `cabezas_inicial`, `cabezas_actuales`, `peso_promedio_ingreso_kg`, `etapa` (RECRIA|ENGORDE), `estado` (ACTIVO|CERRADO)

### Operaciones hijas
`porcinos_pesada`, `porcinos_muerte`, `porcinos_consumo`, `porcinos_evento_sanitario`, `porcinos_venta` (tipo ENGORDE|REPRODUCTOR|FAENA)

### Dietas
`porcinos_dieta`, `porcinos_dieta_fase` (dieta_id, nombre_fase, dias_desde_ingreso, kg_cabeza_dia, insumo_id)

---

## 4. API REST `/api/porcinos`

### Catálogos
| Método | Path |
|--------|------|
| GET/POST | `/establecimientos` |
| PUT | `/establecimientos/{id}` |
| GET/POST | `/establecimientos/{estId}/galpones` |
| GET/POST | `/razas`, `/motivos-baja`, `/causas-mortalidad`, `/tipos-servicio` |

### Reproducción
| Método | Path |
|--------|------|
| GET/POST | `/madres` |
| GET/PUT | `/madres/{id}` |
| GET/POST | `/padrillos` |
| POST | `/madres/{id}/servicios` |
| POST | `/gestaciones/{id}/partos` |
| POST | `/partos/{id}/destetes` |
| GET | `/reproduccion/resumen` |

### Lotes
| Método | Path |
|--------|------|
| GET/POST | `/lotes` |
| GET/PUT | `/lotes/{id}` |
| POST | `/lotes/{id}/cierre` |

### Operaciones (anidadas en lote)
| Método | Path |
|--------|------|
| POST | `/lotes/{id}/pesadas` |
| POST | `/lotes/{id}/consumos` |
| POST | `/lotes/{id}/muertes` |
| POST | `/lotes/{id}/eventos-sanitarios` |
| POST | `/lotes/{id}/ventas` |

### Dietas, panel, reportes
| Método | Path |
|--------|------|
| GET/POST | `/dietas` |
| GET | `/panel/resumen` |
| GET | `/reportes/{tipo}` — productivo, reproductivo, alimentacion, economico |

---

## 5. Matriz migración legacy → v2

| Legacy | v2 | Notas |
|--------|-----|-------|
| `porcinos_legacy_parametros_establecimiento_porcinos` | `porcinos_establecimiento` | Consolidar parámetros en columnas |
| `porcinos_legacy_ubicaciones_internas` | `porcinos_galpon` | Aplanar jerarquía a un nivel |
| `porcinos_legacy_madres` | `porcinos_madre` | Mapear estados |
| `porcinos_legacy_padrillos` | `porcinos_padrillo` | |
| `porcinos_legacy_recria` | `porcinos_lote` | cantidad_animales → cabezas_actuales |
| `porcinos_legacy_registros_peso` | `porcinos_pesada` | |
| `porcinos_legacy_muertes_recria` | `porcinos_muerte` | |
| `porcinos_legacy_consumos_alimento` | `porcinos_consumo` | |
| `porcinos_legacy_ventas_porcinos` | `porcinos_venta` | Incluye FAENA |
| `porcinos_legacy_razas_porcinos` | `porcinos_raza` | |

---

## 6. Deprecación API legacy

Interceptor o anotación `@Deprecated` en controllers `com.agrocloud.controller.*Porcino*`:
- Respuesta HTTP 410 con body `{ "message": "Use /api/porcinos", "nuevaRuta": "..." }`

---

## 7. Frontend

- `porcinosApi.ts` — cliente único BASE `/porcinos`
- Reemplazar `menu.ts`, `routes.ts` con estructura feedlot
- Pantallas: `PanelPorcinosScreen`, `ReproduccionHubScreen`, `LotesPorcinosScreen`, `DetalleLotePorcinosScreen`, `UbicacionEstablecimientosPorcinosScreen`

---

## 8. Tests

`PorcinosControllerIntegracionTest`: ciclo madre → servicio → gestación → parto → destete → lote → venta → cierre.
