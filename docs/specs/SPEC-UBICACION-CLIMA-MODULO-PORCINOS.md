# SPEC — Ubicación (Google Maps) y clima en módulo Porcinos v2

**Versión:** 1.0  
**Fecha:** Julio 2026  
**Estado:** Aprobada para implementación  
**Referencia:** `SPEC-UBICACION-CLIMA-MODULOS-AVICOLA-FEEDLOT.md`

---

## 1. Objetivo

Replicar en Porcinos v2 el patrón de ubicación geográfica y clima de feedlot/avícola: coordenadas JSON en establecimiento, centroide en DTOs, widget de clima en detalle, autocompletar temp/humedad en operaciones.

---

## 2. Alcance

### Incluido

- `porcinos_establecimiento.coordenadas` (TEXT JSON `[{lat,lng}]`).
- Pantalla `/porcinos/establecimientos-mapa` con `EditorMapaUbicacion`.
- `climaLatitud` / `climaLongitud` en DTOs de lote, madre y parto (centroide vía `UtilCentroideCoordenadasCampo`).
- Campos opcionales en `porcinos_pesada`, `porcinos_parto`, `porcinos_consumo`: `temperatura_ambiente`, `humedad_ambiente`.
- Componentes compartidos: `PanelClimaEstablecimiento`, `BotonRellenarClima`, `useRellenarClimaDesdeCoordenadas`.
- API clima existente: `GET /api/v1/weather-simple/coordinates`.

### Excluido

- Mapa por galpón individual (v2).
- Servicio clima backend nuevo.

---

## 3. Persistencia

| Entidad | Campos clima |
|---------|--------------|
| `porcinos_establecimiento` | `ubicacion`, `coordenadas` |
| `porcinos_pesada` | `temperatura_ambiente`, `humedad_ambiente` |
| `porcinos_parto` | `temperatura_ambiente`, `humedad_ambiente` |
| `porcinos_consumo` | `temperatura_ambiente`, `humedad_ambiente` |

---

## 4. Criterios de aceptación

- Widget clima en detalle de lote y madre cuando hay coordenadas.
- Autocompletar clima en formularios de pesada y parto.
- Sin coordenadas: mensaje con enlace a mapa del establecimiento.
