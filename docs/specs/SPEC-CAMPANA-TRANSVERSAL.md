# SPEC: Campaña transversal

**Estado:** Implementación  
**Versión:** 1.0

## Glosario

| Término | Definición |
|---------|------------|
| **Campaña / Período de gestión** | Misma entidad (`core_campanas`). En cultivos se llama *campaña agrícola*; en porcinos/avícola *período de gestión*. Eje para reportes y finanzas. |
| **CicloCultivo** | Instancia siembra→cosecha de un lote dentro de una campaña (solo cultivos). |
| **Lote (Plot)** | Activo físico permanente en cultivos; no es campaña. |
| **Lote operativo porcinos** | Recría / engorde: ciclo batch corto; hereda `campana_id` al crearse. |
| **Lote / galpón avícola** | Unidad operativa batch; consumos y ventas heredan `campana_id` del lote padre. |
| **Lote de engorde feedlot** | Unidad operativa batch bovina; consumos y ventas heredan `campana_id` del lote padre. |
| **Reproducción porcina** | Ciclo individual (madre); reportes por rango de fechas del período, sin filtrar stock reproductivo por campaña. |

## Concepto temporal por módulo (criterio agronómico)

| Módulo | Etiqueta UI | Unidad operativa | Uso de `campana_id` |
|--------|-------------|------------------|---------------------|
| Cultivos | Campaña agrícola | Ciclo de cultivo | Obligatorio en ciclos; filtra lotes/labores/reportes |
| Porcinos | Período de gestión | Lote recría/engorde | Recría, ventas, consumos, derrames al crear |
| Avícola crianza/carne | Período de gestión | Lote avícola | Lote al crear; operaciones hijas heredan |
| Avícola huevos/ponedoras | Período de gestión | Lote postura / galpón | Igual que crianza |
| Feedlot | Período de gestión | Lote de engorde | Lote al crear; consumos y ventas heredan |
| Finanzas (CORE) | Período de gestión | — | Ingresos/egresos/balance |

## Reglas de negocio

1. Una campaña `ACTIVA` por empresa a la vez.
2. Header `X-Campaign-Id` en escrituras; si falta, usar campaña activa de la empresa.
3. Campaña `CERRADA`: solo lectura (403 en POST/PUT/PATCH/DELETE salvo admin).
4. Al sembrar: crear `CicloCultivo` en campaña activa.
5. Al cosechar: cerrar ciclo; costos de labores solo del ciclo.
6. Porcinos/Avícola/Feedlot: `campana_id` automático al crear batch/transacción. Listados históricos admiten `delPeriodoActivo=true` para filtrar por campaña del header.
7. Reproducción porcina (madre/parto): reportes por rango de fechas de campaña.

## Matriz de FKs

| Tabla | campana_id | ciclo_cultivo_id |
|-------|------------|------------------|
| core_campanas | — | — |
| cultivo_ciclos | Sí | — |
| cultivo_labores | — | Sí |
| historial_cosechas | — | Sí |
| cultivo_lotes | — | ciclo_activo_id |
| ingresos / egresos | Sí | Opcional |
| inventario_granos | Sí | — |
| porcinos_recria, ventas, consumos | Sí | — |
| avicola_lote, huevo_lote, ponedoras_galpon | Sí | — |
| feedlot_lote, feedlot_consumo, feedlot_venta | Sí | — |
