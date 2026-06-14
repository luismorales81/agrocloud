package com.agrocloud.controller;

import com.agrocloud.dto.LoginRequest;
import com.agrocloud.dto.LoginResponse;
import com.agrocloud.dto.RegistroPublicoSolicitud;
import com.agrocloud.dto.UserDto;
import com.agrocloud.exception.EulaNoAceptadoException;
import com.agrocloud.core.application.AuthService;
import com.agrocloud.core.application.EmailService;
import org.springframework.beans.factory.annotation.Qualifier;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    @Qualifier("authServiceCore")
    private AuthService authService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired(required = false)
    @Qualifier("emailServiceCore")
    private EmailService emailService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (EulaNoAceptadoException e) {
            throw e;
        } catch (BadCredentialsException | UsernameNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("Usuario inactivo")) {
                throw e;
            }
            logger.error("Error en login: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Credenciales incorrectas");
        } catch (Exception e) {
            logger.error("Error inesperado en login: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error al iniciar sesión");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegistroPublicoSolicitud solicitud) {
        try {
            UserDto userDto = authService.registrarUsuarioPublico(solicitud);
            return ResponseEntity.ok(userDto);
        } catch (Exception e) {
            logger.error("Error en registro: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error al registrar usuario: " + e.getMessage());
        }
    }

    @PostMapping("/generate-hash")
    @Profile("dev")
    public ResponseEntity<String> generateHash(@RequestParam String password) {
        return ResponseEntity.ok(passwordEncoder.encode(password));
    }

    @GetMapping("/generate-hash")
    @Profile("dev")
    public ResponseEntity<String> generateHashGet(@RequestParam String password) {
        return ResponseEntity.ok(passwordEncoder.encode(password));
    }

    @GetMapping("/test-hash")
    @Profile("dev")
    public ResponseEntity<String> testHash(@RequestParam String password) {
        String hash = passwordEncoder.encode(password);
        return ResponseEntity.ok("Matches: " + passwordEncoder.matches(password, hash));
    }

    @PostMapping("/test-password")
    @Profile("dev")
    public ResponseEntity<String> testPassword(@RequestParam String password, @RequestParam String hash) {
        return ResponseEntity.ok("Matches: " + passwordEncoder.matches(password, hash));
    }

    @PostMapping("/request-password-reset")
    public ResponseEntity<?> requestPasswordReset(@RequestBody com.agrocloud.dto.PasswordResetRequest request) {
        try {
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El email es requerido");
            }
            authService.requestPasswordReset(request.getEmail());
            return ResponseEntity.ok().body(
                "Si el email existe en nuestro sistema, recibirás un correo con las instrucciones para recuperar tu contraseña");
        } catch (Exception e) {
            logger.error("Error inesperado en recupero de contraseña: {}", e.getMessage());
            return ResponseEntity.ok().body(
                "Si el email existe en nuestro sistema, recibirás un correo con las instrucciones para recuperar tu contraseña");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody com.agrocloud.dto.PasswordResetRequest request) {
        try {
            authService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok().body("Contraseña restablecida exitosamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error inesperado en reset de contraseña: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error al restablecer la contraseña");
        }
    }

    @PostMapping("/test-email")
    @Profile("dev")
    public ResponseEntity<?> testEmail(@RequestBody com.agrocloud.dto.PasswordResetRequest request) {
        try {
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Email requerido para prueba");
            }
            if (emailService == null) {
                return ResponseEntity.badRequest().body("EmailService no está disponible.");
            }
            emailService.sendPasswordResetEmail(request.getEmail(), "test-token-" + System.currentTimeMillis());
            return ResponseEntity.ok().body("Email de prueba enviado a: " + request.getEmail());
        } catch (Exception e) {
            logger.error("Error en test de email: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error enviando email de prueba: " + e.getMessage());
        }
    }
}
