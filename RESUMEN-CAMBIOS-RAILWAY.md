# 📋 RESUMEN DE CAMBIOS PARA RAILWAY

## 🔍 COMPARACIÓN REALIZADA

Se compararon las estructuras de las bases de datos:
- **Local:** `agrocloud` (localhost:3306)
- **Railway:** `railway` (gondola.proxy.rlwy.net:54893)

---

## ✅ CAMBIOS NECESARIOS

### 1. **TABLAS FALTANTES EN RAILWAY**

#### Tabla: `dosis_insumos`
- **Descripción:** Tabla para almacenar las dosis recomendadas de insumos por tipo de aplicación
- **Columnas principales:**
  - `id` (bigint, PK)
  - `insumo_id` (bigint, FK a insumos)
  - `tipo_aplicacion` (varchar(20))
  - `forma_aplicacion` (varchar(20))
  - `dosis_recomendada_por_ha` (double)
  - `activo` (tinyint(1))
  - `fecha_creacion`, `fecha_actualizacion` (timestamp)

#### Tabla: `formas_aplicacion`
- **Descripción:** Tabla para almacenar las formas de aplicación de insumos
- **Columnas principales:**
  - `id` (bigint, PK)
  - `nombre` (varchar(100))
  - `descripcion` (text)
  - `activo` (tinyint(1))
  - `fecha_creacion`, `fecha_actualizacion` (timestamp)

---

### 2. **MODIFICACIONES A TABLAS EXISTENTES**

#### Tabla: `insumos`
**Agregar columnas para agroquímicos:**
- `principio_activo` (varchar(200))
- `concentracion` (varchar(100))
- `clase_quimica` (varchar(100))
- `categoria_toxicologica` (varchar(50))
- `periodo_carencia_dias` (int)
- `dosis_minima_por_ha` (decimal(10,2))
- `dosis_maxima_por_ha` (decimal(10,2))
- `unidad_dosis` (varchar(50))

#### Tabla: `labores`
**Modificar columna:**
- `costo_total`: Cambiar de `decimal(10,2) DEFAULT NULL` a `decimal(15,2) DEFAULT '0.00'`

---

### 3. **TABLAS CON DIFERENCIAS MENORES**

Las siguientes tablas tienen diferencias que pueden ser solo de formato o menores:
- `movimientos_inventario`
- `movimientos_inventario_granos`
- `usuarios_empresas_roles`
- `usuarios_roles`

**Recomendación:** Revisar manualmente si hay diferencias significativas.

---

### 4. **TABLAS EN RAILWAY PERO NO EN LOCAL**

Estas tablas existen en Railway pero no en la base local:
- `alquiler_maquinaria`
- `cosechas`
- `logs_acceso`
- `mantenimientos_maquinaria`
- `vista_estadisticas_rendimiento_cultivo` (vista)
- `vista_lotes_requieren_descanso` (vista)

**Nota:** Estas tablas/vistas pueden ser específicas del entorno de Railway o versiones anteriores. No se eliminarán.

---

## 🚀 APLICAR CAMBIOS

### Opción 1: Script Automático (Recomendado)

```bash
aplicar-cambios-railway.bat
```

Este script:
1. Verifica la conexión a Railway
2. Aplica el script SQL `aplicar_cambios_completos_railway.sql`
3. Verifica que los cambios se aplicaron correctamente

### Opción 2: Manual

```bash
mysql -h gondola.proxy.rlwy.net -P 54893 -u root -pWSoobrppUQbaPINdsRcoQVkUvtYKjmSe railway < aplicar_cambios_completos_railway.sql
```

---

## ⚠️ ADVERTENCIAS

1. **Backup:** Antes de aplicar cambios, asegúrate de tener un backup de Railway
2. **Columnas duplicadas:** Si alguna columna ya existe, el script producirá un error que puede ignorarse
3. **Claves foráneas:** El script deshabilita temporalmente las verificaciones de claves foráneas

---

## ✅ VERIFICACIÓN POST-APLICACIÓN

Después de aplicar los cambios, verifica:

```sql
-- Verificar tablas creadas
SHOW TABLES LIKE 'dosis_insumos';
SHOW TABLES LIKE 'formas_aplicacion';

-- Verificar columnas agregadas en insumos
SHOW COLUMNS FROM insumos LIKE 'principio_activo';
SHOW COLUMNS FROM insumos LIKE 'concentracion';
SHOW COLUMNS FROM insumos LIKE 'clase_quimica';
SHOW COLUMNS FROM insumos LIKE 'categoria_toxicologica';
SHOW COLUMNS FROM insumos LIKE 'periodo_carencia_dias';
SHOW COLUMNS FROM insumos LIKE 'dosis_minima_por_ha';
SHOW COLUMNS FROM insumos LIKE 'dosis_maxima_por_ha';
SHOW COLUMNS FROM insumos LIKE 'unidad_dosis';

-- Verificar modificación en labores
SHOW COLUMNS FROM labores LIKE 'costo_total';
```

---

## 📝 ARCHIVOS GENERADOS

- `aplicar_cambios_completos_railway.sql` - Script SQL con todos los cambios
- `aplicar_cambios_railway_seguro.sql` - Versión segura con manejo de errores
- `aplicar-cambios-railway.bat` - Script batch para aplicar cambios automáticamente
- `estructura_local_completa.sql` - Estructura completa de la base local
- `estructura_railway_completa.sql` - Estructura completa de Railway
- `tablas_local.txt` - Lista de tablas en local
- `tablas_railway.txt` - Lista de tablas en Railway

---

## 🎯 PRÓXIMOS PASOS

1. ✅ Aplicar cambios a Railway
2. ✅ Verificar que los cambios se aplicaron correctamente
3. ✅ Probar la aplicación en Railway
4. ✅ Preparar para el deploy de producción

---

**Fecha de generación:** 2025-11-04

