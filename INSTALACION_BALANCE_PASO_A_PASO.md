# Guía de Instalación - Balance de Costos y Beneficios

## Problema Identificado
El error `#1005 - Can't create table 'agrocloud'.'ingresos' (errno: 150 "Foreign key constraint is incorrectly formed")` indica que las claves foráneas no se pueden crear porque las tablas referenciadas no existen o tienen una estructura diferente.

## Solución Paso a Paso

### Paso 1: Verificar la Estructura Actual de la Base de Datos

1. Abre phpMyAdmin o tu cliente MySQL preferido
2. Selecciona la base de datos `agrocloud` (o la que uses)
3. Ejecuta el script: `check-database-structure.sql`
4. Anota qué tablas existen y su estructura

### Paso 2: Opción A - Script Completo (Recomendado)

Si las tablas `users`, `campos` y `lotes` no existen:

1. Ejecuta el script: `migration-create-ingresos-table-fixed.sql`
2. Este script creará todas las tablas necesarias en el orden correcto
3. Incluye datos de ejemplo para probar la funcionalidad

### Paso 3: Opción B - Script Simplificado

Si solo quieres crear la tabla de ingresos sin dependencias:

1. Ejecuta el script: `migration-create-ingresos-table-simple.sql`
2. Este script crea solo la tabla `ingresos` sin claves foráneas
3. Las relaciones se manejarán a nivel de aplicación

### Paso 4: Verificar la Instalación

Después de ejecutar cualquiera de los scripts, verifica que todo funcione:

```sql
-- Verificar que la tabla se creó
SHOW TABLES LIKE 'ingresos';

-- Verificar la estructura
DESCRIBE ingresos;

-- Verificar los datos de ejemplo
SELECT * FROM ingresos;
```

### Paso 5: Reiniciar el Backend

1. Detén el backend si está ejecutándose
2. Ejecuta: `.\mvnw.cmd spring-boot:run -Dspring.profiles.active=mysql`
3. Verifica que no hay errores en los logs

### Paso 6: Probar la Funcionalidad

1. Accede al frontend
2. Ve a la sección de "Gestión de Ingresos"
3. Intenta crear un nuevo ingreso
4. Ve a "Balance de Costos y Beneficios"
5. Genera un reporte

## Scripts Disponibles

### 1. `check-database-structure.sql`
- **Propósito**: Verificar qué tablas existen en la base de datos
- **Cuándo usar**: Antes de instalar para entender la estructura actual

### 2. `migration-create-ingresos-table-fixed.sql`
- **Propósito**: Crear todas las tablas necesarias con claves foráneas
- **Cuándo usar**: Si las tablas `users`, `campos`, `lotes` no existen
- **Ventajas**: Estructura completa y consistente
- **Desventajas**: Puede fallar si hay conflictos con tablas existentes

### 3. `migration-create-ingresos-table-simple.sql`
- **Propósito**: Crear solo la tabla `ingresos` sin dependencias
- **Cuándo usar**: Si quieres una instalación rápida y simple
- **Ventajas**: No depende de otras tablas
- **Desventajas**: No hay integridad referencial a nivel de base de datos

## Solución de Problemas

### Error: "Table already exists"
- Ejecuta: `DROP TABLE IF EXISTS ingresos;`
- Luego ejecuta el script de creación

### Error: "Access denied"
- Verifica que tienes permisos de administrador en la base de datos
- Usa un usuario con privilegios CREATE, INSERT, SELECT

### Error: "Unknown database"
- Crea la base de datos: `CREATE DATABASE IF NOT EXISTS agrocloud;`
- Selecciona la base de datos: `USE agrocloud;`

### Error: "Incorrect integer value"
- Verifica que los valores NULL están permitidos en los campos correspondientes
- Ajusta los datos de ejemplo según tu estructura

## Verificación Final

Después de la instalación, deberías poder:

1. ✅ Ver la tabla `ingresos` en phpMyAdmin
2. ✅ Ver datos de ejemplo en la tabla
3. ✅ El backend inicia sin errores
4. ✅ Puedes crear nuevos ingresos desde el frontend
5. ✅ Puedes generar reportes de balance

## Próximos Pasos

Una vez que la tabla esté creada correctamente:

1. Integra los componentes en el menú principal del frontend
2. Configura las rutas en el router
3. Prueba todas las funcionalidades
4. Documenta cualquier problema encontrado

## Contacto

Si encuentras problemas durante la instalación:

1. Ejecuta el script de verificación
2. Anota los errores exactos
3. Verifica la versión de MySQL
4. Revisa los logs del backend
