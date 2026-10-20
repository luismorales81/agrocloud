# SPEC — Feedlot v2: padrón individual y sesión de manga (bastón electrónico)

**Versión:** 1.0  
**Fecha:** Septiembre 2026  
**Metodología:** SDD  
**Estado:** Aprobada  
**Fecha aprobación:** 12 de septiembre de 2026  
**Módulo:** `FEEDLOT` (extensión; no es un módulo nuevo)  
**Referencia:** `SPEC-MODULO-FEEDLOT.md` §11.3 (caravana individual, importación bastón RFID, retiro a faena)  
**Relacionada con:** `DISENO-TECNICO-MODULO-FEEDLOT.md`, `DISENO-TECNICO-FEEDLOT-V2-SESION-MANGA.md`, inventario CORE (`InventoryService`)

---

## 1. Objetivo

Permitir que el operador de feedlot **identifique cada animal** (EID / caravana visual) y **actualice el lote en bloque** a partir de los CSV que generan los bastones electrónicos, según el **tipo de actividad de manga**.

La unidad económica sigue siendo el **lote**. La unidad de evento pasa a ser el **animal**. El inventario de insumos sigue siendo **CORE** (`InventoryService`, origen `FEEDLOT`).

**Posicionamiento:** no integrar Bluetooth ni marcas de hardware en vivo. El valor está en procesar el archivo que el bastón ya produce.

---

## 2. Problema

Hoy Feedlot opera solo por cabezas (`cabezas_actuales`). El CSV del bastón es una lista de lecturas individuales. Sin padrón, importar el archivo no actualiza el rodeo de forma útil (se pierde el EID, la pesada vuelve a ser un promedio y la vacuna no sabe a quién se aplicó).

---

## 3. Alcance de esta SPEC

**Incluye**

- Padrón `feedlot_animal` (EID, caravana visual, pertenencia a lote, estado).
- Sesión de manga con tipos: enrolamiento, pesada, sanidad, recuento, baja (muerte / venta-faena).
- Importación CSV con mapeo de columnas (sin formato único de marca).
- Sanidad: aplicador (personal / veterinario) y origen del insumo; egreso CORE solo si el producto es stock de la empresa.
- Bloqueo de faena por `dias_retiro` a nivel animal (activa RN-S03 / RN-V07 de la spec v1).
- Consistencia de plantel entre animales activos y `cabezas_actuales`.

**No incluye (fase 2 / otra spec)**

- Corral hospital / movimientos entre corrales (`feedlot_movimiento_corral`).
- App móvil offline y lectura Bluetooth en vivo.
- Importación de balanza mixer.
- Módulo de ganadería extensiva (cría, destete, tacto de vientres).
- Reutilizar o modificar el CSV de control lechero.
- Integración SENASA / DTe automática.
- Perfiles automáticos por marca sin archivo de muestra (v1 = mapeo manual + perfil genérico).

---

## 4. Glosario

| Término | Definición |
|---------|------------|
| **EID** | Identificador electrónico de la caravana (ISO 11784/11785). Se normaliza a 15 dígitos decimales. |
| **Caravana visual** | Número impreso / VID. Puede existir sin EID o junto al EID. |
| **Padrón** | Registro individual del animal en el feedlot. |
| **Sesión de manga** | Operación de un turno: un tipo de actividad, un lote, un archivo (o carga manual) y datos de cabecera que el CSV no trae. |
| **Enrolamiento** | Alta o asociación de EID/caravana visual a un animal del lote. |
| **Lote híbrido** | Lote con `cabezas_actuales` mayor que la cantidad de animales enrolados (aún hay cabezas sin identificar). |
| **Aplicador** | Quién realiza la práctica: personal del establecimiento o veterinario. |
| **Origen del insumo** | De dónde sale el producto: stock de la empresa, lo trae el aplicador, o no hay insumo. |

---

## 5. Decisiones de negocio (aprobadas D01–D10)

| ID | Tema | Decisión propuesta |
|----|------|-------------------|
| D01 | Dónde vive | Extensión de **Feedlot**. No módulo Ganadería. No Lechería. |
| D02 | Grano | Lote = economía y closeout. Animal = identidad y eventos. |
| D03 | Enrolar vs identificar | Solo el tipo **ENROLAMIENTO** crea animales. El resto **solo matchea** EID/caravana ya enrolados (fila error si no existe). |
| D04 | Lote híbrido | **Permitido.** `cabezas_actuales` puede ser mayor que animales activos enrolados. Advertencia en UI. |
| D05 | Exceso de enrolados | Si al confirmar enrolamiento los animales activos superarían `cabezas_actuales`, **se bloquea** salvo que el usuario confirme un **ajuste de plantel** auditado (mismo criterio RN-A01). |
| D06 | Inventario | Un egreso CORE por sesión, no por animal. Solo si origen = stock empresa. Sin stock suficiente: **bloquear** toda la confirmación. |
| D07 | Aplicador ≠ stock | Personal / veterinario no decide solo el egreso. Decidido por **origen del insumo**. |
| D08 | Sanidad lote vs manga | La sanidad masiva de lote (v1) sigue existiendo. Una sesión de manga **no** genera además un evento de lote que vuelva a descontar el mismo insumo. |
| D09 | Formato CSV | No hay un CSV canónico de marca. El establecimiento guarda **perfiles de lectura** (alias de encabezado por tipo de evento). Fallback: nombres Data Link (EID, VID, Weight, Date, Time). Detalle: `SPEC-FEEDLOT-V2-CARGA-EVENTO-MANGA.md` v1.1. |
| D10 | Faena con retiro | No se puede dar de baja por **FAENA** un animal con retiro vigente. Venta en pie / descarte: permitido con advertencia. |

---

## 6. Entidades nuevas

Prefijo `feedlot_*`. `empresa_id` en todas. Escritura bloqueada si el lote está `CERRADO` o la campaña está cerrada (RN-L05, RN-L06).

### 6.1 `feedlot_animal`

| Campo | Obligatorio | Notas |
|-------|-------------|--------|
| `lote_id` | Sí | Lote al que pertenece hoy |
| `eid` | Condicional | Único por empresa si no es nulo. Normalizado 15 dígitos |
| `caravana_visual` | Condicional | Única por empresa si no es nula. Al menos EID **o** caravana visual |
| `sexo` | No | `MACHO` / `HEMBRA` / `INDEFINIDO` |
| `categoria_id` | No | Catálogo feedlot existente |
| `fecha_alta` | Sí | Fecha de enrolamiento o ingreso |
| `estado` | Sí | `ACTIVO`, `MUERTO`, `VENDIDO`, `FAENADO`, `DESCARTADO` |
| `fecha_baja` | Si no activo | |
| `peso_ingreso_kg` | No | Si se informó en enrolamiento |
| `observaciones` | No | |

Índices únicos: `(empresa_id, eid)` donde `eid` no nulo; `(empresa_id, caravana_visual)` donde no nulo.

### 6.2 `feedlot_sesion_manga`

Cabecera de la operación.

| Campo | Notas |
|-------|--------|
| `lote_id` | Lote destino |
| `tipo` | `ENROLAMIENTO`, `PESADA`, `SANIDAD`, `RECUENTO`, `BAJA` |
| `fecha` | Fecha operativa (puede diferir de timestamps del archivo) |
| `estado` | `BORRADOR`, `CONFIRMADA`, `ANULADA` |
| `nombre_archivo` | Si hubo CSV |
| `perfil_mapeo_json` | Columnas usadas (auditoría) |
| `filas_ok` / `filas_error` | Tras procesar |
| `detalle_errores` | Texto |
| `usuario_id` | Quién confirmó |
| `movimiento_inventario_ref` | Id de referencia al egreso CORE (si hubo) |

Campos de cabecera según tipo (nulos si no aplican):

| Tipo | Campos extra |
|------|----------------|
| `SANIDAD` | `tipo_evento` (`VACUNA` / `TRATAMIENTO` / `DIAGNOSTICO` / `OTRO`), `insumo_id`, `dosis_por_animal`, `unidad_dosis`, `cantidad_total`, `dias_retiro`, `aplicador` (`PERSONAL_ESTABLECIMIENTO` / `VETERINARIO`), `aplicador_nombre`, `origen_insumo` (`STOCK_EMPRESA` / `PROVEIDO_POR_APLICADOR` / `SIN_INSUMO`), `descripcion` |
| `BAJA` | `subtipo` (`MUERTE` / `FAENA` / `VENTA_EN_PIE` / `DESCARTE`), `motivo_muerte_id`, `precio_kg` (opcional) |
| `PESADA` | (sin extra; peso viene por fila) |
| `RECUENTO` | (sin extra) |
| `ENROLAMIENTO` | `ajustar_plantel` (bool al confirmar si hay exceso) |

### 6.3 `feedlot_sesion_manga_fila`

Resultado por lectura.

| Campo | Notas |
|-------|--------|
| `sesion_id` | |
| `eid_crudo` / `eid_normalizado` | |
| `caravana_visual` | |
| `peso_kg` | Si el archivo lo trae |
| `fecha_hora_lectura` | Si el archivo lo trae |
| `animal_id` | Si matcheó o se creó |
| `resultado` | `OK`, `ERROR`, `ADVERTENCIA` |
| `mensaje` | Motivo de error/advertencia |

### 6.4 Eventos individuales (extensión de tablas v1)

- `feedlot_pesada_animal`: `animal_id`, `sesion_id` (opcional), `fecha`, `peso_kg`. La pesada de lote v1 **sigue existiendo**. Al confirmar sesión `PESADA` se puede **además** registrar una `feedlot_pesada` de lote con promedio y `cabezas_muestreadas` = filas OK (para no romper KPIs actuales de GMD de lote).
- `feedlot_evento_sanitario`: agregar `animal_id` (nullable), `sesion_id` (nullable), `aplicador`, `origen_insumo`. Si `animal_id` es nulo, es el evento masivo v1. Un evento por animal en sesión de manga. **No** egresar inventario en el evento hijo; el egreso es de la sesión.
- `feedlot_muerte` / `feedlot_venta`: agregar `animal_id` (nullable) y `sesion_id`. `cabezas` en el registro agregado de la sesión = cantidad de animales dados de baja (un registro de lote + N animales).

---

## 7. Normalización y matching

### 7.1 EID

- Quitar espacios y no dígitos.
- Si queda hexadecimal de formato FDX-B convertible, convertir a decimal 15 dígitos.
- Rellenar con ceros a la izquierda hasta 15.
- Si no se puede normalizar: fila `ERROR`.

### 7.2 Match (prioridad)

1. EID normalizado + empresa.
2. Si no hay EID, caravana visual (igualdad exacta, recortando espacios, sin distinguir mayúsculas).
3. Si ambos vienen y el EID matchea un animal con otra caravana visual: `ERROR` (conflicto de identidad).

---

## 8. Reglas por tipo de sesión

### 8.1 ENROLAMIENTO (RN-MG-E)

| Regla | Detalle |
|-------|---------|
| RN-MG-E01 | Crea animal si no existe EID/caravana en la empresa. |
| RN-MG-E02 | Si el animal existe **activo en el mismo lote**: idempotente (OK, sin duplicar). |
| RN-MG-E03 | Si existe activo en **otro lote**: `ERROR`. |
| RN-MG-E04 | Si existe no activo: `ERROR` (no reactivar por enrolamiento; usar flujo de corrección futuro). |
| RN-MG-E05 | Fila sin EID ni caravana visual: `ERROR`. |
| RN-MG-E06 | Tras el preview, si `activos_resultantes > cabezas_actuales` y no se marcó ajuste de plantel: no se confirma. |
| RN-MG-E07 | Si se marca ajuste: crear `feedlot_ajuste_plantel` (`cabezas_despues` = activos resultantes) y actualizar `cabezas_actuales`. Solo si el usuario tiene permiso de ajuste (RN-A01). |

### 8.2 PESADA (RN-MG-P)

| Regla | Detalle |
|-------|---------|
| RN-MG-P01 | Solo animales `ACTIVO` del lote de la sesión. |
| RN-MG-P02 | Peso > 0 obligatorio en la fila o columna mapeada. |
| RN-MG-P03 | EID desconocido: `ERROR` (no crea animal). |
| RN-MG-P04 | Al confirmar: una `feedlot_pesada_animal` por fila OK + una `feedlot_pesada` de lote (promedio de filas OK). |
| RN-MG-P05 | El GMD de lote v1 sigue usando la pesada de lote más reciente. |

### 8.3 SANIDAD (RN-MG-S)

| Regla | Detalle |
|-------|---------|
| RN-MG-S01 | Cabecera: tipo de evento y aplicador obligatorios. |
| RN-MG-S02 | Si `tipo_evento` es `VACUNA` o `TRATAMIENTO`: `origen_insumo` no puede ser vacío. |
| RN-MG-S03 | Si `origen_insumo = SIN_INSUMO`: no pedir insumo ni dosis; no hay egreso. |
| RN-MG-S04 | Si `origen_insumo = STOCK_EMPRESA`: `insumo_id` y cantidad obligatorios. Cantidad por defecto = `dosis_por_animal × filas OK`. El usuario puede sobreescribir el total. |
| RN-MG-S05 | Egreso único: `InventoryService.consumir(empresa, insumo, cantidad, FEEDLOT, sesionId, usuario)`. Si `!exito` → no confirmar. |
| RN-MG-S06 | Si `origen_insumo = PROVEIDO_POR_APLICADOR`: registrar eventos y retiro; **no** llamar a inventario. |
| RN-MG-S07 | Default UI: aplicador personal → origen `STOCK_EMPRESA`; veterinario → `PROVEIDO_POR_APLICADOR`. Ambos editables. |
| RN-MG-S08 | Un `feedlot_evento_sanitario` por animal OK, con `animal_id`, `sesion_id`, `dias_retiro` de cabecera. **Sin** `cantidadInsumo` en el hijo. |
| RN-MG-S09 | No crear evento sanitario de lote (v1) en la misma confirmación. |
| RN-MG-S10 | Anular sesión `CONFIRMADA` con egreso: `InventoryService.reponer` la misma cantidad y origen; eventos hijos quedan anulados o se eliminan en la misma transacción. |
| RN-MG-S11 | EID desconocido o animal no activo del lote: `ERROR`. |

### 8.4 RECUENTO (RN-MG-R)

| Regla | Detalle |
|-------|---------|
| RN-MG-R01 | No crea, no pesa, no descuenta stock, no da de baja. |
| RN-MG-R02 | Presentes = EID/caravana que matchean animales `ACTIVO` del lote. |
| RN-MG-R03 | Faltantes = activos del lote no leídos. Se listan; no se dan de baja solos. |
| RN-MG-R04 | Intrusos = EID de otro lote activo de la empresa, o desconocidos. Solo informe. |
| RN-MG-R05 | El lote híbrido (cabezas sin enrolar) se informa: `cabezas_actuales − activos_enrolados` no puede contrastarse por EID. |

Incremento aprobado: resolución de no-presentes en [`SPEC-FEEDLOT-V2-RECUENTO-RESOLUCION.md`](SPEC-FEEDLOT-V2-RECUENTO-RESOLUCION.md). Diseño: [`DISENO-TECNICO-FEEDLOT-V2-RECUENTO-RESOLUCION.md`](DISENO-TECNICO-FEEDLOT-V2-RECUENTO-RESOLUCION.md).

### 8.5 BAJA (RN-MG-B)

| Regla | Detalle |
|-------|---------|
| RN-MG-B01 | Solo `ACTIVO` del lote. |
| RN-MG-B02 | `MUERTE`: estado `MUERTO`, `feedlot_muerte` de lote con `cabezas` = N, descuenta `cabezas_actuales` (RN-M02). |
| RN-MG-B03 | `FAENA` / `VENTA_EN_PIE` / `DESCARTE`: estado según subtipo, `feedlot_venta` con `cabezas` = N (RN-V01–V03). |
| RN-MG-B04 | Faena: rechazar animal con retiro vigente (`fecha_sesion < fecha_evento_sanitario + dias_retiro`). Si alguno falla, esas filas son `ERROR`; el resto puede confirmarse (parcial). |
| RN-MG-B05 | Si tras la baja `cabezas_actuales = 0`: cierre de lote (RN-L07). |

---

## 9. Flujo de importación

1. Usuario elige lote (activo), tipo de sesión y fecha.
2. Completa cabecera (sanidad / baja).
3. Sube CSV (multipart) o pega texto.
4. Elige delimitador si no se detecta (`,`, `;`, tab).
5. Mapea columnas: EID, caravana visual, peso, fecha/hora (las no usadas se ignoran).
6. El sistema normaliza, matchea y muestra preview (OK / error / advertencia) **sin persistir efectos de dominio** (sí puede guardar `BORRADOR`).
7. Usuario corrige mapeo o descarta filas.
8. Confirmar: transacción única. Sesión `CONFIRMADA` + efectos de las secciones 8.x.
9. Resultado: conteos y detalle de errores persistido (patrón de `lecheria_import_control_lechero`, contrato propio).

Codificación: UTF-8; si falla, reintentar Windows-1252. Primera fila se trata como encabezado si no parsea como EID.

---

## 10. Inventario y costos (detalle)

- El stock es el de **insumos de la empresa**, compartido entre módulos. Feedlot solo consume vía `InventoryService`.
- Referencia del movimiento = `id` de `feedlot_sesion_manga`.
- Honorarios del veterinario y producto proveído por el aplicador **no** son stock. Quedan fuera de esta SPEC (egreso financiero CORE opcional en una spec posterior).
- Diagnóstico sin producto: `SIN_INSUMO`.

---

## 11. UI (Feedlot)

- Menú: **Sesión de manga** (`/feedlot/manga`).
- Carga guiada: lecturas (CSV) → tipo de evento → lote → datos del evento → preview → confirmar. Ver [`SPEC-FEEDLOT-V2-CARGA-EVENTO-MANGA.md`](SPEC-FEEDLOT-V2-CARGA-EVENTO-MANGA.md).
- Desde el detalle de lote: acceso a animales del lote y a historial de sesiones.
- Ficha de animal: EID, caravana, pesadas, sanidad, bajas, retiro vigente.
- Sanidad v1 de lote: texto de ayuda «para aplicación por caravana usar Sesión de manga».

Permiso: `@RequiresModule("FEEDLOT")` escritura igual que el resto de operaciones del lote. Ajuste de plantel por exceso de enrolamiento: mismo permiso que ajuste actual.

---

## 12. API propuesta (contrato funcional)

Base existente: `/api/feedlot`.

| Método | Ruta | Uso |
|--------|------|-----|
| GET | `/lotes/{id}/animales` | Padrón del lote |
| GET | `/animales/{id}` | Ficha |
| POST | `/sesiones-manga` | Crea borrador (cabecera) |
| POST | `/sesiones-manga/{id}/archivo` | Sube CSV + mapeo; recalcula filas |
| GET | `/sesiones-manga/{id}` | Cabecera + filas + resumen recuento |
| POST | `/sesiones-manga/{id}/confirmar` | Aplica reglas |
| POST | `/sesiones-manga/{id}/anular` | Revierte (inventario si correspondía) |
| GET | `/lotes/{id}/sesiones-manga` | Historial |

El diseño técnico (DTO, DDL exacto, tests) está en [`DISENO-TECNICO-FEEDLOT-V2-SESION-MANGA.md`](DISENO-TECNICO-FEEDLOT-V2-SESION-MANGA.md).

---

## 13. Criterios de aceptación

- [ ] Empresa con módulo `FEEDLOT` puede enrolar animales desde CSV de EID sin crear otro módulo.
- [ ] EID duplicado en otro lote activo se rechaza.
- [ ] Pesada y sanidad no crean animales nuevos.
- [ ] Recuento lista presentes, faltantes e intrusos sin mutar plantel ni stock.
- [ ] Vacuna con origen stock empresa descuenta **una** vez la cantidad total; stock insuficiente impide confirmar.
- [ ] Vacuna con origen proveído por veterinario no mueve inventario.
- [ ] Cambio de aplicador no cambia el stock si el usuario no cambia el origen.
- [ ] Faena de animal en retiro se rechaza; el resto de la sesión puede confirmarse.
- [ ] Lote híbrido permitido; exceso de enrolados exige ajuste de plantel.
- [ ] Anular sesión de sanidad con stock repone el insumo.
- [ ] Closeout y KPIs de lote v1 siguen calculándose; la pesada de sesión actualiza la pesada promedio de lote.

---

## 14. Riesgos

| Riesgo | Mitigación |
|--------|------------|
| Formatos reales de bastón distintos | Mapeo manual; pedir 2–3 CSV de campo antes del diseño técnico de parsers de marca |
| Doble egreso (lote + manga) | RN-MG-S09; UI de sanidad v1 aclara el canal |
| Plantel vs padrón desfasado | Lote híbrido explícito + bloqueo de exceso |
| EID en HEX vs decimal | Normalización documentada; fila error si no convierte |
| Anulación parcial mal hecha | Anular es todo-o-nada de la sesión |

---

## 15. Impacto en SPEC Feedlot v1

Esta SPEC **concreta** los ítems 1, 3 y 5 de §11.3 (caravana, retiro, bastón) y **no** abre ganadería extensiva (§11.4).

RN-S02 (egreso opcional en sanidad de lote) **no se elimina**: sigue para tratamientos masivos sin caravana. La sesión de manga usa RN-MG-S04–S06.

---

## 16. Aprobación

**Aprobada** el 12 de septiembre de 2026 (D01–D10). Diseño técnico: [`DISENO-TECNICO-FEEDLOT-V2-SESION-MANGA.md`](DISENO-TECNICO-FEEDLOT-V2-SESION-MANGA.md).

Pendiente de campo (no bloquea implementación del mapeo genérico; bloquea parsers de marca): archivos CSV reales (marca/modelo del bastón).
