# 🔍 Cómo Ver el Enmascaramiento de Datos en Funcionamiento

## ✅ Endpoints Listos para Probar

### 1. **Empresas - CUIT y Email Enmascarados** ⭐ RECOMENDADO

Este endpoint ya está funcionando y devuelve datos enmascarados:

**Endpoint:**
```
GET http://localhost:8080/api/v1/empresas/mis-empresas
```

**Headers requeridos:**
```
Authorization: Bearer TU_TOKEN_JWT
```

**Respuesta esperada (con enmascaramiento):**
```json
[
  {
    "id": 1,
    "empresaId": 1,
    "empresaNombre": "Mi Empresa",
    "empresaCuit": "20-****5678-9",  // ✅ ENMASCARADO
    "empresaEmail": "usua***@dominio.com",  // ✅ ENMASCARADO
    "usuarioEmail": "usua***@ejemplo.com",  // ✅ ENMASCARADO
    "rol": "ADMINISTRADOR"
  }
]
```

### 2. **Empresa Individual como DTO** ⭐ NUEVO

Endpoint específico para ver el enmascaramiento completo:

**Endpoint:**
```
GET http://localhost:8080/api/v1/empresas/{id}/dto
```

**Ejemplo:**
```bash
GET http://localhost:8080/api/v1/empresas/1/dto
Authorization: Bearer TU_TOKEN_JWT
```

**Respuesta esperada:**
```json
{
  "id": 1,
  "nombre": "Mi Empresa",
  "cuit": "20-****5678-9",  // ✅ ENMASCARADO
  "emailContacto": "usua***@dominio.com",  // ✅ ENMASCARADO
  "telefonoContacto": "1123***789",  // Si se implementa
  "estado": "ACTIVO"
}
```

## 🧪 Cómo Probar Ahora Mismo

### Opción 1: Desde el Frontend

1. **Inicia sesión** en tu aplicación
2. **Ve a la sección de empresas** (donde se muestran tus empresas)
3. **Observa los campos:**
   - **CUIT**: Debería verse como `20-****5678-9` en lugar de `20-12345678-9`
   - **Email**: Debería verse como `usua***@dominio.com` en lugar del email completo

### Opción 2: Desde el Navegador (con extensión)

1. Instala una extensión como **ModHeader** o **REST Client**
2. Agrega el header: `Authorization: Bearer TU_TOKEN`
3. Visita: `http://localhost:8080/api/v1/empresas/mis-empresas`
4. Revisa la respuesta JSON en la consola del navegador

### Opción 3: Desde Postman o cURL

```bash
# Obtener token primero (si no lo tienes)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"tu@email.com","password":"tu_password"}'

# Luego usar el token para obtener empresas
curl -X GET http://localhost:8080/api/v1/empresas/mis-empresas \
  -H "Authorization: Bearer TU_TOKEN_JWT"
```

### Opción 4: Desde Swagger UI

1. Ve a: `http://localhost:8080/swagger-ui.html`
2. Busca el endpoint `/api/v1/empresas/mis-empresas`
3. Haz clic en "Try it out"
4. Agrega el token en "Authorize"
5. Ejecuta y revisa la respuesta

## 📊 Qué Deberías Ver

### ✅ Datos Enmascarados Correctamente:

- **CUIT**: `20-12345678-9` → `20-****5678-9`
- **Email**: `usuario@dominio.com` → `usua***@dominio.com`
- **Email de usuario**: `admin@empresa.com` → `admi***@empresa.com`

### ❌ Si NO ves el enmascaramiento:

1. **Verifica que estás usando el endpoint correcto** (`/mis-empresas` o `/dto`)
2. **Verifica que el token JWT es válido**
3. **Revisa la consola del backend** para ver si hay errores
4. **Asegúrate de que el servicio está corriendo** con los cambios aplicados

## 🔍 Verificación en Logs

El enmascaramiento también funciona en los logs. Para verificar:

1. Abre la consola del backend
2. Realiza una operación que loguee datos sensibles
3. Los logs **NO deberían** mostrar:
   - CUITs completos
   - Emails completos
   - Montos completos

## 🎯 Próximos Pasos

Para ver el enmascaramiento de **montos** (ingresos/egresos), necesitas:

1. Modificar `IngresoController` y `EgresoController` para que devuelvan DTOs
2. O usar los endpoints públicos que ya existen (pero sin autenticación)

¿Quieres que modifique los controladores de ingresos y egresos para que también devuelvan DTOs con enmascaramiento?
