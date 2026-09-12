# SPEC — Módulo Porcinos v2 (granja completa)

**Versión:** 2.0  
**Fecha:** Julio 2026  
**Metodología:** SDD  
**Estado:** Aprobada para implementación  
**Referencia modelo:** `SPEC-MODULO-FEEDLOT.md`, `SPEC-MODULO-AVICOLA-HUEVOS.md`  
**Relacionada con:** `SPEC-CAMPANA-TRANSVERSAL.md`, `SPEC-UBICACION-CLIMA-MODULO-PORCINOS.md`, `SPEC-EXPEDIENTE-TRAZABILIDAD-CICLO-VIDA.md`

---

## 1. Objetivo

Gestionar una **granja porcina completa**: plantel reproductivo (madres, padrillos), ciclo servicio → gestación → parto → destete, **lotes de engorde** batch post-destete, alimentación, sanidad, ventas/faena unificadas, KPIs y alertas, con **ubicación en mapa y clima** alineado a avícola/feedlot.

Reemplazar el módulo legacy (28 controllers, menú de 16 ítems) por un módulo **autocontenido, uniforme y operable de punta a punta**.

---

## 2. Glosario

| Término | Definición |
|---------|------------|
| **Establecimiento** | Unidad física de la granja con parámetros productivos y coordenadas GPS. |
| **Galpón** | Corral/galpón dentro del establecimiento; aloja madres o un lote de engorde. |
| **Madre** | Hembra reproductora con estado (CACHORRA, ADULTA, GESTACION, LACTANCIA, BAJA). |
| **Padrillo** | Macho reproductor. |
| **Servicio** | Evento de monta o inseminación artificial. |
| **Gestación** | Embarazo activo o finalizado de una madre. |
| **Parto** | Camada; vinculado obligatoriamente a una gestación. |
| **Destete** | Separación de lechones; crea automáticamente un **lote de engorde**. |
| **Lote de engorde** | Unidad batch post-destete o ingreso externo; análogo a `FeedlotLote`. |
| **Período de gestión** | `core_campanas`; agrupa operaciones del módulo. |
| **Faena** | Tipo de venta (`FAENA`); no entidad separada. |
| **GMD** | Ganancia media diaria en lote de engorde. |

---

## 3. Módulo y seguridad

| Concepto | Valor |
|----------|--------|
| Código `modules.code` | `PORCINOS` |
| `@RequiresModule` | `"PORCINOS"` |
| Origen inventario | `InventoryOrigin.PORCINOS` |
| API base | `/api/porcinos` |
| Paquete Java | `com.agrocloud.porcinos` (controller, service, model, repository) |
| Legacy | `com.agrocloud.porcinos.domain` → tablas `porcinos_legacy_*` |
| Frontend | `agrogestion-frontend/src/modules/porcinos` |
| Prefijo tablas v2 | `porcinos_*` |
| Nombre UI | **Porcinos** |

---

## 4. Menú v2 (9 ítems)

| Ítem | Ruta |
|------|------|
| Panel | `/porcinos/panel` |
| Calendario | `/porcinos/calendario` |
| Reproducción | `/porcinos/reproduccion` |
| Lotes de engorde | `/porcinos/lotes` |
| Dietas | `/porcinos/dietas` |
| Ventas | `/porcinos/ventas` |
| Sanidad | `/porcinos/sanidad` |
| Establecimientos | `/porcinos/establecimientos` (+ mapa) |
| Catálogos / Reportes / Períodos | rutas estándar feedlot |

---

## 5. Alcance v1

### Incluido

- Establecimientos con mapa y clima; galpones.
- Madres, padrillos, servicios, gestaciones, partos, destetes.
- Lotes engorde: CRUD, pesadas, consumos, muertes, sanidad, ventas, cierre.
- Dietas por fase; consumo manual.
- Panel KPIs y 4 reportes básicos.
- Migración datos desde `porcinos_legacy_*`.
- API legacy `/api/v1/porcinos/*` → `410 Gone` con mensaje de migración.

### Excluido (v1.5+)

- Wizard IA plan recría.
- Consumo diario automático (scheduler).
- Movimientos entre etapas con split de lotes.
- Closeout económico PDF (estructura preparada en reportes).
- Transferencias de lechones entre partos (v1.5).

---

## 6. Reglas de negocio clave

### Reproducción

| Regla | Detalle |
|-------|---------|
| RN-R01 | Una gestación activa (`EN_CURSO`) por madre (constraint BD). |
| RN-R02 | Cerrar gestación y actualizar estado madre en la misma transacción. |
| RN-R03 | Todo parto requiere `gestacion_id`. |
| RN-R04 | Un destete por parto (`UNIQUE parto_id`). |
| RN-R05 | Al registrar destete se crea lote engorde con origen `DESTETE`. |

### Lote engorde

| Regla | Detalle |
|-------|---------|
| RN-L01 | `cabezas_actuales` inicial = `cabezas_inicial`. |
| RN-L02 | Ventas descuentan `cabezas_actuales`. |
| RN-L03 | Muertes no modifican `cabezas_actuales`; disponibles = `cabezas_actuales - Σ(muertes)`. |
| RN-L04 | Estados: `ACTIVO` \| `CERRADO`. Sin escrituras en lote cerrado. |
| RN-L05 | Toda faena es `venta` tipo `FAENA`. |
| RN-L06 | `campana_id` de campaña activa al crear lote. |

### Inventario

| Regla | Detalle |
|-------|---------|
| RN-I01 | Consumos y sanidad con insumo descuentan inventario CORE origen `PORCINOS`. |

---

## 7. Entidades v2 (resumen)

Ver `DISENO-TECNICO-MODULO-PORCINOS-V2.md` para DDL completo.

- Catálogos: `porcinos_establecimiento`, `porcinos_galpon`, `porcinos_raza`, `porcinos_motivo_baja`, `porcinos_causa_mortalidad`, `porcinos_tipo_servicio`
- Reproducción: `porcinos_madre`, `porcinos_padrillo`, `porcinos_servicio`, `porcinos_gestacion`, `porcinos_parto`, `porcinos_destete`
- Engorde: `porcinos_lote`, `porcinos_pesada`, `porcinos_muerte`, `porcinos_consumo`, `porcinos_evento_sanitario`, `porcinos_venta`
- Alimentación: `porcinos_dieta`, `porcinos_dieta_fase`

---

## 8. Criterios de aceptación MVP

1. Operador configura establecimiento con mapa y ve clima en detalle de lote.
2. Ciclo completo: madre → servicio → gestación → parto → destete → lote auto-creado.
3. Lote: pesada, consumo, muerte, venta/faena, cierre.
4. Menú de 9 ítems sin rutas duplicadas ni stubs.
5. Datos demo migrables desde legacy.

---

## 9. Aprobación

| Rol | Estado | Fecha |
|-----|--------|-------|
| Producto | Aprobada | Julio 2026 |
| Implementación | En curso | Julio 2026 |
