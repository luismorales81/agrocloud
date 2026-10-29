# SPEC — Composición de razas en un lote avícola

**Versión:** 0.2  
**Fecha:** Octubre 2026  
**Metodología:** Spec Driven Development (SDD)  
**Estado:** Aprobada  
**Fecha aprobación:** Octubre 2026  
**Diseño:** `DISENO-TECNICO-AVICOLA-LOTE-COMPOSICION-RAZAS.md`  
**Relacionada con:** `SPEC-MODULO-AVICOLA-CRIANZA.md`, `SPEC-MODULO-AVICOLA-HUEVOS.md`, `SPEC-MODULO-AVICOLA.md`

Decisiones de producto (octubre 2026):

1. Aplica a **todos** los módulos avícolas activos: crianza, huevos y ponedoras.
2. El lote solo **elige razas**. La cantidad de aves se carga en el lote, no por raza.
3. Las bajas (muerte, venta, faena, baja de postura) son del lote.
4. La lista de razas se puede corregir después del alta.
5. El lote tiene **una sola especie**. No se elige especie por raza.

---

## 1. Objetivo

Al definir un lote avícola, poder indicarle **una o varias razas ya registradas** en el catálogo de ese módulo. La cantidad de aves sigue siendo un dato del lote.

Hoy cada alta admite una sola raza:

| Módulo | Dónde se define el lote | Raza hoy |
|--------|-------------------------|----------|
| `AVICOLA_CRIANZA` | `avicola_lote` / `LotesCrianzaScreen` | `raza_id` obligatorio, catálogo `avicola_raza` |
| `AVICOLA_HUEVOS` | `avicola_huevo_lote` / `LotesHuevosScreen` | `raza_id` obligatorio, catálogo `avicola_huevo_raza` |
| `AVICOLA_PONEDORAS` | `avicola_ponedoras_galpon` (el galpón es el lote) / `AvicolaPonedorasListado` | texto libre `raza`, sin catálogo |

`AVICOLA_CARNE` no es un producto aparte: quedó unificado en crianza.

Esta SPEC no permite dar de alta una raza desde el formulario del lote. La raza se registra antes, en el catálogo del módulo.

---

## 2. Alcance

### Incluido

- En crianza, huevos y ponedoras, el alta y la corrección del lote eligen **una o más razas** del catálogo de ese módulo.
- La cantidad inicial y el plantel siguen en el lote, como hoy.
- Ponedoras pasa a tener **catálogo de razas propio** (mismo criterio que crianza y huevos: por empresa, nombre y activo). El galpón deja de guardar la raza como texto libre.
- Cada módulo usa solo su catálogo. Un lote de huevos no cita una raza de crianza ni de ponedoras, y lo mismo entre los demás.
- Lotes ya cargados conservan su raza actual dentro de la lista.
- Listado y ficha muestran los nombres de las razas del lote.
- En crianza, el lote sigue teniendo un único valor de `especie` (`POLLO_PARRILLERO`, `GALLINA_PONEDORA`, `PAVO`, `OTRO`).

### Excluido

- Cantidad de aves por raza.
- Alta de raza en el mismo formulario del lote.
- Trazabilidad individual por ave.
- Muertes, ventas, faena, pesadas, consumo, sanidad o producción de huevos abiertos por raza.
- Especie distinta por cada raza del mismo lote.
- Agregar el dato especie a huevos o a ponedoras. Esos lotes no lo tienen hoy.
- Reabrir `AVICOLA_CARNE` como módulo separado.

---

## 3. Glosario

| Término | Definición |
|---------|------------|
| **Lote** | Unidad productiva. En ponedoras es el galpón. |
| **Raza** | Línea del catálogo de la empresa en el módulo del lote. |
| **Composición** | Conjunto de razas del lote, sin cantidades. |
| **Cantidad** | Aves del lote (`cantidad_inicial` / plantel en crianza, `cantidad_aves_inicial` / `cantidad_aves_actual` en huevos, `cantidad_inicial` / `cantidad_aves` en ponedoras). |
| **Especie** | Clasificación única del lote de crianza. No es un atributo de la raza. |

---

## 4. Reglas de negocio

| Id | Regla |
|----|--------|
| RN-01 | El lote tiene al menos una raza. |
| RN-02 | Cada raza pertenece al catálogo del **mismo módulo** y de la **misma empresa**. |
| RN-03 | Para **agregar** una raza, tiene que estar activa. Si después se desactiva, el lote la conserva. Al corregir, se puede dejar una raza inactiva que ya estaba en el lote; no se puede sumar una inactiva nueva. |
| RN-04 | La misma raza no se repite en el lote. |
| RN-05 | La cantidad de aves se define en el lote, con las mismas validaciones que ya tiene cada módulo. No hay cantidad por raza. |
| RN-06 | Muertes, ventas, faena y bajas de postura descuentan o registran aves del **lote**. No se elige raza en esos movimientos. |
| RN-07 | La lista de razas se puede corregir después del alta, con o sin movimientos, y también si el lote está cerrado. Corregir razas no cambia cantidades ni movimientos ya registrados. |
| RN-08 | En crianza hay una sola especie por lote. Las razas del lote no tienen especie propia. |
| RN-09 | En listados, un lote de una raza muestra ese nombre. Un lote de varias muestra los nombres separados (ejemplo: `Hy-Line · Lohmann`). |

---

## 5. Pantalla

En el alta y en la corrección:

1. El usuario elige una o más razas activas del catálogo del módulo. La raza ya incluida en el lote sigue visible aunque después se haya desactivado.
2. Puede quitar una raza si queda al menos una.
3. La cantidad de aves se carga en el campo del lote, igual que hoy.
4. En crianza, la especie sigue siendo un solo combo del lote.
5. Si no hay razas activas y el lote todavía no tiene ninguna, el alta se bloquea: primero hay que cargar el catálogo.
6. Ponedoras tiene una pantalla de razas equivalente a las de crianza y huevos.
7. Producción, consumo, sanidad, ventas y bajas no piden raza.

---

## 6. Datos existentes

- Crianza y huevos: cada lote actual queda con una sola raza, la de `raza_id`. La cantidad no se parte.
- `raza_id` deja de ser la fuente de verdad. La lectura sale de la composición. El dato histórico se migra a esa lista.
- Ponedoras: el texto `raza` del galpón, si tiene contenido, se convierte en una raza del catálogo nuevo (se reutiliza si ya existe el mismo nombre en la empresa) y queda como única raza del galpón. Un galpón con raza vacía queda sin composición hasta la próxima edición; esa edición exige al menos una raza del catálogo.
- No se borran lotes ni movimientos.

---

## 7. Riesgos

| Riesgo | Por qué importa |
|--------|-----------------|
| Tres modelos de lote | Crianza, huevos y ponedoras tienen tablas y pantallas distintas. La regla es una; la implementación se repite. |
| Ponedoras sin catálogo | Hay que crear el catálogo y dejar de usar el texto libre. Los galpones viejos dependen de esa migración de nombres. |
| Nombres repetidos en ponedoras | Dos galpones con el mismo texto deben compartir una raza del catálogo, no duplicarla. |
| `raza_id` obligatorio | Hoy es `NOT NULL` en crianza y huevos. Alta, edición, listado y ficha lo usan. |
| Filtros por raza | Un lote mixto tiene que aparecer al filtrar por cualquiera de sus razas. |
| Plantel por raza | Esta versión no puede responder cuántas aves quedan de una raza: las bajas son del lote y no hay cantidad por raza. |
| Lote cerrado | Se permite corregir razas en un lote cerrado. No debe reabrir plantel ni rehacer ventas. |

---

## 8. Criterios de aceptación

- [ ] En crianza, huevos y ponedoras se puede crear un lote con dos o más razas del catálogo de ese módulo.
- [ ] La cantidad de aves se carga una sola vez, en el lote.
- [ ] No se puede usar una raza inexistente, de otra empresa, de otro módulo o inactiva (salvo una inactiva que el lote ya tenía).
- [ ] No se puede repetir la misma raza ni guardar el lote sin razas.
- [ ] Un lote de una sola raza se sigue pudiendo crear.
- [ ] Los lotes ya cargados muestran su raza actual. En ponedoras, el texto previo queda como raza de catálogo.
- [ ] Listado y ficha muestran todas las razas, sin cantidades al lado de cada nombre.
- [ ] La lista se puede corregir con movimientos ya cargados y con el lote cerrado, sin alterar cantidades ni movimientos.
- [ ] En crianza, el lote tiene una sola especie.
- [ ] Producción, consumo, sanidad, ventas y bajas siguen siendo del lote, sin raza.

---

## 9. Diseño

Ver `DISENO-TECNICO-AVICOLA-LOTE-COMPOSICION-RAZAS.md`.
