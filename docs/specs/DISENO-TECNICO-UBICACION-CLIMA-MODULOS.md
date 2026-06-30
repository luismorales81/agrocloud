# Diseño técnico — Ubicación y clima módulos avícola y feedlot

**Versión:** 1.0  
**SPEC:** `SPEC-UBICACION-CLIMA-MODULOS-AVICOLA-FEEDLOT.md`

---

## 1. Frontend compartido

| Componente | Ubicación | Rol |
|------------|-----------|-----|
| `EditorMapaUbicacion` | `src/components/` | TerraDraw + Google Maps; reemplaza `EditorMapaUbicacionHuevos` |
| `useRellenarClimaDesdeCoordenadas` | `src/hooks/` | Consulta `weatherService.getWeatherByCoordinates` |
| `BotonRellenarClima` | `src/components/` | UI del botón de autofill |
| `PanelClimaEstablecimiento` | `src/components/` | Widget clima o aviso con enlace a mapa |

Centroide para clima: `climaLatitud` / `climaLongitud` calculados en backend con `UtilCentroideCoordenadasCampo`.

---

## 2. Backend

### Migraciones Flyway `V1_155`

- `avicola_pesada`: `temperatura_ambiente`, `humedad_ambiente` DECIMAL opcionales.
- `avicola_ponedoras_ambiente_diario`: tabla nueva con UNIQUE `(galpon_id, fecha)`.
- `feedlot_establecimiento`: columna `coordenadas` TEXT.
- `feedlot_lectura_comedero`: `temperatura_dia`, `humedad_dia` DECIMAL opcionales.

### Servicios

- Ponedoras: `ServicioAvicolaPonedorasGalpon` inyecta `ObjectMapper`, calcula clima en `aGalponRespuesta`.
- Feedlot: `ServicioFeedlotCatalogos.normalizarJsonCoordenadas`; `ServicioFeedlotLotes` calcula clima vía corral → establecimiento.

### API nuevas (ponedoras)

- `GET /api/avicola-ponedoras/galpones/{id}/ambiente-diario?desde=&hasta=`
- `PUT /api/avicola-ponedoras/galpones/{id}/ambiente-diario` (upsert por fecha)

---

## 3. Rutas frontend nuevas

- `/feedlot/establecimientos-mapa` → `UbicacionEstablecimientosFeedlotScreen`

Enlaces cruzados:

- Carne / ponedoras → `/avicola-crianza/establecimientos-mapa?id={establecimientoId}`
- Feedlot → `/feedlot/establecimientos-mapa?id={establecimientoId}`

---

## 4. Deuda técnica

- `GOOGLE_MAPS_CONFIG.API_KEY` hardcodeada: migrar a `VITE_GOOGLE_MAPS_API_KEY` en iteración futura.
