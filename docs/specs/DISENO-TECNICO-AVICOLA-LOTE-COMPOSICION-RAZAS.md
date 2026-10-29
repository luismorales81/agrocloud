# Diseño técnico — Composición de razas en un lote avícola

**Estado:** Aprobado junto con el pedido de desarrollo (octubre 2026)  
**SPEC:** `SPEC-AVICOLA-LOTE-COMPOSICION-RAZAS.md` v0.2

## Decisión

La composición es una relación muchos a muchos, sin cantidad por raza. La cantidad y, en crianza, la especie siguen en el lote.

| Módulo | Catálogo | Composición |
|--------|----------|-------------|
| Crianza | `avicola_raza` (ya existe) | `avicola_lote_raza` (`lote_id`, `raza_id`) |
| Huevos | `avicola_huevo_raza` (ya existe) | `avicola_huevo_lote_raza` |
| Ponedoras | `avicola_ponedoras_raza` (nuevo) | `avicola_ponedoras_galpon_raza` |

`raza_id` de `avicola_lote` y `avicola_huevo_lote` se mantiene por el `NOT NULL` histórico y se sincroniza con una raza de la lista. La lectura de negocio sale de la tabla de composición.

En ponedoras, la columna texto `raza` se mantiene y se rellena con los nombres unidos, para no romper lecturas viejas. El alta ya no acepta texto libre.

## API

- Alta y edición de lote/galpón reciben `razaIds` (lista, sin duplicados, al menos uno).
- La respuesta agrega `razaIds` y deja `razaNombre` (crianza y huevos) o `raza` (ponedoras) con los nombres unidos por ` · `.
- Si llega solo `razaId`, se trata como lista de un elemento.
- Lote cerrado de crianza o huevos: el `PUT` solo actualiza razas.
- Raza inactiva: se puede conservar si el lote ya la tenía; no se puede agregar.
- Ponedoras: `GET/POST/PUT /api/avicola-ponedoras/razas`.

## Migración `V1_196`

1. Crear tablas de composición y catálogo de ponedoras.
2. Copiar `raza_id` actual de crianza y huevos a la composición.
3. En ponedoras, cada texto distinto por empresa (sin distinguir mayúsculas) pasa a una raza activa y se vincula al galpón.

## Pantallas

- Alta y edición: selector múltiple de razas del catálogo del módulo.
- La cantidad sigue en el campo del lote.
- Crianza: una sola especie.
- Ponedoras: pantalla de razas en el menú.
