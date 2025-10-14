# 🚀 Aplicar Migración de Roles - Guía Rápida

## ⚡ Pasos Rápidos (5 minutos)

### 1️⃣ Backup de Base de Datos (Importante!)
```bash
mysqldump -u root -p agrocloud > backup_antes_migracion_roles.sql
```

### 2️⃣ Aplicar Migración SQL
```bash
# Opción A: Usando el script .bat (Windows)
aplicar-migracion-roles-area.bat

# Opción B: Manual
mysql -u root -p agrocloud < migrar-roles-por-area.sql
```

### 3️⃣ Verificar que se aplicó correctamente
```bash
# Opción A: Usando el script .bat (Windows)
verificar-nuevos-roles.bat

# Opción B: Manual
mysql -u root -p agrocloud < verificar-nuevos-roles.sql
```

Deberías ver:
- ✅ 6 roles activos (SUPERADMIN, ADMINISTRADOR, JEFE_CAMPO, JEFE_FINANCIERO, OPERARIO, CONSULTOR_EXTERNO)
- ✅ Usuarios migrados a nuevos roles
- ✅ 0 usuarios con roles antiguos

### 4️⃣ Reiniciar Backend
```bash
cd agrogestion-backend
# Detener si está corriendo (Ctrl+C)
mvn clean install
mvn spring-boot:run
```

### 5️⃣ Reiniciar Frontend
```bash
cd agrogestion-frontend
# Detener si está corriendo (Ctrl+C)
npm start
```

### 6️⃣ Probar en el Navegador
1. Abrir: http://localhost:3000
2. Presionar: `Ctrl + Shift + Delete` → Limpiar caché
3. Iniciar sesión con diferentes usuarios
4. Verificar permisos

---

## 🔍 Verificación Rápida

### ✅ Todo está bien si:
- [x] Backend inicia sin errores
- [x] Frontend inicia sin errores
- [x] Puedes iniciar sesión
- [x] Los botones aparecen según tu rol
- [x] No hay errores en consola del navegador

### ❌ Hay un problema si:
- [ ] Ves "ReferenceError" en consola
- [ ] No aparecen botones que deberías ver
- [ ] Backend muestra errores de enum
- [ ] No puedes iniciar sesión

**Si hay problemas:**
1. Revisa los logs del backend
2. Revisa la consola del navegador (F12)
3. Ejecuta `verificar-nuevos-roles.bat` nuevamente
4. Lee `NUEVA-ESTRUCTURA-ROLES-POR-AREA.md`

---

## 📊 ¿Qué cambia para los usuarios?

### Si eras PRODUCTOR, ASESOR o TÉCNICO:
- ✅ Ahora eres **JEFE_CAMPO**
- ✅ Sigues teniendo acceso a campos, lotes, cultivos, labores
- ❌ Ya NO tienes acceso a finanzas (antes algunos sí tenían)

### Si eras CONTADOR:
- ✅ Ahora eres **JEFE_FINANCIERO**
- ✅ Sigues teniendo acceso a finanzas
- ✅ Puedes ver operaciones (solo lectura)

### Si eras LECTURA:
- ✅ Ahora eres **CONSULTOR_EXTERNO**
- ✅ Sigues teniendo solo lectura
- ✅ Puedes exportar reportes operativos

### Si eras ADMINISTRADOR u OPERARIO:
- ✅ No hay cambios, todo sigue igual

---

## 🔄 Rollback (si algo sale mal)

Si necesitas revertir los cambios:

```bash
# Restaurar backup de BD
mysql -u root -p agrocloud < backup_antes_migracion_roles.sql

# Revertir cambios en Git (si hiciste commit)
git revert HEAD
```

---

## 📞 ¿Necesitas más información?

Lee estos archivos en orden:

1. `RESUMEN-MIGRACION-ROLES-AREA.md` - Resumen ejecutivo
2. `NUEVA-ESTRUCTURA-ROLES-POR-AREA.md` - Documentación completa
3. Ejecuta `verificar-nuevos-roles.bat` - Para ver estado actual

---

## ✅ Checklist Final

Antes de dar por terminado:

- [ ] ✅ Backup de BD realizado
- [ ] ✅ Migración SQL aplicada
- [ ] ✅ Verificación ejecutada (0 usuarios con roles antiguos)
- [ ] ✅ Backend reiniciado sin errores
- [ ] ✅ Frontend reiniciado sin errores
- [ ] ✅ Probado con usuario ADMINISTRADOR
- [ ] ✅ Probado con usuario JEFE_CAMPO (ex PRODUCTOR)
- [ ] ✅ Probado con usuario JEFE_FINANCIERO (ex CONTADOR)
- [ ] ✅ Probado con usuario OPERARIO
- [ ] ✅ Sin errores en consola del navegador
- [ ] ✅ Sin errores en logs del backend

---

**¡Listo! El sistema ahora tiene roles diferenciados y sin superposición funcional.**




