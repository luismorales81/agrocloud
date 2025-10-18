package com.agrocloud.service;

import com.agrocloud.dto.CreateUserRequest;
import com.agrocloud.dto.LoginRequest;
import com.agrocloud.dto.LoginResponse;
import com.agrocloud.dto.UserDto;
import com.agrocloud.model.entity.Role;
import com.agrocloud.model.entity.User;
import com.agrocloud.repository.RoleRepository;
import com.agrocloud.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthService implements UserDetailsService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private PermissionService permissionService;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    /**
     * Autenticar usuario y generar token JWT
     */
    public LoginResponse login(LoginRequest loginRequest) {
        logger.info("🔧 [AuthService] Intentando login para usuario: {}", loginRequest.getEmail());
        
        try {
            // Verificar que el usuario existe antes de autenticar
            logger.info("🔍 [AuthService] Buscando usuario en la base de datos...");
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + loginRequest.getEmail()));
            
            logger.info("✅ [AuthService] Usuario encontrado: {}, activo: {}, contraseña: {}", 
                user.getEmail(), user.getActivo(), user.getPassword().substring(0, 10) + "...");
            
            // Verificar que el usuario esté activo
            if (!user.getActivo()) {
                logger.error("❌ [AuthService] Usuario inactivo: {}", loginRequest.getEmail());
                throw new RuntimeException("Usuario inactivo");
            }
            
            // Autenticar usuario
            logger.info("🔐 [AuthService] Iniciando autenticación con AuthenticationManager...");
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );
            
            logger.info("✅ [AuthService] Autenticación exitosa, obteniendo UserDetails...");
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            logger.info("✅ [AuthService] Autenticación exitosa para: {}", userDetails.getUsername());
            
            // Generar token JWT
            logger.info("🔑 [AuthService] Generando token JWT...");
            String token = jwtService.generateToken(userDetails);
            
            // Convertir a DTO
            logger.info("🔄 [AuthService] Convirtiendo usuario a DTO...");
            UserDto userDto = convertToDto(user);
            
            logger.info("✅ [AuthService] Login exitoso para usuario: {}", user.getEmail());
            
            return new LoginResponse(token, jwtService.getExpirationTimeInSeconds(), userDto);
            
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            logger.warn("🔐 [AuthService] Credenciales incorrectas para usuario: {}", loginRequest.getEmail());
            throw e; // Re-lanzar BadCredentialsException para que sea manejada por GlobalExceptionHandler
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            logger.warn("👤 [AuthService] Usuario no encontrado: {}", loginRequest.getEmail());
            throw e; // Re-lanzar UsernameNotFoundException para que sea manejada por GlobalExceptionHandler
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("Usuario inactivo")) {
                logger.warn("🚫 [AuthService] Usuario inactivo: {}", loginRequest.getEmail());
                throw e; // Re-lanzar para usuario inactivo
            }
            logger.error("❌ [AuthService] Error inesperado en login para usuario {}: {}", loginRequest.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Error interno del servidor", e);
        } catch (Exception e) {
            logger.error("💥 [AuthService] Error no controlado en login para usuario {}: {}", loginRequest.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Error interno del servidor", e);
        }
    }
    
    /**
     * Crear nuevo usuario
     */
    public UserDto createUser(CreateUserRequest request) {
        logger.info("Creando nuevo usuario: {}", request.getEmail());
        
        // Verificar que el email no exista
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }
        
        // Buscar rol
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        
        // Crear usuario
        User user = new User();
        user.setUsername(request.getEmail()); // Usar email como username
        user.setFirstName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setActivo(true);
        
        // Asignar rol al usuario
        user.getRoles().add(role);
        
        User savedUser = userRepository.save(user);
        
        logger.info("Usuario creado exitosamente: {}", savedUser.getEmail());
        
        return convertToDto(savedUser);
    }
    
    /**
     * Obtener usuario por ID
     */
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return convertToDto(user);
    }
    
    /**
     * Obtener usuario por email
     */
    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return convertToDto(user);
    }
    
    /**
     * Obtener todos los usuarios
     */
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtener usuarios con filtros
     */
    public List<UserDto> getUsersWithFilters(String name, String email, String roleName, Boolean active) {
        return userRepository.findUsersWithFilters(name, email, active).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Actualizar usuario
     */
    public UserDto updateUser(Long id, CreateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Verificar email único si cambió
        if (!user.getEmail().equals(request.getEmail()) && 
            userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }
        
        user.setFirstName(request.getName());
        user.setEmail(request.getEmail());
        
        // Actualizar contraseña solo si se proporcionó una nueva
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        
        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }
    
    /**
     * Activar/desactivar usuario
     */
    public UserDto toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        user.setActivo(!user.getActivo());
        User savedUser = userRepository.save(user);
        
        return convertToDto(savedUser);
    }
    
    /**
     * Eliminar usuario
     */
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado");
        }
        userRepository.deleteById(id);
    }
    
    /**
     * Verificar email
     */
    public boolean verifyEmail(String token) {
        logger.info("Verificando email con token: {}", token);
        
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Token de verificación inválido"));
        
        user.setEmailVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);
        
        logger.info("Email verificado exitosamente para usuario: {}", user.getEmail());
        return true;
    }
    
    /**
     * Solicitar reset de contraseña
     */
    public void requestPasswordReset(String email) {
        logger.info("Solicitando reset de contraseña para: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ese email"));
        
        if (!user.getActivo()) {
            throw new RuntimeException("Usuario inactivo");
        }
        
        // Generar token de reset
        String resetToken = generateResetToken();
        LocalDateTime expiryTime = LocalDateTime.now().plusHours(24); // Token válido por 24 horas
        
        user.setResetPasswordToken(resetToken);
        user.setResetPasswordTokenExpiry(expiryTime);
        userRepository.save(user);
        
        // Enviar email
        emailService.sendPasswordResetEmail(email, resetToken);
        
        logger.info("Token de reset generado y email enviado para: {}", email);
    }
    
    /**
     * Resetear contraseña
     */
    public void resetPassword(String token, String newPassword) {
        logger.info("Reseteando contraseña con token: {}", token);
        
        User user = userRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new RuntimeException("Token de reset inválido"));
        
        // Verificar que el token no haya expirado
        if (user.getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token de reset expirado");
        }
        
        // Actualizar contraseña
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiry(null);
        userRepository.save(user);
        
        logger.info("Contraseña reseteada exitosamente para usuario: {}", user.getEmail());
    }
    
    /**
     * Cambiar contraseña desde el perfil del usuario
     */
    public void changePassword(String userEmail, String currentPassword, String newPassword) {
        logger.info("Cambiando contraseña para usuario: {}", userEmail);
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Verificar contraseña actual
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Contraseña actual incorrecta");
        }
        
        // Actualizar contraseña
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        logger.info("Contraseña cambiada exitosamente para usuario: {}", userEmail);
    }
    
    /**
     * Obtener estadísticas de usuarios
     */
    public Map<String, Object> getUserStats() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalUsers", userRepository.count());
        stats.put("activeUsers", userRepository.count());
        stats.put("unverifiedUsers", 0L);
        
        // Usuarios por rol
        Map<String, Long> roleStats = new HashMap<>();
        roleStats.put("ADMINISTRADOR", userRepository.count());
        stats.put("usersByRole", roleStats);
        
        return stats;
    }
    
    /**
     * Implementación de UserDetailsService
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));
        
        // Obtener permisos según el rol (sistema multitenant o legacy)
        String roleName = "Sin rol";
        List<String> authorities = new ArrayList<>();
        
        // Usar el método getRoles() que tiene manejo de excepciones
        try {
            Set<Role> roles = user.getRoles();
            if (roles != null && !roles.isEmpty()) {
                Role role = roles.iterator().next();
                if (role != null && role.getNombre() != null) {
                    roleName = role.getNombre();
                    authorities.addAll(roles.stream()
                        .map(r -> "ROLE_" + r.getNombre())
                        .collect(Collectors.toList()));
                }
            }
        } catch (Exception e) {
            System.err.println("Error al obtener roles del usuario: " + e.getMessage());
        }
        
        Set<String> permissions = permissionService.getPermissionsByRole(roleName);
        authorities.addAll(permissions);
        
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .disabled(!user.getActivo())
                .accountExpired(false)
                .credentialsExpired(false)
                .accountLocked(false)
                .authorities(authorities.toArray(new String[0]))
                .build();
    }
    
    /**
     * Convertir User a UserDto
     */
    private UserDto convertToDto(User user) {
        // Obtener rol del sistema multitenant o legacy
        String roleName = "Sin rol";
        
        // Consultar directamente la tabla usuario_empresas para obtener el rol
        try {
            String sql = "SELECT rol FROM usuario_empresas WHERE usuario_id = ? AND estado = 'ACTIVO' LIMIT 1";
            List<String> roles = jdbcTemplate.queryForList(sql, String.class, user.getId());
            if (!roles.isEmpty()) {
                roleName = roles.get(0);
            }
        } catch (Exception e) {
            logger.warn("Error consultando roles del usuario: {}", e.getMessage());
        }
        
        // Fallback: verificar en el sistema legacy (Roles directos)
        if ("Sin rol".equals(roleName) && !user.getRoles().isEmpty()) {
            roleName = user.getRoles().iterator().next().getNombre();
        }
        
        Set<String> permissions = permissionService.getPermissionsByRole(roleName);
        
        return new UserDto(
            user.getId(),
            user.getUsername(),
            user.getFirstName() + " " + user.getLastName(),
            user.getEmail(),
            roleName,
            permissions,
            user.getActivo(),
            true,
            null,
            user.getCreatedAt()
        );
    }
    
    /**
     * Generar token de verificación
     */
    @SuppressWarnings("unused")
    private String generateVerificationToken() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * Generar token de reset
     */
    private String generateResetToken() {
        return UUID.randomUUID().toString();
    }
}
