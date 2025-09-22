package com.agrocloud.controller;

import com.agrocloud.model.entity.Empresa;
import com.agrocloud.model.entity.User;
import com.agrocloud.model.entity.UsuarioEmpresa;
import com.agrocloud.model.enums.EstadoEmpresa;
import com.agrocloud.model.enums.EstadoUsuarioEmpresa;
import com.agrocloud.model.enums.RolEmpresa;
import com.agrocloud.repository.EmpresaRepository;
import com.agrocloud.repository.UserRepository;
import com.agrocloud.repository.UsuarioEmpresaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador temporal para corregir la asignación de usuarios a empresas
 * TODO: Eliminar después de resolver el problema
 */
@RestController
@RequestMapping("/api/temp-fix")
@CrossOrigin(origins = "*")
public class TempFixController {
    
    private static final Logger logger = LoggerFactory.getLogger(TempFixController.class);
    
    @Autowired
    private EmpresaRepository empresaRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UsuarioEmpresaRepository usuarioEmpresaRepository;
    
    /**
     * Endpoint temporal para asignar usuarios a empresa por defecto
     */
    @PostMapping("/assign-users-to-default-empresa")
    public ResponseEntity<Map<String, Object>> assignUsersToDefaultEmpresa() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("Iniciando asignación de usuarios a empresa por defecto...");
            
            // Crear empresa por defecto si no existe
            Empresa empresaPorDefecto = createDefaultEmpresa();
            if (empresaPorDefecto == null) {
                response.put("success", false);
                response.put("message", "Error creando empresa por defecto");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Obtener todos los usuarios
            List<User> usuarios = userRepository.findAll();
            int asignados = 0;
            int yaAsignados = 0;
            
            for (User usuario : usuarios) {
                // Verificar si el usuario ya tiene alguna empresa asignada
                if (!usuarioEmpresaRepository.existsByUsuario(usuario)) {
                    // Asignar rol según el tipo de usuario
                    RolEmpresa rol = determinarRolEmpresa(usuario);
                    
                    assignUserToEmpresa(usuario, empresaPorDefecto, rol);
                    asignados++;
                    logger.info("Usuario {} asignado a empresa {} con rol {}", 
                               usuario.getEmail(), empresaPorDefecto.getNombre(), rol);
                } else {
                    yaAsignados++;
                }
            }
            
            response.put("success", true);
            response.put("message", "Asignación completada exitosamente");
            response.put("empresa", empresaPorDefecto.getNombre());
            response.put("usuariosAsignados", asignados);
            response.put("usuariosYaAsignados", yaAsignados);
            response.put("totalUsuarios", usuarios.size());
            
            logger.info("Asignación completada: {} usuarios asignados, {} ya tenían empresa", 
                       asignados, yaAsignados);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error en asignación de usuarios: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Endpoint para verificar el estado actual
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            long totalEmpresas = empresaRepository.count();
            long totalUsuarios = userRepository.count();
            long totalRelaciones = usuarioEmpresaRepository.count();
            
            response.put("totalEmpresas", totalEmpresas);
            response.put("totalUsuarios", totalUsuarios);
            response.put("totalRelaciones", totalRelaciones);
            
            // Obtener lista de empresas
            List<Empresa> empresas = empresaRepository.findAll();
            response.put("empresas", empresas.stream().map(e -> Map.of(
                "id", e.getId(),
                "nombre", e.getNombre(),
                "estado", e.getEstado()
            )).toList());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error obteniendo estado: {}", e.getMessage(), e);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    private Empresa createDefaultEmpresa() {
        try {
            // Verificar si ya existe una empresa por defecto
            if (empresaRepository.count() > 0) {
                logger.info("Empresa por defecto ya existe");
                return empresaRepository.findAll().get(0);
            }
            
            // Crear empresa por defecto
            Empresa empresa = new Empresa();
            empresa.setNombre("AgroCloud Empresa Principal");
            empresa.setCuit("20-12345678-9");
            empresa.setEmailContacto("admin@agrocloud.com");
            empresa.setEstado(EstadoEmpresa.ACTIVO);
            empresa.setActivo(true);
            
            empresaRepository.save(empresa);
            
            logger.info("Empresa por defecto creada: {}", empresa.getNombre());
            return empresa;
            
        } catch (Exception e) {
            logger.error("Error creando empresa por defecto: {}", e.getMessage());
            return null;
        }
    }
    
    private void assignUserToEmpresa(User user, Empresa empresa, RolEmpresa rol) {
        try {
            // Verificar si ya existe la relación
            if (usuarioEmpresaRepository.existsByUsuarioAndEmpresa(user, empresa)) {
                logger.info("Usuario {} ya está asignado a empresa {}", user.getEmail(), empresa.getNombre());
                return;
            }
            
            // Crear relación usuario-empresa
            UsuarioEmpresa usuarioEmpresa = new UsuarioEmpresa();
            usuarioEmpresa.setUsuario(user);
            usuarioEmpresa.setEmpresa(empresa);
            usuarioEmpresa.setRol(rol);
            usuarioEmpresa.setEstado(EstadoUsuarioEmpresa.ACTIVO);
            usuarioEmpresa.setFechaInicio(LocalDate.now());
            usuarioEmpresa.setCreadoPor(user); // El usuario se asigna a sí mismo
            
            usuarioEmpresaRepository.save(usuarioEmpresa);
            
        } catch (Exception e) {
            logger.error("Error asignando usuario a empresa: {}", e.getMessage());
        }
    }
    
    private RolEmpresa determinarRolEmpresa(User usuario) {
        // Verificar si tiene rol de super administrador
        if (usuario.getRoles().stream().anyMatch(r -> "SUPERADMIN".equals(r.getNombre()))) {
            return RolEmpresa.ADMINISTRADOR;
        }
        
        // Para USUARIO_REGISTRADO, asignar como OPERARIO por defecto
        if (usuario.getRoles().stream().anyMatch(r -> "USUARIO_REGISTRADO".equals(r.getNombre()))) {
            logger.info("Usuario {} con rol USUARIO_REGISTRADO asignado como OPERARIO por defecto", 
                       usuario.getEmail());
            return RolEmpresa.OPERARIO;
        }
        
        // Para INVITADO, asignar como LECTURA
        if (usuario.getRoles().stream().anyMatch(r -> "INVITADO".equals(r.getNombre()))) {
            logger.info("Usuario {} con rol INVITADO asignado como LECTURA", 
                       usuario.getEmail());
            return RolEmpresa.LECTURA;
        }
        
        // Por defecto, asignar como OPERARIO
        logger.info("Usuario {} con roles {} asignado como OPERARIO por defecto", 
                   usuario.getEmail(), 
                   usuario.getRoles().stream().map(r -> r.getNombre()).toArray());
        return RolEmpresa.OPERARIO;
    }
}
