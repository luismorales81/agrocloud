package com.agrocloud.config;

import com.agrocloud.model.entity.Empresa;
import com.agrocloud.model.entity.Role;
import com.agrocloud.model.entity.User;
import com.agrocloud.model.entity.UsuarioEmpresa;
import com.agrocloud.model.enums.EstadoEmpresa;
import com.agrocloud.model.enums.EstadoUsuarioEmpresa;
import com.agrocloud.model.enums.RolEmpresa;
import com.agrocloud.repository.EmpresaRepository;
import com.agrocloud.repository.RoleRepository;
import com.agrocloud.repository.UserRepository;
import com.agrocloud.repository.UsuarioEmpresaRepository;
import com.agrocloud.service.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    
    @Autowired
    private RoleService roleService;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmpresaRepository empresaRepository;
    
    @Autowired
    private UsuarioEmpresaRepository usuarioEmpresaRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("Inicializando datos por defecto...");
        
        // Inicializar roles por defecto
        roleService.initializeDefaultRoles();
        
        // Crear empresa por defecto si no existe - TEMPORALMENTE DESHABILITADO
        // Empresa empresaPorDefecto = createDefaultEmpresa();
        
        // Crear usuario administrador por defecto si no existe - TEMPORALMENTE DESHABILITADO
        // User adminUser = createDefaultAdminUser();
        
        // Asignar usuario administrador a la empresa por defecto - TEMPORALMENTE DESHABILITADO
        // if (adminUser != null && empresaPorDefecto != null) {
        //     assignUserToEmpresa(adminUser, empresaPorDefecto, RolEmpresa.ADMINISTRADOR);
        // }
        
        // Asignar todos los usuarios existentes a la empresa por defecto si no tienen empresa - TEMPORALMENTE DESHABILITADO
        // assignExistingUsersToDefaultEmpresa(empresaPorDefecto);
        
        logger.info("Inicialización de datos completada");
    }
    
    private User createDefaultAdminUser() {
        try {
            // Verificar si ya existe un usuario administrador
            if (userRepository.findByEmail("admin@agrocloud.com").isPresent()) {
                logger.info("Usuario administrador ya existe");
                return userRepository.findByEmail("admin@agrocloud.com").get();
            }
            
            // Buscar rol de administrador
            Role adminRole = roleRepository.findByNombre("ADMINISTRADOR")
                    .orElseThrow(() -> new RuntimeException("Rol ADMINISTRADOR no encontrado"));
            
            // Crear usuario administrador
            User adminUser = new User();
            adminUser.setFirstName("Administrador");
            adminUser.setLastName("Sistema");
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@agrocloud.com");
            // TODO: Hacer configurable con application.properties
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.getRoles().add(adminRole);
            adminUser.setActivo(true);
            
            userRepository.save(adminUser);
            
            logger.info("Usuario administrador creado: admin@agrocloud.com / admin123");
            return adminUser;
            
        } catch (Exception e) {
            logger.error("Error creando usuario administrador: {}", e.getMessage());
            return null;
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
            usuarioEmpresa.setFechaInicio(java.time.LocalDate.now());
            usuarioEmpresa.setCreadoPor(user); // El admin se asigna a sí mismo
            
            usuarioEmpresaRepository.save(usuarioEmpresa);
            
            logger.info("Usuario {} asignado a empresa {} con rol {}", 
                       user.getEmail(), empresa.getNombre(), rol);
            
        } catch (Exception e) {
            logger.error("Error asignando usuario a empresa: {}", e.getMessage());
        }
    }
    
    private void assignExistingUsersToDefaultEmpresa(Empresa empresa) {
        try {
            if (empresa == null) {
                logger.warn("No hay empresa por defecto para asignar usuarios");
                return;
            }
            
            // Obtener todos los usuarios que no tienen empresa asignada
            List<User> usuarios = userRepository.findAll();
            int asignados = 0;
            
            for (User usuario : usuarios) {
                // Verificar si el usuario ya tiene alguna empresa asignada
                if (!usuarioEmpresaRepository.existsByUsuario(usuario)) {
                    // Asignar rol según el tipo de usuario
                    RolEmpresa rol = determinarRolEmpresa(usuario);
                    
                    assignUserToEmpresa(usuario, empresa, rol);
                    asignados++;
                }
            }
            
            logger.info("Asignados {} usuarios a la empresa por defecto", asignados);
            
        } catch (Exception e) {
            logger.error("Error asignando usuarios existentes a empresa por defecto: {}", e.getMessage());
        }
    }
    
    /**
     * Determina el rol de empresa apropiado basado en los roles globales del usuario
     * Los roles globales ahora son: SUPERADMIN, USUARIO_REGISTRADO, INVITADO
     */
    private RolEmpresa determinarRolEmpresa(User usuario) {
        // Verificar si tiene rol de super administrador
        if (usuario.getRoles().stream().anyMatch(r -> "SUPERADMIN".equals(r.getNombre()))) {
            return RolEmpresa.ADMINISTRADOR;
        }
        
        // Para USUARIO_REGISTRADO, asignar como OPERARIO por defecto
        // Esto asegura que todos los usuarios registrados tengan acceso básico a la empresa
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
