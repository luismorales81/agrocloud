# Diseño técnico — Chat IA Gemini

**Versión:** 1.0  
**SPEC:** `SPEC-CHAT-IA-GEMINI.md`  
**Estado:** Aprobado

---

## 1. Esquema de base de datos

```sql
CREATE TABLE usuario_configuracion_ia (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL UNIQUE,
    clave_api_cifrada VARCHAR(512) NOT NULL,
    modelo VARCHAR(64) NOT NULL DEFAULT 'gemini-2.5-flash',
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    clave_valida BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_usuario_config_ia_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);
```

Migración: `V1_167__Chat_ia_configuracion_usuario.sql`

---

## 2. Paquete Java

```
com.agrocloud.chatia
├── domain/UsuarioConfiguracionIa.java
├── infrastructure/UsuarioConfiguracionIaRepository.java
├── dto/...
├── herramientas/
│   ├── ContextoConsultaChatIa.java
│   ├── HerramientaConsultaChatIa.java
│   ├── RegistroHerramientasConsulta.java
│   ├── HerramientasUtil.java
│   └── impl/ (una clase por módulo)
├── service/
│   ├── ServicioCifradoClaves.java
│   ├── ServicioConfiguracionIaUsuario.java
│   ├── ServicioClienteGemini.java
│   ├── ServicioLimiteTasaChatIa.java
│   └── ServicioOrquestadorChatIa.java
└── controller/ChatIaController.java
```

---

## 3. Flujo del orquestador

1. Validar rate limit y configuración del usuario.
2. Construir `ContextoConsultaChatIa` (empresa, campaña, módulos activos).
3. Filtrar herramientas por módulos habilitados.
4. Llamar Gemini con system prompt + historial + tools.
5. Si hay `functionCall`, ejecutar herramienta y reenviar `functionResponse` (máx. 5 iteraciones).
6. Devolver texto final + lista de herramientas usadas.

---

## 4. Cliente Gemini

- Endpoint: `POST https://generativelanguage.googleapis.com/v1beta/models/{modelo}:generateContent?key={clave}`
- Cliente: `RestClient` (Spring 6) o `WebClient`
- Validación de clave: `generateContent` con prompt mínimo "ok"

---

## 5. Cifrado

- Algoritmo: AES-256-GCM
- Clave derivada de `CHAT_IA_ENCRYPTION_KEY` (32 bytes hex o SHA-256 del valor)
- IV aleatorio por registro, prefijado en el campo cifrado

---

## 6. Frontend

| Archivo | Rol |
|---------|-----|
| `components/chatia/ChatIaWidget.tsx` | Panel flotante + input |
| `components/chatia/PantallaConfiguracionIa.tsx` | Formulario API key + modelo |
| `hooks/useChatIa.ts` | Estado mensajes + llamadas API |
| `services/chatIaService.ts` | Cliente HTTP |

Montaje en `ModularDashboard.tsx`. Enlace en `ModularSidebar` sección Perfil.

---

## 7. Contratos API (DTO)

### ChatIaEstadoRespuesta
```json
{ "habilitado": true, "modelo": "gemini-2.5-flash", "claveConfigurada": true, "claveInvalida": false }
```

### ChatIaMensajeSolicitud
```json
{
  "mensaje": "texto",
  "historial": [{ "rol": "usuario|asistente", "contenido": "..." }],
  "moduloActivo": "cultivos"
}
```

### ChatIaMensajeRespuesta
```json
{
  "respuesta": "texto",
  "herramientasUsadas": ["consultarLaboresPorLote"],
  "iaDisponible": true
}
```
