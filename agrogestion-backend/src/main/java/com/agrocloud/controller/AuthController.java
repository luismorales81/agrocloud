package com.agrocloud.controller;

import com.agrocloud.dto.LoginRequest;
import com.agrocloud.dto.LoginResponse;
import com.agrocloud.dto.CreateUserRequest;
import com.agrocloud.dto.UserDto;
import com.agrocloud.exception.EulaNoAceptadoException;
import com.agrocloud.service.AuthService;
import com.agrocloud.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired(required = false)
    private EmailService emailService;
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            logger.info("🔧 [AuthController] Recibida petición de login para: {}", loginRequest.getEmail());
            LoginResponse response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (EulaNoAceptadoException e) {
            // Re-lanzar para que GlobalExceptionHandler lo maneje correctamente
            logger.warn("📄 [AuthController] EULA no aceptado: {}", e.getMessage());
            throw e;
        } catch (BadCredentialsException | UsernameNotFoundException e) {
            // Re-lanzar para que GlobalExceptionHandler lo maneje correctamente
            logger.warn("🔐 [AuthController] Error de autenticación: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            // Manejar otros RuntimeException específicos
            if (e.getMessage() != null && e.getMessage().contains("Usuario inactivo")) {
                logger.warn("🚫 [AuthController] Usuario inactivo");
                throw e;
            }
            logger.error("❌ [AuthController] Error en login: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Credenciales incorrectas");
        } catch (Exception e) {
            logger.error("❌ [AuthController] Error inesperado en login: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error al iniciar sesión: " + e.getMessage());
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody CreateUserRequest registerRequest) {
        try {
            logger.info("🔧 [AuthController] Recibida petición de registro para: {}", registerRequest.getEmail());
            
            // Generar hash de la contraseña para verificar
            String passwordHash = passwordEncoder.encode(registerRequest.getPassword());
            logger.info("🔑 [AuthController] Hash generado para '{}': {}", registerRequest.getPassword(), passwordHash);
            
            // Crear usuario usando el servicio
            UserDto userDto = authService.createUser(registerRequest);
            logger.info("✅ [AuthController] Usuario registrado exitosamente: {}", userDto.getEmail());
            
            return ResponseEntity.ok(userDto);
        } catch (Exception e) {
            logger.error("❌ [AuthController] Error en registro: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error al registrar usuario: " + e.getMessage());
        }
    }
    
    @PostMapping("/generate-hash")
    public ResponseEntity<String> generateHash(@RequestParam String password) {
        String hash = passwordEncoder.encode(password);
        logger.info("🔑 [AuthController] Hash generado para '{}': {}", password, hash);
        return ResponseEntity.ok(hash);
    }
    
    @GetMapping("/generate-hash")
    public ResponseEntity<String> generateHashGet(@RequestParam String password) {
        String hash = passwordEncoder.encode(password);
        logger.info("🔑 [AuthController] Hash generado para '{}': {}", password, hash);
        return ResponseEntity.ok(hash);
    }
    
    @GetMapping("/test-hash")
    public ResponseEntity<String> testHash(@RequestParam String password) {
        String hash = passwordEncoder.encode(password);
        boolean matches = passwordEncoder.matches(password, hash);
        String result = String.format("Password: %s\nHash: %s\nMatches: %s", password, hash, matches);
        logger.info("🔑 [AuthController] Test hash: {}", result);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/test-password")
    public ResponseEntity<String> testPassword(@RequestParam String password, @RequestParam String hash) {
        boolean matches = passwordEncoder.matches(password, hash);
        logger.info("🔍 [AuthController] Verificando '{}' contra hash: {} = {}", password, hash, matches);
        return ResponseEntity.ok("Matches: " + matches);
    }
    
    @PostMapping("/request-password-reset")
    public ResponseEntity<?> requestPasswordReset(@RequestBody com.agrocloud.dto.PasswordResetRequest request) {
        try {
            logger.info("🔧 [AuthController] Solicitud de recupero de contraseña para: {}", request.getEmail());
            
            // Validar formato de email
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El email es requerido");
            }
            
            // Procesar solicitud (siempre devuelve éxito por seguridad)
            authService.requestPasswordReset(request.getEmail());
            
            // Siempre devolver el mismo mensaje exitoso, sin revelar si el email existe o no
            // Esto previene ataques de enumeración de usuarios
            return ResponseEntity.ok().body("Si el email existe en nuestro sistema, recibirás un correo con las instrucciones para recuperar tu contraseña");
            
        } catch (Exception e) {
            logger.error("❌ [AuthController] Error inesperado en recupero de contraseña: {}", e.getMessage());
            // Por seguridad, siempre devolver mensaje genérico
            return ResponseEntity.ok().body("Si el email existe en nuestro sistema, recibirás un correo con las instrucciones para recuperar tu contraseña");
        }
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody com.agrocloud.dto.PasswordResetRequest request) {
        try {
            logger.info("🔧 [AuthController] Reset de contraseña con token");
            authService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok().body("Contraseña restablecida exitosamente");
        } catch (RuntimeException e) {
            logger.warn("⚠️ [AuthController] Error en reset de contraseña: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("❌ [AuthController] Error inesperado en reset de contraseña: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error al restablecer la contraseña");
        }
    }
    
    @PostMapping("/test-email")
    public ResponseEntity<?> testEmail(@RequestBody com.agrocloud.dto.PasswordResetRequest request) {
        try {
            logger.info("🧪 [AuthController] Test de envío de email a: {}", request.getEmail());
            
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Email requerido para prueba");
            }
            
            if (emailService == null) {
                return ResponseEntity.badRequest().body("EmailService no está disponible. Verifica la configuración de email.");
            }
            
            // Generar un token de prueba
            String testToken = "test-token-" + System.currentTimeMillis();
            
            // Intentar enviar email de prueba
            emailService.sendPasswordResetEmail(request.getEmail(), testToken);
            
            return ResponseEntity.ok().body("Email de prueba enviado exitosamente a: " + request.getEmail() + ". Revisa los logs del backend para más detalles.");
            
        } catch (Exception e) {
            logger.error("❌ [AuthController] Error en test de email: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error enviando email de prueba: " + e.getMessage() + ". Revisa los logs del backend para más detalles.");
        }
    }
}