# Resumen de Unificación: Ventas y Faenas

## ✅ Cambios Implementados

### 1. Backend - Modelo de Datos

#### ParametrosEstablecimientoPorcino
- ✅ Agregado campo `realizaFaena` (Boolean, default `false`)
- ✅ Script SQL ejecutado: `AGREGAR_CAMPO_REALIZA_FAENA.sql`
- ✅ Columna `realiza_faena` agregada a la tabla `porcinos_parametros_establecimiento_porcinos`

#### VentaPorcino
- ✅ Campo `precioKg` ahora es nullable (opcional para FAENA)
- ✅ Ya tenía todos los campos de faena: `pesoEnvio`, `pesoFaena`, `rendimiento`, `fechaEnvio`, `fechaFaena`
- ✅ Enum `TipoVenta` incluye: `ENGORDE`, `REPRODUCTOR`, `FAENA`

### 2. Backend - Servicio

#### VentaPorcinoService
- ✅ Validación de configuración: verifica `realizaFaena` antes de permitir tipo FAENA
- ✅ `precioKg` obligatorio para ventas normales, opcional para FAENA
- ✅ Cálculo de ingreso total diferenciado por tipo
- ✅ Lógica específica de faena: merma en transporte, rendimiento, etc.

### 3. Backend - Deprecación

#### Clases Deprecadas (mantenidas por compatibilidad)
- ✅ `Faena` (entidad) - marcada como `@Deprecated`
- ✅ `FaenaService` - marcado como `@Deprecated`
- ✅ `FaenaController` - marcado como `@Deprecated`

**Nota**: Estas clases siguen funcionando pero se recomienda usar `VentaPorcino` con `tipo = FAENA`.

### 4. Frontend - TypeScript

#### types.ts
- ✅ Agregado `realizaFaena?: boolean` a `ParametrosEstablecimientoPorcino`
- ✅ `VentaPorcino` ya tenía todos los campos necesarios

### 5. Frontend - Pantalla de Ventas

#### VentasScreen.tsx
- ✅ Carga parámetros del establecimiento al iniciar
- ✅ Muestra solo tipos habilitados según `realizaFaena`
- ✅ Campos específicos de faena solo cuando `tipo === 'FAENA'`
- ✅ `precioKg` obligatorio para ventas normales, opcional para FAENA
- ✅ Cálculo automático de rendimiento al ingresar peso de faena

### 6. Scripts SQL

- ✅ `AGREGAR_CAMPO_REALIZA_FAENA.sql` - Ejecutado exitosamente
- ✅ `MIGRAR_FAENAS_A_VENTAS.sql` - Listo para usar cuando haya datos históricos

**Nota**: No había datos de faenas para migrar (0 registros en `porcinos_faena`).

---

## 🎯 Funcionamiento

### Configuración

1. Ir a **Configuraciones > Parámetros del Establecimiento**
2. Activar **"Realiza Faena"** si el establecimiento hace faenas
3. Guardar configuración

### Registro de Ventas

#### Venta Normal (ENGORDE/REPRODUCTOR)
- Campos básicos: fecha, cantidad, peso promedio, **precio por kg (obligatorio)**
- Cálculo automático: `ingresoTotal = cantidad × pesoPromedio × precioKg`

#### Faena (FAENA)
- Campos base: fecha, cantidad, peso promedio, precio por kg (opcional)
- Campos específicos:
  - **Fecha de Envío** (obligatorio)
  - **Peso de Envío** (obligatorio)
  - Fecha de Faena (opcional)
  - Peso de Faena (opcional)
- Cálculo automático de rendimiento si hay `pesoFaena`
- Cálculo de merma en transporte según configuración económica

---

## 📋 Próximos Pasos Recomendados

1. **Configurar establecimientos existentes**: Si hay establecimientos que realizan faenas, configurar `realizaFaena = true` en sus parámetros.

2. **Migración futura** (cuando haya datos):
   - Ejecutar `MIGRAR_FAENAS_A_VENTAS.sql` si existen registros históricos en `porcinos_faena`
   - Verificar que los datos migrados sean correctos

3. **Eliminación de código deprecado** (futuro):
   - Después de un período de transición, considerar eliminar `Faena`, `FaenaService` y `FaenaController`
   - Eliminar la tabla `porcinos_faena` (o dejarla como backup histórico)

---

## ✨ Beneficios

- ✅ **Un solo modelo de datos** para todas las ventas
- ✅ **Configuración flexible** por establecimiento
- ✅ **UI adaptativa** según configuración
- ✅ **Validaciones consistentes** en un solo lugar
- ✅ **Reportes unificados** para todos los tipos de venta
- ✅ **Código más mantenible** sin duplicación

---

## 📝 Notas Importantes

1. La entidad `Faena` y sus servicios están marcados como `@Deprecated` pero siguen funcionando para compatibilidad.

2. El campo `realizaFaena` por defecto es `false`, por lo que establecimientos nuevos no tendrán faenas habilitadas hasta que lo configuren.

3. Para faenas, el `precioKg` es opcional porque se puede ingresar después de conocer el peso de faena real.

4. La migración de datos históricos es opcional y solo se necesita si hay registros en `porcinos_faena` que se quieran migrar a `porcinos_ventas_porcinos`.
