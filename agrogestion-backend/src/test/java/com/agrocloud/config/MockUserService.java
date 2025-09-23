package com.agrocloud.config;

import com.agrocloud.model.entity.Empresa;
import com.agrocloud.model.entity.Role;
import com.agrocloud.model.entity.User;
import com.agrocloud.model.entity.UserCompanyRole;
import com.agrocloud.model.enums.EstadoEmpresa;
import com.agrocloud.model.enums.RolEmpresa;
import com.agrocloud.repository.EmpresaRepository;
import com.agrocloud.repository.UserRepository;
import com.agrocloud.repository.UserCompanyRoleRepository;
import com.agrocloud.repository.RoleRepository;
import com.agrocloud.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Configuración mock para UserService en tests de integración.
 * Proporciona un mock del UserService para evitar dependencias reales.
 *
 * @author AgroGestion Team
 * @version 1.0.0
 */
@TestConfiguration
@Profile("test")
public class MockUserService {

    @Bean
    @Primary
    public UserService mockUserService(EmpresaRepository empresaRepository, UserRepository userRepository, UserCompanyRoleRepository userCompanyRoleRepository, RoleRepository roleRepository) {
        UserService userService = mock(UserService.class);
        
        // Crear una empresa mock y guardarla en la base de datos de test
        Empresa mockEmpresa = new Empresa();
        mockEmpresa.setNombre("Empresa Test");
        mockEmpresa.setCuit("20-12345678-9");
        mockEmpresa.setEmailContacto("test@empresa.com");
        mockEmpresa.setEstado(EstadoEmpresa.ACTIVO);
        mockEmpresa.setActivo(true);
        
        // Guardar la empresa en la base de datos de test
        Empresa savedEmpresa = empresaRepository.save(mockEmpresa);
        System.out.println("[MOCK_USER_SERVICE] Empresa creada en BD de test con ID: " + savedEmpresa.getId());
        
        // Crear un usuario mock y guardarlo en la base de datos de test
        User mockUser = new User();
        mockUser.setEmail("test@test.com");
        mockUser.setNombreUsuario("testuser");
        mockUser.setNombre("Test");
        mockUser.setApellido("User");
        mockUser.setPassword("password123"); // Contraseña requerida para validación
        mockUser.setActivo(true);
        
        // Guardar el usuario en la base de datos de test
        User savedUser = userRepository.save(mockUser);
        System.out.println("[MOCK_USER_SERVICE] Usuario creado en BD de test con ID: " + savedUser.getId());
        
        // Crear un Role entity y guardarlo en la base de datos de test
        Role mockRole = new Role();
        mockRole.setNombre(RolEmpresa.ADMINISTRADOR.name());
        mockRole.setDescripcion("Rol de administrador");
        
        // Guardar el Role en la base de datos de test
        Role savedRole = roleRepository.save(mockRole);
        System.out.println("[MOCK_USER_SERVICE] Role creado en BD de test con ID: " + savedRole.getId());
        
        // Crear un UserCompanyRole para asignar la empresa al usuario
        UserCompanyRole userCompanyRole = new UserCompanyRole();
        userCompanyRole.setUsuario(savedUser);
        userCompanyRole.setEmpresa(savedEmpresa);
        userCompanyRole.setRol(savedRole);
        userCompanyRole.setActivo(true);
        
        // Guardar el UserCompanyRole en la base de datos de test
        UserCompanyRole savedUserCompanyRole = userCompanyRoleRepository.save(userCompanyRole);
        System.out.println("[MOCK_USER_SERVICE] UserCompanyRole creado en BD de test con ID: " + savedUserCompanyRole.getId());
        
        // Asignar el UserCompanyRole al usuario
        savedUser.getUserCompanyRoles().add(savedUserCompanyRole);
        
        // Configurar el mock para devolver el usuario cuando se busque por email
        when(userService.findByEmail(anyString())).thenReturn(savedUser);
        
        // También configurar para cualquier otro método que pueda ser llamado
        when(userService.findById(any())).thenReturn(java.util.Optional.of(savedUser));
        
        System.out.println("[MOCK_USER_SERVICE] Mock configurado con usuario: " + savedUser.getEmail() + " y empresa: " + savedEmpresa.getNombre() + " (ID: " + savedEmpresa.getId() + ")");
        
        return userService;
    }
}
