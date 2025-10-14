# Solución: Usuario Consultor Externo No Ve Ningún Listado

## 📋 Problema Reportado

El usuario con rol **"Consultor Externo"** no puede ver ningún listado:
- No ve insumos
- No ve lotes
- No ve campos
- No ve ninguna opción del menú con datos

Según la definición del rol, este usuario **DEBERÍA poder**:
- ✅ Ver todos los campos (solo lectura)
- ✅ Ver todos los lotes (solo lectura)
- ✅ Ver todos los cultivos (solo lectura)
- ✅ Ver cosechas registradas (solo lectura)
- ✅ Ver insumos (solo lectura)
- ✅ Ver maquinaria (solo lectura)
- ✅ Ver labores ejecutadas (solo lectura)
- ✅ Ver reportes operativos
- ❌ NO puede ver finanzas

## 🔍 Diagnóstico Realizado

### 1. Verificación del Backend

El backend tiene correctamente implementado el rol CONSULTOR_EXTERNO:

**Archivo:** `PermissionService.java`
```java
private Set<String> getConsultorExternoPermissions() {
    Set<String> permissions = new HashSet<>();
    
    // Solo lectura de todo
    permissions.add("canViewFields");
    permissions.add("canViewLotes");
    permissions.add("canViewCultivos");
    permissions.add("canViewCosechas");
    permissions.add("canViewInsumos");
    permissions.add("canViewMaquinaria");
    permissions.add("canViewLabores");
    
    // Reportes (sin acceso a finanzas)
    permissions.add("canViewReports");
    permissions.add("canExportReports");
    
    return permissions;
}
```

**Archivo:** `InsumoService.java` (y todos los demás servicios)
```java
public List<Insumo> getInsumosByUser(User user) {
    if (user.isAdmin() || 
        user.tieneRolEnEmpresa(RolEmpresa.JEFE_CAMPO) || 
        user.tieneRolEnEmpresa(RolEmpresa.OPERARIO) ||
        user.tieneRolEnEmpresa(RolEmpresa.CONSULTOR_EXTERNO)) {
        // CONSULTOR_EXTERNO ve todos los insumos de la empresa
        return insumoRepository.findAll();
    }
    ...
}
```

### 2. Verificación del Frontend

El frontend también está configurado correctamente:

**Archivo:** `EmpresaContext.tsx`
```typescript
const esConsultorExterno = () => rolUsuario === 'CONSULTOR_EXTERNO' || 
                                   rolUsuario === 'LECTURA';
```

**Archivo:** `usePermissions.ts`
```typescript
canViewInsumos: !isGlobalSuperAdmin,  // Todos excepto SUPERADMIN pueden ver insumos
```

## 🎯 Posibles Causas

1. **El usuario no tiene correctamente asignado el rol en la base de datos**
   - El rol podría estar mal guardado en la tabla `usuario_empresas`
   - El estado del usuario podría estar inactivo
   - La relación usuario-empresa podría no existir

2. **El usuario no tiene una empresa asignada**
   - Sin empresa, el backend devuelve listas vacías

3. **Problema de serialización del rol**
   - El enum `RolEmpresa` no se está serializando correctamente al JSON

## 🔧 Soluciones Implementadas

### 1. Mejora en la Serialización del DTO

**Archivo:** `UsuarioEmpresaDTO.java`

Se agregaron anotaciones de Jackson para asegurar la correcta serialización:

```java
@JsonProperty("rol")
private RolEmpresa rol;

@JsonProperty("estado")
private EstadoUsuarioEmpresa estado;
```

### 2. Endpoint de Diagnóstico

**Nuevo archivo:** `DiagnosticoController.java`

Se creó un controlador especial para diagnosticar problemas de permisos:

- **GET** `/api/diagnostico/mi-info` - Muestra toda la información del usuario:
  - Email, nombre, estado
  - Empresas a las que pertenece
  - Roles que tiene
  - Permisos calculados

- **GET** `/api/diagnostico/verificar-acceso-insumos` - Verifica específicamente el acceso a insumos

### 3. Herramientas de Diagnóstico

Se crearon archivos para facilitar el diagnóstico:

1. **`verificar-rol-consultor-externo.sql`** - Script SQL para verificar el rol en la BD
2. **`verificar-rol-consultor-externo.bat`** - Ejecuta el script SQL
3. **`test-diagnostico-usuario.html`** - Página web para probar el acceso
4. **`diagnosticar-usuario-consultor.bat`** - Script automatizado completo

## 📝 Pasos para Solucionar

### Opción 1: Usar el Script Automatizado (Recomendado)

1. Ejecuta `diagnosticar-usuario-consultor.bat`
2. Espera a que compile el backend
3. Reinicia el backend manualmente
4. La página de diagnóstico se abrirá automáticamente
5. Inicia sesión con el usuario "Consultor Externo"
6. Haz clic en cada botón de diagnóstico
7. Revisa los resultados en formato JSON

### Opción 2: Diagnóstico Manual

#### Paso 1: Verificar el Rol en la Base de Datos

Ejecuta el script SQL:

```bash
mysql -u root -p agrogestion_db < verificar-rol-consultor-externo.sql
```

O ejecuta el archivo `.bat`:
```
verificar-rol-consultor-externo.bat
```

**Buscar:**
- ¿El usuario existe en la tabla `usuarios`?
- ¿Tiene una relación en la tabla `usuario_empresas`?
- ¿Qué rol tiene asignado?
- ¿El estado es 'ACTIVO'?

#### Paso 2: Compilar y Reiniciar el Backend

```bash
cd agrogestion-backend
mvn clean package -DskipTests
```

Luego reinicia el backend:
```bash
cd ..
iniciar-backend.bat
```

#### Paso 3: Probar con la Página de Diagnóstico

1. Abre `test-diagnostico-usuario.html` en tu navegador
2. Ingresa las credenciales del usuario Consultor Externo
3. Haz clic en "Iniciar Sesión"
4. Haz clic en "Ver Mi Información" y revisa:
   - ¿Qué empresas tiene asignadas?
   - ¿Qué rol tiene en cada empresa?
   - ¿Qué permisos tiene?
5. Haz clic en "Verificar Acceso a Insumos" y verifica:
   - `tieneRolConsultorExterno`: debería ser `true`
   - `deberiaVerInsumos`: debería ser `true`
6. Haz clic en "Obtener Lista de Insumos":
   - Si devuelve un array vacío `[]`, es porque no hay insumos cargados
   - Si devuelve un error, hay un problema de permisos

#### Paso 4: Acciones Correctivas

**Si el usuario NO tiene el rol en la BD:**

```sql
-- Verificar el ID del usuario
SELECT id, email, first_name, last_name FROM usuarios WHERE email = 'consultor@agrogestion.com';

-- Verificar las empresas disponibles
SELECT id, nombre FROM empresas;

-- Asignar el rol CONSULTOR_EXTERNO al usuario en una empresa
INSERT INTO usuario_empresas (usuario_id, empresa_id, rol, estado, fecha_inicio, fecha_creacion, fecha_actualizacion)
VALUES (
    [ID_USUARIO],  -- Reemplazar con el ID del usuario
    [ID_EMPRESA],  -- Reemplazar con el ID de la empresa
    'CONSULTOR_EXTERNO',
    'ACTIVO',
    CURDATE(),
    NOW(),
    NOW()
);
```

**Si el usuario tiene el rol LECTURA (rol antiguo):**

El sistema debería mapear automáticamente `LECTURA` a `CONSULTOR_EXTERNO`, pero si no funciona:

```sql
UPDATE usuario_empresas
SET rol = 'CONSULTOR_EXTERNO'
WHERE rol = 'LECTURA'
  AND usuario_id = [ID_USUARIO];
```

**Si el estado está INACTIVO:**

```sql
UPDATE usuario_empresas
SET estado = 'ACTIVO'
WHERE usuario_id = [ID_USUARIO];
```

## 🧪 Verificación Final

Después de aplicar las correcciones:

1. **Reinicia el backend**
2. **Cierra sesión y vuelve a iniciar sesión** en el frontend
3. Verifica que puedas acceder a:
   - ✅ Insumos
   - ✅ Maquinaria
   - ✅ Campos
   - ✅ Lotes
   - ✅ Cultivos
   - ✅ Cosechas
   - ✅ Labores
   - ✅ Reportes operativos
4. Verifica que NO puedas acceder a:
   - ❌ Finanzas
   - ❌ Gestión de usuarios
   - ❌ Creación/edición de recursos

## 📞 Siguiente Paso

Por favor:

1. **Ejecuta el script de diagnóstico**:
   ```
   diagnosticar-usuario-consultor.bat
   ```

2. **Comparte los resultados** de los 4 botones de diagnóstico:
   - Ver Mi Información
   - Verificar Acceso a Insumos
   - Obtener Lista de Insumos
   - Obtener Mis Empresas

3. Si encuentras algún error, **copia y pega el mensaje de error completo**

Con esa información podré identificar exactamente dónde está el problema y proporcionar la solución específica.

---

## 📚 Archivos Creados/Modificados

### Nuevos Archivos:
1. `agrogestion-backend/src/main/java/com/agrocloud/controller/DiagnosticoController.java`
2. `verificar-rol-consultor-externo.sql`
3. `verificar-rol-consultor-externo.bat`
4. `test-diagnostico-usuario.html`
5. `diagnosticar-usuario-consultor.bat`
6. `SOLUCION-CONSULTOR-EXTERNO-SIN-ACCESO.md` (este archivo)

### Archivos Modificados:
1. `agrogestion-backend/src/main/java/com/agrocloud/dto/UsuarioEmpresaDTO.java` - Mejorada serialización

---

**Fecha:** 10 de Octubre de 2025  
**Estado:** Pendiente de verificación por parte del usuario

