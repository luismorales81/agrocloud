# Configuración de AgroCloud con MySQL (XAMPP)

## 📋 Prerrequisitos

1. **XAMPP instalado y ejecutándose**
   - MySQL debe estar iniciado en el puerto 3306
   - Apache debe estar iniciado para phpMyAdmin

2. **Verificar que MySQL esté ejecutándose**
   ```powershell
   netstat -ano | findstr ":3306"
   ```

## 🚀 Configuración Paso a Paso

### Paso 1: Configurar Base de Datos MySQL

1. **Ejecutar el script de configuración**:
   ```powershell
   .\setup-mysql-phpmyadmin.bat
   ```

2. **Abrir phpMyAdmin**:
   - URL: http://localhost/phpmyadmin
   - Usuario: `root` (sin contraseña por defecto)

3. **Ejecutar script SQL**:
   - Ve a la pestaña 'SQL'
   - Copia y pega el contenido del archivo `setup-mysql-simple.sql`
   - Haz clic en 'Continuar'

### Paso 2: Verificar Configuración

El script SQL creará:
- ✅ Base de datos: `agroclouddb`
- ✅ Usuario: `agrocloudbd`
- ✅ Contraseña: `Jones1212`
- ✅ Tablas del sistema
- ✅ Roles por defecto
- ✅ Usuario administrador

### Paso 3: Ejecutar Sistema con MySQL

```powershell
.\start-project-mysql.bat
```

## 🔧 Configuración de Base de Datos

### Credenciales MySQL
- **Host**: localhost:3306
- **Base de datos**: agroclouddb
- **Usuario**: agrocloudbd
- **Contraseña**: Jones1212

### Usuario Administrador del Sistema
- **Email**: admin@agrocloud.com
- **Password**: admin123

## 📁 Archivos de Configuración

### Backend (Spring Boot)
- **Perfil activo**: `mysql`
- **Archivo**: `application-mysql.properties`
- **Configuración**: Base de datos MySQL local

### Scripts Disponibles
- `setup-mysql-phpmyadmin.bat` - Configuración manual con phpMyAdmin
- `start-project-mysql.bat` - Ejecuta sistema completo con MySQL
- `run-backend-mysql.bat` - Ejecuta solo backend con MySQL
- `setup-mysql-simple.sql` - Script SQL para phpMyAdmin

## 🌐 URLs de Acceso

### Sistema Principal
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080/api
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health

### Base de Datos
- **phpMyAdmin**: http://localhost/phpmyadmin
- **Base de datos**: agroclouddb

## 🔍 Verificación de Estado

### Verificar MySQL
```powershell
netstat -ano | findstr ":3306"
```

### Verificar Servicios
```powershell
netstat -ano | findstr ":8080"
netstat -ano | findstr ":3000"
```

### Verificar Procesos
```powershell
tasklist | findstr java
tasklist | findstr node
```

## 🛠️ Solución de Problemas

### Error: MySQL no está ejecutándose
1. Abre XAMPP Control Panel
2. Inicia MySQL
3. Verifica que esté en el puerto 3306

### Error: Acceso denegado a MySQL
1. Verifica credenciales en phpMyAdmin
2. Asegúrate de que el usuario `agrocloudbd` tenga permisos
3. Ejecuta el script SQL nuevamente

### Error: Base de datos no existe
1. Ejecuta el script `setup-mysql-simple.sql` en phpMyAdmin
2. Verifica que la base de datos `agroclouddb` se haya creado

## 📊 Diferencias con H2

| Característica | H2 (Memoria) | MySQL (XAMPP) |
|----------------|---------------|---------------|
| Persistencia | ❌ Temporal | ✅ Permanente |
| Rendimiento | ✅ Rápido | ✅ Estable |
| Configuración | ✅ Automática | ⚠️ Manual |
| Escalabilidad | ❌ Limitada | ✅ Escalable |
| Backup | ❌ No disponible | ✅ Completo |

## ✅ Estado Final Esperado

- ✅ MySQL ejecutándose en puerto 3306
- ✅ Base de datos `agroclouddb` creada
- ✅ Usuario `agrocloudbd` configurado
- ✅ Backend conectado a MySQL
- ✅ Frontend ejecutándose en puerto 3000
- ✅ Sistema completamente operativo

---
**Fecha**: 25 de Agosto, 2025
**Versión**: 1.0.0
