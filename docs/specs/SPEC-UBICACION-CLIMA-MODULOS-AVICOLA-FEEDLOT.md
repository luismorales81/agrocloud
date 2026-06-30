# SPEC — Ubicación (Google Maps) y clima en módulos avícola y feedlot

**Versión:** 1.0  
**Fecha:** Junio 2026  
**Estado:** Aprobada para implementación  
**Referencia:** patrón del módulo `AVICOLA_HUEVOS`

---

## 1. Objetivo

Replicar en **crianza**, **carne**, **ponedoras** y **feedlot** el patrón de ubicación geográfica (texto + coordenadas JSON en mapa Google) y consulta de clima del módulo huevos, con persistencia opcional de temperatura/humedad en registros operativos relevantes.

---

## 2. Alcance

### Incluido

- Formato de coordenadas: `[{ "lat": number, "lng": number }]` (punto o polígono).
- Componentes frontend compartidos: editor de mapa, panel de clima en detalle, botón de autocompletar clima.
- **Crianza:** clima en detalle de lote; temp/humedad en pesadas.
- **Carne:** hereda establecimientos de crianza; clima en detalle; temp/humedad en pesadas.
- **Ponedoras:** clima en detalle de galpón vía establecimiento de crianza; tabla `avicola_ponedoras_ambiente_diario`.
- **Feedlot:** coordenadas en `feedlot_establecimiento`; pantalla mapa; clima en detalle de lote; temp/humedad en lecturas de comedero.
- Reportes v1.1: gráficos temp/humedad donde hay persistencia.

### Excluido

- Duplicar catálogo de establecimientos en carne o ponedoras (usan `avicola_establecimiento`).
- Mapa visual del yard feedlot v2 (corrales individuales en mapa).
- Migración de API key Google Maps a variable de entorno (documentado como deuda técnica).

---

## 3. Persistencia de temperatura y humedad

| Módulo | Tabla / entidad | Campos |
|--------|-----------------|--------|
| Huevos | `avicola_huevo_produccion_diaria` | Ya existente |
| Crianza / Carne | `avicola_pesada` | `temperatura_ambiente`, `humedad_ambiente` |
| Ponedoras | `avicola_ponedoras_ambiente_diario` | único por `galpon_id` + `fecha` |
| Feedlot | `feedlot_lectura_comedero` | `temperatura_dia`, `humedad_dia` |

---

## 4. Criterios de aceptación

- Widget de clima en pantalla de detalle cuando hay coordenadas válidas del establecimiento.
- Feedlot permite guardar polígono o punto en mapa.
- Autocompletar temp/humedad desde clima actual en formularios operativos; valores editables y opcionales.
- Sin coordenadas: mensaje con enlace al mapa del establecimiento correspondiente.
