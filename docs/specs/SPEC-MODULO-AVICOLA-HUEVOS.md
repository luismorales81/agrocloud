# SPEC — Módulo Avícola Producción de huevos

**Versión:** 1.0  
**Fecha:** Mayo 2026  
**Metodología:** SDD  
**Estado:** Implementación base en curso (entidades/repos bajo `com.agrocloud.avicola.huevos`)

---

## 1. Objetivo

Gestionar **postura y producción de huevos** en explotaciones independientes de la crianza: establecimientos y razas propios, lotes de puesta, **registro diario de huevos** (una fila por lote y fecha), consumo de insumos (CORE) y sanidad.

---

## 2. Módulo y seguridad

| Concepto | Valor |
|----------|--------|
| Código en `modules.code` | `AVICOLA_HUEVOS` |
| `@RequiresModule` | `"AVICOLA_HUEVOS"` |
| `moduloOrigen` inventario | `ModuloOrigenInventarioAvicola.HUEVOS` (`"AVICOLA_HUEVOS"`) |

---

## 3. Tablas y código

- Prefijo: `avicola_huevo_*` — `avicola_huevo_establecimiento`, `avicola_huevo_raza`, `avicola_huevo_lote`, `avicola_huevo_produccion_diaria` (único por `lote_id` + `fecha`), `avicola_huevo_consumo`, `avicola_huevo_evento_sanitario`.
- Flyway: `V1_137__avicola_modulos_crianza_y_huevos.sql` (crea tablas huevo y actualiza catálogo `modules`).
- Paquetes: `com.agrocloud.avicola.huevos.model.*`, `com.agrocloud.avicola.huevos.repository`.

---

## 4. Alcance v1 (actual)

- Lotes con `cantidad_aves_inicial` / `cantidad_aves_actual` y estado `ACTIVO` | `CERRADO`.
- Producción diaria agregada por día (sin clasificación por calibre en v1).
- Sin FK a tablas `avicola_*` de crianza.

## 5. Excluido (v2+)

- Clasificación comercial de huevos (tamaños), integración balanza, múltiples naves por lote con detalle fino.
