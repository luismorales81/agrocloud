package com.agrocloud.chatia.service;

import com.agrocloud.chatia.dto.ChatIaMensajeHistorialItem;
import com.agrocloud.chatia.dto.ChatIaMensajeRespuesta;
import com.agrocloud.chatia.dto.ChatIaMensajeSolicitud;
import com.agrocloud.chatia.herramientas.ContextoConsultaChatIa;
import com.agrocloud.chatia.herramientas.RegistroHerramientasConsulta;
import com.agrocloud.core.application.CampanaContextService;
import com.agrocloud.core.application.CompanyModuleService;
import com.agrocloud.core.application.UserService;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.security.ServicioSeguridadContexto;
import com.agrocloud.dto.CompanyModuleDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ServicioOrquestadorChatIa {

    private static final Logger logger = LoggerFactory.getLogger(ServicioOrquestadorChatIa.class);
    private static final int MAX_ITERACIONES_HERRAMIENTAS = 5;

    private final ServicioConfiguracionIaUsuario servicioConfiguracion;
    private final ServicioClienteGemini servicioClienteGemini;
    private final RegistroHerramientasConsulta registroHerramientas;
    private final ServicioSeguridadContexto servicioSeguridadContexto;
    private final ServicioLimiteTasaChatIa servicioLimiteTasa;
    private final CompanyModuleService companyModuleService;
    private final CampanaContextService campanaContextService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public ServicioOrquestadorChatIa(
            ServicioConfiguracionIaUsuario servicioConfiguracion,
            ServicioClienteGemini servicioClienteGemini,
            RegistroHerramientasConsulta registroHerramientas,
            ServicioSeguridadContexto servicioSeguridadContexto,
            ServicioLimiteTasaChatIa servicioLimiteTasa,
            CompanyModuleService companyModuleService,
            @Qualifier("campanaContextServiceCore") CampanaContextService campanaContextService,
            @Qualifier("userServiceCore") UserService userService,
            ObjectMapper objectMapper) {
        this.servicioConfiguracion = servicioConfiguracion;
        this.servicioClienteGemini = servicioClienteGemini;
        this.registroHerramientas = registroHerramientas;
        this.servicioSeguridadContexto = servicioSeguridadContexto;
        this.servicioLimiteTasa = servicioLimiteTasa;
        this.companyModuleService = companyModuleService;
        this.campanaContextService = campanaContextService;
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    public ChatIaMensajeRespuesta procesarMensaje(ChatIaMensajeSolicitud solicitud) {
        Long usuarioId = servicioSeguridadContexto.obtenerUsuarioIdActual();
        servicioLimiteTasa.verificarLimite(usuarioId);

        CredencialesChatIa credenciales = servicioConfiguracion.obtenerCredencialesActivas(usuarioId);
        String claveApi = credenciales.claveApi();

        ContextoConsultaChatIa contexto = construirContexto(solicitud.getModuloActivo());
        List<Map<String, Object>> declaraciones = registroHerramientas.declaracionesParaContexto(contexto);
        String instruccionSistema = construirInstruccionSistema(contexto);

        List<String> herramientasUsadas = new ArrayList<>();
        ChatIaMensajeRespuesta respuesta = new ChatIaMensajeRespuesta();

        try {
            ServicioClienteGemini.ResultadoGemini resultado = servicioClienteGemini.generar(
                    claveApi,
                    credenciales.modelo(),
                    instruccionSistema,
                    solicitud.getHistorial(),
                    solicitud.getMensaje(),
                    declaraciones);

            ArrayNode contenidosAcumulados = construirContenidosIniciales(solicitud);
            int iteraciones = 0;

            while (resultado.tieneLlamadasFuncion() && iteraciones < MAX_ITERACIONES_HERRAMIENTAS) {
                iteraciones++;
                contenidosAcumulados.add(resultado.getContenidoModelo().deepCopy());

                ObjectNode turnoFuncion = contenidosAcumulados.addObject();
                turnoFuncion.put("role", "user");
                ArrayNode partesFuncion = turnoFuncion.putArray("parts");

                for (ServicioClienteGemini.LlamadaFuncionGemini llamada : resultado.getLlamadasFuncion()) {
                    herramientasUsadas.add(llamada.nombre());
                    Object resultadoHerramienta = registroHerramientas.ejecutar(
                            llamada.nombre(), contexto, llamada.argumentos());
                    ObjectNode parte = partesFuncion.addObject();
                    ObjectNode functionResponse = parte.putObject("functionResponse");
                    functionResponse.put("name", llamada.nombre());
                    if (llamada.id() != null && !llamada.id().isBlank()) {
                        functionResponse.put("id", llamada.id());
                    }
                    functionResponse.set(
                            "response",
                            servicioClienteGemini.normalizarRespuestaHerramienta(resultadoHerramienta));
                }

                resultado = servicioClienteGemini.generarConRespuestaFuncion(
                        claveApi,
                        credenciales.modelo(),
                        instruccionSistema,
                        contenidosAcumulados,
                        declaraciones);
            }

            String textoFinal = resultado.getTexto();
            if (textoFinal == null || textoFinal.isBlank()) {
                textoFinal = "No pude generar una respuesta con los datos disponibles.";
            }
            respuesta.setRespuesta(textoFinal);
            respuesta.setHerramientasUsadas(herramientasUsadas);
            respuesta.setIaDisponible(true);
            if (credenciales.claveDeUsuario()) {
                servicioConfiguracion.marcarClaveValida(usuarioId);
            }
        } catch (Exception e) {
            if (credenciales.claveDeUsuario() && esErrorAutenticacionGemini(e.getMessage())) {
                servicioConfiguracion.marcarClaveInvalida(usuarioId);
            }
            respuesta.setIaDisponible(false);
            respuesta.setError("error_consulta_ia");
            respuesta.setRespuesta("No se pudo completar la consulta con IA. Intentá de nuevo en unos momentos.");
            logger.warn("Error en consulta chat IA usuario {}: {}", usuarioId, e.getMessage());
        }

        return respuesta;
    }

    private ContextoConsultaChatIa construirContexto(String moduloActivoUi) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        Long campanaId = campanaContextService.resolverCampanaIdActiva(empresaId);
        Long usuarioId = servicioSeguridadContexto.obtenerUsuarioIdActual();
        User usuario = userService.findById(usuarioId)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        Set<String> modulos = companyModuleService.getEnabledModulesForCompany(empresaId).stream()
                .filter(cm -> Boolean.TRUE.equals(cm.getEnabled()))
                .map(CompanyModuleDTO::getModuleCode)
                .map(RegistroHerramientasConsulta::normalizarCodigoModulo)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return new ContextoConsultaChatIa(empresaId, campanaId, usuarioId, usuario, modulos, moduloActivoUi);
    }

    private String construirInstruccionSistema(ContextoConsultaChatIa contexto) {
        return """
                Sos un asistente de AgroGestion especializado en información productiva agropecuaria.
                REGLAS ESTRICTAS:
                1. Solo respondé sobre datos del establecimiento del usuario usando las herramientas disponibles.
                2. NUNCA inventes cifras, fechas ni nombres: usá únicamente resultados de herramientas.
                3. Si no hay herramienta o datos suficientes, decí claramente que no podés responder.
                4. No respondas preguntas generales, de actualidad, política, ni temas fuera de la producción agropecuaria.
                5. Respondé siempre en castellano, de forma clara y concisa.
                6. Si el usuario menciona "lote" sin especificar módulo, priorizá el módulo activo: %s.
                7. Empresa ID: %d. Campaña ID: %d. Módulos contratados: %s.
                8. Las herramientas CORE siempre están disponibles y cubren datos compartidos de la empresa activa:
                   insumos (incluido insumosPorVencer), campos, lotes, labores, maquinaria, finanzas e inventario.
                   Usá estas herramientas para consultas sobre stock, vencimientos, labores, maquinaria o balance.
                9. Para insumos por vencer o vencidos, usá la herramienta insumosPorVencer.
                10. Para kilometraje, mantenimiento o costos de equipos, usá listarMaquinaria (incluye kilometrosUso,
                    kilometrosHastaProximoMantenimiento y requiereMantenimiento).
                """.formatted(
                contexto.getModuloActivo() != null ? contexto.getModuloActivo() : "cultivos",
                contexto.getEmpresaId(),
                contexto.getCampanaId(),
                String.join(", ", contexto.getModulosActivos()));
    }

    private ArrayNode construirContenidosIniciales(ChatIaMensajeSolicitud solicitud) {
        ArrayNode contents = objectMapper.createArrayNode();
        List<ChatIaMensajeHistorialItem> historial = solicitud.getHistorial();
        if (historial != null && historial.size() > 20) {
            historial = historial.subList(historial.size() - 20, historial.size());
        }
        if (historial != null) {
        for (ChatIaMensajeHistorialItem item : historial) {
            ObjectNode turno = contents.addObject();
            String rol = "asistente".equalsIgnoreCase(item.getRol()) ? "model" : "user";
            turno.put("role", rol);
            turno.putArray("parts").addObject().put("text", item.getContenido());
        }
        }
        ObjectNode turnoUsuario = contents.addObject();
        turnoUsuario.put("role", "user");
        turnoUsuario.putArray("parts").addObject().put("text", solicitud.getMensaje());
        return contents;
    }

    private boolean esErrorAutenticacionGemini(String mensaje) {
        if (mensaje == null || mensaje.isBlank()) {
            return false;
        }
        String normalizado = mensaje.toLowerCase();
        return normalizado.contains("api key not valid")
                || normalizado.contains("api_key_invalid")
                || normalizado.contains("invalid api key")
                || normalizado.contains("permission denied")
                || (normalizado.contains("api key") && normalizado.contains("invalid"));
    }
}
