# ✅ Scripts de Migración Actualizados

## 🎯 Problema Corregido

**Antes:** Los scripts solo eliminaban las tablas pero no las recreaban.

**Ahora:** Los scripts hacen TODO en un solo paso:
1. ✅ Eliminan tablas existentes (respetando integridad referencial)
2. ✅ Crean 19 tablas nuevas
3. ✅ Insertan roles y cultivos base
4. ✅ Insertan datos (testing o production)
5. ✅ Verifican que todo se creó correctamente

---

## ⭐ Scripts Principales (USA ESTOS)

### Para Testing:
```
database-migration-testing-completo.sql
```

### Para Production:
```
database-migration-production-completo.sql
```

Estos archivos contienen TODO en un solo lugar. No necesitan archivos externos.

---

## 📋 Qué hace el script de Testing

```sql
PASO 1: Eliminar tablas existentes
  ↓ (en orden correcto para respetar FK)
  labor_insumos
  labor_maquinaria
  labor_mano_obra
  labores
  historial_cosechas
  ingresos
  egresos
  inventario_granos
  lotes
  campos
  cultivos
  insumos
  maquinaria
  weather_api_usage
  usuario_empresas
  usuarios_empresas_roles
  usuarios
  roles
  empresas
  
PASO 2: Crear 19 tablas nuevas
  ↓
  (Estructura completa de la aplicación)
  
PASO 3: Insertar datos iniciales
  ↓
  13 Roles del sistema
  14 Cultivos base
  
PASO 4: Insertar datos de testing
  ↓
  5 Usuarios de prueba (password: password123)
  1 Empresa demo
  Asignación de roles
  3 Campos
  6 Lotes
  8 Insumos
  3 Maquinarias
  
PASO 5: Verificación
  ↓
  Muestra conteo de tablas y datos
  Muestra credenciales de acceso
```

---

## 🚀 Cómo Usar

### Opción 1: Script .bat (Windows - Local)

```bash
# Testing
.\aplicar-migracion-testing.bat

# Production
.\aplicar-migracion-production.bat
```

Te pedirá confirmación antes de ejecutar.

### Opción 2: MySQL Directo (Local)

```bash
# Testing
mysql -u root -p agrogestion_db < database-migration-testing-completo.sql

# Production  
mysql -u root -p agrogestion_db < database-migration-production-completo.sql
```

### Opción 3: Railway (Remoto)

```bash
# Reemplaza los valores entre [] con los de Railway

# Testing
mysql -h [mysql.railway.internal] -P [3306] -u [root] -p[WSoobrppUQbaPINdsRcoQVkUvtYKjmSe] [railway] < database-migration-testing-completo.sql

# Production
mysql -h [mysql-f883.railway.internal] -P [3306] -u [root] -p[phHVCDrfTiKNzzkqOdcwipOaMEEUKeuz] [railway] < database-migration-production-completo.sql
```

---

## ⚠️ IMPORTANTE: Orden de Eliminación

Las tablas se eliminan en este orden específico para respetar las Foreign Keys:

```
Tablas dependientes → Tablas intermedias → Tablas principales
      ↓                      ↓                    ↓
labor_insumos           labores              usuarios
labor_maquinaria        lotes                roles
labor_mano_obra         campos               empresas
historial_cosechas
ingresos
egresos
```

**¿Por qué?** Porque si intentas eliminar `usuarios` antes que `labores`, MySQL dará error porque `labores` tiene una FK a `usuarios`.

---

## 📊 Resultado Esperado

Al ejecutar el script verás:

```
============================================
✅ MIGRACION COMPLETADA EXITOSAMENTE
============================================

Tablas creadas:
Total: 19

Datos cargados:
Roles: 13
Usuarios: 5 (Testing) / 1 (Production)
Empresas: 1
Cultivos: 14
Campos: 3 (solo Testing)
Lotes: 6 (solo Testing)
Insumos: 8 (solo Testing)
Maquinaria: 3 (solo Testing)

============================================
🔐 CREDENCIALES:
============================================
(Se muestran las credenciales según el ambiente)
```

---

## 🛡️ Seguridad

### ⚠️ ADVERTENCIAS:

1. **Estos scripts ELIMINAN todos los datos existentes**
   - Haz backup ANTES de ejecutar
   - Usa `backup-antes-de-migrar.bat`

2. **Testing vs Production:**
   - Testing: Password débil (password123) - OK para pruebas
   - Production: Password temporal (Admin2025!Temp) - **CAMBIAR INMEDIATAMENTE**

3. **No ejecutes el script de Production sin confirmar:**
   - El script .bat te pedirá escribir "SI" para confirmar
   - Esto evita ejecuciones accidentales

---

## 🔄 Hacer Backup Primero

Antes de ejecutar cualquier script de migración:

```bash
.\backup-antes-de-migrar.bat
```

Esto creará un backup en la carpeta `backups/` con timestamp.

---

## ✅ Checklist Pre-Ejecución

Antes de ejecutar el script, verifica:

- [ ] Tienes backup de la BD actual (si tiene datos importantes)
- [ ] MySQL está corriendo
- [ ] Sabes las credenciales de MySQL
- [ ] Estás usando el script correcto (testing vs production)
- [ ] Has leído las advertencias

---

## 🆘 Si Algo Falla

### Error: "Table doesn't exist"
➜ Normal. El script elimina tablas que podrían no existir.

### Error: "Can't DROP database"
➜ No intentes eliminar la BD completa, solo usa los scripts.

### Error: "Foreign key constraint fails"
➜ El script ya desactiva FK al inicio. Si falla, verifica que ejecutaste el script completo.

### Error: "Access denied"
➜ Verifica usuario y password de MySQL.

---

## 📝 Archivos Relacionados

- `database-migration-testing-completo.sql` ⭐ Script completo Testing
- `database-migration-production-completo.sql` ⭐ Script completo Production
- `aplicar-migracion-testing.bat` - Ejecutor Windows (Testing)
- `aplicar-migracion-production.bat` - Ejecutor Windows (Production)
- `backup-antes-de-migrar.bat` - Crear backup
- `NOTA-SCRIPTS-MIGRACION.md` - Este archivo

---

**Versión:** 2.1 (actualizada con eliminación de tablas)  
**Fecha:** 10 de Octubre de 2025

