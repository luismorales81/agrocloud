package com.agrocloud.cultivos.application;

import com.agrocloud.dto.ConfirmacionCambioEstado;
import com.agrocloud.dto.RespuestaCambioEstado;
import com.agrocloud.model.TransicionEstadoLote;
import com.agrocloud.cultivos.domain.EstadoLoteConfig;
import com.agrocloud.cultivos.domain.Plot;
import com.agrocloud.cultivos.domain.TransicionEstadoConfig;
import com.agrocloud.cultivos.util.MapeadorEstadoLoteConfig;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.Role;
import com.agrocloud.core.domain.Empresa;
import com.agrocloud.model.enums.EstadoLote;
import com.agrocloud.model.enums.Rol;
import com.agrocloud.cultivos.infrastructure.PlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de estados de lotes.
 * Maneja las transiciones de estado con validaciones y confirmaciones.
 *
 * @author AgroGestion Team
 * @version 1.0.0
 */
@Service("estadoLoteServiceCultivos")
@Transactional
public class EstadoLoteService {

    @Autowired
    private PlotRepository plotRepository;

    @Autowired
    private ConfiguracionEstadosService configuracionEstadosService;

    @Autowired
    private ProgresoEstadoLoteService progresoEstadoLoteService;

    /**
     * Proponer cambio de estado con validaciones y mensajes.
     * Si el lote usa configuración por cultivo, debe indicarse estadoDestinoConfigId.
     */
    public RespuestaCambioEstado proponerCambioEstado(Long loteId, EstadoLote nuevoEstado, String motivo, User usuario) {
        return proponerCambioEstado(loteId, nuevoEstado, null, motivo, usuario);
    }

    public RespuestaCambioEstado proponerCambioEstado(Long loteId, EstadoLote nuevoEstado,
                                                       Long estadoDestinoConfigId, String motivo, User usuario) {
        Plot lote = plotRepository.findById(loteId)
            .orElseThrow(() -> new RuntimeException("Lote no encontrado"));

        Long empresaId = obtenerEmpresaDelLote(lote);
        if (lote.getEstadoConfigurado() != null || estadoDestinoConfigId != null) {
            return proponerCambioEstadoConfigurado(lote, estadoDestinoConfigId, motivo, usuario, empresaId);
        }

        RespuestaCambioEstado respuesta = new RespuestaCambioEstado();
        respuesta.setLoteId(loteId);
        respuesta.setLoteNombre(lote.getNombre());
        respuesta.setEstadoActual(lote.getEstado());
        respuesta.setEstadoPropuesto(nuevoEstado);
        respuesta.setMotivo(motivo);

        // Validar permisos del usuario
        if (!tienePermisoParaCambiarEstado(usuario, lote, nuevoEstado)) {
            respuesta.setRequiereConfirmacion(false);
            respuesta.setMensaje("❌ **Sin permisos suficientes**\n\n" +
                "No tienes permisos para cambiar el estado del lote '" + lote.getNombre() + "' " +
                "de '" + lote.getEstado().getDescripcion() + "' a '" + nuevoEstado.getDescripcion() + "'.\n\n" +
                "**Permisos requeridos:** " + getPermisosRequeridos(nuevoEstado));
            return respuesta;
        }

        // Validar si el cambio es válido
        if (!TransicionEstadoLote.esTransicionValida(lote.getEstado(), nuevoEstado)) {
            respuesta.setRequiereConfirmacion(false);
            respuesta.setMensaje("❌ **Cambio de estado no válido**\n\n" +
                "No se puede cambiar de **" + lote.getEstado().getDescripcion() + "** " +
                "a **" + nuevoEstado.getDescripcion() + "**.\n\n" +
                "**Estados válidos desde " + lote.getEstado().getDescripcion() + ":**\n" +
                TransicionEstadoLote.getEstadosValidos(lote.getEstado()).stream()
                    .map(EstadoLote::getDescripcion)
                    .collect(Collectors.joining("\n• ", "• ", "")));
            return respuesta;
        }

        // Generar mensaje de confirmación
        String mensaje = generarMensajeConfirmacion(lote, nuevoEstado, motivo);
        List<String> consecuencias = generarConsecuencias(lote, nuevoEstado);

        respuesta.setRequiereConfirmacion(true);
        respuesta.setMensaje(mensaje);
        respuesta.setConsecuencias(consecuencias);
        respuesta.setPuedeCancelar(true);
        respuesta.setAccionRequerida("¿Desea confirmar este cambio de estado?");

        return respuesta;
    }

    private RespuestaCambioEstado proponerCambioEstadoConfigurado(Plot lote, Long estadoDestinoConfigId,
                                                                   String motivo, User usuario, Long empresaId) {
        RespuestaCambioEstado respuesta = new RespuestaCambioEstado();
        respuesta.setLoteId(lote.getId());
        respuesta.setLoteNombre(lote.getNombre());
        respuesta.setEstadoActual(lote.getEstado());

        if (estadoDestinoConfigId == null) {
            respuesta.setRequiereConfirmacion(false);
            respuesta.setMensaje("Este lote usa estados configurados. Indique el estado destino desde las transiciones permitidas.");
            return respuesta;
        }

        EstadoLoteConfig destino = configuracionEstadosService.obtenerEstadoPorId(estadoDestinoConfigId)
            .orElseThrow(() -> new RuntimeException("Estado destino no encontrado"));

        if (MapeadorEstadoLoteConfig.esEstadoDerivadoPorEvento(destino)) {
            respuesta.setRequiereConfirmacion(false);
            respuesta.setMensaje("El estado '" + destino.getNombre() + "' se actualiza automáticamente al registrar siembra o cosecha.");
            return respuesta;
        }

        EstadoLoteConfig origen = lote.getEstadoConfigurado();
        if (origen == null) {
            respuesta.setRequiereConfirmacion(false);
            respuesta.setMensaje("El lote no tiene un estado configurado asignado. Registre una labor para recalcular el estado.");
            return respuesta;
        }

        if (!configuracionEstadosService.validarTransicion(origen.getId(), destino.getId(), empresaId)) {
            respuesta.setRequiereConfirmacion(false);
            respuesta.setMensaje("No se puede cambiar de '" + origen.getNombre() + "' a '" + destino.getNombre()
                + "'. Configure la transición en Cultivos → Configuración.");
            return respuesta;
        }

        Optional<TransicionEstadoConfig> transicionOpt = configuracionEstadosService
            .buscarTransicion(origen.getId(), destino.getId(), empresaId);
        if (transicionOpt.isPresent() && Boolean.TRUE.equals(transicionOpt.get().getRequiereMotivo())
                && (motivo == null || motivo.isBlank())) {
            respuesta.setRequiereConfirmacion(false);
            respuesta.setMensaje("Esta transición requiere indicar un motivo.");
            return respuesta;
        }

        if (!tienePermisoParaCambiarEstado(usuario, lote, MapeadorEstadoLoteConfig.mapearAEnum(destino))) {
            respuesta.setRequiereConfirmacion(false);
            respuesta.setMensaje("Sin permisos para cambiar el estado del lote.");
            return respuesta;
        }

        respuesta.setEstadoPropuesto(MapeadorEstadoLoteConfig.mapearAEnum(destino));
        respuesta.setRequiereConfirmacion(true);
        respuesta.setMensaje("Cambio propuesto: " + origen.getNombre() + " → " + destino.getNombre()
            + (motivo != null && !motivo.isBlank() ? "\nMotivo: " + motivo : ""));
        respuesta.setPuedeCancelar(true);
        respuesta.setAccionRequerida("¿Confirmar cambio a " + destino.getNombre() + "?");
        return respuesta;
    }

    /**
     * Aplica un cambio de estado manual confirmado por el usuario.
     */
    public void aplicarCambioEstadoManual(ConfirmacionCambioEstado confirmacion, User usuario) {
        if (!confirmacion.isConfirmado()) {
            throw new RuntimeException("El cambio de estado no fue confirmado");
        }

        Plot lote = plotRepository.findById(confirmacion.getLoteId())
            .orElseThrow(() -> new RuntimeException("Lote no encontrado"));

        if (confirmacion.getEstadoDestinoConfigId() != null || lote.getEstadoConfigurado() != null) {
            aplicarCambioEstadoConfiguradoManual(confirmacion, usuario, lote);
            return;
        }

        if (confirmacion.getEstadoPropuesto() == EstadoLote.SEMBRADO || confirmacion.getEstadoPropuesto() == EstadoLote.COSECHADO) {
            throw new IllegalStateException("Los estados SEMBRADO y COSECHADO son derivados del dominio y no pueden setearse manualmente.");
        }

        // Validar permisos nuevamente (por seguridad)
        if (!tienePermisoParaCambiarEstado(usuario, lote, confirmacion.getEstadoPropuesto())) {
            throw new RuntimeException("No tienes permisos para realizar este cambio de estado");
        }

        // Validar nuevamente (por seguridad)
        if (!TransicionEstadoLote.esTransicionValida(lote.getEstado(), confirmacion.getEstadoPropuesto())) {
            throw new IllegalStateException("El cambio de estado ya no es válido");
        }

        // Realizar el cambio
        lote.cambiarEstado(confirmacion.getEstadoPropuesto(), confirmacion.getMotivo());
        plotRepository.save(lote);

        // Log del cambio
        logCambioEstado(lote, confirmacion.getEstadoPropuesto(), confirmacion.getMotivo(), usuario);
    }

    private void aplicarCambioEstadoConfiguradoManual(ConfirmacionCambioEstado confirmacion, User usuario, Plot lote) {
        Long estadoDestinoId = confirmacion.getEstadoDestinoConfigId();
        if (estadoDestinoId == null) {
            throw new IllegalStateException("Debe indicar el estado destino configurado.");
        }

        EstadoLoteConfig destino = configuracionEstadosService.obtenerEstadoPorId(estadoDestinoId)
            .orElseThrow(() -> new RuntimeException("Estado destino no encontrado"));

        if (MapeadorEstadoLoteConfig.esEstadoDerivadoPorEvento(destino)) {
            throw new IllegalStateException("El estado destino es derivado por evento de dominio.");
        }

        EstadoLoteConfig origen = lote.getEstadoConfigurado();
        if (origen == null) {
            throw new IllegalStateException("El lote no tiene estado configurado asignado.");
        }

        Long empresaId = obtenerEmpresaDelLote(lote);
        if (!configuracionEstadosService.validarTransicion(origen.getId(), destino.getId(), empresaId)) {
            throw new IllegalStateException("La transición ya no es válida.");
        }

        if (!tienePermisoParaCambiarEstado(usuario, lote, MapeadorEstadoLoteConfig.mapearAEnum(destino))) {
            throw new RuntimeException("No tienes permisos para realizar este cambio de estado");
        }

        lote.cambiarEstadoConfigurado(destino, confirmacion.getMotivo());
        plotRepository.save(lote);
        logCambioEstado(lote, lote.getEstado(), confirmacion.getMotivo(), usuario);
    }

    @Transactional(readOnly = true)
    public com.agrocloud.dto.ProgresoEstadoLoteDTO obtenerProgresoEstado(Long loteId, Long empresaId) {
        return progresoEstadoLoteService.calcularProgreso(loteId, empresaId);
    }

    private boolean tienePermisoParaCambiarEstado(User usuario, Plot lote, EstadoLote nuevoEstado) {
        if (!esPropietarioOLider(usuario, lote)) {
            return false;
        }
        Rol rolUsuario = obtenerRolUsuario(usuario);
        switch (nuevoEstado) {
            case SEMBRADO:
            case EN_CRECIMIENTO:
            case EN_FLORACION:
            case EN_FRUTIFICACION:
                return rolUsuario == Rol.PRODUCTOR || rolUsuario == Rol.ADMINISTRADOR || rolUsuario == Rol.TECNICO;
            case LISTO_PARA_COSECHA:
            case EN_COSECHA:
            case COSECHADO:
                return rolUsuario == Rol.PRODUCTOR || rolUsuario == Rol.ADMINISTRADOR || rolUsuario == Rol.TECNICO;
            case EN_DESCANSO:
            case EN_PREPARACION:
            case DISPONIBLE:
            case PREPARADO:
                return rolUsuario == Rol.PRODUCTOR || rolUsuario == Rol.ADMINISTRADOR || rolUsuario == Rol.TECNICO;
            case ENFERMO:
            case ABANDONADO:
                return rolUsuario == Rol.PRODUCTOR || rolUsuario == Rol.ADMINISTRADOR || rolUsuario == Rol.TECNICO || rolUsuario == Rol.ASESOR;
            default:
                return rolUsuario == Rol.ADMINISTRADOR;
        }
    }

    private boolean esPropietarioOLider(User usuario, Plot lote) {
        if (lote.getUser().getId().equals(usuario.getId())) {
            return true;
        }
        if (lote.getUser().getParentUser() != null &&
            lote.getUser().getParentUser().getId().equals(usuario.getId())) {
            return true;
        }
        Rol rolUsuario = obtenerRolUsuario(usuario);
        if (rolUsuario == Rol.ADMINISTRADOR) {
            return perteneceAMismaEmpresa(usuario, lote);
        }
        return false;
    }

    private boolean perteneceAMismaEmpresa(User usuario, Plot lote) {
        Empresa empresaUsuario = usuario.getEmpresa();
        if (empresaUsuario != null) {
            Long empresaUsuarioId = empresaUsuario.getId();
            Long empresaLote = obtenerEmpresaDelLote(lote);
            return empresaUsuarioId.equals(empresaLote);
        }
        return true;
    }

    private Long obtenerEmpresaDelLote(Plot lote) {
        if (lote.getUser() != null) {
            Empresa empresa = lote.getUser().getEmpresa();
            if (empresa != null) {
                return empresa.getId();
            }
        }
        return 1L;
    }

    private Rol obtenerRolUsuario(User usuario) {
        if (usuario == null) {
            return Rol.PRODUCTOR;
        }
        try {
            Set<Role> roles = usuario.getRoles();
            if (roles != null && !roles.isEmpty()) {
                Role role = roles.iterator().next();
                if (role != null && role.getNombre() != null) {
                    try {
                        return Rol.valueOf(role.getNombre());
                    } catch (IllegalArgumentException e) {
                        System.err.println("Rol no válido en Role: " + role.getNombre());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error al obtener roles del usuario: " + e.getMessage());
        }
        if (!usuario.getRoles().isEmpty()) {
            Role role = usuario.getRoles().iterator().next();
            if (role.getNombre() != null) {
                try {
                    return Rol.valueOf(role.getNombre());
                } catch (IllegalArgumentException e) {
                    System.err.println("Rol no válido en Roles legacy: " + role.getNombre());
                }
            }
        }
        return Rol.PRODUCTOR;
    }

    private String getPermisosRequeridos(EstadoLote estado) {
        switch (estado) {
            case SEMBRADO:
            case EN_CRECIMIENTO:
            case EN_FLORACION:
            case EN_FRUTIFICACION:
                return "PRODUCTOR, TÉCNICO o ADMINISTRADOR";
            case LISTO_PARA_COSECHA:
            case EN_COSECHA:
            case COSECHADO:
                return "PRODUCTOR, TÉCNICO o ADMINISTRADOR";
            case EN_DESCANSO:
            case EN_PREPARACION:
            case DISPONIBLE:
            case PREPARADO:
                return "PRODUCTOR, TÉCNICO o ADMINISTRADOR";
            case ENFERMO:
            case ABANDONADO:
                return "PRODUCTOR, TÉCNICO, ASESOR o ADMINISTRADOR";
            default:
                return "ADMINISTRADOR";
        }
    }

    private String generarMensajeConfirmacion(Plot lote, EstadoLote nuevoEstado, String motivo) {
        StringBuilder mensaje = new StringBuilder();
        mensaje.append("🔄 **Cambio de Estado Propuesto**\n\n");
        mensaje.append("**Lote:** ").append(lote.getNombre()).append("\n");
        mensaje.append("**Estado Actual:** ").append(lote.getEstado().getDescripcion()).append("\n");
        mensaje.append("**Estado Propuesto:** ").append(nuevoEstado.getDescripcion()).append("\n");
        mensaje.append("**Motivo:** ").append(motivo).append("\n\n");
        switch (nuevoEstado) {
            case SEMBRADO:
                mensaje.append("🌱 **Al cambiar a SEMBRADO:**\n• Se registrará la fecha de siembra\n• Se iniciará el seguimiento del cultivo\n• Se podrán programar labores de mantenimiento\n");
                break;
            case EN_CRECIMIENTO:
                mensaje.append("🌿 **Al cambiar a EN CRECIMIENTO:**\n• Se monitoreará el desarrollo del cultivo\n• Se podrán programar labores de fertilización\n");
                break;
            case EN_FLORACION:
                mensaje.append("🌸 **Al cambiar a EN FLORACIÓN:**\n• Se monitoreará la floración\n• Se podrán programar labores de polinización\n");
                break;
            case EN_FRUTIFICACION:
                mensaje.append("🍅 **Al cambiar a EN FRUTIFICACIÓN:**\n• Se monitoreará el desarrollo de frutos\n• Se podrán programar labores de protección\n");
                break;
            case LISTO_PARA_COSECHA:
                mensaje.append("🌾 **Al cambiar a LISTO PARA COSECHA:**\n• Se podrá programar la labor de cosecha\n• Se calculará el rendimiento esperado\n");
                break;
            case EN_COSECHA:
                mensaje.append("🚜 **Al cambiar a EN COSECHA:**\n• Se registrarán las labores de cosecha\n• Se calculará el rendimiento real\n");
                break;
            case COSECHADO:
                mensaje.append("✅ **Al cambiar a COSECHADO:**\n• Se registrará la fecha de cosecha real\n• Se calculará el rendimiento obtenido\n• El lote pasará a descanso\n");
                break;
            case PREPARADO:
                mensaje.append("🔧 **Al cambiar a PREPARADO:**\n• El lote está listo para la siembra\n• Se pueden programar labores de preparación\n");
                break;
            case EN_PREPARACION:
                mensaje.append("⚙️ **Al cambiar a EN PREPARACIÓN:**\n• Se están realizando labores de preparación\n• Se preparará el suelo para la siembra\n");
                break;
            case DISPONIBLE:
                mensaje.append("✅ **Al cambiar a DISPONIBLE:**\n• El lote está disponible para uso\n• Se puede asignar a nuevos cultivos\n");
                break;
            case ABANDONADO:
                mensaje.append("❌ **Al cambiar a ABANDONADO:**\n• El lote no se utilizará temporalmente\n• Se requiere revisión antes de reutilizar\n");
                break;
            case EN_DESCANSO:
                mensaje.append("😴 **Al cambiar a EN DESCANSO:**\n• El lote no podrá tener nuevas labores\n• Se recomendará tiempo de descanso\n");
                break;
            case ENFERMO:
                mensaje.append("🚨 **Al cambiar a ENFERMO:**\n• Se registrará el problema del cultivo\n• Se recomendará tratamiento\n");
                break;
        }
        return mensaje.toString();
    }

    private List<String> generarConsecuencias(Plot lote, EstadoLote nuevoEstado) {
        List<String> consecuencias = new ArrayList<>();
        switch (nuevoEstado) {
            case SEMBRADO:
                consecuencias.add("Se iniciará el ciclo de cultivo");
                consecuencias.add("Se podrán programar labores de mantenimiento");
                consecuencias.add("Se calculará la fecha estimada de cosecha");
                break;
            case EN_CRECIMIENTO:
            case EN_FLORACION:
            case EN_FRUTIFICACION:
                consecuencias.add("Se monitoreará el desarrollo del cultivo");
                break;
            case LISTO_PARA_COSECHA:
                consecuencias.add("Se podrá programar la labor de cosecha");
                consecuencias.add("Se calculará el rendimiento esperado");
                break;
            case EN_COSECHA:
                consecuencias.add("Se registrarán las labores de cosecha");
                consecuencias.add("Se calculará el rendimiento real");
                break;
            case COSECHADO:
                consecuencias.add("Se finalizará el ciclo de cultivo actual");
                consecuencias.add("El lote pasará a descanso");
                consecuencias.add("Se podrá planificar el próximo ciclo");
                break;
            case PREPARADO:
            case EN_PREPARACION:
                consecuencias.add("El lote está listo o en preparación para la siembra");
                break;
            case DISPONIBLE:
                consecuencias.add("El lote está disponible para uso");
                consecuencias.add("Se puede asignar a nuevos cultivos");
                break;
            case ABANDONADO:
                consecuencias.add("El lote no se utilizará temporalmente");
                break;
            case EN_DESCANSO:
                consecuencias.add("No se podrán realizar nuevas labores");
                consecuencias.add("Se recomendará tiempo de descanso del suelo");
                break;
            case ENFERMO:
                consecuencias.add("Se registrará el problema del cultivo");
                consecuencias.add("Se recomendará tratamiento específico");
                break;
        }
        return consecuencias;
    }

    private void logCambioEstado(Plot lote, EstadoLote nuevoEstado, String motivo, User usuario) {
        System.out.println("Cambio de estado registrado: " +
            "Lote: " + lote.getNombre() +
            ", Estado: " + lote.getEstado() + " -> " + nuevoEstado +
            ", Motivo: " + motivo +
            ", Usuario: " + usuario.getEmail());
    }

    @Transactional(readOnly = true)
    public List<Plot> getLotesPorEstado(EstadoLote estado) {
        return plotRepository.findByEstado(estado);
    }

    @Transactional(readOnly = true)
    public List<Plot> getLotesListosParaSiembra() {
        return plotRepository.findByEstadoIn(
            Arrays.asList(EstadoLote.DISPONIBLE, EstadoLote.PREPARADO)
        );
    }

    @Transactional(readOnly = true)
    public List<Plot> getLotesListosParaCosecha() {
        return plotRepository.findByEstado(EstadoLote.LISTO_PARA_COSECHA);
    }
}
