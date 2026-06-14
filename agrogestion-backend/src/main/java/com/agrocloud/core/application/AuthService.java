package com.agrocloud.core.application;

import com.agrocloud.dto.CreateUserRequest;
import com.agrocloud.dto.LoginRequest;
import com.agrocloud.dto.LoginResponse;
import com.agrocloud.dto.UserDto;
import com.agrocloud.core.domain.Role;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.infrastructure.RoleRepository;
import com.agrocloud.core.infrastructure.UserRepository;
import com.agrocloud.core.application.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

@Service("authServiceCore")
@Transactional
public class AuthService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    @Qualifier("userRepositoryCore")
    private UserRepository userRepository;

    @Autowired
    @Qualifier("roleRepositoryCore")
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    @Qualifier("jwtServiceCore")
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    @Qualifier("emailServiceCore")
    private EmailService emailService;

    @Autowired
    @Qualifier("permissionServiceCore")
    private PermissionService permissionService;

    @Autowired
    @Qualifier("userServiceCore")
    private UserService userService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public LoginResponse login(LoginRequest loginRequest) {
        logger.info("Intentando login para usuario: {}", loginRequest.getEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

            if (!user.getActivo()) {
                logger.warn("Usuario inactivo: {}", loginRequest.getEmail());
                throw new RuntimeException("Usuario inactivo");
            }

            if (user.getEulaAceptado() == null || !user.getEulaAceptado()) {
                logger.warn("Usuario sin EULA aceptado: {}", loginRequest.getEmail());
                throw new com.agrocloud.exception.EulaNoAceptadoException(
                    "Debe aceptar el EULA antes de acceder al sistema. Por favor, acepte los términos y condiciones.");
            }

            String token = jwtService.generateToken(userDetails);
            UserDto userDto = convertToDto(user);
            logger.info("Login exitoso para usuario: {}", user.getEmail());

            return new LoginResponse(token, jwtService.getExpirationTimeInSeconds(), userDto);

        } catch (com.agrocloud.exception.EulaNoAceptadoException e) {
            throw e;
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            logger.warn("Credenciales incorrectas para usuario: {}", loginRequest.getEmail());
            throw e;
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            logger.warn("Credenciales incorrectas para usuario: {}", loginRequest.getEmail());
            throw new org.springframework.security.authentication.BadCredentialsException("Credenciales incorrectas");
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("Usuario inactivo")) {
                throw e;
            }
            logger.error("Error inesperado en login para usuario {}: {}", loginRequest.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Error interno del servidor", e);
        } catch (Exception e) {
            logger.error("Error no controlado en login para usuario {}: {}", loginRequest.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Error interno del servidor", e);
        }
    }

    /**
     * Registro público: asigna siempre el rol USUARIO_REGISTRADO. Ignora cualquier roleId del cliente.
     */
    public UserDto registrarUsuarioPublico(com.agrocloud.dto.RegistroPublicoSolicitud solicitud) {
        logger.info("Registro público de usuario: {}", solicitud.getEmail());

        if (userRepository.existsByEmail(solicitud.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        Role role = roleRepository.findByNombre("USUARIO_REGISTRADO")
                .orElseThrow(() -> new RuntimeException("Rol USUARIO_REGISTRADO no configurado en el sistema"));

        User user = new User();
        user.setUsername(solicitud.getEmail());
        user.setFirstName(solicitud.getName());
        user.setEmail(solicitud.getEmail());
        user.setPassword(passwordEncoder.encode(solicitud.getPassword()));
        user.setActivo(true);
        user.getRoles().add(role);

        User savedUser = userRepository.save(user);
        logger.info("Usuario registrado exitosamente: {}", savedUser.getEmail());
        return convertToDto(savedUser);
    }

    public UserDto createUser(CreateUserRequest request) {
        logger.info("Creando nuevo usuario: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        if ("SUPERADMIN".equals(role.getNombre())) {
            throw new RuntimeException("No se puede asignar el rol SUPERADMIN mediante este endpoint");
        }

        User user = new User();
        user.setUsername(request.getEmail());
        user.setFirstName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setActivo(true);
        user.getRoles().add(role);

        User savedUser = userRepository.save(user);
        logger.info("Usuario creado exitosamente: {}", savedUser.getEmail());
        return convertToDto(savedUser);
    }

    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return convertToDto(user);
    }

    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return convertToDto(user);
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<UserDto> getUsersWithFilters(String name, String email, String roleName, Boolean active) {
        return userRepository.findUsersWithFilters(name, email, active).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public UserDto updateUser(Long id, CreateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!user.getEmail().equals(request.getEmail()) &&
            userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        user.setFirstName(request.getName());
        user.setEmail(request.getEmail());

        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }

    public UserDto toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setActivo(!user.getActivo());
        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado");
        }
        userRepository.deleteById(id);
    }

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

    public void requestPasswordReset(String email) {
        logger.info("Solicitando reset de contraseña para: {}", email);

        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null && user.getActivo()) {
            String resetToken = generateResetToken();
            LocalDateTime expiryTime = LocalDateTime.now().plusHours(24);

            user.setResetPasswordToken(resetToken);
            user.setResetPasswordTokenExpiry(expiryTime);
            userRepository.save(user);

            try {
                emailService.sendPasswordResetEmail(email, resetToken);
                logger.info("Token de reset generado y email enviado para: {}", email);
            } catch (Exception e) {
                logger.error("Error enviando email de recupero para: {}", email, e);
            }
        } else {
            if (user == null) {
                logger.warn("Intento de recupero de contraseña para email inexistente: {}", email);
            } else {
                logger.warn("Intento de recupero de contraseña para usuario inactivo: {}", email);
            }
            try {
                Thread.sleep(100 + (long)(Math.random() * 200));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        logger.info("Procesamiento de recupero de contraseña completado para: {} (sin revelar resultado)", email);
    }

    public void resetPassword(String token, String newPassword) {
        logger.info("Reseteando contraseña con token: {}", token);

        User user = userRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new RuntimeException("Token de reset inválido"));

        if (user.getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token de reset expirado");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiry(null);
        userRepository.save(user);

        logger.info("Contraseña reseteada exitosamente para usuario: {}", user.getEmail());
    }

    public void changePassword(String userEmail, String currentPassword, String newPassword) {
        logger.info("Cambiando contraseña para usuario: {}", userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Contraseña actual incorrecta");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        logger.info("Contraseña cambiada exitosamente para usuario: {}", userEmail);
    }

    public Map<String, Object> getUserStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalUsers", userRepository.count());
        stats.put("activeUsers", userRepository.count());
        stats.put("unverifiedUsers", 0L);

        Map<String, Long> roleStats = new HashMap<>();
        roleStats.put("ADMINISTRADOR", userRepository.count());
        stats.put("usersByRole", roleStats);

        return stats;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userService.findByEmailWithAllRelationsCombined(email);

        if (user == null) {
            throw new UsernameNotFoundException("Usuario no encontrado: " + email);
        }

        String roleName = "Sin rol";
        List<String> authorities = new ArrayList<>();

        try {
            if (user.getUsuarioEmpresas() != null && !user.getUsuarioEmpresas().isEmpty()) {
                for (com.agrocloud.core.domain.UsuarioEmpresa ue : user.getUsuarioEmpresas()) {
                    if (ue.getEstado() == com.agrocloud.model.enums.EstadoUsuarioEmpresa.ACTIVO && ue.getRol() != null) {
                        com.agrocloud.model.enums.RolEmpresa rol = ue.getRol();
                        String rolNombre = rol.name();
                        roleName = rolNombre;
                        authorities.add("ROLE_" + rolNombre);

                        if (rolNombre.equals("ADMINISTRADOR")) {
                            authorities.add("ROLE_ADMINISTRADOR");
                        }
                        if (rolNombre.equals("SUPERADMIN")) {
                            authorities.add("ROLE_SUPERADMIN");
                        }

                        System.out.println("🔍 [AuthService] Rol encontrado en usuario_empresas: " + rolNombre + " para usuario: " + email);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ [AuthService] Error al obtener roles del sistema nuevo: " + e.getMessage());
        }

        if ("Sin rol".equals(roleName)) {
            try {
                Set<Role> roles = user.getRoles();
                if (roles != null && !roles.isEmpty()) {
                    Role role = roles.iterator().next();
                    if (role != null && role.getNombre() != null) {
                        roleName = role.getNombre();
                        authorities.addAll(roles.stream()
                            .map(r -> "ROLE_" + r.getNombre())
                            .collect(Collectors.toList()));

                        System.out.println("🔍 [AuthService] Rol encontrado en sistema legacy: " + roleName + " para usuario: " + email);
                    }
                }
            } catch (Exception e) {
                System.err.println("⚠️ [AuthService] Error al obtener roles del sistema legacy: " + e.getMessage());
            }
        }

        Set<String> permissions = permissionService.getPermissionsByRole(roleName);
        authorities.addAll(permissions);

        System.out.println("🔍 [AuthService] Autoridades asignadas para " + email + ": " + authorities);

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

    private UserDto convertToDto(User user) {
        String roleName = "Sin rol";

        try {
            String sql = "SELECT rol FROM usuario_empresas WHERE usuario_id = ? AND estado = 'ACTIVO' LIMIT 1";
            List<String> roles = jdbcTemplate.queryForList(sql, String.class, user.getId());
            if (!roles.isEmpty()) {
                roleName = roles.get(0);
            }
        } catch (Exception e) {
            logger.warn("Error consultando roles del usuario: {}", e.getMessage());
        }

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

    @SuppressWarnings("unused")
    private String generateVerificationToken() {
        return UUID.randomUUID().toString();
    }

    private String generateResetToken() {
        return UUID.randomUUID().toString();
    }
}
