# 📍 Dónde Ver el Enmascaramiento de Datos

## 🎯 Páginas del Frontend donde Verás el Enmascaramiento

### 1. **Selector de Empresas** (Componente: `EmpresaSelector`)

**Ubicación:** 
- Aparece en la parte superior de la aplicación cuando tienes múltiples empresas
- Es el dropdown que muestra las empresas disponibles

**Qué verás enmascarado:**
- ✅ **CUIT**: Se mostrará como `20-****5678-9` en lugar de `20-12345678-9`

**Cómo acceder:**
1. Inicia sesión en la aplicación
2. Si tienes múltiples empresas, verás un selector en la parte superior
3. Haz clic en el selector para ver la lista de empresas
4. En cada empresa verás: `CUIT: 20-****5678-9` (enmascarado)

**Línea de código:** `EmpresaSelector.tsx` línea 163

---

### 2. **Administración de Empresas** (Componente: `AdminEmpresas`)

**Ubicación:**
- Menú: **Administración** → **Empresas**
- O ruta: `/admin/empresas` (si tienes acceso de SuperAdmin)

**Qué verás enmascarado:**
- ✅ **CUIT**: Se mostrará como `20-****5678-9` en la columna "CUIT"
- ✅ **Email de Contacto**: Se mostrará como `usua***@dominio.com` en la columna "Email"

**Cómo acceder:**
1. Inicia sesión como **SuperAdmin** o **Administrador**
2. Ve al menú de **Administración**
3. Selecciona **Empresas**
4. Verás una tabla con todas las empresas
5. Las columnas "CUIT" y "Email Contacto" mostrarán datos enmascarados

**Líneas de código:** 
- `AdminEmpresas.tsx` línea 392 (CUIT)
- `AdminEmpresas.tsx` línea 395 (Email)

---

### 3. **Gestión de Finanzas** (Componente: `FinanzasManagement`)

**Ubicación:**
- Menú: **Finanzas** → **Ingresos y Egresos**
- O ruta: `/finanzas`

**Qué verás enmascarado:**
- ✅ **Montos de Ingresos**: Se mostrarán como `******.78` (solo decimales visibles)
- ✅ **Montos de Egresos**: Se mostrarán como `******.50` (solo decimales visibles)

**Cómo acceder:**
1. Inicia sesión
2. Ve al menú **Finanzas**
3. Selecciona **Ingresos y Egresos**
4. En las tablas de ingresos y egresos, los montos aparecerán enmascarados
5. También verás los totales enmascarados en los resúmenes

**Líneas de código:**
- `FinanzasManagement.tsx` línea 1043 (montos de ingresos)
- `FinanzasManagement.tsx` línea 1281 (montos de egresos)

---

## 🔍 Verificación Rápida

### Test Visual Rápido:

1. **Abre la aplicación** en tu navegador
2. **Inicia sesión**
3. **Ve al selector de empresas** (arriba a la derecha)
4. **Haz clic** para ver la lista
5. **Busca el CUIT** - debería verse como `20-****5678-9` ✅

### Test con Postman/Swagger:

**Endpoint para probar:**
```
GET http://localhost:8080/api/v1/empresas/mis-empresas
Authorization: Bearer TU_TOKEN_JWT
```

**Respuesta esperada:**
```json
[
  {
    "empresaId": 1,
    "empresaNombre": "Mi Empresa",
    "empresaCuit": "20-****5678-9",  // ✅ ENMASCARADO
    "empresaEmail": "usua***@dominio.com",  // ✅ ENMASCARADO
    "usuarioEmail": "usua***@ejemplo.com"  // ✅ ENMASCARADO
  }
]
```

---

## 📋 Resumen de Ubicaciones

| Página/Componente | Dato Enmascarado | Ubicación Visual |
|-------------------|------------------|------------------|
| **Selector de Empresas** | CUIT | Dropdown superior derecho |
| **Admin → Empresas** | CUIT y Email | Tabla de empresas |
| **Finanzas → Ingresos** | Montos | Tabla de ingresos |
| **Finanzas → Egresos** | Montos | Tabla de egresos |

---

## ⚠️ Nota Importante

**El enmascaramiento funciona automáticamente** cuando:
- Los endpoints devuelven DTOs (no entidades directamente)
- Los DTOs tienen los serializers configurados (`@JsonSerialize`)

**Actualmente funcionan:**
- ✅ `/api/v1/empresas/mis-empresas` - Devuelve `UsuarioEmpresaDTO` con enmascaramiento
- ✅ `/api/v1/empresas/{id}/dto` - Devuelve `EmpresaDTO` con enmascaramiento

**Pendiente de modificar:**
- ⚠️ `/api/v1/ingresos` - Devuelve entidades directamente (necesita modificación)
- ⚠️ `/api/v1/egresos` - Devuelve entidades directamente (necesita modificación)

Para ver el enmascaramiento de montos en Finanzas, necesitas modificar los controladores para que devuelvan DTOs.

---

## 🎯 Prueba Rápida Ahora Mismo

1. **Abre tu aplicación frontend**
2. **Inicia sesión**
3. **Busca el selector de empresas** (arriba)
4. **Haz clic** y verás los CUITs enmascarados como `20-****5678-9`

¡Eso es todo! El enmascaramiento ya está funcionando en el selector de empresas. 🎉

