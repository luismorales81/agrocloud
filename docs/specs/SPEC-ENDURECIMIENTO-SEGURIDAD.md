# SPEC — Endurecimiento de seguridad

**Versión:** 1.0  
**Fecha:** Julio 2026  
**Metodología:** SDD  
**Estado:** Aprobada para implementación  
**Origen:** Auditoría de seguridad post-integración Chat IA

---

## 1. Objetivo

Remediar vulnerabilidades críticas, altas y medias identificadas en backend, frontend y configuración de despliegue, sin degradar funcionalidad productiva.

---

## 2. Mapeo OWASP Top 10 (2021)

| Hallazgo | OWASP |
|----------|-------|
| Bypass JWT perfil test, fallback test@test.com, endpoints /test | A01 Broken Access Control |
| Secretos por defecto, claves API en bundle, CHAT_IA sin prod | A02 Cryptographic Failures |
| Enumeración login, logs de tokens | A07 Identification and Authentication Failures |
| Defaults en application.properties, endpoints debug | A05 Security Misconfiguration |
| XSS BalanceReport, historial chat manipulable | A03 Injection |
| Rate limit ausente en auth | A04 Insecure Design |

---

## 3. Alcance por fase

### Fase 1 — Críticos
- Secretos sin defaults en perfiles desplegables; validación de arranque
- Eliminar bypass JWT y impersonación test@test.com
- API keys fuera del bundle; clima vía backend
- Contraseñas fuera de localStorage (AdminUsuarios)
- gitignore archivos sensibles

### Fase 2 — Altos
- Endpoints /test eliminados o restringidos
- Mensajes de error genéricos; sin logs de tokens
- Chat IA: whitelist rol, límites, errores sanitizados
- Multi-tenant avícola vía ServicioSeguridadContexto
- XSS, SW sin cache /api/, logs solo DEV
- Rate limiting login/register

### Fase 3 — Mediano plazo
- JWT en cookies httpOnly
- CSP Vercel completa
- Rate limit distribuido (documentado)
- CI: npm audit, detección de secretos

---

## 4. Acción manual obligatoria (operaciones)

Tras desplegar Fase 1 en producción:

1. Rotar `JWT_SECRET` en Railway
2. Definir `CHAT_IA_ENCRYPTION_KEY` (32+ caracteres); usuarios deberán re-ingresar clave Gemini si se rotó desde default
3. Rotar/restringir claves Google Maps y OpenWeather
4. Eliminar del disco `CREDENCIALES-*.txt`, `DATOS-SUPERMAN-*.txt` (nunca commitear)
5. Confirmar `SPRING_PROFILES_ACTIVE=prod` o `railway`

---

## 5. Criterios de aceptación

### Fase 1
- [ ] Backend prod no arranca sin JWT_SECRET, DATABASE_PASSWORD, CHAT_IA_ENCRYPTION_KEY
- [ ] No hay `test@test.com` fallback en controladores
- [ ] No hay bypass JWT en JwtAuthenticationFilter
- [ ] googleMaps.ts sin clave hardcodeada
- [ ] WeatherWidget usa /api/v1/weather del backend
- [ ] AdminUsuarios no persiste password en localStorage

### Fase 2
- [ ] /api/test/** denegado o eliminado
- [ ] Login: mensaje único para email inexistente y contraseña incorrecta
- [ ] Chat: rol historial solo usuario|asistente; mensaje max 4000 chars
- [ ] Avícola usa ServicioSeguridadContexto
- [ ] Sin console.log de tokens en producción
- [ ] SW no cachea /api/
- [ ] Rate limit login/register activo

### Fase 3
- [ ] Login emite cookie httpOnly; frontend sin token en localStorage
- [ ] vercel.json CSP con connect-src y script-src
- [ ] Workflow CI con npm audit

---

## 6. Riesgos de regresión

| Riesgo | Mitigación |
|--------|------------|
| Widgets clima | Probar con backend y OPENWEATHER_API_KEY |
| PWA offline | Cache solo estáticos |
| Rotación CHAT_IA_ENCRYPTION_KEY | Invalidar configs IA; mensaje en UI |
| Tests CI | MockMvc addFilters=false ya usado |

---

## 7. Referencias

- Auditoría de seguridad (conversación Julio 2026)
- `DISENO-TECNICO-ENDURECIMIENTO-SEGURIDAD.md`
