# ✅ Sistema Robusto de Eliminación y Anulación de Labores

## 📋 Resumen

Se ha implementado un **sistema completo de gestión de eliminación y anulación de labores** con restauración inteligente de inventario según el estado de la labor.

---

## 🎯 Características Implementadas

### 1. **Nuevos Estados de Labor**

Se agregó el estado `ANULADA` al enum `EstadoLabor`:

```java
public enum EstadoLabor {
    PLANIFICADA,    // Labor planificada, aún no ejecutada
    EN_PROGRESO,    // Labor en ejecución
    COMPLETADA,     // Labor finalizada exitosamente
    CANCELADA,      // Labor cancelada antes de ejecutar (insumos restaurados)
    ANULADA         // Labor anulada después de ejecutar (requiere justificación)
}
```

### 2. **Campos de Auditoría**

Se agregaron campos para trazabilidad completa de anulaciones:

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `motivoAnulacion` | `String(1000)` | Justificación obligatoria de por qué se anuló |
| `fechaAnulacion` | `LocalDateTime` | Fecha y hora exacta de la anulación |
| `usuarioAnulacion` | `User` | Usuario que realizó la anulación (FK) |

---

## 🔄 Lógica de Eliminación Según Estado

### **Método: `eliminarLabor(Long id, User usuario)`**

#### **Caso 1: Labor PLANIFICADA**
✅ **Se puede eliminar directamente**

```java
// Comportamiento:
1. ✅ Se marca como CANCELADA
2. ✅ Se restauran TODOS los insumos al inventario automáticamente
3. ✅ Se registran movimientos de ENTRADA en el inventario
4. ✅ Se marca como inactiva (activo = false)
```

**Ejemplo:**
```bash
DELETE /api/labores/123
# Si está PLANIFICADA → Cancela y restaura insumos
```

#### **Caso 2: Labor EN_PROGRESO o COMPLETADA**
❌ **NO se puede eliminar directamente**

```java
// Comportamiento:
1. ❌ Lanza excepción
2. 💡 Mensaje: "Requiere anulación formal con justificación (solo ADMINISTRADOR)"
```

**Ejemplo:**
```bash
DELETE /api/labores/123
# Si está EN_PROGRESO → ERROR: "Requiere anulación formal"
```

#### **Caso 3: Ya está CANCELADA o ANULADA**
```java
// Comportamiento:
1. ✅ Solo se marca como inactiva
```

---

## 🔐 Anulación Formal de Labores

### **Método: `anularLabor(Long id, String justificacion, boolean restaurarInsumos, User usuario)`**

#### **Requisitos:**
1. ✅ Usuario debe ser **ADMINISTRADOR** o **SUPERADMIN**
2. ✅ Justificación **obligatoria** (máx. 1000 caracteres)
3. ✅ Opción de restaurar o no restaurar insumos

#### **Proceso de Anulación:**

```java
1. Verificar permisos (solo ADMINISTRADOR)
2. Validar justificación (obligatoria)
3. Si restaurarInsumos = true:
   - Obtener todos los insumos de la labor
   - Restaurar cada insumo al inventario
   - Registrar movimientos de ENTRADA con motivo
4. Actualizar labor:
   - estado = ANULADA
   - activo = false
   - motivoAnulacion = justificacion
   - fechaAnulacion = ahora
   - usuarioAnulacion = usuario actual
5. Guardar en base de datos
```

---

## 🌐 Endpoints API

### **1. Eliminar Labor (con lógica inteligente)**

```http
DELETE /api/labores/{id}
Authorization: Bearer {token}
```

**Respuestas:**

✅ **Éxito (Labor PLANIFICADA):**
```json
{
  "mensaje": "Labor eliminada exitosamente"
}
```

❌ **Error (Labor ejecutada):**
```json
{
  "error": "Esta labor está EN_PROGRESO y requiere anulación formal. Use el proceso de anulación con justificación (solo ADMINISTRADOR)."
}
```

---

### **2. Anular Labor (solo ADMINISTRADOR)**

```http
POST /api/labores/{id}/anular
Authorization: Bearer {token}
Content-Type: application/json

{
  "justificacion": "Se detectó error en los datos de la labor",
  "restaurarInsumos": true
}
```

**Respuestas:**

✅ **Éxito:**
```json
{
  "mensaje": "Labor anulada exitosamente",
  "insumosRestaurados": true
}
```

❌ **Error (sin permisos):**
```json
{
  "error": "Solo los ADMINISTRADORES pueden anular labores ejecutadas"
}
```

❌ **Error (sin justificación):**
```json
{
  "error": "La justificación es obligatoria para anular una labor"
}
```

---

## 📦 Restauración de Inventario

### **Servicio: `InventarioService.restaurarInventarioLabor()`**

```java
public void restaurarInventarioLabor(
    List<LaborInsumo> insumosLabor, 
    User usuario, 
    String motivo
)
```

#### **Proceso:**

1. **Por cada insumo de la labor:**
   ```java
   - Obtener el insumo del inventario
   - Sumar la cantidad usada al stock actual
   - Guardar el insumo actualizado
   - Registrar movimiento de ENTRADA
   ```

2. **Auditoría completa:**
   - Tipo: `ENTRADA`
   - Cantidad: Cantidad que se había usado
   - Motivo: "Cancelación de labor planificada" o "Anulación de labor: {justificacion}"
   - Usuario: Usuario que realizó la acción
   - Labor: Labor cancelada/anulada

---

## 🔍 Ejemplos de Uso

### **Ejemplo 1: Cancelar Labor Planificada**

```javascript
// Escenario: Se creó una labor de fertilización pero no se ejecutó
// Estado: PLANIFICADA
// Insumos usados: 50kg de fertilizante

// 1. Usuario elimina la labor
await fetch('/api/labores/123', { method: 'DELETE' });

// Resultado:
// ✅ Labor estado = CANCELADA
// ✅ Labor activo = false
// ✅ Inventario fertilizante: +50kg restaurados
// ✅ Movimiento registrado: ENTRADA, "Cancelación de labor planificada"
```

---

### **Ejemplo 2: Anular Labor Completada (con restauración)**

```javascript
// Escenario: Se completó una cosecha pero los datos eran incorrectos
// Estado: COMPLETADA
// Insumos usados: 100L de combustible

// 1. Admin intenta eliminar (falla)
await fetch('/api/labores/456', { method: 'DELETE' });
// ❌ Error: "Requiere anulación formal"

// 2. Admin anula formalmente
await fetch('/api/labores/456/anular', {
  method: 'POST',
  body: JSON.stringify({
    justificacion: "Datos de cosecha incorrectos, registrados en lote equivocado",
    restaurarInsumos: true
  })
});

// Resultado:
// ✅ Labor estado = ANULADA
// ✅ Labor activo = false
// ✅ motivoAnulacion = "Datos de cosecha incorrectos..."
// ✅ fechaAnulacion = 2025-09-30 14:35:22
// ✅ usuarioAnulacion = admin@agrocloud.com
// ✅ Inventario combustible: +100L restaurados
// ✅ Movimiento registrado: ENTRADA, "Anulación de labor: Datos de cosecha incorrectos..."
```

---

### **Ejemplo 3: Anular Labor sin Restaurar Insumos**

```javascript
// Escenario: Labor de siembra se ejecutó con semilla defectuosa
// Estado: COMPLETADA
// Insumos usados: 200kg de semilla

// Admin decide anular SIN restaurar porque la semilla ya se usó
await fetch('/api/labores/789/anular', {
  method: 'POST',
  body: JSON.stringify({
    justificacion: "Semilla defectuosa confirmada por laboratorio, perdida total",
    restaurarInsumos: false  // ← No restaura
  })
});

// Resultado:
// ✅ Labor estado = ANULADA
// ✅ Labor activo = false
// ✅ motivoAnulacion = "Semilla defectuosa..."
// ✅ fechaAnulacion = 2025-09-30 15:10:45
// ✅ usuarioAnulacion = admin@agrocloud.com
// ❌ Inventario semilla: Sin cambios (ya se usó físicamente)
// ❌ Sin movimientos de inventario
```

---

## 🗄️ Migración de Base de Datos

### **Archivo:** `V1_11__Add_Anulacion_Fields_To_Labores.sql`

```sql
-- Nuevos campos de auditoría
ALTER TABLE labores
ADD COLUMN motivo_anulacion VARCHAR(1000) NULL,
ADD COLUMN fecha_anulacion TIMESTAMP NULL,
ADD COLUMN usuario_anulacion_id BIGINT NULL;

-- Foreign key
ALTER TABLE labores
ADD CONSTRAINT fk_labores_usuario_anulacion 
FOREIGN KEY (usuario_anulacion_id) REFERENCES usuarios(id) ON DELETE SET NULL;

-- Índices para performance
CREATE INDEX idx_labores_usuario_anulacion ON labores(usuario_anulacion_id);
CREATE INDEX idx_labores_fecha_anulacion ON labores(fecha_anulacion);

-- Nuevo estado ANULADA
ALTER TABLE labores 
MODIFY COLUMN estado ENUM('PLANIFICADA', 'EN_PROGRESO', 'COMPLETADA', 'CANCELADA', 'ANULADA') 
DEFAULT 'PLANIFICADA';
```

---

## ✅ Ventajas del Sistema Implementado

### **1. Trazabilidad Completa**
- ✅ Todo cambio queda registrado
- ✅ Se sabe quién, cuándo y por qué se anuló
- ✅ Auditoría completa para cumplimiento normativo

### **2. Prevención de Fraudes**
- ✅ No se puede eliminar labores ejecutadas sin justificación
- ✅ Solo administradores pueden anular labores completadas
- ✅ Todos los movimientos de inventario quedan registrados

### **3. Flexibilidad Operativa**
- ✅ Labores planificadas se cancelan fácilmente
- ✅ Administrador decide si restaurar insumos caso por caso
- ✅ Refleja la realidad operativa del campo

### **4. Gestión Inteligente de Inventario**
- ✅ Restauración automática para labores planificadas
- ✅ Restauración opcional para labores ejecutadas
- ✅ Registro completo de todos los movimientos

### **5. Seguridad y Permisos**
- ✅ Restricción por roles (ADMINISTRADOR)
- ✅ Validación de permisos sobre lotes
- ✅ Justificación obligatoria para anulaciones

---

## 🔧 Métodos Helper Agregados

```java
// En Labor.java
public boolean isPlanificada()
public boolean isEnProgreso()
public boolean isCompletada()
public boolean isCancelada()
public boolean isAnulada()
public boolean puedeEliminarseDirectamente()  // true si PLANIFICADA
public boolean requiereAnulacionFormal()      // true si EN_PROGRESO o COMPLETADA
```

---

## 📊 Diagrama de Flujo

```
┌─────────────────────┐
│  Usuario elimina    │
│  labor (DELETE)     │
└──────────┬──────────┘
           │
           ▼
    ┌──────────────┐
    │ ¿Estado?     │
    └──────┬───────┘
           │
     ┌─────┴─────────────────────┐
     │                           │
     ▼                           ▼
┌────────────┐          ┌──────────────────┐
│PLANIFICADA │          │ EN_PROGRESO /    │
└─────┬──────┘          │ COMPLETADA       │
      │                 └────────┬─────────┘
      │                          │
      ▼                          ▼
┌──────────────────┐    ┌─────────────────────┐
│ CANCELAR         │    │ ERROR: Requiere     │
│ ✅ Estado=CANCEL │    │ anulación formal    │
│ ✅ Restaura ins. │    └─────────────────────┘
│ ✅ activo=false  │              │
└──────────────────┘              │
                                  ▼
                      ┌──────────────────────┐
                      │ Admin usa POST       │
                      │ /labores/{id}/anular │
                      │ con justificación    │
                      └──────────┬───────────┘
                                 │
                                 ▼
                      ┌──────────────────────┐
                      │ ANULAR               │
                      │ ✅ Estado=ANULADA    │
                      │ ⚙️ Restaura si elige│
                      │ ✅ Auditoría completa│
                      │ ✅ activo=false      │
                      └──────────────────────┘
```

---

## 🚀 Próximos Pasos Recomendados

### **Frontend:**
1. Agregar botón "Anular" solo visible para administradores
2. Modal de confirmación con campo de justificación
3. Checkbox para decidir si restaurar insumos
4. Mostrar campos de auditoría en vista de detalles

### **Backend (Opcional):**
1. Endpoint para consultar historial de anulaciones
2. Reporte de labores anuladas por período
3. Notificaciones cuando se anula una labor

---

## 📝 Notas Importantes

1. **La migración V1_11 debe ejecutarse** en la base de datos antes de usar el sistema
2. **Los permisos se validan** tanto en backend como deben validarse en frontend
3. **La justificación es obligatoria** para anulaciones (máx. 1000 caracteres)
4. **Los insumos se restauran con auditoría completa** en la tabla `movimientos_inventario`
5. **El sistema mantiene compatibilidad** con labores existentes

---

## ✅ Confirmación Final

### **¿Los insumos vuelven a estar disponibles al eliminar una labor?**

**Respuesta: SÍ, pero de forma inteligente:**

✅ **Labor PLANIFICADA** → Se restauran AUTOMÁTICAMENTE
✅ **Labor ejecutada** → Solo si el ADMIN lo decide al anular
✅ **Todos los movimientos quedan registrados** para auditoría

---

**Implementado por:** AgroGestion Team  
**Fecha:** 30 de Septiembre, 2025  
**Versión:** 1.0.0
