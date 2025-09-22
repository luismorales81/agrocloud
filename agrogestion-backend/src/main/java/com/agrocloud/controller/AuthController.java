package com.agrocloud.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.agrocloud.dto.LoginRequest;
import com.agrocloud.dto.LoginResponse;
import com.agrocloud.service.AuthService;

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
        LoginResponse response = authService.login(loginRequest);
        System.out.println("✅ [AuthController] Login exitoso para: " + loginRequest.getEmail());
        return ResponseEntity.ok(response);
    }
}
