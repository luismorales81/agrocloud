# 🐛 Bug: Usuario OPERARIO muestra "Jefe Campo" en el menú lateral

## 📋 Descripción del Problema

El usuario **Luis Operario** (`operario.luis@agrocloud.com`) con rol **OPERARIO** está mostrando incorrectamente **"Jefe Campo"** en el menú lateral del dashboard.

### Lo que se ve:
```
Perfil
Luis Operario
Jefe Campo  ← ❌ INCORRECTO (debería decir "Operario")
```

### Lo que debería verse:
```
Perfil
Luis Operario
Operario  ← ✅ CORRECTO
```

---

## 🔍 Causa Probable

Hay **DOS posibles causas**:

### Causa 1: Rol incorrecto en la Base de Datos
El usuario Luis puede tener asignado el rol **JEFE_CAMPO** en lugar de **OPERARIO** en la tabla `usuario_empresas` o `usuarios_empresas_roles`.

Esto pudo haber ocurrido durante la migración de roles antiguos.

### Causa 2: Error en el mapeo del frontend
El frontend está obteniendo el rol incorrecto del contexto de empresa.

---

## 🔧 Solución

He creado dos archivos para diagnosticar y corregir el problema:

### 1. Script SQL: `verificar-y-corregir-rol-luis.sql`

Este script:
- ✅ Verifica los datos del usuario Luis
- ✅ Verifica el rol en `usuario_empresas`
- ✅ Verifica el rol en `usuarios_empresas_roles`
- ✅ **CORRIGE** el rol a OPERARIO si está incorrecto
- ✅ Muestra resumen de la corrección

### 2. Script Batch: `verificar-y-corregir-rol-luis.bat`

Ejecuta el script SQL automáticamente.

---

## 🚀 Cómo Aplicar la Corrección

### Paso 1: Ejecutar el script
```bash
.\verificar-y-corregir-rol-luis.bat
```

O manualmente con MySQL:
```bash
mysql -u root -p agrocloud < verificar-y-corregir-rol-luis.sql
```

### Paso 2: Cerrar sesión en el frontend
- Hacer logout del usuario Luis
- Cerrar la pestaña del navegador

### Paso 3: Volver a iniciar sesión
```
Email: operario.luis@agrocloud.com
Contraseña: admin123
```

### Paso 4: Verificar que ahora dice correctamente
```
Perfil
Luis Operario
Operario  ← ✅ CORRECTO
```

---

## 📊 Verificación del Problema

El script SQL mostrará algo como esto:

### Si el rol está incorrecto:
```sql
🏢 ROL EN TABLA USUARIO_EMPRESAS:
email                           | rol         | estado
--------------------------------|-------------|------------------
operario.luis@agrocloud.com     | JEFE_CAMPO  | ❌ INCORRECTO

🎭 ROL EN TABLA USUARIOS_EMPRESAS_ROLES:
email                           | ROL_ACTUAL  | estado
--------------------------------|-------------|------------------
operario.luis@agrocloud.com     | JEFE_CAMPO  | ❌ INCORRECTO
```

### Después de la corrección:
```sql
✅ VERIFICACIÓN POST-CORRECCIÓN:
email                           | rol         | estado
--------------------------------|-------------|------------------
operario.luis@agrocloud.com     | OPERARIO    | ✅ TODO CORRECTO

🎯 RESUMEN:
✅ Luis tiene el rol OPERARIO asignado correctamente (1 empresa(s))
```

---

## 🎯 Permisos Correctos para OPERARIO

Una vez corregido, Luis Operario debería tener:

### ✅ Puede hacer:
- Registrar labores realizadas
- Registrar cosechas
- Ver campos y lotes asignados (solo lectura)

### ❌ NO puede hacer:
- Crear/editar/eliminar campos
- Crear/editar/eliminar lotes
- Crear/editar cultivos, insumos o maquinaria
- Ver información financiera
- Acceder a gestión de usuarios

---

## 📝 Otros Usuarios OPERARIO

Si tienes otros usuarios con rol OPERARIO, verifica que también tengan el rol correcto:

```sql
-- Verificar TODOS los operarios
SELECT 
    u.email,
    CONCAT(u.first_name, ' ', u.last_name) as nombre,
    e.nombre as empresa,
    ue.rol
FROM usuario_empresas ue
INNER JOIN usuarios u ON ue.usuario_id = u.id
INNER JOIN empresas e ON ue.empresa_id = e.id
WHERE ue.rol = 'OPERARIO';
```

---

## 🔄 ¿Por qué pudo haber pasado?

Durante la migración de roles, es posible que:

1. Luis originalmente no tenía el rol OPERARIO asignado
2. El script de migración lo asignó a JEFE_CAMPO por defecto
3. No se creó correctamente en las tablas de roles por empresa

---

## ⚠️ Importante: Limpiar Caché del Navegador

Después de ejecutar el script SQL, **ES CRUCIAL**:

### 1. Cerrar sesión completamente
- Logout del usuario Luis
- Cerrar todas las pestañas del navegador

### 2. Limpiar caché (opcional pero recomendado)
- **Chrome/Edge**: Ctrl + Shift + Delete
- Seleccionar "Últimas 24 horas"
- Marcar "Cookies y datos de sitios" 
- Limpiar

### 3. Abrir navegador en modo incógnito (para probar)
- **Chrome/Edge**: Ctrl + Shift + N
- Ir a http://localhost:3000
- Login con Luis

---

## 📋 Checklist de Verificación

Después de aplicar la corrección:

- [ ] ✅ Script SQL ejecutado sin errores
- [ ] ✅ Usuario Luis tiene rol OPERARIO en BD
- [ ] ✅ Cerrada sesión en el frontend
- [ ] ✅ Limpiado caché del navegador
- [ ] ✅ Iniciada sesión nuevamente
- [ ] ✅ El menú lateral muestra "Operario"
- [ ] ✅ NO aparece información financiera
- [ ] ✅ NO aparece botón de rentabilidad en reportes
- [ ] ✅ Puede ver campos/lotes (solo lectura)
- [ ] ✅ Puede registrar labores

---

## 🎓 Lección Aprendida

**Problema:** Durante las migraciones de roles, algunos usuarios pueden quedar con roles incorrectos si:
- No se crearon originalmente con el rol correcto
- La migración no contempló todos los casos
- Hay inconsistencias entre tablas

**Solución:** Siempre verificar la asignación de roles después de migraciones con scripts de verificación.

---

## 📅 Fecha del Bug

**Detectado:** 9 de Octubre, 2025  
**Prioridad:** Media (Afecta visualización de permisos)  
**Impacto:** Usuario ve información incorrecta sobre su rol, pero los permisos funcionales están correctos

---

## ✅ Estado

- ✅ Script de verificación creado
- ✅ Script de corrección creado
- ⏳ Pendiente: Ejecutar script y verificar


