# SPEC: Unificación y usabilidad de estados, transiciones y tareas

**Estado:** Aprobada (implementación solicitada por el usuario)  
**Módulo:** Cultivos  
**Fecha:** 2025-06-19

## Objetivo

Hacer predecible y amigable el ciclo de estados del lote: una sola fuente de verdad en configuración, panel visual del camino del lote, y reglas claras de avance.

## Alcance implementado

1. **Motor unificado:** cambio manual y recálculo usan transiciones de BD cuando el lote tiene configuración por tipo de cultivo.
2. **Tareas obligatorias:** solo las marcadas como obligatorias bloquean el avance por tareas (si no hay obligatorias, se exigen todas).
3. **Días configurables:** campo `dias_minimos` por estado para avance fenológico por tiempo.
4. **Modo de avance:** campo `modo_avance` (EVENTO, TIEMPO, TAREAS, MIXTO) para mensajes en UI.
5. **Panel de progreso:** endpoint y componente con camino del lote, tareas pendientes y mensaje de avance.
6. **Transiciones editables:** PUT en backend + botón editar en frontend.
7. **Endpoint faltante:** GET `/api/labores/tareas-disponibles/lote/{loteId}`.

## Reglas de negocio

| Tipo avance | Comportamiento |
|-------------|----------------|
| EVENTO | Sembrado/cosecha/abandono derivados de labores o historial |
| TIEMPO | Avanza cuando `días desde siembra >= dias_minimos` del estado |
| TAREAS | Avanza al completar tareas obligatorias (o todas si no hay obligatorias) + transición válida |
| MIXTO | Cualquiera de las anteriores según recálculo |

Estados con nombre que contenga "sembrado" o "cosechado" no se setean manualmente.

## Fase 2 (implementada)

- Job diario `RecalculoEstadosLoteScheduler` — cron `0 0 1 * * ?` (01:00).
- Recálculo manual — `POST /api/estados-lotes/recalcular-todos`.
- Validación completa — `GET /api/v1/configuracion-estados/validacion-completa`.
- Vista guiada — tab con diagrama, validación y asistente paso a paso.
