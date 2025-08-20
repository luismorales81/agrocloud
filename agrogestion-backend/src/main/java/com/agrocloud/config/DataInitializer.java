package com.agrocloud.config;

import com.agrocloud.model.entity.Role;
import com.agrocloud.model.entity.User;
import com.agrocloud.repository.RoleRepository;
import com.agrocloud.repository.UserRepository;
import com.agrocloud.service.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

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
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("Inicializando datos por defecto...");
        
        // Inicializar roles por defecto
        roleService.initializeDefaultRoles();
        
        // Crear usuario administrador por defecto si no existe
        createDefaultAdminUser();
        
        logger.info("Inicialización de datos completada");
    }
    
    private void createDefaultAdminUser() {
        try {
            // Verificar si ya existe un usuario administrador
            if (userRepository.findByEmail("admin@agrocloud.com").isPresent()) {
                logger.info("Usuario administrador ya existe");
                return;
            }
            
            // Buscar rol de administrador
            Role adminRole = roleRepository.findByName("ADMINISTRADOR")
                    .orElseThrow(() -> new RuntimeException("Rol ADMINISTRADOR no encontrado"));
            
            // Crear usuario administrador
            User adminUser = new User();
            adminUser.setFirstName("Administrador");
            adminUser.setLastName("Sistema");
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@agrocloud.com");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.getRoles().add(adminRole);
            adminUser.setActivo(true);
            
            userRepository.save(adminUser);
            
            logger.info("Usuario administrador creado: admin@agrocloud.com / admin123");
            
        } catch (Exception e) {
            logger.error("Error creando usuario administrador: {}", e.getMessage());
        }
    }
}
