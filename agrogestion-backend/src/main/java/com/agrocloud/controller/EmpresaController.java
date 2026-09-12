package com.agrocloud.controller;

import com.agrocloud.dto.EmpresaDTO;
import com.agrocloud.dto.UsuarioEmpresaDTO;
import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.UsuarioEmpresa;
import com.agrocloud.model.enums.RolEmpresa;
import com.agrocloud.core.application.EmpresaService;
import com.agrocloud.core.application.EmpresaUsuarioService;
import com.agrocloud.core.application.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controlador para la gestión de empresas desde el lado del usuario.
 * Este controlador permite a los usuarios gestionar sus empresas y usuarios.
 * 
 * @author AgroGestion Team
 * @version 2.0.0
 */
@RestController
@RequestMapping("/api/v1/empresas")
public class EmpresaController {

    private static final Logger logger = LoggerFactory.getLogger(EmpresaController.class);

    @Autowired
    @Qualifier("empresaServiceCore")
    private EmpresaService empresaService;

    @Autowired
    @Qualifier("userServiceCore")
    private UserService userService;

    @Autowired
    @Qualifier("empresaUsuarioServiceCore")
    private EmpresaUsuarioService empresaUsuarioService;

    /**
     * Obtiene las empresas del usuario autenticado con su rol en cada empresa
     */
    @GetMapping("/mis-empresas")
    public ResponseEntity<List<UsuarioEmpresaDTO>> obtenerMisEmpresas(Authentication authentication) {
        try {
            String email = authentication.getName();
            logger.info("Obteniendo empresas para usuario: {}", email);
            
            User usuario;
            try {
                usuario = userService.findByEmailWithAllRelations(email);
            } catch (RuntimeException e) {
                logger.error("Usuario no encontrado: {}", email);
                return ResponseEntity.badRequest().build();
            }

            logger.info("Usuario encontrado: {} (ID: {})", usuario.getEmail(), usuario.getId());
            
            // Siempre devolver solo las empresas asignadas al usuario.
            // Antes, SUPERADMIN recibía todas las empresas del sistema (cientos),
            // lo que hacía timeout en el frontend y dejaba el selector sin módulos.
            List<UsuarioEmpresa> relaciones = empresaUsuarioService.obtenerEmpresasActivasDeUsuario(usuario.getId());
            logger.info("Relaciones encontradas: {}", relaciones.size());

            List<UsuarioEmpresaDTO> empresasDTO = relaciones.stream()
                    .map(relacion -> {
                        UsuarioEmpresaDTO dto = new UsuarioEmpresaDTO();
                        dto.setId(relacion.getId());
                        dto.setUsuarioId(usuario.getId());
                        dto.setUsuarioEmail(usuario.getEmail());
                        dto.setUsuarioNombre(usuario.getFirstName() + " " + usuario.getLastName());
                        dto.setEmpresaId(relacion.getEmpresa().getId());
                        dto.setEmpresaNombre(relacion.getEmpresa().getNombre());
                        dto.setEmpresaCuit(relacion.getEmpresa().getCuit());
                        dto.setEmpresaEmail(relacion.getEmpresa().getEmailContacto());
                        RolEmpresa rolOriginal = relacion.getRol();
                        RolEmpresa rolActualizado = rolOriginal != null ? rolOriginal.getRolActualizado() : rolOriginal;
                        dto.setRol(rolActualizado);
                        dto.setEstado(relacion.getEstado());
                        return dto;
                    })
                    .collect(Collectors.toList());
            
            logger.info("Empresas obtenidas: {}", empresasDTO.size());
            return ResponseEntity.ok(empresasDTO);
        } catch (Exception e) {
            logger.error("Error obteniendo empresas del usuario: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Convierte una entidad Empresa a EmpresaDTO
     */
    @SuppressWarnings("unused")
    private EmpresaDTO convertirAEmpresaDTO(Empresa empresa) {
        return new EmpresaDTO(
            empresa.getId(),
            empresa.getNombre(),
            empresa.getCuit(),
            empresa.getEmailContacto(),
            empresa.getTelefonoContacto(),
            empresa.getDireccion(),
            empresa.getEstado(),
            empresa.getActivo(),
            empresa.getFechaInicioTrial(),
            empresa.getFechaFinTrial(),
            empresa.getFechaCreacion(),
            empresa.getFechaActualizacion()
        );
    }
    
    /**
     * Convierte una entidad UsuarioEmpresa a UsuarioEmpresaDTO
     */
    @SuppressWarnings("unused")
    private UsuarioEmpresaDTO convertirAUsuarioEmpresaDTO(UsuarioEmpresa usuarioEmpresa) {
        UsuarioEmpresaDTO dto = new UsuarioEmpresaDTO();
        dto.setId(usuarioEmpresa.getId());
        dto.setUsuarioId(usuarioEmpresa.getUsuario().getId());
        dto.setUsuarioEmail(usuarioEmpresa.getUsuario().getEmail());
        dto.setUsuarioNombre(usuarioEmpresa.getUsuario().getFirstName() + " " + usuarioEmpresa.getUsuario().getLastName());
        dto.setEmpresaId(usuarioEmpresa.getEmpresa().getId());
        dto.setEmpresaNombre(usuarioEmpresa.getEmpresa().getNombre());
        dto.setEmpresaCuit(usuarioEmpresa.getEmpresa().getCuit());
        dto.setEmpresaEmail(usuarioEmpresa.getEmpresa().getEmailContacto());
        dto.setRol(usuarioEmpresa.getRol());
        dto.setEstado(usuarioEmpresa.getEstado());
        dto.setFechaInicio(usuarioEmpresa.getFechaInicio());
        dto.setFechaFin(usuarioEmpresa.getFechaFin());
        if (usuarioEmpresa.getCreadoPor() != null) {
            dto.setCreadoPorId(usuarioEmpresa.getCreadoPor().getId());
            dto.setCreadoPorEmail(usuarioEmpresa.getCreadoPor().getEmail());
        }
        return dto;
    }

    /**
     * Obtiene una empresa específica del usuario como DTO (con enmascaramiento)
     */
    @GetMapping("/{id}/dto")
    public ResponseEntity<EmpresaDTO> obtenerEmpresaDTO(@PathVariable Long id, Authentication authentication) {
        try {
            String email = authentication.getName();
            User usuario = userService.findByEmailWithAllRelations(email);
            if (usuario == null) {
                return ResponseEntity.badRequest().build();
            }

            // Verificar que el usuario pertenece a esta empresa
            if (!usuario.perteneceAEmpresa(id)) {
                return ResponseEntity.status(403).build();
            }

            Optional<Empresa> empresa = empresaService.obtenerEmpresaPorId(id);
            if (empresa.isPresent()) {
                EmpresaDTO dto = convertirAEmpresaDTO(empresa.get());
                return ResponseEntity.ok(dto);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error obteniendo empresa DTO: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtiene una empresa específica del usuario
     */
    @GetMapping("/{id}")
    public ResponseEntity<Empresa> obtenerEmpresa(@PathVariable Long id, Authentication authentication) {
        try {
            String email = authentication.getName();
            User usuario = userService.findByEmailWithAllRelations(email)
;
            if (usuario == null) {
                return ResponseEntity.badRequest().build();
            }

            // Verificar que el usuario pertenece a esta empresa
            if (!usuario.perteneceAEmpresa(id)) {
                return ResponseEntity.status(403).build();
            }

            Optional<Empresa> empresa = empresaService.obtenerEmpresaPorId(id);
            return empresa.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Actualiza una empresa (solo administradores)
     */
    @PutMapping("/{id}")
    public ResponseEntity<Empresa> actualizarEmpresa(@PathVariable Long id, 
                                                    @RequestBody ActualizarEmpresaRequest request,
                                                    Authentication authentication) {
        try {
            String email = authentication.getName();
            User usuario = userService.findByEmailWithAllRelations(email)
;
            if (usuario == null) {
                return ResponseEntity.badRequest().build();
            }

            // Verificar que el usuario es administrador de esta empresa
            if (!usuario.esAdministradorEmpresa(id)) {
                return ResponseEntity.status(403).build();
            }

            Empresa empresaActualizada = new Empresa();
            empresaActualizada.setNombre(request.getNombre());
            empresaActualizada.setCuit(request.getCuit());
            empresaActualizada.setEmailContacto(request.getEmailContacto());
            empresaActualizada.setTelefonoContacto(request.getTelefonoContacto());
            empresaActualizada.setDireccion(request.getDireccion());

            Empresa empresa = empresaService.actualizarEmpresa(id, empresaActualizada);
            return ResponseEntity.ok(empresa);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtiene los usuarios de una empresa
     */
    @GetMapping("/{id}/usuarios")
    public ResponseEntity<List<UsuarioEmpresa>> obtenerUsuariosDeEmpresa(@PathVariable Long id, 
                                                                        Authentication authentication) {
        try {
            String email = authentication.getName();
            User usuario = userService.findByEmailWithAllRelations(email)
;
            if (usuario == null) {
                return ResponseEntity.badRequest().build();
            }

            // Verificar que el usuario pertenece a esta empresa
            if (!usuario.perteneceAEmpresa(id)) {
                return ResponseEntity.status(403).build();
            }

            // Aquí necesitarías implementar el método para obtener usuarios de la empresa
            // List<UsuarioEmpresa> usuarios = empresaService.obtenerUsuariosDeEmpresa(id);
            // return ResponseEntity.ok(usuarios);
            
            return ResponseEntity.ok(List.of()); // Temporal
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Agrega un usuario a la empresa (solo administradores)
     */
    @PostMapping("/{id}/usuarios")
    public ResponseEntity<UsuarioEmpresa> agregarUsuarioAEmpresa(@PathVariable Long id,
                                                                @RequestBody AgregarUsuarioRequest request,
                                                                Authentication authentication) {
        try {
            String email = authentication.getName();
            User usuario = userService.findByEmailWithAllRelations(email)
;
            if (usuario == null) {
                return ResponseEntity.badRequest().build();
            }

            // Verificar que el usuario es administrador de esta empresa
            if (!usuario.esAdministradorEmpresa(id)) {
                return ResponseEntity.status(403).build();
            }

            UsuarioEmpresa usuarioEmpresa = empresaService.agregarUsuarioAEmpresa(
                    id, request.getUsuarioId(), request.getRol(), usuario);
            return ResponseEntity.ok(usuarioEmpresa);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Remueve un usuario de la empresa (solo administradores)
     */
    @DeleteMapping("/{id}/usuarios/{usuarioId}")
    public ResponseEntity<Void> removerUsuarioDeEmpresa(@PathVariable Long id,
                                                        @PathVariable Long usuarioId,
                                                        Authentication authentication) {
        try {
            String email = authentication.getName();
            User usuario = userService.findByEmailWithAllRelations(email)
;
            if (usuario == null) {
                return ResponseEntity.badRequest().build();
            }

            // Verificar que el usuario es administrador de esta empresa
            if (!usuario.esAdministradorEmpresa(id)) {
                return ResponseEntity.status(403).build();
            }

            empresaService.removerUsuarioDeEmpresa(id, usuarioId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Cambia el rol de un usuario en la empresa (solo administradores)
     */
    @PutMapping("/{id}/usuarios/{usuarioId}/rol")
    public ResponseEntity<UsuarioEmpresa> cambiarRolUsuario(@PathVariable Long id,
                                                           @PathVariable Long usuarioId,
                                                           @RequestBody CambiarRolRequest request,
                                                           Authentication authentication) {
        try {
            String email = authentication.getName();
            User usuario = userService.findByEmailWithAllRelations(email)
;
            if (usuario == null) {
                return ResponseEntity.badRequest().build();
            }

            // Verificar que el usuario es administrador de esta empresa
            if (!usuario.esAdministradorEmpresa(id)) {
                return ResponseEntity.status(403).build();
            }

            UsuarioEmpresa usuarioEmpresa = empresaService.cambiarRolUsuarioEnEmpresa(
                    id, usuarioId, request.getRol());
            return ResponseEntity.ok(usuarioEmpresa);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtiene estadísticas de la empresa
     */
    @GetMapping("/{id}/estadisticas")
    public ResponseEntity<Object[]> obtenerEstadisticasEmpresa(@PathVariable Long id,
                                                              Authentication authentication) {
        try {
            String email = authentication.getName();
            User usuario = userService.findByEmailWithAllRelations(email)
;
            if (usuario == null) {
                return ResponseEntity.badRequest().build();
            }

            // Verificar que el usuario pertenece a esta empresa
            if (!usuario.perteneceAEmpresa(id)) {
                return ResponseEntity.status(403).build();
            }

            // Aquí necesitarías implementar el método para obtener estadísticas de la empresa
            // Object[] estadisticas = empresaService.obtenerEstadisticasEmpresa(id);
            // return ResponseEntity.ok(estadisticas);
            
            return ResponseEntity.ok(new Object[]{}); // Temporal
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Verifica si la empresa puede agregar más usuarios
     */
    @GetMapping("/{id}/puede-agregar-usuario")
    public ResponseEntity<Boolean> puedeAgregarUsuario(@PathVariable Long id,
                                                      Authentication authentication) {
        try {
            String email = authentication.getName();
            User usuario = userService.findByEmailWithAllRelations(email)
;
            if (usuario == null) {
                return ResponseEntity.badRequest().build();
            }

            // Verificar que el usuario pertenece a esta empresa
            if (!usuario.perteneceAEmpresa(id)) {
                return ResponseEntity.status(403).build();
            }

            boolean puedeAgregar = empresaService.puedeAgregarUsuario(id);
            return ResponseEntity.ok(puedeAgregar);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Verifica si la empresa puede agregar más campos
     */
    @GetMapping("/{id}/puede-agregar-campo")
    public ResponseEntity<Boolean> puedeAgregarCampo(@PathVariable Long id,
                                                    Authentication authentication) {
        try {
            String email = authentication.getName();
            User usuario = userService.findByEmailWithAllRelations(email)
;
            if (usuario == null) {
                return ResponseEntity.badRequest().build();
            }

            // Verificar que el usuario pertenece a esta empresa
            if (!usuario.perteneceAEmpresa(id)) {
                return ResponseEntity.status(403).build();
            }

            boolean puedeAgregar = empresaService.puedeAgregarCampo(id);
            return ResponseEntity.ok(puedeAgregar);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Clases DTO para las requests
    public static class ActualizarEmpresaRequest {
        private String nombre;
        private String cuit;
        private String emailContacto;
        private String telefonoContacto;
        private String direccion;

        // Getters and Setters
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getCuit() { return cuit; }
        public void setCuit(String cuit) { this.cuit = cuit; }
        public String getEmailContacto() { return emailContacto; }
        public void setEmailContacto(String emailContacto) { this.emailContacto = emailContacto; }
        public String getTelefonoContacto() { return telefonoContacto; }
        public void setTelefonoContacto(String telefonoContacto) { this.telefonoContacto = telefonoContacto; }
        public String getDireccion() { return direccion; }
        public void setDireccion(String direccion) { this.direccion = direccion; }
    }

    public static class AgregarUsuarioRequest {
        private Long usuarioId;
        private RolEmpresa rol;

        // Getters and Setters
        public Long getUsuarioId() { return usuarioId; }
        public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
        public RolEmpresa getRol() { return rol; }
        public void setRol(RolEmpresa rol) { this.rol = rol; }
    }

    public static class CambiarRolRequest {
        private RolEmpresa rol;

        // Getters and Setters
        public RolEmpresa getRol() { return rol; }
        public void setRol(RolEmpresa rol) { this.rol = rol; }
    }

}
