# Implementación de Guardado de Maquinaria y Mano de Obra

## 🎯 **Problema Identificado**

Los datos de maquinaria y mano de obra se están enviando al backend pero no se están guardando en la base de datos. Solo se guarda la labor básica.

## 🔧 **Solución Propuesta**

### **1. Crear Endpoints Específicos**

#### **Endpoint para Guardar Maquinaria:**
```java
@PostMapping("/{laborId}/maquinaria")
public ResponseEntity<LaborMaquinaria> agregarMaquinaria(
    @PathVariable Long laborId, 
    @RequestBody LaborMaquinaria maquinaria, 
    @AuthenticationPrincipal UserDetails userDetails) {
    // Implementar guardado de maquinaria
}
```

#### **Endpoint para Guardar Mano de Obra:**
```java
@PostMapping("/{laborId}/mano-obra")
public ResponseEntity<LaborManoObra> agregarManoObra(
    @PathVariable Long laborId, 
    @RequestBody LaborManoObra manoObra, 
    @AuthenticationPrincipal UserDetails userDetails) {
    // Implementar guardado de mano de obra
}
```

### **2. Modificar el Frontend**

#### **Enviar Datos por Separado:**
```typescript
// 1. Crear la labor básica
const response = await fetch('http://localhost:8080/api/labores', {
  method: 'POST',
  body: JSON.stringify(laborBasica)
});

// 2. Si la labor se creó exitosamente, guardar maquinaria
if (response.ok) {
  const laborCreada = await response.json();
  
  // Guardar maquinaria
  for (const maq of selectedMaquinaria) {
    await fetch(`http://localhost:8080/api/labores/${laborCreada.id}/maquinaria`, {
      method: 'POST',
      body: JSON.stringify(maq)
    });
  }
  
  // Guardar mano de obra
  for (const mo of selectedManoObra) {
    await fetch(`http://localhost:8080/api/labores/${laborCreada.id}/mano-obra`, {
      method: 'POST',
      body: JSON.stringify(mo)
    });
  }
}
```

### **3. Verificar Tablas de Base de Datos**

#### **Tabla labor_maquinaria:**
```sql
CREATE TABLE IF NOT EXISTS labor_maquinaria (
    id_labor_maquinaria BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_labor BIGINT NOT NULL,
    tipo ENUM('PROPIA', 'ALQUILADA') NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    proveedor VARCHAR(255),
    costo DECIMAL(10,2) NOT NULL,
    horas_uso DECIMAL(5,2),
    kilometros_recorridos DECIMAL(8,2),
    observaciones TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_labor) REFERENCES labores(id) ON DELETE CASCADE
);
```

#### **Tabla labor_mano_obra:**
```sql
CREATE TABLE IF NOT EXISTS labor_mano_obra (
    id_labor_mano_obra BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_labor BIGINT NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    cantidad_personas INT NOT NULL,
    proveedor VARCHAR(255),
    costo_total DECIMAL(10,2) NOT NULL,
    horas_trabajo DECIMAL(5,2),
    costo_por_hora DECIMAL(8,2),
    observaciones TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_labor) REFERENCES labores(id) ON DELETE CASCADE
);
```

## 📊 **Flujo de Datos Corregido**

### **Proceso Completo:**
1. **Usuario completa** el formulario con maquinaria y mano de obra
2. **Frontend envía** labor básica al backend
3. **Backend crea** la labor y devuelve el ID
4. **Frontend envía** datos de maquinaria por separado
5. **Frontend envía** datos de mano de obra por separado
6. **Backend guarda** todos los datos en sus respectivas tablas
7. **Frontend recarga** los datos para mostrar la información completa

## ✅ **Beneficios de la Implementación**

- ✅ **Datos persistentes** de maquinaria y mano de obra
- ✅ **Relaciones correctas** entre labor y sus componentes
- ✅ **Consultas eficientes** para obtener datos detallados
- ✅ **Integridad referencial** con foreign keys
- ✅ **Escalabilidad** para futuras funcionalidades

## 🔧 **Próximos Pasos**

1. **Verificar** que las tablas existan en la base de datos
2. **Implementar** los endpoints en el backend
3. **Modificar** el frontend para enviar datos por separado
4. **Probar** el flujo completo de creación de labores
5. **Verificar** que los datos se guarden correctamente


