# Diseño técnico — Carga de evento sobre lecturas del bastón

**Versión:** 1.1  
**Fecha:** 15 de septiembre de 2026  
**Estado:** Aprobado  
**Fecha aprobación:** 15 de septiembre de 2026  
**SPEC:** `SPEC-FEEDLOT-V2-CARGA-EVENTO-MANGA.md` v1.1  
**Complementa:** `DISENO-TECNICO-FEEDLOT-V2-SESION-MANGA.md`

---

## 1. Objetivo

Perfiles de lectura persistidos, análisis de CSV contra alias custom, un evento por confirmación, y UI de configuración + stepper evento-primero.

---

## 2. Persistencia

Migración `V1_179__Feedlot_perfil_lectura_manga.sql`.

Tabla `feedlot_perfil_lectura_manga`: `empresa_id`, `nombre`, `tipo`, `predeterminado`, `mapeo_alias_json` (TEXT), `insumo_id` nullable, auditoría. Índice `(empresa_id, tipo)`. Unique `(empresa_id, nombre)`.

`feedlot_sesion_manga.perfil_lectura_id` nullable (auditoría).

JSON de alias:

```json
{
  "eid": ["EID"],
  "vid": ["VID", "LID"],
  "peso": ["Weight", "Peso vivo"],
  "fecha": ["Date"],
  "hora": ["Time"],
  "dosis": [],
  "loteProducto": [],
  "retiro": [],
  "esi": []
}
```

---

## 3. Backend

### 3.1 `ServicioFeedlotPerfilLecturaManga`

CRUD + `extraerEncabezados(archivo)`. Al marcar predeterminado, desmarcar el resto del mismo tipo. RN-CE-20 al guardar.

### 3.2 Auto-mapeo (`ServicioFeedlotImportacionManga`)

`FeedlotMapeoColumnas` incluye listas de alias. Si índices nulos, completar desde encabezado: primero alias del perfil, luego nombres de fábrica.

Tras parsear: colapsar duplicados (última gana); calcular `%` EID no normalizables; listar encabezados no mapeados.

`analizar(..., perfil, tipo)`:

- RN-CE-16 si perfil.tipo ≠ tipo.
- RN-CE-17 si PESADA y no hay peso (salvo flag carga manual, solo UI; backend de confirmar ya exige pesos en filas).
- RN-CE-18 si >50% EID inválidos.
- RN-CE-19 avisos de columnas extra.

### 3.3 DTO resumen

Campos extra: `perfilArchivo`, `columnasNoMapeadas`, `porcentajeEidInvalidos`, `avisos`, `bloqueantes`, `perfilLecturaId`.

`perfilArchivo`: `SESION_ESTANDAR` | `SESION_CON_PESO` | `EID_ONLY` | `DESCONOCIDO`.

### 3.4 API

CRUD `/api/feedlot/perfiles-lectura-manga`.  
`analizar-archivo` y `archivo`: params `perfilLecturaId`, `tipo` opcionales. `@RequiresModule FEEDLOT write`.

### 3.5 Tests

- Alias `Peso vivo` → peso.
- Perfil SANIDAD en analizar PESADA → excepción.
- Guardar perfil PESADA sin peso → excepción.
- CSV notación científica → bloqueante.
- Recuento con Weight: filas sin exigir peso (ya cubierto por evaluarFila).

---

## 4. Frontend

- `/feedlot/lecturas-baston`: lista de perfiles, alta desde CSV de muestra, asignación de encabezado → campo.
- `SesionMangaFeedlotScreen`: pasos Evento → Lecturas → Lote → Datos → Revisar. Selector de perfil del tipo. Semáforo de `bloqueantes`/`avisos`.
- `DetalleSesionMangaFeedlotScreen`: sin índices 0-based en camino feliz.
- Enlace en menú Feedlot.

---

## 5. Fuera de alcance técnico

Bluetooth, commit múltiple, PATCH de filas, tipos de evento nuevos.
