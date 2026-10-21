# Configuración de Base de Datos AgroCloud

## 📋 Descripción

Este directorio contiene los scripts SQL necesarios para configurar la base de datos `agroclouddb` para el sistema de gestión agrícola AgroCloud.

## 🗄️ Base de Datos

- **Nombre**: `agroclouddb`
- **Charset**: `utf8mb4`
- **Collation**: `utf8mb4_unicode_ci`
- **Usuario**: `agrocloudbd`
- **Password**: definirla en el entorno, nunca en este archivo

## 📁 Archivos Disponibles

### 1. `setup-agroclouddb-complete.sql`
**Script principal de configuración completa**

Este script incluye:
- ✅ Creación de la base de datos
- ✅ Creación de todas las tablas
- ✅ Creación de índices para optimización
- ✅ Inserción de datos iniciales
- ✅ Usuarios de prueba
- ✅ Verificación de la instalación

### 2. `verify-agroclouddb.sql`
**Script de verificación**

Este script verifica:
- ✅ Existencia de todas las tablas
- ✅ Datos insertados correctamente
- ✅ Índices creados
- ✅ Estadísticas generales
- ✅ Configuración de la base de datos

### 3. `schema-mysql.sql`
**Esquema básico de la base de datos**

Contiene solo la estructura de tablas sin datos.

### 4. `migration-add-polygon-coordinates.sql`
**Script de migración para agregar soporte de polígonos**

Para bases de datos existentes que necesiten agregar las columnas de polígonos y coordenadas.

### 5. `migration-add-polygon-coordinates-agrocloud.sql`
**Script de migración avanzado**

Versión mejorada del script de migración con verificaciones de seguridad.

## 🚀 Instrucciones de Instalación

### Opción 1: Instalación Completa (Recomendada)

1. **Abrir MySQL Workbench o phpMyAdmin**

2. **Ejecutar el script completo**:
   ```sql
   -- Copiar y pegar el contenido de setup-agroclouddb-complete.sql
   ```

3. **Verificar la instalación**:
   ```sql
   -- Copiar y pegar el contenido de verify-agroclouddb.sql
   ```

### Opción 2: Instalación por Línea de Comandos

1. **Abrir terminal/cmd**

2. **Conectarse a MySQL**:
   ```bash
   mysql -u agrocloudbd -p
   ```

3. **Ejecutar el script**:
   ```bash
   source setup-agroclouddb-complete.sql
   ```

4. **Verificar la instalación**:
   ```bash
   source verify-agroclouddb.sql
   ```

### Opción 3: Instalación con XAMPP

1. **Abrir phpMyAdmin** (http://localhost/phpmyadmin)

2. **Crear nueva base de datos**:
   - Nombre: `agroclouddb`
   - Collation: `utf8mb4_unicode_ci`

3. **Seleccionar la base de datos** y ejecutar el script completo

## 👥 Usuarios de Prueba Creados

### Usuario Administrador
- **Usuario**: `admin`
- **Password**: `admin123`
- **Email**: `admin@agrocloud.com`
- **Rol**: `ADMIN`

### Usuario de Prueba
- **Usuario**: `usuario`
- **Password**: `user123`
- **Email**: `usuario@agrocloud.com`
- **Rol**: `USUARIO`

## 📊 Datos Iniciales Incluidos

### Campos de Ejemplo
- Campo Norte (25.50 ha)
- Campo Sur (18.75 ha)
- Campo Este (32.00 ha)

### Lotes de Ejemplo
- 6 lotes distribuidos en los campos
- Diferentes cultivos (soja, maíz, trigo, girasol)
- Estados variados (en crecimiento, planificado, cosechado)

### Insumos de Ejemplo
- Herbicidas (Glifosato, 2,4-D)
- Fertilizantes (Urea, Fosfato Diamónico)
- Semillas (Soja RR, Maíz)

### Maquinaria de Ejemplo
- Tractor John Deere 5075E
- Sembradora Directa Agrometal
- Pulverizadora Metalfor
- Cosechadora New Holland
- Cultivador Agrometal

### Labores de Ejemplo
- Siembras
- Pulverizaciones
- Cosechas

### Mantenimientos de Ejemplo
- Mantenimientos preventivos
- Mantenimientos correctivos

## 🔧 Configuración del Backend

### Archivo de Configuración
El archivo `application-mysql.properties` ya está configurado para usar `agroclouddb`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/agroclouddb?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=agrocloudbd
spring.datasource.password=${DATABASE_PASSWORD}
```

## 🆘 Solución de Problemas

### Error de Conexión
Si hay problemas de conexión:
1. Verificar que MySQL esté ejecutándose
2. Verificar las credenciales del usuario `agrocloudbd`
3. Verificar que la base de datos `agroclouddb` exista

### Error de Permisos
Si hay problemas de permisos:
```sql
GRANT ALL PRIVILEGES ON agroclouddb.* TO 'agrocloudbd'@'localhost';
FLUSH PRIVILEGES;
```

### Error de Charset
Si hay problemas de caracteres:
```sql
ALTER DATABASE agroclouddb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

## 📝 Notas Importantes

1. **Backup**: Siempre hacer backup antes de ejecutar scripts de migración
2. **Pruebas**: Probar en un entorno de desarrollo antes de producción
3. **Seguridad**: Cambiar las contraseñas por defecto en producción
4. **Actualizaciones**: Mantener los scripts actualizados con los cambios del sistema

## 🔄 Migración desde Base de Datos Existente

Si ya tienes una base de datos `agrocloud` y quieres migrar a `agroclouddb`:

1. **Hacer backup de la base actual**
2. **Ejecutar el script de migración**:
   ```sql
   -- Usar migration-add-polygon-coordinates-agrocloud.sql
   ```
3. **Verificar la migración**:
   ```sql
   -- Usar verify-agroclouddb.sql
   ```

## 📞 Soporte

Para problemas o consultas sobre la configuración de la base de datos, revisar:
1. Los logs del servidor MySQL
2. Los logs de la aplicación Spring Boot
3. La documentación del sistema AgroCloud

---

**¡El sistema AgroCloud está listo para usar! 🚀**
