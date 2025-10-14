package com.agrocloud.service;

import com.agrocloud.dto.ConfirmacionCambioEstado;
import com.agrocloud.dto.RespuestaCambioEstado;
import com.agrocloud.model.TransicionEstadoLote;
import com.agrocloud.model.entity.Plot;
import com.agrocloud.model.entity.User;
import com.agrocloud.model.entity.UserCompanyRole;
import com.agrocloud.model.entity.Role;
import com.agrocloud.model.enums.EstadoLote;
import com.agrocloud.model.enums.Rol;
import com.agrocloud.repository.PlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de estados de lotes.
 * Maneja las transiciones de estado con validaciones y confirmaciones.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@Service
@Transactional
public class EstadoLoteService {
    
    @Autowired
    private PlotRepository plotRepository;
    
    /**
     * Proponer cambio de estado con validaciones y mensajes
     */
    public RespuestaCambioEstado proponerCambioEstado(Long loteId, EstadoLote nuevoEstado, String motivo, User usuario) {
        Plot lote = plotRepository.findById(loteId)
            .orElseThrow(() -> new RuntimeException("Lote no encontrado"));
            
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
    
    /**
     * Confirmar cambio de estado
     */
    public void confirmarCambioEstado(ConfirmacionCambioEstado confirmacion, User usuario) {
        if (!confirmacion.isConfirmado()) {
            throw new RuntimeException("El cambio de estado no fue confirmado");
        }
        
        Plot lote = plotRepository.findById(confirmacion.getLoteId())
            .orElseThrow(() -> new RuntimeException("Lote no encontrado"));
            
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
    
    /**
     * Validar permisos del usuario para cambiar estado
     */
    private boolean tienePermisoParaCambiarEstado(User usuario, Plot lote, EstadoLote nuevoEstado) {
        // Verificar si el usuario es propietario del lote o tiene permisos administrativos
        if (!esPropietarioOLider(usuario, lote)) {
            return false;
        }
        
        // Verificar permisos específicos por rol
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
    
    /**
     * Verificar si el usuario es propietario o líder del lote
     */
    private boolean esPropietarioOLider(User usuario, Plot lote) {
        // Es propietario directo
        if (lote.getUser().getId().equals(usuario.getId())) {
            return true;
        }
        
        // Es líder (parent_user_id)
        if (lote.getUser().getParentUser() != null && 
            lote.getUser().getParentUser().getId().equals(usuario.getId())) {
            return true;
        }
        
        // Es administrador de la empresa
        Rol rolUsuario = obtenerRolUsuario(usuario);
        if (rolUsuario == Rol.ADMINISTRADOR) {
            // Verificar que pertenece a la misma empresa que el lote
            return perteneceAMismaEmpresa(usuario, lote);
        }
        
        return false;
    }
    
    /**
     * Verificar si el usuario pertenece a la misma empresa que el lote
     */
    private boolean perteneceAMismaEmpresa(User usuario, Plot lote) {
        // Obtener empresa del usuario desde UserCompanyRoles
        if (!usuario.getUserCompanyRoles().isEmpty()) {
            Long empresaUsuario = usuario.getUserCompanyRoles().iterator().next().getEmpresa().getId();
            
            // Obtener empresa del lote (asumiendo que el lote tiene una empresa asociada)
            // Si el lote no tiene empresa directa, usar la empresa del usuario propietario
            Long empresaLote = obtenerEmpresaDelLote(lote);
            
            return empresaUsuario.equals(empresaLote);
        }
        
        // Si no tiene UserCompanyRoles, asumir que puede acceder (compatibilidad legacy)
        return true;
    }
    
    /**
     * Obtener la empresa asociada al lote
     */
    private Long obtenerEmpresaDelLote(Plot lote) {
        // Como la tabla lotes no tiene empresa_id, usar la empresa del usuario propietario
        if (lote.getUser() != null && !lote.getUser().getUserCompanyRoles().isEmpty()) {
            return lote.getUser().getUserCompanyRoles().iterator().next().getEmpresa().getId();
        }
        
        // Default: empresa ID 1 (para compatibilidad)
        return 1L;
    }
    
    /**
     * Obtener rol del usuario usando el sistema completo de roles multitenant
     */
    private Rol obtenerRolUsuario(User usuario) {
        if (usuario == null) {
            return Rol.PRODUCTOR; // Default
        }
        
        // 1. Verificar en UserCompanyRoles (sistema multitenant) primero
        if (!usuario.getUserCompanyRoles().isEmpty()) {
            UserCompanyRole ucr = usuario.getUserCompanyRoles().iterator().next();
            if (ucr.getRol() != null && ucr.getRol().getNombre() != null) {
                try {
                    return Rol.valueOf(ucr.getRol().getNombre());
                } catch (IllegalArgumentException e) {
                    System.err.println("Rol no válido en UserCompanyRole: " + ucr.getRol().getNombre());
                }
            }
        }
        
        // 2. Fallback: verificar en el sistema legacy (Roles directos)
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
        
        // 3. Default: PRODUCTOR
        return Rol.PRODUCTOR;
    }
    
    /**
     * Obtener permisos requeridos para un estado
     */
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
        
        // Agregar información específica según el estado
        switch (nuevoEstado) {
            case SEMBRADO:
                mensaje.append("🌱 **Al cambiar a SEMBRADO:**\n");
                mensaje.append("• Se registrará la fecha de siembra\n");
                mensaje.append("• Se iniciará el seguimiento del cultivo\n");
                mensaje.append("• Se podrán programar labores de mantenimiento\n");
                break;
                
            case EN_CRECIMIENTO:
                mensaje.append("🌿 **Al cambiar a EN CRECIMIENTO:**\n");
                mensaje.append("• Se monitoreará el desarrollo del cultivo\n");
                mensaje.append("• Se podrán programar labores de fertilización\n");
                break;
                
            case EN_FLORACION:
                mensaje.append("🌸 **Al cambiar a EN FLORACIÓN:**\n");
                mensaje.append("• Se monitoreará la floración\n");
                mensaje.append("• Se podrán programar labores de polinización\n");
                break;
                
            case EN_FRUTIFICACION:
                mensaje.append("🍅 **Al cambiar a EN FRUTIFICACIÓN:**\n");
                mensaje.append("• Se monitoreará el desarrollo de frutos\n");
                mensaje.append("• Se podrán programar labores de protección\n");
                break;
                
            case LISTO_PARA_COSECHA:
                mensaje.append("🌾 **Al cambiar a LISTO PARA COSECHA:**\n");
                mensaje.append("• Se podrá programar la labor de cosecha\n");
                mensaje.append("• Se calculará el rendimiento esperado\n");
                break;
                
            case EN_COSECHA:
                mensaje.append("🚜 **Al cambiar a EN COSECHA:**\n");
                mensaje.append("• Se registrarán las labores de cosecha\n");
                mensaje.append("• Se calculará el rendimiento real\n");
                break;
                
            case COSECHADO:
                mensaje.append("✅ **Al cambiar a COSECHADO:**\n");
                mensaje.append("• Se registrará la fecha de cosecha real\n");
                mensaje.append("• Se calculará el rendimiento obtenido\n");
                mensaje.append("• El lote pasará a descanso\n");
                break;
                
            case PREPARADO:
                mensaje.append("🔧 **Al cambiar a PREPARADO:**\n");
                mensaje.append("• El lote está listo para la siembra\n");
                mensaje.append("• Se pueden programar labores de preparación\n");
                break;
                
            case EN_PREPARACION:
                mensaje.append("⚙️ **Al cambiar a EN PREPARACIÓN:**\n");
                mensaje.append("• Se están realizando labores de preparación\n");
                mensaje.append("• Se preparará el suelo para la siembra\n");
                break;
                
            case DISPONIBLE:
                mensaje.append("✅ **Al cambiar a DISPONIBLE:**\n");
                mensaje.append("• El lote está disponible para uso\n");
                mensaje.append("• Se puede asignar a nuevos cultivos\n");
                break;
                
            case ABANDONADO:
                mensaje.append("❌ **Al cambiar a ABANDONADO:**\n");
                mensaje.append("• El lote no se utilizará temporalmente\n");
                mensaje.append("• Se requiere revisión antes de reutilizar\n");
                break;
                
            case EN_DESCANSO:
                mensaje.append("😴 **Al cambiar a EN DESCANSO:**\n");
                mensaje.append("• El lote no podrá tener nuevas labores\n");
                mensaje.append("• Se recomendará tiempo de descanso\n");
                break;
                
            case ENFERMO:
                mensaje.append("🚨 **Al cambiar a ENFERMO:**\n");
                mensaje.append("• Se registrará el problema del cultivo\n");
                mensaje.append("• Se recomendará tratamiento\n");
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
                consecuencias.add("Se monitoreará el desarrollo del cultivo");
                consecuencias.add("Se podrán programar labores de fertilización");
                break;
                
            case EN_FLORACION:
                consecuencias.add("Se monitoreará la floración");
                consecuencias.add("Se podrán programar labores de polinización");
                break;
                
            case EN_FRUTIFICACION:
                consecuencias.add("Se monitoreará el desarrollo de frutos");
                consecuencias.add("Se podrán programar labores de protección");
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
                consecuencias.add("El lote está listo para la siembra");
                consecuencias.add("Se pueden programar labores de preparación");
                break;
                
            case EN_PREPARACION:
                consecuencias.add("Se están realizando labores de preparación");
                consecuencias.add("Se preparará el suelo para la siembra");
                break;
                
            case DISPONIBLE:
                consecuencias.add("El lote está disponible para uso");
                consecuencias.add("Se puede asignar a nuevos cultivos");
                break;
                
            case ABANDONADO:
                consecuencias.add("El lote no se utilizará temporalmente");
                consecuencias.add("Se requiere revisión antes de reutilizar");
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
        // TODO: Implementar logging de cambios de estado
        System.out.println("Cambio de estado registrado: " + 
            "Lote: " + lote.getNombre() + 
            ", Estado: " + lote.getEstado() + " -> " + nuevoEstado + 
            ", Motivo: " + motivo + 
            ", Usuario: " + usuario.getEmail());
    }
    
    /**
     * Obtener lotes por estado
     */
    @Transactional(readOnly = true)
    public List<Plot> getLotesPorEstado(EstadoLote estado) {
        return plotRepository.findByEstado(estado);
    }
    
    /**
     * Obtener lotes listos para siembra
     */
    @Transactional(readOnly = true)
    public List<Plot> getLotesListosParaSiembra() {
        return plotRepository.findByEstadoIn(
            Arrays.asList(EstadoLote.DISPONIBLE, EstadoLote.PREPARADO)
        );
    }
    
    /**
     * Obtener lotes listos para cosecha
     */
    @Transactional(readOnly = true)
    public List<Plot> getLotesListosParaCosecha() {
        return plotRepository.findByEstado(EstadoLote.LISTO_PARA_COSECHA);
    }
}
