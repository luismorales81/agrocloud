# SPEC: Wizard IA — Plan de recría porcina

**Versión:** 1.0 · **Estado:** Aprobada para implementación

## 1. Problema

Tras destete o alta de lote externo, definir etapas, recetas y recordatorios es laborioso.

## 2. Solución

Plantilla reutilizable **`PlanRecria`** por empresa: etapas, sugerencias de receta (nombre de `InsumoCompuesto` + kg/animal/día), recordatorios sugeridos. Flujos:

- **Vista previa / confirmar:** crea el plan en BD.
- **Aplicar:** `POST /api/wizard/recria/aplicar/{recriaId}` asocia recetas existentes por coincidencia de nombre de insumo compuesto y crea `Recordatorio` para el usuario autenticado (fechas desde fecha de ingreso de la recría).

## 3. Entidades nuevas

- `PlanRecria`, `PlanRecriaEtapa`, `PlanRecriaRecetaSugerencia`, `PlanRecriaRecordatorioSugerido` (tablas `porcinos_plan_recria_*`).

## 4. Advertencias

- Si el nombre de insumo compuesto no existe, la aplicación **omite** esa receta y la lista en la respuesta de advertencias.
- Umbrales de mortalidad: referencia conceptual respecto a `cantidad_inicial` vs suma de `MuerteRecria` (documentado; el wizard guarda el porcentaje umbral en etapa).

## 5. Endpoints

- `POST /api/wizard/recria/vista-previa`
- `POST /api/wizard/recria/confirmar`
- `POST /api/wizard/recria/aplicar/{recriaId}` — body: `{ "planRecriaId": n }`

Módulo `pigs`, header empresa, JWT.

## 6. Criterios de aceptación

- Confirmar persiste plan y etapas.
- Aplicar crea al menos un `RecetaAlimentacionPorEtapa` cuando existe `InsumoCompuesto` homónimo para la empresa.
