# 📊 Análisis de Tablas No Utilizadas o Subutilizadas

## 🔍 Auditoría Realizada: 30 de Septiembre, 2025

Se analizaron 6 tablas sospechosas de no estar en uso activo en el sistema.

---

## 📋 Resumen Ejecutivo

| Tabla | Registros | Entidad Java | Repositorio | Servicio | Controlador | Estado |
|-------|-----------|--------------|-------------|----------|-------------|--------|
| `movimientos_inventario` | 1 | ✅ | ✅ | ✅ | ❌ | ✅ **EN USO** |
| `logs_acceso` | 0 | ✅ | ✅ | ✅ | ❌ | ⚠️ **CÓDIGO SIN USAR** |
| `alquiler_maquinaria` | 0 | ✅ | ✅ | Parcial | ❌ | ❌ **NO USADO** |
| `mantenimiento_maquinaria` | 0 | ✅ | ✅ | ❌ | ❌ | ❌ **NO USADO** |
| `auditoria_empresa` | 0 | ❌ | ❌ | ❌ | ❌ | ❌ **ABANDONADA** |
| `configuracion_empresa` | 0 | ❌ | ❌ | ❌ | ❌ | ❌ **ABANDONADA** |

---

## ✅ **TABLA 1: `movimientos_inventario`** - EN USO ACTIVO

### **Archivos:**
- ✅ `MovimientoInventario.java` (entidad)
- ✅ `MovimientoInventarioRepository.java`
- ✅ `InventarioService.java` (usa activamente)
- ✅ `V1_7__Create_Movimientos_Inventario_Table.sql` (migración)

### **Uso:**
```java
// InventarioService.java
private void registrarMovimiento(Insumo insumo, Labor labor, 
                               TipoMovimiento tipo, BigDecimal cantidad, 
                               String motivo, User usuario) {
    MovimientoInventario movimiento = new MovimientoInventario(...);
    movimientoInventarioRepository.save(movimiento);
}
```

### **Función:**
- Registra ENTRADA/SALIDA de insumos
- Auditoría completa de inventario
- **Usado en:** Sistema de restauración de insumos al cancelar/anular labores

### **Conclusión:** ✅ **MANTENER - Tabla crítica para auditoría de inventario**

### **Datos:** 1 registro (se está usando activamente)

---

## ⚠️ **TABLA 2: `logs_acceso`** - CÓDIGO IMPLEMENTADO PERO NO USADO

### **Archivos:**
- ✅ `LogAcceso.java` (entidad)
- ✅ `LogAccesoRepository.java`
- ✅ `LogAccesoService.java` (servicio completo con 10+ métodos)
- ❌ **NO tiene controlador**
- ❌ **NO se llama desde ningún lugar**

### **Código Implementado:**
```java
// LogAccesoService.java
public void registrarLoginExitoso(User usuario, String ipAddress, String userAgent)
public void registrarLoginFallido(String username, String ipAddress, String userAgent, String motivo)
public void registrarLogout(User usuario, String ipAddress, String userAgent)
public Map<String, Object> obtenerEstadisticasAccesos()
```

### **Problema:**
- El código está implementado profesionalmente
- Pero **NUNCA se llama** desde AuthController o algún filtro
- La tabla existe pero tiene **0 registros**

### **Opciones:**

**A. Activar el Sistema de Logs** (Recomendado)
```java
// En AuthController o filtro de seguridad:
@PostMapping("/login")
public ResponseEntity<?> login(...) {
    try {
        // ... autenticación
        logAccesoService.registrarLoginExitoso(user, request.getRemoteAddr(), request.getHeader("User-Agent"));
    } catch (Exception e) {
        logAccesoService.registrarLoginFallido(username, request.getRemoteAddr(), request.getHeader("User-Agent"), e.getMessage());
    }
}
```

**B. Eliminar Todo** (Si no se necesita)
- Eliminar `LogAcceso.java`, `LogAccesoRepository.java`, `LogAccesoService.java`
- DROP TABLE `logs_acceso`

### **Recomendación:** ⚠️ **ACTIVAR - Útil para seguridad y auditoría**

---

## ❌ **TABLA 3: `alquiler_maquinaria`** - NO USADO ACTIVAMENTE

### **Archivos:**
- ✅ `AlquilerMaquinaria.java` (entidad completa)
- ✅ `AlquilerMaquinariaRepository.java`
- ✅ `AlquilerMaquinariaDTO.java`
- ⚠️ Mencionado en `EgresoService.java` (línea 31) pero **NO se usa**
- ❌ **NO tiene controlador**
- ❌ **NO hay endpoints**

### **Código:**
```java
// EgresoService.java línea 31
@Autowired
private AlquilerMaquinariaRepository alquilerRepository;  
// ❌ Se inyecta pero NUNCA se usa en el código
```

### **Problema:**
- La entidad está definida
- El repositorio existe
- Pero NO hay funcionalidad implementada
- **0 registros** en la tabla

### **Funcionalidad Planeada (no implementada):**
Registrar alquileres de maquinaria a terceros:
- Fecha inicio/fin
- Costo por día
- Lote asignado

### **Opciones:**

**A. Eliminar Todo**
```sql
-- Ya está cubierto por labor_maquinaria con tipo=ALQUILADA
DROP TABLE alquiler_maquinaria;
```

**B. Implementar Sistema Completo**
- Crear controlador
- Crear endpoints
- Integrar con UI

### **Recomendación:** ❌ **ELIMINAR - Ya está cubierto por `labor_maquinaria` con `tipoMaquinaria = ALQUILADA`**

---

## ❌ **TABLA 4: `mantenimiento_maquinaria`** - NO USADO

### **Archivos:**
- ✅ `MantenimientoMaquinaria.java` (entidad)
- ✅ `MantenimientoMaquinariaRepository.java`
- ❌ **NO tiene servicio**
- ❌ **NO tiene controlador**
- ❌ **NO hay endpoints**

### **Problema:**
- Solo existe la entidad básica
- **0 registros** en la tabla
- Nunca se usa en el código

### **Funcionalidad Planeada (no implementada):**
Registrar mantenimientos preventivos/correctivos de maquinaria:
- Fecha de mantenimiento
- Tipo (preventivo/correctivo)
- Costo
- Próximo mantenimiento

### **Opciones:**

**A. Eliminar** (Simple)
```sql
DROP TABLE mantenimiento_maquinaria;
```

**B. Implementar Sistema** (Complejo)
- Sistema completo de mantenimientos
- Alertas de mantenimiento próximo
- Historial por maquinaria

### **Recomendación:** ❌ **ELIMINAR - No es prioritario, se puede implementar después si se necesita**

---

## ❌ **TABLA 5: `auditoria_empresa`** - ABANDONADA

### **Archivos:**
- ❌ Solo en `migration-multiempresa.sql`
- ❌ **NO tiene entidad Java**
- ❌ **NO tiene repositorio**
- ❌ **NO tiene código asociado**

### **Problema:**
- La tabla fue creada pero nunca se implementó
- **0 registros**
- **Código huérfano**

### **Funcionalidad Planeada (nunca implementada):**
Auditoría de cambios en empresas:
- Quién modificó qué
- Cuándo
- Qué cambió

### **Recomendación:** ❌ **ELIMINAR - Tabla huérfana sin implementación**

---

## ❌ **TABLA 6: `configuracion_empresa`** - ABANDONADA

### **Archivos:**
- ❌ Solo en `migration-multiempresa.sql`
- ❌ **NO tiene entidad Java**
- ❌ **NO tiene repositorio**
- ❌ **NO tiene código asociado**

### **Problema:**
- La tabla fue creada pero nunca se implementó
- **0 registros**
- **Código huérfano**

### **Funcionalidad Planeada (nunca implementada):**
Configuraciones personalizadas por empresa:
- Parámetros específicos
- Preferencias
- Ajustes

### **Recomendación:** ❌ **ELIMINAR - Tabla huérfana sin implementación**

---

## 🎯 **Recomendaciones Finales**

### ✅ **MANTENER (2 tablas):**

1. **`movimientos_inventario`** ✅
   - **EN USO ACTIVO**
   - Crítica para auditoría de inventario
   - Se usa en sistema de restauración de insumos

2. **`logs_acceso`** ⚠️
   - **CÓDIGO COMPLETO PERO NO ACTIVADO**
   - Útil para seguridad y auditoría
   - Fácil de activar (solo agregar llamadas)

### ❌ **ELIMINAR (4 tablas):**

3. **`alquiler_maquinaria`** ❌
   - Ya cubierto por `labor_maquinaria`
   - 0 registros, sin uso

4. **`mantenimiento_maquinaria`** ❌
   - No es prioritario
   - 0 registros, sin uso
   - Se puede implementar después si se necesita

5. **`auditoria_empresa`** ❌
   - Tabla huérfana sin código
   - 0 registros

6. **configuracion_empresa** ❌
   - Tabla huérfana sin código
   - 0 registros

---

## 📝 **Acciones Propuestas**

### **Fase 1: Limpieza Inmediata** (Recomendado)

Crear migración `V1_13__Cleanup_Unused_Tables.sql`:

```sql
-- Eliminar tablas no utilizadas

-- 1. alquiler_maquinaria (redundante con labor_maquinaria)
DROP TABLE IF EXISTS alquiler_maquinaria;

-- 2. mantenimiento_maquinaria (sin implementación)
DROP TABLE IF EXISTS mantenimiento_maquinaria;

-- 3. auditoria_empresa (sin código asociado)
DROP TABLE IF EXISTS auditoria_empresa;

-- 4. configuracion_empresa (sin código asociado)
DROP TABLE IF EXISTS configuracion_empresa;

SELECT 'Tablas no utilizadas eliminadas exitosamente' AS mensaje;
```

### **Fase 2: Activar logs_acceso** (Opcional pero recomendado)

1. Modificar `AuthController` para registrar logins
2. Agregar filtro de seguridad para registrar accesos
3. Crear endpoint `/api/admin/logs` para consultar

---

## 📊 **Impacto de la Limpieza**

### **Antes:**
- 32 tablas totales
- 6 tablas sospechosas
- 4 tablas abandonadas (código huérfano)

### **Después:**
- 28 tablas totales
- Solo tablas con código funcional
- Base de datos más limpia
- Menos confusión para desarrollo

### **Ventajas:**
✅ Base de datos más limpia  
✅ Menos confusión  
✅ Mejor performance (menos índices innecesarios)  
✅ Más fácil de mantener  
✅ Documentación más clara  

---

## ⚠️ **Notas Importantes**

1. **Todas las tablas a eliminar tienen 0 registros** - No se pierde información
2. **`alquiler_maquinaria` está cubierta** por `labor_maquinaria` con `tipo=ALQUILADA`
3. **`mantenimiento_maquinaria`** se puede reimplementar después si se necesita
4. **`logs_acceso`** es útil pero requiere activación manual

---

## 🚀 **Siguiente Paso Sugerido**

**¿Quieres que cree la migración V1_13 para eliminar las 4 tablas no utilizadas?**

```bash
# Script propuesto:
.\aplicar-limpieza-tablas-no-utilizadas.bat

# Eliminaría:
- alquiler_maquinaria
- mantenimiento_maquinaria
- auditoria_empresa
- configuracion_empresa

# Conservaría:
- movimientos_inventario (en uso)
- logs_acceso (código listo, puede activarse después)
```

---

**Análisis realizado por:** AgroGestion Team  
**Método:** Búsqueda de código + Conteo de registros  
**Resultado:** 4/6 tablas sin uso real detectadas  
**Recomendación:** Eliminar 4 tablas para optimizar la base de datos
