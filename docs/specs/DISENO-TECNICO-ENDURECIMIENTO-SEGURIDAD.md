# Diseño técnico — Endurecimiento de seguridad

**SPEC:** `SPEC-ENDURECIMIENTO-SEGURIDAD.md`  
**Estado:** Aprobado

---

## 1. Backend — secretos

| Archivo | Cambio |
|---------|--------|
| `application.properties` | Quitar defaults jwt, password BD, chat.ia.encryption-key |
| `application-dev.properties` | Nuevo: defaults solo desarrollo local |
| `application-prod.properties` | `chat.ia.encryption-key=${CHAT_IA_ENCRYPTION_KEY}` |
| `ValidacionSecretosArranque.java` | `@Configuration` ApplicationListener; perfiles prod/railway |

## 2. Backend — auth

| Archivo | Cambio |
|---------|--------|
| `JwtAuthenticationFilter.java` | Eliminar bypass perfil test |
| `FieldController`, `InsumoController`, `LaborController`, `TrazabilidadComercialController` | 401 si userDetails null |
| `SecurityConfig.java` | `denyAll()` para `/api/test/**` |
| `TestDashboardController.java` | Eliminar |
| Varios controllers | Eliminar métodos `/test` |

## 3. Backend — errores y chat

| Archivo | Cambio |
|---------|--------|
| `GlobalExceptionHandler.java` | Mensajes genéricos |
| `AuthService.java` | Sin log token reset |
| `ChatIaMensajeHistorialItem.java` | `@Pattern` rol |
| `ChatIaMensajeSolicitud.java` | `@Size(max=4000)` |
| `ServicioOrquestadorChatIa.java` | Max 20 historial; error genérico |
| `ServicioLimiteTasaAuth.java` | Nuevo: login 10/min, register 3/h por IP |

## 4. Backend — avícola

Inyectar `ServicioSeguridadContexto` en AvicolaHuevosController, AvicolaCrianzaController, GestionPlanRecriaController; reemplazar `@RequestHeader X-Company-Id` por `resolverEmpresaId(request)`.

## 5. Frontend

| Archivo | Cambio |
|---------|--------|
| `googleMaps.ts` | `VITE_GOOGLE_MAPS_API_KEY` |
| `WeatherWidget.tsx`, `FieldWeatherWidget.tsx` | API backend |
| `AdminUsuarios.tsx` | Sin password en localStorage |
| `sw.js` | Sin cache `/api/` |
| `BalanceReport.tsx` | `escaparHtml()` |
| `api.ts`, AuthContext, etc. | Logs solo DEV |

## 6. Fase 3 — cookies JWT

| Archivo | Cambio |
|---------|--------|
| `AuthController` / `AuthService` | Set-Cookie httpOnly en login |
| `JwtAuthenticationFilter` | Leer cookie `agro_token` |
| `AuthContext.tsx` | withCredentials; sin localStorage token |
| `vercel.json` | CSP ampliada |
| `.github/workflows/security.yml` | npm audit |
