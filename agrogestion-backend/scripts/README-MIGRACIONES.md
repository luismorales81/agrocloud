# Migraciones de Base de Datos - Configuración Maestra Porcinos

## 📋 Información General

Este proyecto usa **Flyway** para gestionar las migraciones de base de datos. Las migraciones se ejecutan **automáticamente** al iniciar la aplicación Spring Boot.

## 🚀 Ejecución Automática (Recomendado)

Las migraciones se ejecutan automáticamente cuando inicias la aplicación:

```bash
cd agrogestion-backend
mvn spring-boot:run
```

Flyway detectará el script `V1_30__Create_Configuracion_Maestra_Porcinos_Tables.sql` y lo ejecutará automáticamente.

## 🔧 Ejecución Manual (Si es necesario)

Si necesitas ejecutar el script manualmente en MySQL:

### Opción 1: Desde línea de comandos

```bash
# Conectar a MySQL
mysql -u root -p

# Seleccionar la base de datos
USE agrocloud;

# Ejecutar el script
SOURCE agrogestion-backend/src/main/resources/db/migration/V1_30__Create_Configuracion_Maestra_Porcinos_Tables.sql;
```

### Opción 2: Desde archivo

```bash
mysql -u root -p agrocloud < agrogestion-backend/scripts/V1_30_Ejecutar_Manual.sql
```

### Opción 3: Desde XAMPP (phpMyAdmin)

1. Abre phpMyAdmin (http://localhost/phpmyadmin)
2. Selecciona la base de datos `agrocloud`
3. Ve a la pestaña "SQL"
4. Copia y pega el contenido del archivo `V1_30__Create_Configuracion_Maestra_Porcinos_Tables.sql`
5. Haz clic en "Ejecutar"

## ✅ Verificar que se Ejecutó Correctamente

Ejecuta el script de verificación:

```bash
mysql -u root -p agrocloud < agrogestion-backend/scripts/verificar-migracion-V1_30.sql
```

O manualmente en MySQL:

```sql
USE agrocloud;

-- Verificar que las tablas existen
SHOW TABLES LIKE '%porcinos%';

-- Verificar estructura de una tabla
DESCRIBE razas_porcinos;

-- Verificar migraciones de Flyway
SELECT * FROM flyway_schema_history 
WHERE version = '1.30' 
ORDER BY installed_rank DESC 
LIMIT 1;
```

## 📊 Tablas Creadas

La migración V1_30 crea las siguientes **10 tablas**:

### Catálogos Configurables (6 tablas + 1 eliminada)
1. `razas_porcinos` - Razas configurables
2. `tipos_alimento_porcinos` - Tipos de alimento
3. `tipos_servicio_porcinos` - Tipos de servicio reproductivo
4. `causas_mortalidad_porcinos` - Causas de mortalidad por etapa
5. `motivos_baja_porcinos` - Motivos de baja
6. `esquemas_sanitarios_porcinos` - Esquemas sanitarios
7. ~~`tipos_corral_porcinos`~~ - **ELIMINADA (V1_113)** - Reemplazada por `porcinos_ubicaciones_internas`

### Parámetros del Sistema (3 tablas)
8. `parametros_establecimiento_porcinos` - Parámetros del establecimiento
9. `parametros_productivos_porcinos` - Parámetros productivos
10. `datos_economicos_porcinos` - Datos económicos

**Nota:** La tabla `tipos_corral_porcinos` fue eliminada en la migración V1_113 porque estaba en superposición con `porcinos_ubicaciones_internas`. El sistema ahora usa únicamente UbicacionInterna (estructura jerárquica: Galpón → Sala → Corral) que tiene FK en Madre, Padrillo y Recria.

## ⚠️ Notas Importantes

- **No ejecutes el script manualmente si Flyway ya lo ejecutó** - Esto causará un error de migración duplicada
- Flyway registra todas las migraciones en la tabla `flyway_schema_history`
- Si una migración falla, Flyway la marcará como fallida y no continuará
- Para ver el estado de las migraciones: `SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC;`

## 🔍 Solución de Problemas

### Error: "Migration checksum mismatch"
Esto significa que el archivo fue modificado después de ejecutarse. Soluciones:
1. Revertir los cambios al archivo original
2. O reparar Flyway: `mvn flyway:repair`

### Error: "Table already exists"
La tabla ya existe. Esto puede pasar si:
- Ya ejecutaste el script manualmente
- Flyway ya lo ejecutó

Solución: Verificar si la tabla existe y si tiene la estructura correcta.

### Verificar estado de Flyway
```sql
SELECT * FROM flyway_schema_history 
WHERE version LIKE '1.30%' 
ORDER BY installed_rank DESC;
```

## 📝 Archivos Relacionados

- **Migración Flyway**: `src/main/resources/db/migration/V1_30__Create_Configuracion_Maestra_Porcinos_Tables.sql`
- **Script Manual**: `scripts/V1_30_Ejecutar_Manual.sql`
- **Script Verificación**: `scripts/verificar-migracion-V1_30.sql`

