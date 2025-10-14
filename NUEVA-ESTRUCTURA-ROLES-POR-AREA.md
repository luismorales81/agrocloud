# 🎯 Nueva Estructura de Roles por Área

## 📋 Resumen

Se ha implementado una **simplificación de roles** basada en áreas de responsabilidad para eliminar funcionalidades redundantes y facilitar la gestión de permisos.

---

## 🔄 Cambios Realizados

### **Antes (8 roles con superposición):**
- SUPERADMIN
- ADMINISTRADOR
- PRODUCTOR *(permisos muy similares)*
- ASESOR *(permisos muy similares)*
- TÉCNICO *(permisos muy similares)*
- CONTADOR *(permisos similares entre sí)*
- OPERARIO
- LECTURA

### **Ahora (6 roles diferenciados):**
- **SUPERADMIN** - Administrador global del sistema
- **ADMINISTRADOR** - Gerente de empresa
- **JEFE_CAMPO** - Responsable de operaciones agrícolas *(fusión de PRODUCTOR + ASESOR + TÉCNICO)*
- **JEFE_FINANCIERO** - Responsable de finanzas *(reemplaza a CONTADOR)*
- **OPERARIO** - Ejecutor de labores de campo
- **CONSULTOR_EXTERNO** - Solo lectura *(reemplaza a LECTURA)*

---

## 📊 Matriz de Permisos por Rol

| **Funcionalidad** | **SUPER ADMIN** | **ADMINISTRADOR** | **JEFE CAMPO** | **JEFE FINANCIERO** | **OPERARIO** | **CONSULTOR** |
|-------------------|-----------------|-------------------|----------------|---------------------|--------------|---------------|
| **🏢 Gestión Usuarios** | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| **🏢 Gestión Empresas** | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| **🗺️ Campos** | ❌ | ✅ | ✅ | 👁️ | 👁️ | 👁️ |
| **📦 Lotes** | ❌ | ✅ | ✅ | 👁️ | 👁️ | 👁️ |
| **🌾 Cultivos** | ❌ | ✅ | ✅ | 👁️ | 👁️ | 👁️ |
| **🧪 Insumos** | ❌ | ✅ | ✅ | 👁️ | 👁️ | 👁️ |
| **🚜 Maquinaria** | ❌ | ✅ | ✅ | 👁️ | 👁️ | 👁️ |
| **👷 Labores** | ❌ | ✅ | ✅ | 👁️ | ✅ | 👁️ |
| **🌽 Cosechas** | ❌ | ✅ | ✅ | 👁️ | ✅ | 👁️ |
| **💰 Finanzas** | ❌ | ✅ | ❌ | ✅ | ❌ | ❌ |
| **📊 Reportes Operativos** | ❌ | ✅ | ✅ | 👁️ | 👁️ | ✅ |
| **📊 Reportes Financieros** | ❌ | ✅ | ❌ | ✅ | ❌ | ❌ |

**Leyenda:**
- ✅ = Crear/Editar/Eliminar
- 👁️ = Solo Lectura
- ❌ = Sin Acceso

---

## 🔍 Descripción de Roles

### 👨‍💼 **ADMINISTRADOR**
**¿Quién?** Gerente general, dueño de la empresa, administrador de operaciones.

**Responsabilidad:** Gestión completa de la empresa.

**Puede hacer:**
- ✅ Gestionar usuarios y permisos
- ✅ Crear/editar/eliminar campos, lotes, cultivos
- ✅ Gestionar insumos y maquinaria
- ✅ Planificar y aprobar labores
- ✅ Gestión financiera completa
- ✅ Todos los reportes

**NO puede:** *(Solo SUPERADMIN)*
- ❌ Gestionar empresas globales

---

### 👨‍🌾 **JEFE_CAMPO**
**¿Quién?** Productor agropecuario, ingeniero agrónomo, técnico agrícola, responsable de campo.

**Responsabilidad:** Operaciones y producción agrícola.

**Puede hacer:**
- ✅ Crear/editar/eliminar campos y lotes
- ✅ Planificar cultivos y rotaciones
- ✅ Gestionar insumos y maquinaria
- ✅ Crear y ejecutar labores
- ✅ Registrar cosechas
- ✅ Ver reportes operativos

**NO puede:**
- ❌ Ver o gestionar finanzas
- ❌ Gestionar usuarios
- ❌ Ver reportes financieros

**Nota:** Este rol unifica PRODUCTOR, ASESOR y TÉCNICO que tenían permisos muy similares.

---

### 💰 **JEFE_FINANCIERO**
**¿Quién?** Contador, administrador financiero, responsable de finanzas.

**Responsabilidad:** Gestión financiera y contable de la empresa.

**Puede hacer:**
- ✅ Gestión completa de finanzas (ingresos/egresos)
- ✅ Ver costos de operaciones
- ✅ Reportes financieros
- 👁️ Ver operaciones (campos, labores, cosechas) para contexto

**NO puede:**
- ❌ Crear o modificar campos, lotes, labores
- ❌ Gestionar usuarios
- ❌ Modificar inventarios

**Nota:** Reemplaza al rol CONTADOR con permisos más claros.

---

### 👷‍♂️ **OPERARIO**
**¿Quién?** Trabajador de campo, operador de maquinaria, personal operativo.

**Responsabilidad:** Ejecución de tareas asignadas.

**Puede hacer:**
- ✅ Registrar labores ejecutadas
- ✅ Registrar cosechas
- 👁️ Ver campos, lotes y tareas asignadas

**NO puede:**
- ❌ Crear campos o lotes
- ❌ Planificar cultivos
- ❌ Ver finanzas
- ❌ Gestionar insumos o maquinaria

---

### 👁️ **CONSULTOR_EXTERNO**
**¿Quién?** Asesor externo, auditor, invitado, consultor.

**Responsabilidad:** Visualización y análisis sin modificación.

**Puede hacer:**
- 👁️ Ver todas las operaciones
- ✅ Exportar reportes operativos

**NO puede:**
- ❌ Crear o modificar nada
- ❌ Ver finanzas
- ❌ Ver reportes financieros

**Nota:** Reemplaza a LECTURA con un nombre más descriptivo.

---

## 🔧 Implementación Técnica

### Backend (Java)

#### `RolEmpresa.java`
```java
public enum RolEmpresa {
    SUPERADMIN("Super Administrador"),
    ADMINISTRADOR("Administrador de Empresa"),
    JEFE_CAMPO("Jefe de Campo"),
    JEFE_FINANCIERO("Jefe Financiero"),
    OPERARIO("Operario de Campo"),
    CONSULTOR_EXTERNO("Consultor Externo"),
    
    // Roles legacy @Deprecated
    PRODUCTOR, ASESOR, TECNICO, CONTADOR, LECTURA;
    
    // Método de migración automática
    public RolEmpresa getRolActualizado() {
        return switch (this) {
            case PRODUCTOR, ASESOR, TECNICO -> JEFE_CAMPO;
            case CONTADOR -> JEFE_FINANCIERO;
            case LECTURA -> CONSULTOR_EXTERNO;
            default -> this;
        };
    }
}
```

#### `PermissionService.java`
- ✅ Métodos actualizados: `getJefeCampoPermissions()`, `getJefeFinancieroPermissions()`, `getConsultorExternoPermissions()`
- ✅ Retrocompatibilidad con roles antiguos

### Frontend (React/TypeScript)

#### `EmpresaContext.tsx`
```typescript
interface UsuarioEmpresa {
  rol: 'ADMINISTRADOR' | 'JEFE_CAMPO' | 'JEFE_FINANCIERO' | 
       'OPERARIO' | 'CONSULTOR_EXTERNO' |
       // Legacy
       'PRODUCTOR' | 'ASESOR' | 'TECNICO' | 'CONTADOR' | 'LECTURA';
}

// Nuevos helpers
const esJefeCampo = () => rolUsuario === 'JEFE_CAMPO' || 
                          rolUsuario === 'PRODUCTOR' || ...;
const esJefeFinanciero = () => ...;
const esConsultorExterno = () => ...;
```

#### `usePermissions.ts`
- ✅ Permisos actualizados basados en nuevos roles
- ✅ Simplificación de lógica
- ✅ Retrocompatibilidad

### Base de Datos

#### Script de Migración: `migrar-roles-por-area.sql`
```sql
-- Agregar nuevos roles
INSERT INTO roles (name, descripcion) VALUES 
  ('JEFE_CAMPO', 'Jefe de Campo'),
  ('JEFE_FINANCIERO', 'Jefe Financiero'),
  ('CONSULTOR_EXTERNO', 'Consultor Externo');

-- Migrar usuarios
UPDATE usuarios_empresas_roles 
SET rol_empresa = 'JEFE_CAMPO' 
WHERE rol_empresa IN ('PRODUCTOR', 'ASESOR', 'TECNICO');

UPDATE usuarios_empresas_roles 
SET rol_empresa = 'JEFE_FINANCIERO' 
WHERE rol_empresa = 'CONTADOR';

UPDATE usuarios_empresas_roles 
SET rol_empresa = 'CONSULTOR_EXTERNO' 
WHERE rol_empresa = 'LECTURA';

-- Deprecar roles antiguos
UPDATE roles SET activo = 0 
WHERE name IN ('PRODUCTOR', 'ASESOR', 'TECNICO', 'CONTADOR', 'LECTURA');
```

---

## 📦 Archivos Modificados

### Backend
- ✅ `RolEmpresa.java` - Enum con nuevos roles y método de migración
- ✅ `PermissionService.java` - Permisos por área

### Frontend
- ✅ `EmpresaContext.tsx` - Helpers de roles actualizados
- ✅ `usePermissions.ts` - Lógica de permisos simplificada
- ✅ `LotesManagement.tsx` - Protección con PermissionGate
- ✅ `FinanzasManagement.tsx` - Protección con PermissionGate

### Base de Datos
- ✅ `migrar-roles-por-area.sql` - Script de migración
- ✅ `aplicar-migracion-roles-area.bat` - Ejecutor de migración

### Documentación
- ✅ `NUEVA-ESTRUCTURA-ROLES-POR-AREA.md` - Este archivo
- 🔄 `CREDENCIALES-PRUEBA.md` - *Pendiente actualizar*

---

## 🚀 Cómo Aplicar la Migración

### Paso 1: Ejecutar Migración SQL
```bash
# Windows
aplicar-migracion-roles-area.bat

# Linux/Mac
mysql -u root -p agrocloud < migrar-roles-por-area.sql
```

### Paso 2: Reiniciar Backend
```bash
cd agrogestion-backend
mvn spring-boot:run
```

### Paso 3: Reiniciar Frontend
```bash
cd agrogestion-frontend
npm start
```

### Paso 4: Verificar
1. Iniciar sesión con diferentes usuarios
2. Verificar que los permisos sean correctos
3. Revisar que no haya errores en consola

---

## ✅ Ventajas de la Nueva Estructura

### 1. **Simplicidad**
- De 8 roles a 6 roles
- Menos confusión para administradores
- Más fácil de explicar a usuarios

### 2. **Claridad**
- Cada rol tiene un propósito específico
- No hay superposición de funcionalidades
- Nombres descriptivos

### 3. **Mantenibilidad**
- Menos código duplicado
- Más fácil agregar nuevos permisos
- Lógica centralizada

### 4. **Retrocompatibilidad**
- Los roles antiguos siguen funcionando
- Migración automática en el código
- Sin pérdida de datos

---

## 🔄 Retrocompatibilidad

Los roles antiguos se mantienen funcionando mediante:

1. **Mapeo automático en backend:**
   ```java
   PRODUCTOR → JEFE_CAMPO
   ASESOR → JEFE_CAMPO
   TECNICO → JEFE_CAMPO
   CONTADOR → JEFE_FINANCIERO
   LECTURA → CONSULTOR_EXTERNO
   ```

2. **Helpers legacy en frontend:**
   ```typescript
   esProductor() // Devuelve true si es JEFE_CAMPO o PRODUCTOR
   esAsesor()    // Devuelve true si es JEFE_CAMPO o ASESOR
   esContador()  // Devuelve true si es JEFE_FINANCIERO o CONTADOR
   ```

---

## 📝 Próximos Pasos

1. ✅ **Aplicar migración SQL** a base de datos de producción
2. 🔄 **Actualizar credenciales de prueba** en `CREDENCIALES-PRUEBA.md`
3. 🔄 **Notificar a usuarios** sobre el cambio de roles
4. 🔄 **Actualizar documentación de usuario**
5. 🔄 **Crear tests automatizados** para verificar permisos

---

## 📞 Soporte

Si tienes problemas con la migración:
1. Revisa los logs del backend
2. Verifica que la migración SQL se ejecutó correctamente
3. Limpia caché del navegador y localStorage
4. Reinicia ambos servicios (backend + frontend)

---

**Fecha de implementación:** 2025-10-08  
**Versión:** 2.0.0




