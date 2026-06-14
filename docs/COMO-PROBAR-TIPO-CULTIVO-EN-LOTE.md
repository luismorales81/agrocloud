# Cómo probar la implementación de tipo_cultivo_id en el lote

## Qué se implementó (Opción A)

- Al **asignar cultivo al lote** (siembra nueva o labor de siembra con cultivo), el backend ahora guarda también **tipo_cultivo_id** en el lote.
- La resolución de TipoCultivo usa: primero `cultivo.getTipo()`, luego `cultivo.getNombre()` (ej. cultivo "Alfalfa" con tipo "Forrajera" → TipoCultivo "Alfalfa").
- Con eso, al cargar **Labores** para un lote con cultivo y configuración de estados (ej. Alfalfa), **no** debería aparecer "Configuración Requerida" y se listan las tareas disponibles.

---

## 1. Siembra nueva (lote con cultivo Alfalfa)

1. Crear o elegir un lote sin cultivo (o liberado para siembra).
2. Realizar **Siembra** asignando el cultivo **Alfalfa** (o el que tenga configuración de estados en tu BD, ej. TipoCultivo "Alfalfa").
3. **Comprobar en BD** que el lote tenga `tipo_cultivo_id` no nulo:
   - Tabla `plot`: columna `tipo_cultivo_id` debe coincidir con el id del TipoCultivo "Alfalfa" (o el que corresponda).
4. **Comprobar en la app**: en **Labores**, elegir ese lote y verificar que **no** aparezca "Configuración Requerida" y que se listen los tipos de labor (ej. Siembra, Fumigación, Cosecha, etc.).

---

## 2. Labores para un lote ya con cultivo

1. Ir a **Labores** y seleccionar un lote que ya tenga cultivo asignado y para el cual exista configuración de estados (ej. Lote B1 - Alfalfa).
2. Verificar que:
   - **No** aparezca el mensaje "Configuración Requerida".
   - Se listen las **tareas disponibles** según el estado actual del lote (Disponible, En Crecimiento, Listo para Cosecha, etc.).

Si el lote fue sembrado **antes** de este cambio, puede que aún no tenga `tipo_cultivo_id` en BD. En ese caso el backend sigue resolviendo el tipo por cultivo (nombre/tipo) para mostrar las tareas; para que quede persistido en el lote, puedes reasignar el cultivo (ej. editando la siembra o usando un backfill).

---

## 3. Lotes ya existentes (opcional: backfill)

Para lotes que ya tienen `cultivo_id` pero `tipo_cultivo_id` en null:

- **Opción A:** Volver a guardar el lote/cultivo (ej. editar siembra o crear una labor que asigne cultivo) para que se ejecute la lógica que setea `tipo_cultivo_id`.
- **Opción B:** Ejecutar un script o endpoint de backfill que, para cada lote con `cultivo_id` y sin `tipo_cultivo_id`, resuelva el TipoCultivo con la misma lógica (por `cultivo.tipo` y `cultivo.nombre`) y actualice `plot.tipo_cultivo_id`.

---

## Resumen de archivos tocados

- **LaborService:** método `resolverTipoCultivoParaCultivo(Cultivo)`; uso en `asignarCultivoAlLotePorSiembra` y en `actualizarInformacionCultivo`.
- **SiembraService:** al sembrar, asigna TipoCultivo al lote con `laborService.resolverTipoCultivoParaCultivo(cultivo).ifPresent(lote::setTipoCultivo)`.
