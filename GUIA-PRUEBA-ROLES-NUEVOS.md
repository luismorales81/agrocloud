# 🧪 Guía de Prueba: Nuevos Roles

## 🎯 Objetivo
Verificar que cada rol tiene los permisos correctos después de la migración.

---

## 🔑 Credenciales para Probar

### 1. **JEFE_CAMPO** (Juan - ex TÉCNICO)
```
Email: tecnico.juan@agrocloud.com
Contraseña: admin123
```

**Deberías ver:**
- ✅ Menú: Campos, Lotes, Cultivos, Insumos, Maquinaria, Labores, Cosechas, Reportes
- ✅ Botón "Agregar Campo", "Agregar Lote", etc.
- ❌ NO ver menú "Finanzas"
- ❌ NO ver reportes financieros

---

### 2. **JEFE_CAMPO** (María - ex ASESOR)
```
Email: asesor.maria@agrocloud.com
Contraseña: admin123
```

**Deberías ver:**
- ✅ Mismo comportamiento que Juan (JEFE_CAMPO)
- ✅ Puede crear y editar campos, lotes, cultivos
- ❌ NO puede ver finanzas

---

### 3. **JEFE_CAMPO** (Pedro - ex PRODUCTOR)
```
Email: productor.pedro@agrocloud.com
Contraseña: admin123
```

**Deberías ver:**
- ✅ Mismo comportamiento que Juan y María
- ✅ Gestión completa de operaciones
- ❌ Sin acceso a finanzas

---

### 4. **JEFE_FINANCIERO** (Raúl - ex CONTADOR)
```
Email: raul@agrocloud.com
Contraseña: admin123
```

**Deberías ver:**
- ✅ Menú: Finanzas, Reportes
- ✅ Puede ver campos, lotes (SOLO LECTURA)
- ✅ Puede crear/editar/eliminar ingresos y egresos
- ❌ NO puede crear/editar campos, lotes, cultivos
- ❌ NO aparecen botones "Agregar Campo", "Agregar Lote"

---

### 5. **OPERARIO** (Luis)
```
Email: operario.luis@agrocloud.com
Contraseña: admin123
```

**Deberías ver:**
- ✅ Puede ver campos, lotes (SOLO LECTURA)
- ✅ Puede registrar labores ejecutadas
- ✅ Puede registrar cosechas
- ❌ NO puede crear campos, lotes, cultivos
- ❌ NO puede ver finanzas
- ❌ NO aparecen botones "Agregar Campo", "Agregar Lote"

---

### 6. **ADMINISTRADOR**
```
Email: admin.empresa@agrocloud.com
Contraseña: admin123
```

**Deberías ver:**
- ✅ TODO: Usuarios, Campos, Lotes, Cultivos, Insumos, Maquinaria, Labores, Cosechas, Finanzas, Reportes
- ✅ Todos los botones de crear/editar/eliminar
- ✅ Gestión completa

---

## ✅ Checklist de Pruebas

### **JEFE_CAMPO (Juan/María/Pedro)**
- [ ] Iniciar sesión
- [ ] Verificar que dice "Rol: JEFE_CAMPO" (NO "TECNICO", "ASESOR", "PRODUCTOR")
- [ ] Ir a **Campos** → Ver botón "➕ Agregar Campo"
- [ ] Ir a **Lotes** → Ver botón "➕ Agregar Lote"
- [ ] Ir a **Cultivos** → Ver botón "➕ Agregar Cultivo"
- [ ] Verificar que **NO aparece** menú "Finanzas"
- [ ] Ir a **Reportes** → Ver reportes operativos
- [ ] Verificar que **NO aparecen** reportes financieros

### **JEFE_FINANCIERO (Raúl)**
- [ ] Iniciar sesión
- [ ] Verificar que dice "Rol: JEFE_FINANCIERO" (NO "CONTADOR")
- [ ] Ir a **Campos** → Ver campos pero **SIN** botón "➕ Agregar Campo"
- [ ] Ir a **Lotes** → Ver lotes pero **SIN** botón "➕ Agregar Lote"
- [ ] Ir a **Finanzas** → Ver botones "➕ Nuevo Ingreso" y "➖ Nuevo Egreso"
- [ ] Crear un ingreso/egreso de prueba
- [ ] Ver reportes financieros

### **OPERARIO (Luis)**
- [ ] Iniciar sesión
- [ ] Verificar que dice "Rol: OPERARIO"
- [ ] Ir a **Campos** → Ver campos pero **SIN** botón "➕ Agregar Campo"
- [ ] Ir a **Labores** → Poder registrar labor
- [ ] Verificar que **NO aparece** menú "Finanzas"
- [ ] Verificar que **NO aparece** menú "Insumos" o "Maquinaria" (o están en solo lectura)

---

## 🚨 Problemas Comunes

### Problema 1: "Sigo viendo el rol antiguo (TÉCNICO, ASESOR, PRODUCTOR)"

**Solución:**
1. Cerrar sesión
2. Presionar `F12` → Consola
3. Ejecutar:
   ```javascript
   localStorage.clear();
   sessionStorage.clear();
   location.reload();
   ```
4. Volver a iniciar sesión

---

### Problema 2: "No veo botones que debería ver"

**Solución:**
1. Verificar en la BD que el rol está correcto:
   ```sql
   SELECT u.email, r.name as rol 
   FROM usuarios u 
   INNER JOIN usuarios_empresas_roles uer ON u.id = uer.usuario_id 
   INNER JOIN roles r ON uer.rol_id = r.id 
   WHERE u.email = 'tecnico.juan@agrocloud.com';
   ```
2. Limpiar caché y volver a iniciar sesión
3. Verificar que el backend esté ejecutándose con los cambios

---

### Problema 3: "Veo botones que NO debería ver"

**Solución:**
1. Verificar que el frontend tenga los cambios más recientes
2. Hacer `Ctrl + F5` para forzar recarga
3. Verificar en el navegador (F12 → Network) que los archivos se están cargando desde el servidor y no del caché

---

## 📊 Tabla de Comparación Rápida

| Usuario | Email | Rol Anterior | Rol Nuevo | Campos | Finanzas |
|---------|-------|--------------|-----------|--------|----------|
| Juan | tecnico.juan@ | TÉCNICO | **JEFE_CAMPO** | ✅ Crear/Editar | ❌ Sin acceso |
| María | asesor.maria@ | ASESOR | **JEFE_CAMPO** | ✅ Crear/Editar | ❌ Sin acceso |
| Pedro | productor.pedro@ | PRODUCTOR | **JEFE_CAMPO** | ✅ Crear/Editar | ❌ Sin acceso |
| Raúl | raul@ | CONTADOR | **JEFE_FINANCIERO** | 👁️ Solo ver | ✅ Crear/Editar |
| Luis | operario.luis@ | OPERARIO | **OPERARIO** | 👁️ Solo ver | ❌ Sin acceso |

---

## ✅ Verificación Exitosa

Si todo funciona correctamente, deberías ver:

1. ✅ Juan, María y Pedro tienen el mismo rol: **JEFE_CAMPO**
2. ✅ Todos los JEFE_CAMPO pueden gestionar campos pero NO finanzas
3. ✅ Raúl (JEFE_FINANCIERO) puede gestionar finanzas pero NO crear campos
4. ✅ Luis (OPERARIO) solo puede registrar labores y cosechas
5. ✅ NO quedan usuarios con roles antiguos (TÉCNICO, ASESOR, PRODUCTOR, CONTADOR)

---

**¿Encontraste algún problema? Revisa la sección de "Problemas Comunes" arriba.**




