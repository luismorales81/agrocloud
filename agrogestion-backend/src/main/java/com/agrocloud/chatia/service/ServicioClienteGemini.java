package com.agrocloud.chatia.service;

import com.agrocloud.chatia.dto.ChatIaMensajeHistorialItem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Cliente HTTP para la API REST de Google Gemini (function calling).
 */
@Service
public class ServicioClienteGemini {

    private static final Logger log = LoggerFactory.getLogger(ServicioClienteGemini.class);
    private static final String URL_BASE = "https://generativelanguage.googleapis.com/v1beta/models/";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public ServicioClienteGemini(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().build();
    }

    public boolean validarClave(String claveApi, String modelo) {
        try {
            ObjectNode cuerpo = objectMapper.createObjectNode();
            ArrayNode contents = cuerpo.putArray("contents");
            ObjectNode turno = contents.addObject();
            turno.put("role", "user");
            turno.putArray("parts").addObject().put("text", "Responde solo: ok");
            llamarGemini(modelo, claveApi, cuerpo);
            return true;
        } catch (Exception e) {
            log.warn("Validación de clave Gemini fallida: {}", e.getMessage());
            return false;
        }
    }

    public ResultadoGemini generar(
            String claveApi,
            String modelo,
            String instruccionSistema,
            List<ChatIaMensajeHistorialItem> historial,
            String mensajeUsuario,
            List<Map<String, Object>> declaracionesHerramientas) {

        ObjectNode cuerpo = objectMapper.createObjectNode();
        ObjectNode systemInstruction = cuerpo.putObject("systemInstruction");
        systemInstruction.putArray("parts").addObject().put("text", instruccionSistema);

        ArrayNode contents = cuerpo.putArray("contents");
        for (ChatIaMensajeHistorialItem item : historial) {
            agregarTurnoHistorial(contents, item);
        }
        ObjectNode turnoUsuario = contents.addObject();
        turnoUsuario.put("role", "user");
        turnoUsuario.putArray("parts").addObject().put("text", mensajeUsuario);

        if (!declaracionesHerramientas.isEmpty()) {
            ArrayNode tools = cuerpo.putArray("tools");
            ObjectNode tool = tools.addObject();
            ArrayNode functionDeclarations = tool.putArray("functionDeclarations");
            for (Map<String, Object> declaracion : declaracionesHerramientas) {
                functionDeclarations.add(objectMapper.valueToTree(declaracion));
            }
        }

        ObjectNode generationConfig = cuerpo.putObject("generationConfig");
        generationConfig.put("temperature", 0.2);

        JsonNode respuesta = llamarGemini(modelo, claveApi, cuerpo);
        return parsearResultado(respuesta);
    }

    public ResultadoGemini generarConRespuestaFuncion(
            String claveApi,
            String modelo,
            String instruccionSistema,
            ArrayNode contenidosAcumulados,
            List<Map<String, Object>> declaracionesHerramientas) {

        ObjectNode cuerpo = objectMapper.createObjectNode();
        ObjectNode systemInstruction = cuerpo.putObject("systemInstruction");
        systemInstruction.putArray("parts").addObject().put("text", instruccionSistema);
        cuerpo.set("contents", contenidosAcumulados);

        if (!declaracionesHerramientas.isEmpty()) {
            ArrayNode tools = cuerpo.putArray("tools");
            ObjectNode tool = tools.addObject();
            ArrayNode functionDeclarations = tool.putArray("functionDeclarations");
            for (Map<String, Object> declaracion : declaracionesHerramientas) {
                functionDeclarations.add(objectMapper.valueToTree(declaracion));
            }
        }

        ObjectNode generationConfig = cuerpo.putObject("generationConfig");
        generationConfig.put("temperature", 0.2);

        JsonNode respuesta = llamarGemini(modelo, claveApi, cuerpo);
        return parsearResultado(respuesta);
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * Gemini exige que {@code functionResponse.response} sea un Struct (objeto JSON), no un array ni escalar.
     */
    public JsonNode normalizarRespuestaHerramienta(Object resultado) {
        JsonNode nodo = objectMapper.valueToTree(resultado);
        if (nodo == null || nodo.isNull()) {
            ObjectNode vacio = objectMapper.createObjectNode();
            vacio.putNull("resultado");
            return vacio;
        }
        if (nodo.isObject()) {
            return nodo;
        }
        ObjectNode envoltorio = objectMapper.createObjectNode();
        if (nodo.isArray()) {
            envoltorio.set("datos", nodo);
        } else {
            envoltorio.set("resultado", nodo);
        }
        return envoltorio;
    }

    private void agregarTurnoHistorial(ArrayNode contents, ChatIaMensajeHistorialItem item) {
        ObjectNode turno = contents.addObject();
        String rol = "asistente".equalsIgnoreCase(item.getRol()) ? "model" : "user";
        turno.put("role", rol);
        turno.putArray("parts").addObject().put("text", item.getContenido());
    }

    private JsonNode llamarGemini(String modelo, String claveApi, ObjectNode cuerpo) {
        String url = URL_BASE + modelo + ":generateContent";
        try {
            return restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-goog-api-key", claveApi)
                    .body(cuerpo)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientResponseException e) {
            log.error("Error Gemini HTTP {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new IllegalStateException("Error al comunicarse con Gemini: " + extraerMensajeError(e.getResponseBodyAsString()));
        }
    }

    private String extraerMensajeError(String cuerpo) {
        try {
            JsonNode nodo = objectMapper.readTree(cuerpo);
            if (nodo.has("error") && nodo.get("error").has("message")) {
                return nodo.get("error").get("message").asText();
            }
        } catch (Exception ignored) {
            // usar mensaje genérico
        }
        return "respuesta no válida del proveedor";
    }

    private ResultadoGemini parsearResultado(JsonNode respuesta) {
        ResultadoGemini resultado = new ResultadoGemini();
        if (respuesta == null || !respuesta.has("candidates") || respuesta.get("candidates").isEmpty()) {
            resultado.setTexto("No se recibió respuesta del modelo.");
            return resultado;
        }
        JsonNode content = respuesta.get("candidates").get(0).get("content");
        if (content == null || !content.has("parts")) {
            resultado.setTexto("No se recibió contenido del modelo.");
            return resultado;
        }
        List<LlamadaFuncionGemini> llamadas = new ArrayList<>();
        StringBuilder texto = new StringBuilder();
        for (JsonNode part : content.get("parts")) {
            if (part.has("text")) {
                texto.append(part.get("text").asText());
            }
            if (part.has("functionCall")) {
                JsonNode fc = part.get("functionCall");
                String nombre = fc.get("name").asText();
                String id = fc.has("id") ? fc.get("id").asText() : null;
                Map<String, Object> args = fc.has("args")
                        ? objectMapper.convertValue(fc.get("args"), Map.class)
                        : Map.of();
                llamadas.add(new LlamadaFuncionGemini(nombre, args, id));
            }
        }
        resultado.setTexto(texto.toString());
        resultado.setLlamadasFuncion(llamadas);
        resultado.setContenidoModelo(content);
        return resultado;
    }

    public static class ResultadoGemini {
        private String texto = "";
        private List<LlamadaFuncionGemini> llamadasFuncion = new ArrayList<>();
        private JsonNode contenidoModelo;

        public String getTexto() {
            return texto;
        }

        public void setTexto(String texto) {
            this.texto = texto;
        }

        public List<LlamadaFuncionGemini> getLlamadasFuncion() {
            return llamadasFuncion;
        }

        public void setLlamadasFuncion(List<LlamadaFuncionGemini> llamadasFuncion) {
            this.llamadasFuncion = llamadasFuncion;
        }

        public JsonNode getContenidoModelo() {
            return contenidoModelo;
        }

        public void setContenidoModelo(JsonNode contenidoModelo) {
            this.contenidoModelo = contenidoModelo;
        }

        public boolean tieneLlamadasFuncion() {
            return llamadasFuncion != null && !llamadasFuncion.isEmpty();
        }

        public Optional<LlamadaFuncionGemini> primeraLlamada() {
            if (llamadasFuncion == null || llamadasFuncion.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(llamadasFuncion.get(0));
        }
    }

    public record LlamadaFuncionGemini(String nombre, Map<String, Object> argumentos, String id) {
        public LlamadaFuncionGemini(String nombre, Map<String, Object> argumentos) {
            this(nombre, argumentos, null);
        }
    }
}
