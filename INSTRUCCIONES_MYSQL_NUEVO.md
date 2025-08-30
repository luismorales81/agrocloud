# 🚀 Instrucciones para Configurar MySQL - AgroCloud

## 📋 Estado Actual
- ✅ MySQL ejecutándose en puerto 3306
- ✅ phpMyAdmin accesible en http://localhost/phpmyadmin
- ✅ Script SQL preparado: `setup-mysql-simple.sql`

## 🔧 Pasos para Configurar la Base de Datos

### Paso 1: Abrir phpMyAdmin
1. Ve a: http://localhost/phpmyadmin
2. Inicia sesión con:
   - **Usuario**: `root`
   - **Contraseña**: (deja vacío por defecto)

### Paso 2: Ejecutar Script SQL
1. En phpMyAdmin, haz clic en la pestaña **"SQL"**
2. Copia TODO el contenido del archivo `setup-mysql-simple.sql`
3. Pega el contenido en el área de texto SQL
4. Haz clic en **"Continuar"** para ejecutar

### Paso 3: Verificar Configuración
Después de ejecutar el script, deberías ver:
- ✅ Base de datos `agroclouddb` creada
- ✅ Usuario `agrocloudbd` creado
- ✅ Tablas del sistema creadas
- ✅ Roles por defecto insertados
- ✅ Usuario administrador creado

## 📊 Contenido del Script SQL

El script `setup-mysql-simple.sql` creará:

### Base de Datos y Usuario
- **Base de datos**: `agroclouddb`
- **Usuario**: `agrocloudbd`
- **Contraseña**: `Jones1212`

### Tablas del Sistema
1. **roles** - Roles de usuario del sistema
2. **usuarios** - Información de usuarios
3. **usuario_roles** - Relación usuario-rol
4. **campos** - Gestión de campos agrícolas
5. **lotes** - Gestión de lotes de cultivo
6. **insumos** - Gestión de insumos agrícolas
7. **maquinaria** - Gestión de maquinaria
8. **labores** - Gestión de labores agrícolas

### Datos Iniciales
- **Roles**: ADMINISTRADOR, OPERARIO, INGENIERO_AGRONOMO, INVITADO
- **Usuario Admin**: admin@agrocloud.com / admin123

## 🎯 Después de Configurar la Base de Datos

Una vez que hayas ejecutado el script SQL exitosamente:

### Ejecutar el Sistema Completo
```powershell
.\start-project-mysql.bat
```

### Verificar Funcionamiento
- **Frontend**: http://localhost:3000
- **Backend**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **phpMyAdmin**: http://localhost/phpmyadmin

## 🔍 Verificación de Estado

### Verificar Base de Datos
1. En phpMyAdmin, verifica que existe la base de datos `agroclouddb`
2. Verifica que existen las tablas del sistema
3. Verifica que existe el usuario `agrocloudbd`

### Verificar Servicios
```powershell
# Verificar MySQL
netstat -ano | findstr ":3306"

# Verificar Backend
netstat -ano | findstr ":8080"

# Verificar Frontend
netstat -ano | findstr ":3000"
```

## 🛠️ Solución de Problemas

### Error: "Access denied for user 'agrocloudbd'"
1. Verifica que el script SQL se ejecutó completamente
2. Verifica que el usuario `agrocloudbd` existe en phpMyAdmin
3. Verifica que tiene permisos en la base de datos `agroclouddb`

### Error: "Database 'agroclouddb' doesn't exist"
1. Ejecuta nuevamente el script SQL en phpMyAdmin
2. Verifica que no haya errores en la ejecución

### Error: "Table doesn't exist"
1. Verifica que todas las tablas se crearon correctamente
2. Ejecuta el script SQL nuevamente

## ✅ Estado Final Esperado

Después de completar todos los pasos:

- ✅ Base de datos `agroclouddb` configurada
- ✅ Usuario `agrocloudbd` con permisos
- ✅ Todas las tablas creadas
- ✅ Datos iniciales insertados
- ✅ Backend conectado a MySQL
- ✅ Frontend ejecutándose
- ✅ Sistema completamente operativo

## 🎉 ¡Listo para Usar!

Una vez configurado, podrás:
- Acceder al sistema en http://localhost:3000
- Iniciar sesión con: admin@agrocloud.com / admin123
- Gestionar campos, lotes, insumos y maquinaria
- Administrar usuarios y roles
- Ver datos persistentes en MySQL

---
**Fecha**: 25 de Agosto, 2025
**Estado**: ✅ Configuración lista
