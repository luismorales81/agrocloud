package com.agrocloud.controller;

import com.agrocloud.dto.LoginRequest;
import com.agrocloud.dto.LoginResponse;
import com.agrocloud.dto.CreateUserRequest;
import com.agrocloud.dto.UserDto;
import com.agrocloud.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            logger.info("🔧 [AuthController] Recibida petición de login para: {}", loginRequest.getEmail());
            LoginResponse response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ [AuthController] Error en login: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Credenciales incorrectas");
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
}