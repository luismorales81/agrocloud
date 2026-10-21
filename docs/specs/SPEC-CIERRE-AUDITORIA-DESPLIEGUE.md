# SPEC — Cierre de auditoría previa al despliegue

**Fecha:** 2026-10-20  
**Estado:** Aprobada para implementación (pedido explícito de aplicar los cambios de la auditoría)  
**Origen:** Auditoría de seguridad del 20 de octubre de 2026

## Objetivo

Cerrar las fallas altas detectadas antes de publicar AgroGestion, sin sacar de servicio a las cuentas que ya operan.

## Alcance

1. El PDF y el estado del EULA exigen sesión. La aceptación pública por email se reemplaza por un ticket de corta vida emitido solo después de una contraseña correcta. La IP y el navegador se toman de la petición, no del cuerpo.
2. Caddy y nginx reemplazan `X-Forwarded-For` por la IP real. El límite de tasa usa `X-Real-IP` o el último tramo de la cadena.
3. Se eliminan las rutas de hash, prueba de contraseña y correo de prueba.
4. Se borran de la documentación las contraseñas históricas de Zoho y de MySQL. La rotación en los servicios vivos queda como acción manual.
5. El registro público sigue abierto, pero la cuenta no entra hasta confirmar el correo. Las cuentas ya existentes quedan confirmadas por migración. El recupero de contraseña tiene límite de tasa. Cambiar la contraseña invalida los JWT anteriores.
6. El JAR no fija el perfil `dev`. El default de la cookie en producción pasa a `SameSite=Lax`.

## Fuera de alcance

Restringir la clave de Google Maps en Google Cloud (consola del operador). Content-Security-Policy estricta: la interfaz usa estilos en línea y un CSP sin `unsafe-inline` rompería la pantalla.
