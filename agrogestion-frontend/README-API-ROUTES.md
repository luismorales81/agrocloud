# 🚀 Guía de Rutas API - AgroCloud Frontend

## 📋 Resumen Rápido

**Problema**: Duplicación de `/api` en las URLs causaba errores 404/403.  
**Solución**: Convención única + interceptor automático + scripts de verificación.

## 🎯 Convención Establecida

### ✅ **CORRECTO**
```typescript
api.get('/v1/lotes')        // ✅ Se convierte en /api/v1/lotes
api.post('/campos')         // ✅ Se convierte en /api/campos
api.get('/auth/login')      // ✅ Se convierte en /api/auth/login
```

### ❌ **INCORRECTO**
```typescript
api.get('/api/v1/lotes')    // ❌ Se convierte en /api/api/v1/lotes
api.post('/api/campos')     // ❌ Se convierte en /api/api/campos
```

## 🛠️ Herramientas Disponibles

### **1. Verificación Automática**
```bash
# Windows
npm run check-api-routes:win

# Linux/Mac
npm run check-api-routes:unix

# Cross-platform
npm run check-api-routes
```

### **2. Interceptor Automático**
El sistema detecta y corrige automáticamente duplicaciones:
```typescript
// Si accidentalmente escribes:
api.get('/api/v1/lotes')

// El interceptor lo corrige a:
api.get('/v1/lotes')
```

### **3. Documentación Completa**
Ver `API-ROUTES-CONVENTION.md` para detalles técnicos.

## 🔧 Uso Diario

### **Al Escribir Nuevas Rutas**
```typescript
// ✅ Siempre así:
api.get('/v1/endpoint')
api.post('/endpoint')
api.put('/v1/endpoint/123')
api.delete('/endpoint/456')
```

### **Al Revisar Código**
1. Ejecuta `npm run check-api-routes`
2. Si hay errores, corrige las rutas
3. Verifica que no uses `/api/` literal

### **Al Hacer Deploy**
1. Ejecuta verificación antes de commit
2. Revisa logs de consola del navegador
3. Asegúrate de que no aparezcan warnings de URL duplicada

## 🚨 Solución de Problemas

### **Error: 404 Not Found**
```typescript
// ❌ Problema
api.get('/api/v1/lotes')  // URL: /api/api/v1/lotes

// ✅ Solución
api.get('/v1/lotes')      // URL: /api/v1/lotes
```

### **Error: 403 Forbidden**
```typescript
// ❌ Problema
api.get('/api/auth/login')  // URL: /api/api/auth/login

// ✅ Solución
api.get('/auth/login')      // URL: /api/auth/login
```

### **Warning en Consola**
```
🚨 [API] URL duplicada detectada: /api/api/v1/lotes
🔧 [API] URL corregida: /api/v1/lotes
```
**Solución**: Corrige la ruta en el código para evitar el warning.

## 📝 Checklist de Verificación

- [ ] No hay rutas con `/api/` literal
- [ ] Todas las rutas usan `/v1/` o sin prefijo
- [ ] Script de verificación pasa sin errores
- [ ] No hay warnings en consola del navegador
- [ ] Todas las peticiones HTTP funcionan

## 🎯 Beneficios

1. **Prevención automática**: El interceptor corrige errores
2. **Verificación rápida**: Scripts detectan problemas
3. **Consistencia**: Todas las rutas siguen el mismo patrón
4. **Mantenibilidad**: Fácil de entender y modificar
5. **Escalabilidad**: Fácil agregar nuevos endpoints

## 📞 Soporte

Si encuentras problemas:

1. **Ejecuta verificación**: `npm run check-api-routes`
2. **Revisa consola**: Busca warnings de URL duplicada
3. **Consulta documentación**: `API-ROUTES-CONVENTION.md`
4. **Verifica rutas**: Asegúrate de no usar `/api/` literal

---

**Mantenido por**: Equipo AgroCloud  
**Última actualización**: Octubre 2025
