# SPEC: Wizard IA — Configuración de tipo de cultivo

**Versión:** 1.0 · **Estado:** Aprobada para implementación

## 1. Problema

Configurar estados, transiciones y tareas por tipo de cultivo es complejo para usuarios no técnicos.

## 2. Solución

Wizard de tres pasos (selección → contexto → revisión) que obtiene una **propuesta JSON** desde IA, permite edición en UI y **confirma** persistiendo `TipoCultivo`, `EstadoLoteConfig`, `TransicionEstadoConfig`, `TareaPorEstadoConfig` y `PlantillaLabor`, scoped a **`Empresa`**.

## 3. Relación con código existente

- Reutilizar la misma semántica que `ImportacionConfiguracionEstadosService` (orden: estados → transiciones → tareas; tipos de labor válidos = enum `Labor.TipoLabor`).
- Ver `SPEC-IMPORTACION-EXCEL-CONFIGURACION-ESTADOS.md`.

## 4. Contrato JSON (IA → backend)

Coincide con `WizardCultivoPropuestaDto` en el código. Tipos de labor: `SIEMBRA`, `FERTILIZACION`, `RIEGO`, `COSECHA`, `MANTENIMIENTO`, `PODA`, `CONTROL_PLAGAS`, `CONTROL_MALEZAS`, `ANALISIS_SUELO`, `OTROS`.

## 5. Endpoints

- `POST /api/wizard/cultivo/vista-previa` — body: nombreCultivo, region, objetivoProductivo.
- `POST /api/wizard/cultivo/confirmar` — body: propuesta completa revisada.

Ambos requieren JWT y header de empresa (`X-Company-Id`). Módulo `crops` (`@RequiresModule("crops")`).

## 6. Reglas

- Si el nombre de `TipoCultivo` ya existe globalmente, sufijar para unicidad (ej. nombre + empresa).
- Un estado inicial y uno final (marcados por la propuesta o derivados del orden).
- Fallback manual si `iaDisponible` es false.

## 7. Criterios de aceptación

- `confirmar` crea registros consultables por las pantallas existentes de configuración de estados.
- `PlantillaLabor` queda persistida por tipo de cultivo y empresa.
