# Análisis de Solapamiento: TipoAlimentoPorcino vs InsumoCompuesto

## Resumen

Se identificó un **solapamiento funcional** entre `TipoAlimentoPorcino` (catálogo) y `InsumoCompuesto` (recetas). Las categorías de "Raciones" en `TipoAlimentoPorcino` están duplicadas en `InsumoCompuesto`.

## Estado Actual

### 1. TipoAlimentoPorcino (Catálogo en Configuraciones)

**Ubicación:** `porcinos_tipos_alimento_porcinos`

**Categorías:**
- `BALANCEADO` - Balanceados comerciales (ej: "Balanceado 18%", "Balanceado 21%")
- `GRANO_PROPIO` - Granos propios (ej: "Maíz Propio", "Sorgo Propio")
- `RACION_INICIADOR` - ⚠️ **SOLAPADO** con InsumoCompuesto tipo RACION + etapa F1
- `RACION_TERMINADOR` - ⚠️ **SOLAPADO** con InsumoCompuesto tipo RACION + etapa TERMINACION
- `RACION_GESTACION` - ⚠️ **SOLAPADO** con InsumoCompuesto tipo RACION + etapa GESTACION
- `RACION_LACTANCIA` - ⚠️ **SOLAPADO** con InsumoCompuesto tipo RACION + etapa LACTANCIA
- `OTRO`

**Uso actual:** ❌ **NO SE USA** en el código funcional
- Solo se gestiona desde la UI (ConfiguracionesScreen)
- `ConsumoAlimento.tipoAlimento` es un enum simple (`BALANCEADO`, `MAIZ`, `GRANO_PROPIO`), NO referencia a `TipoAlimentoPorcino`

### 2. InsumoCompuesto (Recetas)

**Ubicación:** `insumos_compuestos`

**Tipos:**
- `RACION` - Ración completa (maíz + soja + núcleo)
- `NUCLEO` - Núcleo vitamínico-mineral
- `MEZCLA` - Mezcla simple de granos
- `PREMEZCLA` - Premezcla de aditivos
- `OTRO`

**Asociación a Etapas:** `RecetaAlimentacionPorEtapa` (tabla `porcinos_recetas_alimentacion_etapa`)
- Etapas: `GESTACION`, `LACTANCIA`, `F1`, `F2`, `F3`, `F4`, `DESARROLLO`, `TERMINACION`
- Permite definir qué receta usar para cada etapa
- Incluye cantidad diaria por animal, rangos de peso/edad, etc.

**Uso actual:** ✅ **SE USA** para:
- Crear recetas con componentes
- Preparar recetas (generar stock)
- Asociar recetas a etapas de alimentación
- Control de inventario y costos

### 3. ConsumoAlimento

**Ubicación:** `porcinos_consumos_alimento`

**tipoAlimento:** Enum simple
- `BALANCEADO`
- `MAIZ`
- `GRANO_PROPIO`

**Uso:** ✅ **SE USA** para registrar consumos de alimento
- **NO referencia** a `TipoAlimentoPorcino`
- **NO referencia** a `InsumoCompuesto`
- Solo usa enum simple

## Solapamiento Identificado

### Categorías Solapadas:

| TipoAlimentoPorcino (NO usado) | InsumoCompuesto + RecetaAlimentacionPorEtapa (USADO) |
|--------------------------------|-----------------------------------------------------|
| `RACION_INICIADOR` | `InsumoCompuesto` tipo `RACION` + etapa `F1` |
| `RACION_TERMINADOR` | `InsumoCompuesto` tipo `RACION` + etapa `TERMINACION` |
| `RACION_GESTACION` | `InsumoCompuesto` tipo `RACION` + etapa `GESTACION` |
| `RACION_LACTANCIA` | `InsumoCompuesto` tipo `RACION` + etapa `LACTANCIA` |

### Problema:

1. **TipoAlimentoPorcino** tiene categorías de "Raciones" que **NO se usan funcionalmente**
2. **InsumoCompuesto** ya cubre este concepto con mayor funcionalidad:
   - Recetas con componentes detallados
   - Stock y preparación
   - Asociación a etapas específicas
   - Control de costos

## Recomendación

### Opción 1: Eliminar categorías RACION_* de TipoAlimentoPorcino (Recomendado)

Mantener `TipoAlimentoPorcino` solo para:
- `BALANCEADO` - Balanceados comerciales (si tiene sentido mantenerlos como catálogo)
- `GRANO_PROPIO` - Granos propios (si tiene sentido mantenerlos como catálogo)
- `OTRO`

Eliminar:
- `RACION_INICIADOR`
- `RACION_TERMINADOR`
- `RACION_GESTACION`
- `RACION_LACTANCIA`

**Ventaja:** Las recetas ahora se gestionan completamente en `InsumoCompuesto` que es más funcional.

### Opción 2: Eliminar completamente TipoAlimentoPorcino (Si no se usa)

Si `TipoAlimentoPorcino` solo está en la UI pero no se usa funcionalmente, se podría eliminar completamente y dejar que:
- Balanceados comerciales se gestionen como `Insumo` (cultivo_insumos)
- Recetas se gestionen como `InsumoCompuesto`
- Granos propios ya se gestionan como `Cultivo`

**Ventaja:** Simplifica el modelo de datos eliminando redundancia.

### Opción 3: Migrar ConsumoAlimento a usar InsumoCompuesto

Cambiar `ConsumoAlimento.tipoAlimento` (enum) para que referencia a `InsumoCompuesto` cuando sea una receta.

**Ventaja:** Integración completa del sistema de recetas.

## Verificación de Uso de TipoAlimentoPorcino

### ❌ NO se usa en:
- `ConsumoAlimento` - Usa enum simple, no referencia
- Validaciones de stock - Usa enum simple
- Cálculos de costos - Usa enum simple
- Reportes - Solo muestra el enum simple

### ✅ Solo se usa para:
- **UI (ConfiguracionesScreen)** - Listar, crear, editar, eliminar desde la interfaz
- **Inicialización** - Crear valores por defecto en `InicializacionPorcinoService`

## Conclusión

**TipoAlimentoPorcino está solapado y NO se usa funcionalmente.** Las categorías `RACION_*` están duplicadas en `InsumoCompuesto` que es más completo y funcional.

**Recomendación:** Eliminar las categorías `RACION_*` de `TipoAlimentoPorcino` y actualizar:
1. Enum `CategoriaAlimento` para eliminar `RACION_INICIADOR`, `RACION_TERMINADOR`, `RACION_GESTACION`, `RACION_LACTANCIA`
2. Scripts de inicialización para no crear estas categorías
3. Frontend para no permitir crear estas categorías
