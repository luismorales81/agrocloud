# SPEC — Chat con IA (Gemini BYOK)

**Versión:** 1.0  
**Fecha:** Julio 2026  
**Metodología:** SDD  
**Estado:** Aprobada  
**Relacionada con:** `SPEC-SPRING-AI-INTEGRACION.md` (wizards Ollama — caso de uso distinto)

---

## 1. Objetivo

Ofrecer un **chat conversacional** integrado en la aplicación para que cada usuario consulte en **lenguaje natural** información productiva de su establecimiento, limitada a:

- Los **módulos contratados** por la empresa activa.
- Los **datos almacenados** accesibles para ese usuario y empresa.
- El **período de gestión (campaña)** activo cuando aplique.

El usuario configura su propia **API key de Google Gemini** (BYOK). Sin clave válida, el chat permanece deshabilitado.

---

## 2. Alcance v1

| Incluido | Excluido |
|----------|----------|
| Consultas de solo lectura | Crear, editar o eliminar registros |
| Los 7 módulos productivos + CORE | Consultas fuera del dominio agropecuario |
| Modelos: gemini-2.5-flash (default), gemini-2.5-pro, gemini-2.0-flash | Otros proveedores en v1 |
| Historial en sesión del navegador | Persistencia de conversaciones en BD |
| Widget global en todos los módulos | Chat embebido por pantalla |

---

## 3. Configuración por usuario (BYOK)

| Campo | Descripción |
|-------|-------------|
| `clave_api` | API key de Google AI Studio / Gemini |
| `modelo` | Uno de los modelos permitidos |
| `activo` | true si hay clave válida guardada |

Reglas:

- La clave se almacena **cifrada** en servidor (`usuario_configuracion_ia`).
- El frontend **nunca** envía la clave a Gemini directamente.
- Tras guardar, la UI muestra solo máscara (`****...últimos4`).
- Al guardar se valida la clave con una llamada de prueba a Gemini.

---

## 4. Seguridad y acotación

1. **Multi-tenant:** toda herramienta filtra por `empresaId` del contexto (`X-Company-Id`).
2. **Campaña:** cuando el módulo use campaña, filtrar por `X-Campaign-Id` o campaña activa por defecto.
3. **Módulos:** solo se exponen herramientas de módulos habilitados en `company_modules`.
4. **Sin SQL dinámico:** el modelo invoca funciones Java predefinidas (function calling).
5. **System prompt:** responder únicamente con datos devueltos por herramientas.
6. **Rate limit:** 20 mensajes por minuto por usuario.
7. **Clave de cifrado:** variable de entorno `CHAT_IA_ENCRYPTION_KEY` en el servidor.

---

## 5. Herramientas por módulo

### CORE (siempre disponible)

| Función | Descripción |
|---------|-------------|
| `modulosActivos` | Lista módulos contratados de la empresa |
| `campanaActiva` | Campaña/período de gestión actual |
| `resumenEmpresa` | Nombre e identificador de la empresa activa |

### CULTIVOS

| Función | Descripción |
|---------|-------------|
| `listarLotesCultivo` | Lotes agrícolas accesibles |
| `consultarLaboresPorLote` | Labores por lote (filtro tipo, límite, costos) |
| `ultimasCosechas` | Últimas cosechas registradas |

### PORCINOS

| Función | Descripción |
|---------|-------------|
| `listarLotesPorcinos` | Lotes porcinos de la empresa/campaña |
| `consumosRecientesPorcinos` | Consumos de alimento recientes |
| `eventosSanitariosPorcinos` | Eventos sanitarios recientes |
| `ventasRecientesPorcinos` | Ventas recientes |
| `pesadasRecientesPorcinos` | Pesadas recientes |

### LECHERIA

| Función | Descripción |
|---------|-------------|
| `listarAnimalesLecheria` | Animales del establecimiento |
| `produccionLechePeriodo` | Litros ordeñados en rango de fechas |
| `eventosSanitariosLecheria` | Sanidad reciente |
| `ventasLecheLecheria` | Ventas de leche recientes |

### FEEDLOT

| Función | Descripción |
|---------|-------------|
| `listarLotesFeedlot` | Lotes de engorde |
| `consumosRecientesFeedlot` | Consumos de alimento |
| `pesadasRecientesFeedlot` | Pesadas recientes |

### AVICOLA_CRIANZA

| Función | Descripción |
|---------|-------------|
| `listarLotesAvicolaCrianza` | Lotes de crianza/parrilleros |
| `mortalidadAvicolaCrianza` | Registros de mortalidad recientes |
| `pesadasAvicolaCrianza` | Pesadas recientes |

### AVICOLA_HUEVOS

| Función | Descripción |
|---------|-------------|
| `listarLotesAvicolaHuevos` | Lotes de postura |
| `produccionDiariaHuevos` | Producción diaria de huevos |

### AVICOLA_PONEDORAS

| Función | Descripción |
|---------|-------------|
| `listarGalponesPonedoras` | Galpones del módulo |
| `produccionPonedoras` | Producción reciente |
| `ambienteDiarioPonedoras` | Ambiente diario reciente |

---

## 6. Desambiguación de entidades

- Si el usuario menciona "lote" sin módulo, priorizar el **módulo activo** en la UI.
- Si hay ambigüedad entre módulos, la herramienta devuelve candidatos y el modelo pide aclaración.
- Cultivos: lote = `Plot` (parcela agrícola). Porcinos/Feedlot/Avícola: lote propio del módulo.

---

## 7. API REST

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/chat-ia/estado` | `{ habilitado, modelo, claveConfigurada, claveInvalida }` |
| GET | `/api/chat-ia/configuracion` | Config actual (sin clave en claro) |
| PUT | `/api/chat-ia/configuracion` | Guardar clave + modelo |
| DELETE | `/api/chat-ia/configuracion` | Eliminar configuración |
| POST | `/api/chat-ia/mensaje` | Enviar mensaje + historial de sesión |

---

## 8. Ejemplo de uso

**Pregunta:** "¿Cuál fue el monto de las últimas 5 fumigaciones en el lote 1?"

**Flujo:**
1. Modelo invoca `consultarLaboresPorLote(nombreLote="1", tipos=["CONTROL_PLAGAS","CONTROL_MALEZAS"], limite=5, incluirCostos=true)`.
2. Backend resuelve lote, consulta labores, suma costos.
3. Modelo responde en castellano con montos y fechas.

---

## 9. Criterios de aceptación

- Sin API key configurada: widget oculto o deshabilitado con enlace a configuración.
- Con API key válida y módulo cultivos: responde preguntas sobre labores/fumigaciones del lote.
- Usuario de empresa sin módulo porcinos: herramientas porcinas no disponibles para el modelo.
- Clave inválida: estado `claveInvalida=true` y mensaje claro en UI.
- Historial no se persiste en BD.

---

## 10. Riesgos

| Riesgo | Mitigación |
|--------|------------|
| Alucinaciones | System prompt + solo datos de tools |
| Fuga de clave API | Cifrado at-rest, nunca en logs ni respuestas |
| Costo Gemini del usuario | Aviso BYOK en pantalla de configuración |
| Prompt injection | Tools acotadas; sin acceso a datos de otras empresas |
