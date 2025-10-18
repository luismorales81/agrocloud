package com.agrocloud.controller;

import com.agrocloud.model.entity.User;
import com.agrocloud.model.entity.UsuarioEmpresa;
import com.agrocloud.model.enums.RolEmpresa;
import com.agrocloud.service.UserService;
import com.agrocloud.service.EmpresaUsuarioService;
import com.agrocloud.service.PermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controlador de diagnóstico para verificar permisos y roles de usuario
 */
@RestController
@RequestMapping("/api/diagnostico")
@CrossOrigin(origins = "*")
public class DiagnosticoController {

    private static final Logger logger = LoggerFactory.getLogger(DiagnosticoController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private EmpresaUsuarioService empresaUsuarioService;

    @Autowired
    private PermissionService permissionService;

    /**
     * Endpoint para diagnosticar el estado del usuario actual
     */
    @GetMapping("/mi-info")
    public ResponseEntity<Map<String, Object>> obtenerMiInfo(Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userService.findByEmailWithRelations(email);

            Map<String, Object> info = new HashMap<>();
            info.put("email", user.getEmail());
            info.put("nombre", user.getFirstName() + " " + user.getLastName());
            info.put("activo", user.getActivo());
            info.put("isAdmin", user.isAdmin());

            // Obtener empresas y roles
            List<UsuarioEmpresa> empresas = empresaUsuarioService.obtenerEmpresasActivasDeUsuario(user.getId());
            
            List<Map<String, Object>> empresasInfo = empresas.stream()
                .map(ue -> {
                    Map<String, Object> empresaInfo = new HashMap<>();
                    empresaInfo.put("empresaId", ue.getEmpresa().getId());
                    empresaInfo.put("empresaNombre", ue.getEmpresa().getNombre());
                    empresaInfo.put("rol", ue.getRol().name());
                    empresaInfo.put("rolActualizado", ue.getRol().getRolActualizado().name());
                    empresaInfo.put("estado", ue.getEstado().name());
                    return empresaInfo;
                })
                .collect(Collectors.toList());

            info.put("empresas", empresasInfo);

            // Verificar roles específicos
            Map<String, Boolean> rolesCheck = new HashMap<>();
            rolesCheck.put("ADMINISTRADOR", user.tieneRolEnEmpresa(RolEmpresa.ADMINISTRADOR));
            rolesCheck.put("JEFE_CAMPO", user.tieneRolEnEmpresa(RolEmpresa.JEFE_CAMPO));
            rolesCheck.put("JEFE_FINANCIERO", user.tieneRolEnEmpresa(RolEmpresa.JEFE_FINANCIERO));
            rolesCheck.put("OPERARIO", user.tieneRolEnEmpresa(RolEmpresa.OPERARIO));
            rolesCheck.put("CONSULTOR_EXTERNO", user.tieneRolEnEmpresa(RolEmpresa.CONSULTOR_EXTERNO));
            info.put("tieneRoles", rolesCheck);

            // Obtener permisos si tiene alguna empresa
            if (!empresas.isEmpty()) {
                String rolPrincipal = empresas.get(0).getRol().getRolActualizado().name();
                Set<String> permisos = permissionService.getPermissionsByRole(rolPrincipal);
                info.put("rolPrincipal", rolPrincipal);
                info.put("permisos", permisos);
            }

            logger.info("🔍 [Diagnóstico] Info de usuario: {}", info);
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            logger.error("Error en diagnóstico: ", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("stackTrace", e.getStackTrace());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Endpoint para verificar acceso a insumos
     */
    @GetMapping("/verificar-acceso-insumos")
    public ResponseEntity<Map<String, Object>> verificarAccesoInsumos(Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userService.findByEmailWithRelations(email);

            Map<String, Object> resultado = new HashMap<>();
            resultado.put("email", user.getEmail());
            resultado.put("isAdmin", user.isAdmin());
            resultado.put("tieneRolJefeCampo", user.tieneRolEnEmpresa(RolEmpresa.JEFE_CAMPO));
            resultado.put("tieneRolOperario", user.tieneRolEnEmpresa(RolEmpresa.OPERARIO));
            resultado.put("tieneRolConsultorExterno", user.tieneRolEnEmpresa(RolEmpresa.CONSULTOR_EXTERNO));
            
            boolean deberiaVerInsumos = user.isAdmin() || 
                                       user.tieneRolEnEmpresa(RolEmpresa.JEFE_CAMPO) || 
                                       user.tieneRolEnEmpresa(RolEmpresa.OPERARIO) ||
                                       user.tieneRolEnEmpresa(RolEmpresa.CONSULTOR_EXTERNO);
            
            resultado.put("deberiaVerInsumos", deberiaVerInsumos);
            
            logger.info("🔍 [Diagnóstico] Acceso a insumos: {}", resultado);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            logger.error("Error verificando acceso a insumos: ", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}

