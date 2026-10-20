# SPEC — Feedlot v2: resolución de no-presentes en recuento de manga

**Versión:** 1.0  
**Fecha:** 12 de septiembre de 2026  
**Estado:** Aprobada  
**Fecha aprobación:** 12 de septiembre de 2026  
**Módulo:** `FEEDLOT`  
**Padre:** `SPEC-FEEDLOT-V2-SESION-MANGA-BASTON.md` §8.4  
**Diseño técnico:** `DISENO-TECNICO-FEEDLOT-V2-RECUENTO-RESOLUCION.md` (aprobado 12 Sep 2026)

---

## 1. Qué ya está decidido (no se cambia)

El recuento **clasifica y no muta solo**. Confirmación de recuento sin acciones: no crea animales, no mueve plantel, no da de baja, no toca stock (RN-MG-R01).

---

## 2. Problema

El operador ve “desconocidos” y no tiene camino en la misma sesión. Un EID no presente puede ser compra sin enrolar, mezcla con otro lote, error de lectura, o un animal ya dado de baja.

---

## 3. Clasificación (obligatoria en preview)

| Código | Criterio |
|--------|----------|
| `PRESENTE` | `ACTIVO` del lote de la sesión |
| `FALTANTE` | Activo del lote, no está en el CSV |
| `OTRO_LOTE` | `ACTIVO` en otro lote de la misma empresa (v1: otro corral) |
| `INACTIVO` | Existe pero no `ACTIVO` |
| `DESCONOCIDO` | No hay animal en la empresa |
| `SIN_CONTRASTE` | Lote híbrido: cabezas sin EID |

Hospital / movimiento de corral fuera de alcance. “Otro corral” = otro lote.

---

## 4. Decisiones aprobadas (12 Sep 2026)

| ID | Decisión |
|----|----------|
| D11 | El recuento es informe por defecto. Las mutaciones son **acciones explícitas** por fila/grupo, no un “Confirmar” mudo. |
| D12 | Desconocido: `IGNORAR` (default), `ENROLAR_EN_ESTE_LOTE` o `ENROLAR_EN_LOTE_NUEVO`. |
| D13 | Otro lote: solo informe (lote/corral origen). Sin traslado. |
| D14 | Inactivo: solo conflicto. Sin reactivar. |
| D15 | Faltantes: lista + **atajo que crea una sesión BAJA en borrador** con esos EID. El recuento no descuenta. El operador confirma la baja (subtipo, motivo). |
| D16 | Confirmar recuento guarda el snapshot. Enrolamientos marcados se ejecutan en el mismo commit. |
| D17 | **Solo administrador de empresa** (rol `ADMINISTRADOR` o `SUPERADMIN`) puede enrolar desde recuento (este lote o lote nuevo). Operario ve y confirma el informe; no enrola. |
| D18 | Si enrolar en **este lote** supera el plantel: **ajuste de cabezas** (mismo criterio RN-A01 / D05) **o** mandar esos EID a **lote nuevo**. El admin elige. |
| D19 | Lote nuevo: corral `DISPONIBLE` de la empresa, nombre, fecha (default = fecha sesión), cabezas = cantidad enrolada. Hereda categoría/raza/campaña del lote del recuento si el admin no las cambia. Un corral / un lote activo (RN-E02). |

---

## 5. Reglas

| Regla | Detalle |
|-------|---------|
| RN-MG-R06 | Toda fila `DESCONOCIDO` arranca en `IGNORAR`. |
| RN-MG-R07 | `ENROLAR_EN_ESTE_LOTE` / `ENROLAR_EN_LOTE_NUEVO`: solo si el usuario es admin (D17). Si no → 409. |
| RN-MG-R08 | Enrolar reutiliza RN-MG-E01–E06 (no crea si EID existe; conflicto visual). |
| RN-MG-R09 | Exceso en este lote: bloquear salvo `ajustarPlantel` o destino `LOTE_NUEVO`. |
| RN-MG-R10 | Lote nuevo: persistir lote + enrolar ahí + ocupar corral. Si el alta de lote falla, no se enrola ninguno de ese grupo. |
| RN-MG-R11 | Atajo baja: crea `feedlot_sesion_manga` tipo `BAJA` `BORRADOR` con filas de los faltantes elegidos (`OK` si siguen `ACTIVO`). No confirma. |
| RN-MG-R12 | Operario puede confirmar el recuento-informe. No puede persistir acciones de enrolar ni crear lote. |
| RN-MG-R13 | Anular recuento: no deshace enrolamientos ni el lote nuevo ni la sesión de baja creada (quedan como hechos posteriores). |

---

## 6. UX

1. Resumen por origen (no un solo “intrusos”).
2. Desconocidos: por fila Ignorar / Este lote / Lote nuevo. Admin only habilitado.
3. Si “este lote” + exceso: checkbox ajuste **o** cambiar destino a lote nuevo.
4. Si “lote nuevo”: elegir corral disponible + nombre.
5. Otro lote / inactivo: solo lectura.
6. Faltantes: “Generar sesión de baja” → navega al borrador BAJA.

---

## 7. Fuera de alcance

- Traslado entre corrales / hospital.
- SENASA / DTe.
- Auto-enrolar o auto-baja.
- Reactivar inactivos.

---

## 8. Criterios de aceptación

- [ ] Preview distingue `DESCONOCIDO`, `OTRO_LOTE`, `INACTIVO`, `FALTANTE`.
- [ ] Operario confirma recuento sin poder enrolar (403/409).
- [ ] Admin enrola desconocidos en este lote; si hay exceso, ajuste o lote nuevo.
- [ ] Admin crea lote nuevo en corral libre y esos EID quedan `ACTIVO` ahí.
- [ ] Atajo baja abre sesión BAJA borrador; plantel no cambia hasta confirmar esa baja.
- [ ] Confirmar recuento sin acciones no muta padrón.
