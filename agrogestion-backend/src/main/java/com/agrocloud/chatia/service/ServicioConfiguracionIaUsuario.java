package com.agrocloud.chatia.service;

import com.agrocloud.chatia.domain.UsuarioConfiguracionIa;
import com.agrocloud.chatia.dto.ChatIaConfiguracionRespuesta;
import com.agrocloud.chatia.dto.ChatIaConfiguracionSolicitud;
import com.agrocloud.chatia.dto.ChatIaEstadoRespuesta;
import com.agrocloud.chatia.infrastructure.UsuarioConfiguracionIaRepository;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.infrastructure.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServicioConfiguracionIaUsuario {

    public static final List<String> MODELOS_PERMITIDOS = List.of(
            "gemini-flash-latest",
            "gemini-3.6-flash",
            "gemini-3.1-pro-preview",
            "gemini-pro-latest",
            "gemini-2.5-flash",
            "gemini-2.5-pro",
            "gemini-2.0-flash"
    );

    private final UsuarioConfiguracionIaRepository repositorio;
    private final UserRepository userRepository;
    private final ServicioCifradoClaves servicioCifrado;
    private final ServicioClienteGemini servicioClienteGemini;
    private final String claveApiProyecto;
    private final boolean iaProyectoHabilitada;
    private final String modeloProyecto;

    public ServicioConfiguracionIaUsuario(
            UsuarioConfiguracionIaRepository repositorio,
            UserRepository userRepository,
            ServicioCifradoClaves servicioCifrado,
            ServicioClienteGemini servicioClienteGemini,
            @Value("${ia.gemini.api-key:}") String claveApiProyecto,
            @Value("${ia.gemini.habilitado:true}") boolean iaProyectoHabilitada,
            @Value("${ia.gemini.model:gemini-flash-latest}") String modeloProyecto) {
        this.repositorio = repositorio;
        this.userRepository = userRepository;
        this.servicioCifrado = servicioCifrado;
        this.servicioClienteGemini = servicioClienteGemini;
        this.claveApiProyecto = claveApiProyecto != null ? claveApiProyecto.trim() : "";
        this.iaProyectoHabilitada = iaProyectoHabilitada;
        this.modeloProyecto = modeloProyecto != null && !modeloProyecto.isBlank()
                ? modeloProyecto.trim()
                : "gemini-flash-latest";
    }

    @Transactional(readOnly = true)
    public ChatIaEstadoRespuesta obtenerEstado(Long usuarioId) {
        ChatIaEstadoRespuesta estado = new ChatIaEstadoRespuesta();
        estado.setModelosDisponibles(MODELOS_PERMITIDOS);
        return repositorio.findByUsuarioId(usuarioId)
                .map(config -> {
                    if (tieneClaveProyecto()) {
                        return aplicarEstadoClaveProyecto(estado);
                    }
                    estado.setClaveConfigurada(true);
                    estado.setModelo(config.getModelo());
                    estado.setClaveInvalida(!config.isClaveValida());
                    estado.setHabilitado(config.isActivo());
                    return estado;
                })
                .orElseGet(() -> aplicarEstadoClaveProyecto(estado));
    }

    @Transactional(readOnly = true)
    public ChatIaConfiguracionRespuesta obtenerConfiguracion(Long usuarioId) {
        ChatIaConfiguracionRespuesta respuesta = new ChatIaConfiguracionRespuesta();
        respuesta.setModelo("gemini-flash-latest");
        return repositorio.findByUsuarioId(usuarioId)
                .map(config -> {
                    respuesta.setModelo(config.getModelo());
                    respuesta.setActivo(config.isActivo());
                    respuesta.setClaveConfigurada(true);
                    respuesta.setClaveInvalida(!config.isClaveValida());
                    respuesta.setClaveEnmascarada(enmascararClave(servicioCifrado.descifrar(config.getClaveApiCifrada())));
                    return respuesta;
                })
                .orElseGet(() -> aplicarConfiguracionClaveProyecto(respuesta));
    }

    @Transactional
    public ChatIaConfiguracionRespuesta guardarConfiguracion(Long usuarioId, ChatIaConfiguracionSolicitud solicitud) {
        validarModelo(solicitud.getModelo());

        var existente = repositorio.findByUsuarioId(usuarioId);
        String clavePlana;
        boolean claveNueva = solicitud.getClaveApi() != null && !solicitud.getClaveApi().isBlank();

        if (claveNueva) {
            clavePlana = solicitud.getClaveApi().trim();
        } else if (existente.isPresent()) {
            clavePlana = servicioCifrado.descifrar(existente.get().getClaveApiCifrada());
        } else {
            throw new IllegalArgumentException("Ingresá tu clave API de Gemini para activar el asistente.");
        }

        boolean claveValida = servicioClienteGemini.validarClave(clavePlana, solicitud.getModelo());
        if (!claveValida) {
            throw new IllegalArgumentException("La clave API de Gemini no es válida o el modelo no está disponible.");
        }

        User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        UsuarioConfiguracionIa config = existente.orElseGet(() -> {
            UsuarioConfiguracionIa nueva = new UsuarioConfiguracionIa();
            nueva.setUsuario(usuario);
            return nueva;
        });

        if (claveNueva) {
            config.setClaveApiCifrada(servicioCifrado.cifrar(clavePlana));
        }
        config.setModelo(solicitud.getModelo());
        config.setActivo(true);
        config.setClaveValida(true);
        repositorio.save(config);

        ChatIaConfiguracionRespuesta respuesta = new ChatIaConfiguracionRespuesta();
        respuesta.setModelo(config.getModelo());
        respuesta.setActivo(config.isActivo());
        respuesta.setClaveConfigurada(true);
        respuesta.setClaveInvalida(false);
        respuesta.setClaveEnmascarada(enmascararClave(clavePlana));
        return respuesta;
    }

    @Transactional
    public void eliminarConfiguracion(Long usuarioId) {
        repositorio.findByUsuarioId(usuarioId).ifPresent(repositorio::delete);
    }

    @Transactional(readOnly = true)
    public UsuarioConfiguracionIa requerirConfiguracionActiva(Long usuarioId) {
        UsuarioConfiguracionIa config = repositorio.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new IllegalStateException("No hay configuración de IA. Configurá tu clave API de Gemini."));
        if (!config.isActivo()) {
            throw new IllegalStateException("El chat con IA no está habilitado. Revisá tu configuración.");
        }
        return config;
    }

    @Transactional(readOnly = true)
    public CredencialesChatIa obtenerCredencialesActivas(Long usuarioId) {
        if (tieneClaveProyecto()) {
            return new CredencialesChatIa(claveApiProyecto, resolverModeloProyecto(), false);
        }
        var existente = repositorio.findByUsuarioId(usuarioId);
        if (existente.isPresent() && existente.get().isActivo()) {
            UsuarioConfiguracionIa config = existente.get();
            return new CredencialesChatIa(
                    servicioCifrado.descifrar(config.getClaveApiCifrada()),
                    config.getModelo(),
                    true);
        }
        throw new IllegalStateException("No hay configuración de IA. Configurá tu clave API de Gemini o definí IA_GEMINI_API_KEY en .env.local.");
    }

    @Transactional
    public void marcarClaveValida(Long usuarioId) {
        repositorio.findByUsuarioId(usuarioId).ifPresent(config -> {
            if (!config.isClaveValida()) {
                config.setClaveValida(true);
                repositorio.save(config);
            }
        });
    }

    @Transactional
    public void marcarClaveInvalida(Long usuarioId) {
        repositorio.findByUsuarioId(usuarioId).ifPresent(config -> {
            config.setClaveValida(false);
            repositorio.save(config);
        });
    }

    public String obtenerClaveDescifrada(UsuarioConfiguracionIa config) {
        return servicioCifrado.descifrar(config.getClaveApiCifrada());
    }

    private void validarModelo(String modelo) {
        if (!MODELOS_PERMITIDOS.contains(modelo)) {
            throw new IllegalArgumentException("Modelo no permitido: " + modelo);
        }
    }

    private boolean tieneClaveProyecto() {
        return iaProyectoHabilitada && claveApiProyecto != null && !claveApiProyecto.isBlank();
    }

    private String resolverModeloProyecto() {
        if ("gemini-flash-latest".equals(modeloProyecto) || "gemini-pro-latest".equals(modeloProyecto)) {
            return modeloProyecto;
        }
        if (modeloProyecto != null && modeloProyecto.startsWith("gemini-3") && MODELOS_PERMITIDOS.contains(modeloProyecto)) {
            return modeloProyecto;
        }
        return "gemini-flash-latest";
    }

    private ChatIaEstadoRespuesta aplicarEstadoClaveProyecto(ChatIaEstadoRespuesta estado) {
        if (tieneClaveProyecto()) {
            estado.setClaveConfigurada(true);
            estado.setHabilitado(true);
            estado.setClaveInvalida(false);
            estado.setModelo(resolverModeloProyecto());
        } else {
            estado.setClaveConfigurada(false);
            estado.setHabilitado(false);
            estado.setClaveInvalida(false);
            estado.setModelo("gemini-flash-latest");
        }
        return estado;
    }

    private ChatIaConfiguracionRespuesta aplicarConfiguracionClaveProyecto(ChatIaConfiguracionRespuesta respuesta) {
        if (tieneClaveProyecto()) {
            respuesta.setActivo(true);
            respuesta.setClaveConfigurada(true);
            respuesta.setClaveInvalida(false);
            respuesta.setModelo(resolverModeloProyecto());
            respuesta.setClaveEnmascarada(enmascararClave(claveApiProyecto));
        } else {
            respuesta.setActivo(false);
            respuesta.setClaveConfigurada(false);
            respuesta.setClaveInvalida(false);
        }
        return respuesta;
    }

    private String enmascararClave(String clave) {
        if (clave == null || clave.length() < 4) {
            return "****";
        }
        return "****" + clave.substring(clave.length() - 4);
    }
}
