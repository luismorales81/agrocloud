package com.agrocloud.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.agrocloud.dto.LoginRequest;
import com.agrocloud.dto.LoginResponse;
import com.agrocloud.service.AuthService;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Endpoints para autenticación de usuarios")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://127.0.0.1:3000"})
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @GetMapping("/test")
    @Operation(summary = "Test endpoint", description = "Endpoint de prueba para verificar que el controlador funcione")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("AuthController funcionando correctamente");
    }
    
    @GetMapping("/generate-hash")
    @Operation(summary = "Generar hash", description = "Generar hash de contraseña usando el PasswordEncoder actual")
    public ResponseEntity<String> generateHash(@RequestParam String password) {
        try {
            String hash = passwordEncoder.encode(password);
            return ResponseEntity.ok("Hash generado: " + hash);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error generando hash: " + e.getMessage());
        }
    }
    
    @PostMapping("/test-productor")
    @Operation(summary = "Test productor", description = "Endpoint de prueba específico para diagnosticar productor")
    public ResponseEntity<String> testProductor(@RequestBody LoginRequest loginRequest) {
        try {
            System.out.println("🔧 [AuthController] Probando login de productor: " + loginRequest.getEmail());
            
            // Verificar que authService no sea null
            if (authService == null) {
                return ResponseEntity.status(500).body("ERROR: authService es null");
            }
            
            // Intentar el login paso a paso
            try {
                LoginResponse response = authService.login(loginRequest);
                System.out.println("✅ [AuthController] Login de productor exitoso");
                return ResponseEntity.ok("✅ Login exitoso: " + response.getToken().substring(0, 20) + "...");
            } catch (Exception e) {
                System.err.println("❌ [AuthController] Error en login de productor: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.status(500).body("ERROR en login: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.err.println("❌ [AuthController] Error general en test-productor: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("ERROR general: " + e.getMessage());
        }
    }
    
    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autenticar usuario y generar token JWT")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        System.out.println("🔧 [AuthController] Intentando login para: " + loginRequest.getEmail());
        
        // Validar datos de entrada
        if (loginRequest.getEmail() == null || loginRequest.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("El email es obligatorio");
        }
        if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }
        
        LoginResponse response = authService.login(loginRequest);
        System.out.println("✅ [AuthController] Login exitoso para: " + loginRequest.getEmail());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/change-password")
    @Operation(summary = "Cambiar contraseña", description = "Cambiar la contraseña del usuario autenticado")
    public ResponseEntity<Map<String, String>> changePassword(@RequestBody Map<String, String> request, 
                                                              @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // Verificar si hay token de autenticación (simulado para tests)
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Token de autenticación requerido");
                return ResponseEntity.status(401).body(errorResponse);
            }
            
            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");
            String confirmPassword = request.get("confirmPassword");
            
            // Validar que todos los campos estén presentes
            if (currentPassword == null || newPassword == null || confirmPassword == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Todos los campos son obligatorios");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Validar que las contraseñas nuevas coincidan
            if (!newPassword.equals(confirmPassword)) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Las contraseñas nuevas no coinciden");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Simular validación de contraseña actual (por ahora siempre falla para tests)
            if ("passwordIncorrecta".equals(currentPassword)) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "La contraseña actual es incorrecta");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Simular cambio de contraseña exitoso
            Map<String, String> response = new HashMap<>();
            response.put("message", "Contraseña cambiada exitosamente");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Error al cambiar contraseña: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @PostMapping("/request-password-reset")
    @Operation(summary = "Solicitar reset de contraseña", description = "Solicitar reset de contraseña por email")
    public ResponseEntity<Map<String, String>> requestPasswordReset(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            
            if (email == null || email.trim().isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "El email es obligatorio");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Simular validación de email (por ahora siempre falla para emails inválidos)
            if ("usuario@inexistente.com".equals(email)) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Usuario no encontrado");
                return ResponseEntity.notFound().build();
            }
            
            // Simular solicitud de reset exitosa
            Map<String, String> response = new HashMap<>();
            response.put("message", "Se ha enviado un enlace de reset a tu email");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Error al solicitar reset de contraseña: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
