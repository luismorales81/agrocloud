# SPEC: Trazabilidad comercial certificable (reporte PDF inmutable)

**Estado:** implementada (versión 1)  
**Fecha:** 2026-04-27

## Resumen

Generación de reportes de trazabilidad con reglas de certificación **configurables en base de datos** (sin hardcode de reglas en servicios de cultivo/porcinos), validación automática, snapshot JSON + hash, PDF almacenado (inmutable).

## Endpoints

- `GET /api/trazabilidad/reportes` — listado de reportes de la empresa (sin binario PDF; metadatos y hashes).
- `POST /api/trazabilidad/reportes`
- `GET /api/trazabilidad/reportes/{id}/pdf`
- `GET /api/trazabilidad/certificaciones`
- `GET /api/trazabilidad/certificaciones/{codigo}`
- `PUT /api/admin/trazabilidad/reglas/{id}` — actualiza `parametrosJson` de una regla (JSON validado). Requiere rol administrador.

## Reglas iniciales (semilla SQL)

- `LIBRE_AGROQUIMICOS` — `NINGUNO_TIPOS_INSUMO` (plaguicidas y fertilizante según parámetros JSON)
- `SIN_ANTIBIOTICOS` — `SIN_CATEGORIA_SANITARIA` (eventos con categoría `ANTIBIOTICO` en `TipoEventoSanitario`)

## Riesgos

- Definición de “agroquímico” comercial vs `TipoInsumo` (ajustar solo vía reglas/parámetros, no lógica global de `Insumo`).

## Base de datos

Aplicar la migración `V1_133__Trazabilidad_comercial_certificable.sql` en MySQL (el proyecto puede tener Flyway deshabilitado: ejecutar el script a mano).

## Cómo probar (API)

1. Autenticarse (JWT) con un usuario con empresa.
2. `POST /api/trazabilidad/reportes` con cuerpo JSON, por ejemplo:
   ```json
   { "entidadTipo": "LOTE", "entidadId": 1, "certificacion": "LIBRE_AGROQUIMICOS" }
   ```
3. Si el lote no tiene insumos prohibidos: `201` y cuerpo con `id`, `hashSnapshot`, `hashPdf`.
4. `GET /api/trazabilidad/reportes/{id}/pdf` con el mismo `Authorization` → PDF.
5. Si hay insumo prohibido: `400` con `error: CERTIFICACION_RECHAZADA` e `incidencias`.

### Ajuste de reglas (admin)

- Actualizar `parametrosJson` de una regla:

```json
PUT /api/admin/trazabilidad/reglas/{id}
{ "parametrosJson": "{\"tiposInsumoProhibidos\":[\"HERBICIDA\"]}" }
```

- Si `parametrosJson` no es JSON válido: `400` con `error: "Argumento inválido"`.

Prueba unitaria: `mvn test -Dtest=ValidacionTrazabilidadServiceTest`.

## Cómo probar (tests de integración)

- Ejecutar test de integración de endpoints (MockMvc, sin filtros de seguridad):

```bash
cd agrogestion-backend
mvn test -Dtest=TrazabilidadComercialControllerIntegracionTest
```

Incluye:
- Caso **rechazado** por uso de `TipoInsumo` prohibido en el lote.
- Caso **válido** con generación y descarga de PDF (`GET /pdf`).
