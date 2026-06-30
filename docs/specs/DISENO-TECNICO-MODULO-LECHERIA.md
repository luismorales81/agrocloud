# Diseño técnico — Módulo Lechería

**Versión:** 1.0  
**Fecha:** Junio 2026  
**SPEC:** `SPEC-MODULO-LECHERIA.md`  
**Estado:** Aprobado para implementación

---

## 1. Migraciones Flyway

| Archivo | Contenido |
|---------|-----------|
| `V1_158__Modulo_lecheria.sql` | Módulo, tablas v1, seeds especies/parámetros |
| `V1_159__lecheria_v15.sql` | Tabla import control lechero, closeout rodeo |

---

## 2. Esquema principal (V1_158)

- `lecheria_establecimiento` — coordenadas TEXT, capacidad_animales
- `lecheria_rodeo` — FK establecimiento
- `lecheria_raza` — empresa_id, especie, nombre
- `lecheria_motivo_baja`
- `lecheria_parametro_especie` — especie PK, dias_gestacion, ordeñes_dia, umbral_rcs, dim_objetivo_secado
- `lecheria_animal` — identificacion, especie, raza_id, sexo, estado, rodeo_id, campana_id, fechas
- `lecheria_lactancia` — animal_id, numero, fecha_parto, fecha_secado, activa
- `lecheria_registro_ordene` — lactancia_id, animal_id, fecha, turno, litros, grasa_pct, proteina_pct, rcs, temp, humedad
- `lecheria_evento_reproductivo` — animal_id, tipo, fecha, resultado, toro_pajuela, observaciones
- `lecheria_evento_sanitario` — animal_id, tipo, fecha, insumo_id, cantidad, dias_retiro
- `lecheria_score_corporal` — animal_id, fecha, valor (1-5)
- `lecheria_consumo` — rodeo_id, campana_id, insumo_id, fecha, cantidad_kg
- `lecheria_venta_leche` — fecha, litros, precio_litro, comprador, campana_id
- `lecheria_baja_animal` — animal_id, fecha, motivo_id

---

## 3. API REST (`LecheriaController`)

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/panel/resumen` | KPIs + acciones |
| GET/POST/PUT | `/establecimientos` | CRUD establecimientos |
| GET/POST | `/establecimientos/{id}/rodeos` | Rodeos |
| GET/POST/PUT | `/catalogos/razas` | Razas por especie |
| GET/POST/PUT | `/animales` | Listado y alta |
| GET | `/animales/{id}` | Detalle con lactancia activa |
| GET/POST | `/animales/{id}/ordenes` | Ordeñes |
| GET/POST | `/animales/{id}/eventos-reproductivos` | Reproducción |
| GET/POST | `/animales/{id}/eventos-sanitarios` | Sanidad |
| GET/POST | `/animales/{id}/scores-corporales` | ECC |
| POST | `/animales/{id}/baja` | Baja |
| GET/POST | `/rodeos/{id}/consumos` | Consumos |
| GET/POST | `/ventas-leche` | Ventas |
| GET | `/reportes/curvas-lactancia` | Curvas por animal |
| GET | `/reportes/ranking-produccion` | Ranking |
| POST | `/import/control-lechero` | v1.5 CSV |
| GET | `/rodeos/{id}/closeout` | v1.5 económico |
| GET | `/reportes/clima-produccion` | v1.5 correlación |

---

## 4. Servicios Java

- `ServicioLecheriaCatalogos` — establecimientos, rodeos, razas
- `ServicioLecheriaAnimales` — CRUD animal, estados
- `ServicioLecheriaLactancia` — lactancias, DIM
- `ServicioLecheriaOrdene` — registros ordeñe
- `ServicioLecheriaReproduccion` — eventos reproductivos
- `ServicioLecheriaOperaciones` — sanidad, consumos, ventas, bajas
- `ServicioLecheriaPanel` — KPIs y acciones
- `ServicioLecheriaReportes` — curvas, ranking, clima-producción
- `ServicioLecheriaImportacion` — CSV v1.5
- `ServicioLecheriaCloseout` — v1.5

---

## 5. Frontend

```
modules/lecheria/
  index.ts, menu.ts, routes.tsx, types.ts
  services/lecheriaApi.ts
  screens/
    PanelLecheriaScreen.tsx
    AnimalesLecheriaScreen.tsx
    DetalleAnimalLecheriaScreen.tsx
    RegistroOrdeneScreen.tsx
    EstablecimientosLecheriaScreen.tsx
    UbicacionEstablecimientosLecheriaScreen.tsx
    CatalogosLecheriaScreen.tsx
    VentasLecheScreen.tsx
    ReportesLecheriaScreen.tsx
    ImportControlLecheroScreen.tsx  (v1.5)
```

Reutilizar: `EditorMapaUbicacion`, `PanelClimaEstablecimiento`, `BotonRellenarClima`.

---

## 6. Registro módulo

- `ModuloSistema.LECHERIA`
- `InventoryOrigin.LECHERIA`
- `ModuleUserController` → id `lecheria`
- `availableModules` + `ModuleId` type
