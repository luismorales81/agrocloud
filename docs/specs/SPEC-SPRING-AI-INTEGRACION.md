# SPEC: Integración Spring AI (Ollama / Gemini)

**Versión:** 1.0 · **Estado:** Aprobada para implementación  
**Contexto:** Wizards de configuración asistida por IA para Cultivos y Porcinos.

## 1. Objetivo

Centralizar llamadas a modelos de lenguaje en el **backend**, con proveedor intercambiable por perfil, respuestas estructuradas en JSON y fallback seguro si el servicio no está disponible.

## 2. Estrategia técnica (implementado v1)

- **Cliente HTTP** a la API REST de **Ollama** (`POST /api/chat`, `format: json`) desde `ServicioClienteIaOllama`, sin dependencia Maven de Spring AI (evita choques de BOM con el `pom` actual). Compatible con sustituir por `ChatClient` de Spring AI en una fase posterior.
- **Alternativa producción:** Google Gemini u otro proveedor: añadir starter Spring AI o cliente HTTP equivalente cuando se defina API key y política de costos.
- **Fallback:** Si la llamada falla o el JSON no es válido, los controladores devuelven `{ "iaDisponible": false, "datos": null }` con **HTTP 200** para no bloquear la UI (formulario manual).

## 3. Variables de entorno

Ver `.env.example` en la raíz del repositorio (sección IA).

## 4. Seguridad

- Nunca exponer API keys al frontend.
- Construcción de prompts solo en servidor.

## 5. Caché

- `@Cacheable` en métodos `preview` con clave compuesta por empresa + parámetros de entrada (ver implementación).

## 6. Criterios de aceptación

- Arranque del backend sin Ollama: los endpoints `preview` responden `iaDisponible: false`.
- Con Ollama y modelo instalado: `preview` devuelve JSON válido mapeable a DTOs.
