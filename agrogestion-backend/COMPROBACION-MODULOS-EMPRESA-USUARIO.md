# Comprobación de módulos respecto a empresa y usuario

## ¿Se realiza la comprobación?

**Sí, existe el mecanismo**, pero **solo se ejecuta** cuando el endpoint está anotado con `@RequiresModule` y el frontend envía la empresa activa.

## Cómo funciona

1. **Interceptor** (`ModuleAccessInterceptor`): se ejecuta en peticiones a `/api/**` (con excepciones en auth, admin-global, etc.).
2. **Solo valida** si el controlador o el método tiene la anotación `@RequiresModule("crops")` o `@RequiresModule("pigs")` (u otro código de módulo).
3. Cuando **sí** está anotado, el interceptor comprueba:
   - Usuario autenticado.
   - **Empresa en el request**: header `X-Company-Id` o parámetro `companyId`.
   - **Usuario pertenece a esa empresa** (`user.perteneceAEmpresa(companyId)`).
   - **Empresa tiene el módulo habilitado** (`companyModuleService.hasModuleEnabled(companyId, moduleCode)`).
   - **Usuario tiene el permiso** sobre el módulo (read/write/manage según `@RequiresModule(..., permission = "..."`).
4. **Superadmin** no pasa por esta validación (acceso total).
5. Si falta algo: responde 401/400/402/403 y no llama al controlador.

## Estado actual

| Elemento | Estado |
|----------|--------|
| Interceptor registrado en `WebMvcConfig` | ✅ |
| Frontend envía `X-Company-Id` (empresa activa en localStorage) | ✅ (añadido en `api.ts`) |
| Controladores anotados con `@RequiresModule` | ❌ Ninguno todavía |
| Servicios que filtran por empresa (ej. `obtenerEmpresaPrincipalDelUsuario`) | ✅ Varios (porcinos, cultivos, etc.) |

**Conclusión:** La comprobación de módulo/empresa/usuario **está implementada** y el front ya envía `X-Company-Id`. Para que se aplique a un endpoint hay que anotar el controlador o el método con `@RequiresModule`, por ejemplo:

- Cultivos / lotes / labores: `@RequiresModule("crops")` (o permiso `write`/`manage` donde corresponda).
- Porcinos: `@RequiresModule("pigs")`.

Ver `EJEMPLO-USO-REQUIRES-MODULE.md` para ejemplos de uso.
