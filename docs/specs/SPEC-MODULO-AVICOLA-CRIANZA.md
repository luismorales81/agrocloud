# SPEC — Módulo Avícola Crianza (parrilleros)

**Versión:** 1.0  
**Fecha:** Mayo 2026  
**Metodología:** SDD  
**Estado:** Implementación base en curso (entidades/repos bajo `com.agrocloud.avicola.crianza`)

---

## 1. Objetivo

Gestionar **crianza** (pollos parrilleros y equivalentes): lotes, establecimientos y razas propios del módulo, pesadas, muertes, ventas/faena, consumo de insumos desde el inventario CORE y sanidad.

---

## 2. Módulo y seguridad

| Concepto | Valor |
|----------|--------|
| Código en `modules.code` | `AVICOLA_CRIANZA` (migración `V1_137` renombra `AVICOLA` si existía) |
| `@RequiresModule` | `"AVICOLA_CRIANZA"` |
| `moduloOrigen` inventario | `ModuloOrigenInventarioAvicola.CRIANZA` (`"AVICOLA_CRIANZA"`) |

---

## 3. Tablas y código

- Prefijo tablas: `avicola_` (sin subprefijo): `avicola_establecimiento`, `avicola_raza`, `avicola_lote`, `avicola_pesada`, `avicola_muerte`, `avicola_venta`, `avicola_consumo`, `avicola_evento_sanitario`.
- Flyway inicial: `V1_136__avicola_initial.sql`.
- Paquetes Java: `com.agrocloud.avicola.crianza.model.entity|dto|enums`, `com.agrocloud.avicola.crianza.repository`.

---

## 4. Reglas heredadas

Multiempresa (`empresaId` desde contexto), sin FK a entidades de huevos, consumos vía `MovimientoInventarioService`, ventas con ingreso CORE según SPEC original. Detalle funcional: ver `SPEC-MODULO-AVICOLA.md` (contenido histórico completo).
