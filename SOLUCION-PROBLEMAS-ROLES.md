# 🔧 Solución de Problemas Detectados

## 🚨 **PROBLEMAS IDENTIFICADOS:**

### ❌ **1. Raúl (JEFE_FINANCIERO): "No tienes acceso a ninguna empresa"**
**Causa:** Caché del frontend con datos antiguos

### ❌ **2. Operario puede crear campos**
**Causa:** Frontend no actualizado con nuevos permisos

### ❌ **3. No cargan campos en lotes / lotes en labores**
**Causa:** Problema de datos en caché o backend no actualizado

### ❌ **4. JEFE_CAMPO puede ver reportes financieros**
**Causa:** Frontend no actualizado

---

## ✅ **SOLUCIÓN COMPLETA:**

### **Paso 1: Detener servicios actuales**

```bash
# Detener procesos de Node.js
taskkill /F /IM node.exe

# Si el backend está corriendo, presiona Ctrl+C en su ventana
```

---

### **Paso 2: Reiniciar Backend**

```bash
cd agrogestion-backend
mvn clean install
mvn spring-boot:run
```

**Espera hasta ver:**
```
Started AgroGestionApplication in X.XXX seconds
```

---

### **Paso 3: Reiniciar Frontend**

**Opción A - Usando el script (RECOMENDADO):**
```bash
reiniciar-frontend-con-cambios.bat
```

**Opción B - Manual:**
```bash
cd agrogestion-frontend
rd /s /q node_modules\.cache
npm start
```

**Espera hasta ver:**
```
webpack compiled successfully
```

---

### **Paso 4: Limpiar COMPLETAMENTE el navegador**

1. **Cerrar TODAS las ventanas del navegador**
2. **Abrir una ventana nueva**
3. **Presionar `F12`** (DevTools)
4. **Ir a Application → Storage → Clear site data**
5. **O ejecutar en consola:**
   ```javascript
   localStorage.clear();
   sessionStorage.clear();
   indexedDB.databases().then(dbs => dbs.forEach(db => indexedDB.deleteDatabase(db.name)));
   location.reload();
   ```

---

### **Paso 5: Probar de nuevo**

#### **🧪 Test 1: JEFE_CAMPO (Juan)**
```
Email: tecnico.juan@agrocloud.com
Contraseña: admin123
```

**Verificar:**
- [ ] ✅ Dice "Rol: JEFE_CAMPO" (NO "TECNICO")
- [ ] ✅ En Campos: puede crear campos
- [ ] ✅ En Lotes: muestra lista de campos para seleccionar
- [ ] ✅ En Labores: muestra lista de lotes para seleccionar
- [ ] ❌ NO aparece menú "Finanzas"
- [ ] ❌ En Reportes: NO aparecen reportes financieros

---

#### **🧪 Test 2: JEFE_FINANCIERO (Raúl)**
```
Email: raul@agrocloud.com
Contraseña: admin123
```

**Verificar:**
- [ ] ✅ Puede iniciar sesión (NO "No tienes acceso")
- [ ] ✅ Dice "Rol: JEFE_FINANCIERO"
- [ ] ✅ Aparece menú "Finanzas"
- [ ] ✅ Puede crear ingresos y egresos
- [ ] ✅ Puede ver reportes financieros
- [ ] 👁️ Puede VER campos pero SIN botón "Agregar Campo"
- [ ] 👁️ Puede VER lotes pero SIN botón "Agregar Lote"

---

#### **🧪 Test 3: OPERARIO (Luis)**
```
Email: operario.luis@agrocloud.com
Contraseña: admin123
```

**Verificar:**
- [ ] ✅ Dice "Rol: OPERARIO"
- [ ] 👁️ Puede VER campos pero SIN botón "Agregar Campo"
- [ ] ✅ En Labores: puede registrar labores
- [ ] ✅ En Lotes: muestra lista de campos (solo lectura)
- [ ] ❌ NO aparece menú "Finanzas"

---

## 🔍 **SI LOS PROBLEMAS PERSISTEN:**

### **Problema: "No cargan campos en lotes"**

**Verificar que hay campos creados:**
```sql
SELECT id, nombre FROM campos WHERE activo = 1;
```

**Si no hay campos, crear uno como ADMINISTRADOR:**
```
Email: admin.empresa@agrocloud.com
Contraseña: admin123
```

---

### **Problema: "Raúl sigue sin acceso"**

**Verificar en BD:**
```sql
SELECT u.email, r.name as rol, e.nombre, uer.estado 
FROM usuarios u 
INNER JOIN usuarios_empresas_roles uer ON u.id = uer.usuario_id 
INNER JOIN roles r ON uer.rol_id = r.id 
INNER JOIN empresas e ON uer.empresa_id = e.id 
WHERE u.email = 'raul@agrocloud.com';
```

**Debe mostrar:**
- rol: JEFE_FINANCIERO
- empresa: AgroCloud Demo
- estado: ACTIVO

**Si no aparece, ejecutar:**
```sql
-- Verificar que el usuario existe
SELECT id, email FROM usuarios WHERE email = 'raul@agrocloud.com';

-- Si no existe, crearlo (ver scripts de creación)
```

---

### **Problema: "Operario puede crear campos"**

**Verificar en el navegador (F12 → Console):**
```javascript
// Ejecutar esto después de iniciar sesión como operario
console.log(localStorage.getItem('rolUsuario'));
```

**Debe mostrar:** `"OPERARIO"`

**Si muestra otra cosa:**
1. Cerrar sesión
2. Ejecutar: `localStorage.clear(); sessionStorage.clear();`
3. Volver a iniciar sesión

---

## 📋 **CHECKLIST COMPLETO:**

- [ ] ✅ Backend reiniciado con cambios
- [ ] ✅ Frontend reiniciado con cambios
- [ ] ✅ Caché del navegador limpiado COMPLETAMENTE
- [ ] ✅ Todas las ventanas del navegador cerradas y reabiertas
- [ ] ✅ Juan (JEFE_CAMPO) puede crear campos pero NO ver finanzas
- [ ] ✅ Raúl (JEFE_FINANCIERO) puede acceder y gestionar finanzas
- [ ] ✅ Luis (OPERARIO) NO puede crear campos
- [ ] ✅ Los selectores de campos y lotes cargan correctamente

---

## 🆘 **SI NADA FUNCIONA:**

### **Reset Completo:**

1. **Detener todo:**
   ```bash
   taskkill /F /IM node.exe
   taskkill /F /IM java.exe
   ```

2. **Limpiar compilaciones:**
   ```bash
   cd agrogestion-backend
   mvn clean
   
   cd ..\agrogestion-frontend
   rd /s /q node_modules\.cache
   rd /s /q build
   ```

3. **Reinstalar dependencias frontend:**
   ```bash
   cd agrogestion-frontend
   npm install
   ```

4. **Iniciar todo de nuevo:**
   ```bash
   iniciar-proyecto.bat
   ```

5. **Limpiar navegador:**
   - Cerrar TODO
   - Borrar historial completo (Ctrl+Shift+Delete)
   - Reiniciar navegador

---

## 📞 **NECESITAS AYUDA:**

Si después de estos pasos sigues teniendo problemas:

1. **Captura de pantalla** del error
2. **Console log** del navegador (F12)
3. **Log del backend** (ventana donde corre Spring Boot)
4. **Resultado de:**
   ```sql
   SELECT u.email, r.name, e.nombre 
   FROM usuarios u 
   INNER JOIN usuarios_empresas_roles uer ON u.id = uer.usuario_id 
   INNER JOIN roles r ON uer.rol_id = r.id 
   INNER JOIN empresas e ON uer.empresa_id = e.id;
   ```

---

**Última actualización:** 2025-10-08  
**Versión:** 2.0.1




