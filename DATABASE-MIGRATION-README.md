## 📦 Scripts de Migración de Base de Datos - AgroGestion v2.0

### 📋 Contenido

Este paquete incluye todos los scripts SQL necesarios para crear la base de datos de AgroGestion desde cero, tanto para Testing como para Producción.

---

## 📁 Archivos Incluidos

### 🔧 Scripts de Estructura:
- **`database-migration-structure.sql`** - Estructura completa de todas las tablas

### 📊 Scripts de Datos:
- **`database-migration-data-initial.sql`** - Roles del sistema y cultivos base (común para Testing y Production)
- **`database-migration-data-testing.sql`** - Datos de prueba (solo Testing)
- **`database-migration-data-production.sql`** - Datos iniciales mínimos (solo Production)

### 🎯 Scripts Maestros:
- **`database-migration-master-testing.sql`** - Ejecuta todo para Testing
- **`database-migration-master-production.sql`** - Ejecuta todo para Production

### 🖥️ Scripts de Ejecución (.bat):
- **`aplicar-migracion-testing.bat`** - Ejecuta migración Testing (Windows)
- **`aplicar-migracion-production.bat`** - Ejecuta migración Production (Windows)
- **`generar-dump-base-datos.bat`** - Genera backup de BD actual

---

## 🚀 Uso Rápido

### Para Testing (Local):

**Opción 1: Usar el script .bat (Windows)**
```bash
aplicar-migracion-testing.bat
```

**Opción 2: MySQL directo**
```bash
mysql -u root -p < database-migration-master-testing.sql
```

### Para Production (Railway):

**1. Conectarse a Railway MySQL:**
```bash
mysql -h [RAILWAY_HOST] -u [RAILWAY_USER] -p[RAILWAY_PASSWORD] -D [DATABASE_NAME] < database-migration-master-production.sql
```

**2. O usar Railway CLI:**
```bash
railway connect mysql
source database-migration-master-production.sql;
```

---

## 📝 Credenciales

### 🧪 Testing:

| Rol | Email | Password |
|-----|-------|----------|
| Admin | admin.testing@agrogestion.com | password123 |
| Jefe Campo | jefe.campo@agrogestion.com | password123 |
| Jefe Financiero | jefe.financiero@agrogestion.com | password123 |
| Operario | operario.test@agrogestion.com | password123 |
| Consultor | consultor.test@agrogestion.com | password123 |

### 🚀 Production:

| Rol | Email | Password Temporal |
|-----|-------|-------------------|
| Admin | admin@tudominio.com | Admin2025!Temp |

⚠️ **IMPORTANTE:** Cambia la contraseña inmediatamente después del primer login.

---

## 🗂️ Estructura de Tablas Creadas

### 👥 Usuarios y Roles:
- `usuarios` - Usuarios del sistema
- `roles` - Roles globales (SUPERADMIN, USUARIO_REGISTRADO, etc.)
- `empresas` - Empresas/organizaciones
- `usuario_empresas` - Relación usuario-empresa con rol (NUEVO sistema)
- `usuarios_empresas_roles` - Sistema antiguo (mantener compatibilidad)

### 🌾 Producción:
- `campos` - Campos agrícolas
- `lotes` - Lotes dentro de campos
- `cultivos` - Catálogo de cultivos
- `labores` - Labores agrícolas
- `labor_insumos` - Insumos usados en labores
- `labor_maquinaria` - Maquinaria usada en labores
- `labor_mano_obra` - Mano de obra en labores

### 📦 Recursos:
- `insumos` - Inventario de insumos
- `maquinaria` - Maquinaria disponible

### 📊 Finanzas:
- `ingresos` - Registro de ingresos
- `egresos` - Registro de egresos
- `inventario_granos` - Stock de granos cosechados

### 📈 Historial:
- `historial_cosechas` - Histórico de cosechas realizadas

---

## 🔄 Migración Paso a Paso

### Para Testing:

```bash
# 1. Crear la base de datos
CREATE DATABASE agrogestion_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE agrogestion_db;

# 2. Crear estructura
SOURCE database-migration-structure.sql;

# 3. Insertar datos iniciales
SOURCE database-migration-data-initial.sql;

# 4. Insertar datos de testing
SOURCE database-migration-data-testing.sql;
```

### Para Production:

```bash
# 1. Crear la base de datos
CREATE DATABASE agrogestion_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE agrogestion_db;

# 2. Crear estructura
SOURCE database-migration-structure.sql;

# 3. Insertar datos iniciales
SOURCE database-migration-data-initial.sql;

# 4. Insertar datos de production
SOURCE database-migration-data-production.sql;
```

---

## 🔐 Seguridad - Importante

### ⚠️ Antes de ir a Production:

1. **Cambia las credenciales:**
   - Password del administrador
   - Email del administrador

2. **Actualiza datos de la empresa:**
   - Edita `database-migration-data-production.sql`
   - Cambia: nombre, CUIT, dirección, teléfono, email

3. **Genera JWT_SECRET seguro:**
   ```bash
   openssl rand -hex 64
   ```

4. **Configura backups automáticos** en Railway

5. **No uses datos de testing** en producción

---

## 🆘 Troubleshooting

### Error: "Access denied"
```bash
# Verifica credenciales de MySQL
mysql -u root -p
```

### Error: "Unknown database"
```bash
# Crea la base de datos primero
mysql -u root -p -e "CREATE DATABASE agrogestion_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

### Error: "Cannot find file"
```bash
# Asegúrate de estar en el directorio correcto
cd /ruta/al/proyecto
dir database-migration-*.sql
```

### Error: Foreign key constraint fails
```bash
# Los scripts ya tienen SET FOREIGN_KEY_CHECKS=0/1
# Si falla, ejecuta manualmente:
SET FOREIGN_KEY_CHECKS = 0;
SOURCE tu-script.sql;
SET FOREIGN_KEY_CHECKS = 1;
```

---

## 📊 Verificación Post-Migración

### Verificar tablas creadas:
```sql
SELECT COUNT(*) AS total_tablas 
FROM information_schema.tables 
WHERE table_schema = 'agrogestion_db';
-- Debería mostrar ~20 tablas
```

### Verificar usuarios:
```sql
SELECT id, username, email, activo 
FROM usuarios;
```

### Verificar roles:
```sql
SELECT usuario_id, empresa_id, rol, estado 
FROM usuario_empresas;
```

### Verificar cultivos:
```sql
SELECT COUNT(*) FROM cultivos;
-- Debería mostrar ~14 cultivos
```

---

## 🔄 Actualizar Base de Datos Existente

Si ya tienes una BD y quieres actualizarla:

### Opción 1: Backup y recrear
```bash
# 1. Hacer backup
mysqldump -u root -p agrogestion_db > backup_$(date +%Y%m%d).sql

# 2. Eliminar BD antigua
mysql -u root -p -e "DROP DATABASE agrogestion_db;"

# 3. Crear nueva
mysql -u root -p < database-migration-master-testing.sql
```

### Opción 2: Migración selectiva
```bash
# Solo actualizar estructura (sin perder datos)
mysql -u root -p agrogestion_db < database-migration-structure.sql
```

⚠️ **ADVERTENCIA:** La opción 2 puede fallar si hay cambios incompatibles.

---

## 📦 Para Railway

### Conectar a Railway MySQL:

**1. Obtener credenciales desde Railway Dashboard:**
- Ve a tu proyecto → MySQL → Variables
- Copia: `MYSQL_HOST`, `MYSQL_USER`, `MYSQL_PASSWORD`, `MYSQL_DATABASE`

**2. Conectar:**
```bash
mysql -h [MYSQL_HOST] -P [MYSQL_PORT] -u [MYSQL_USER] -p[MYSQL_PASSWORD] [MYSQL_DATABASE]
```

**3. Ejecutar migración:**
```sql
source database-migration-master-production.sql;
```

**O desde terminal local:**
```bash
mysql -h [HOST] -P [PORT] -u [USER] -p[PASSWORD] [DATABASE] < database-migration-master-production.sql
```

---

## 📋 Checklist Post-Deployment

### Testing:
- [ ] Base de datos creada
- [ ] Todas las tablas existen
- [ ] Usuarios de prueba creados
- [ ] Login funciona con credenciales de testing
- [ ] Datos de ejemplo cargados

### Production:
- [ ] Base de datos creada
- [ ] Todas las tablas existen
- [ ] Usuario admin creado
- [ ] **Contraseña cambiada** ⚠️
- [ ] Datos de empresa actualizados
- [ ] Backup automático configurado
- [ ] Login funciona

---

## 📞 Soporte

Si encuentras problemas:

1. Verifica que MySQL está corriendo
2. Verifica las credenciales
3. Revisa los logs de error de MySQL
4. Asegúrate de tener permisos de escritura

---

**Versión:** 2.0  
**Fecha:** Octubre 2025  
**Compatible con:** MySQL 8.0+

