# Diseño técnico — Resolución de recuento de manga

**Estado:** Aprobado (12 Sep 2026)  
**Spec:** `SPEC-FEEDLOT-V2-RECUENTO-RESOLUCION.md` (aprobada 12 Sep 2026)  
**Código:** no generar hasta aprobar este diseño

---

## 1. Enfoque

No hay migración grande. Se clasifica en el recálculo de filas, se enriquecen DTOs y se agregan dos escrituras:

1. Acciones de desconocidos al **confirmar** el recuento (mismo `POST .../confirmar`).
2. `POST .../sesiones-manga/{id}/generar-baja-faltantes` que solo crea un borrador `BAJA`.

`ConfirmacionRecuento` deja de ser un no-op cuando vienen acciones de enrolar.

Identificadores en castellano.

---

## 2. Modelo

### 2.1 Enums (Java)

`FeedlotOrigenFilaRecuento`: `PRESENTE`, `FALTANTE`, `OTRO_LOTE`, `INACTIVO`, `DESCONOCIDO`.

`FeedlotAccionDesconocido`: `IGNORAR`, `ENROLAR_EN_ESTE_LOTE`, `ENROLAR_EN_LOTE_NUEVO`.

No se persiste `FALTANTE` en fila (el faltante no está en el CSV). El origen va en `feedlot_sesion_manga_fila.origen_recuento` (`VARCHAR(20)`, nullable) — ALTER en `V1_178__Feedlot_recuento_resolucion.sql`.

`accion_desconocido` no se guarda en la fila hasta confirmar; viaja en el body. Tras confirmar, se escribe en la fila para auditoría (`accion_desconocido`, `lote_destino_id`).

### 2.2 Columnas nuevas (`feedlot_sesion_manga_fila`)

| Columna | Tipo |
|---------|------|
| `origen_recuento` | `VARCHAR(20)` NULL |
| `accion_desconocido` | `VARCHAR(30)` NULL |
| `lote_destino_id` | `BIGINT` NULL (FK lógica a `feedlot_lote`, sin FK rígida si el lote se crea en el mismo commit) |

---

## 3. Clasificación (`evaluarRecuento`)

1. Match `ACTIVO` del lote sesión → `OK` + `PRESENTE`.
2. Match `ACTIVO` otro lote → `ADVERTENCIA` + `OTRO_LOTE` + mensaje con nombre lote/corral.
3. Match no activo → `ADVERTENCIA` + `INACTIVO`.
4. Sin match → `ADVERTENCIA` + `DESCONOCIDO`.

`armarRecuento` agrupa por origen (deja de mezclar todo en `intrusos`). Se mantiene `intrusos` deprecado = `DESCONOCIDO` + `OTRO_LOTE` + `INACTIVO` para no romper el front viejo.

`FeedlotResumenRecuento` (ampliado):

- `presentes`
- `faltantes[]` (id, eid, visual)
- `desconocidos[]` (filaId, eid, visual)
- `otroLote[]` (eid, visual, loteId, loteNombre, corralNombre)
- `inactivos[]` (eid, visual, estado)
- `cabezasSinEnrolar`

---

## 4. Confirmación

`FeedlotConfirmarSesionSolicitud` suma:

```
accionesDesconocidos: [{ filaId, accion }]
ajustarPlantel: boolean
loteNuevo: { corralId, nombre, categoriaId?, razaId? } | null
```

`ConfirmacionRecuento.confirmar`:

1. Persiste snapshot (filas ya están).
2. Si no hay acciones distintas de `IGNORAR` → fin.
3. `validarAdminEnrolamiento(usuario)` — `ADMINISTRADOR` o `SUPERADMIN` en `usuario_empresas` o autoridad `ROLE_ADMINISTRADOR` / `ROLE_SUPERADMIN`. Operario → `ResourceConflictException`.
4. Filas `ENROLAR_EN_ESTE_LOTE`: reutilizar `ConfirmacionEnrolamiento` (o extraer `ServicioFeedlotEnrolamientoFilas`) sobre esas filas + lote de la sesión + `ajustarPlantel`.
5. Filas `ENROLAR_EN_LOTE_NUEVO`: si `loteNuevo` es null → 400. Crear lote (`ServicioFeedlotLotes.crearLote`) con `cabezasInicial = cabezasActuales = N`, fecha = sesión, campaña/categoría/raza heredadas. Enrolar ahí. Guardar `lote_destino_id`.
6. Un solo lote nuevo por sesión (todas las filas `ENROLAR_EN_LOTE_NUEVO` van juntos). Mezclar “este lote” y “lote nuevo” en la misma confirmación está permitido.

Anular recuento: RN-MG-R13 — no revierte enrolamientos ni lote nuevo.

---

## 5. Atajo baja

`POST /api/feedlot/sesiones-manga/{id}/generar-baja-faltantes`

Body: `{ animalIds: number[], subtipoBaja?: MUERTE }`

- Sesión origen: recuento `BORRADOR` o `CONFIRMADA`.
- Escritura `FEEDLOT` write (operario puede: no es enrolar).
- Crea sesión `BAJA` `BORRADOR` mismo lote/fecha, filas sintéticas (sin archivo) con EID/visual de cada animal `ACTIVO` faltante pedido.
- Respuesta: la sesión BAJA (id para navegar).

No descuenta cabezas.

---

## 6. API

| Método | Ruta | Notas |
|--------|------|--------|
| GET | ya existente sesión | Resumen clasificado |
| POST | `/sesiones-manga/{id}/confirmar` | Body ampliado |
| POST | `/sesiones-manga/{id}/generar-baja-faltantes` | Nuevo |

---

## 7. Frontend

`DetalleSesionMangaFeedlotScreen` si `tipo === RECUENTO`:

- Bloques: presentes, faltantes, desconocidos, otro lote, inactivos.
- Desconocidos: select por fila (Ignorar / Este lote / Lote nuevo). Disabled si no es admin (el front oculta; el back valida).
- Si hay “este lote” y `desconocidosAEnrolar + activos > cabezas`: checkbox ajuste **o** aviso de pasar a lote nuevo.
- Si hay “lote nuevo”: select corral disponible + nombre.
- Faltantes: checkboxes + “Generar sesión de baja” → `/feedlot/manga/{idBaja}`.

---

## 8. Tests

- Clasifica los 4 orígenes de no-presente.
- Operario + enrolar → 409.
- Admin enrola en este lote con ajuste.
- Admin enrola en lote nuevo (corral libre); origen no cambia cabezas.
- Generar baja: sesión BAJA borrador, lote intacto.
- Confirmar sin acciones: 0 animales nuevos.

---

## 9. Riesgos

| Riesgo | Mitigación |
|--------|------------|
| Corral ocupado al crear lote | 409 de `crearLote` (RN-E02) |
| Enrolar y lote nuevo en el mismo TX | Un commit; rollback total si falla el lote |
| Front viejo manda confirmar sin acciones | Compatible: informe only |
