package com.agrocloud.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;
import com.agrocloud.model.entity.User;
import com.agrocloud.model.entity.Role;
import com.agrocloud.repository.UserRepository;
import com.agrocloud.service.JwtService;
import com.agrocloud.service.AuthService;
import com.agrocloud.service.UserService;
import org.springframework.security.core.Authentication;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:5173", "http://127.0.0.1:3000", "http://127.0.0.1:3001", "http://127.0.0.1:5173"})
public class TestController {
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;
    
    @GetMapping("/hash")
    public Map<String, String> generateHash(@RequestParam String password) {
        String hash = passwordEncoder.encode(password);
        Map<String, String> response = new HashMap<>();
        response.put("password", password);
        response.put("hash", hash);
        return response;
    }
    
    @GetMapping("/verify")
    public Map<String, Object> verifyHash(@RequestParam String password, @RequestParam String hash) {
        boolean matches = passwordEncoder.matches(password, hash);
        Map<String, Object> response = new HashMap<>();
        response.put("password", password);
        response.put("hash", hash);
        response.put("matches", matches);
        return response;
    }
    
    @GetMapping("/check-user")
    public Map<String, Object> checkUser(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                response.put("found", true);
                response.put("username", user.getUsername());
                response.put("email", user.getEmail());
                response.put("password", user.getPassword());
                response.put("active", user.getActivo());
                response.put("roles", user.getRoles().stream().map(Role::getNombre).collect(Collectors.toList()));
            } else {
                response.put("found", false);
            }
        } catch (Exception e) {
            response.put("error", e.getMessage());
        }
        return response;
    }
    
    @PostMapping("/test-login")
    public Map<String, Object> testLogin(@RequestBody Map<String, String> loginRequest) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = loginRequest.get("email");
            String password = loginRequest.get("password");
            
            response.put("email", email);
            response.put("password", password);
            
            // Buscar usuario
            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                response.put("error", "Usuario no encontrado");
                return response;
            }
            
            response.put("userFound", true);
            response.put("userActive", user.getActivo());
            response.put("userPassword", user.getPassword().substring(0, 10) + "...");
            
            // Verificar contraseña
            boolean passwordMatches = passwordEncoder.matches(password, user.getPassword());
            response.put("passwordMatches", passwordMatches);
            
            if (passwordMatches && user.getActivo()) {
                response.put("loginSuccess", true);
            } else {
                response.put("loginSuccess", false);
                if (!passwordMatches) {
                    response.put("error", "Contraseña incorrecta");
                } else if (!user.getActivo()) {
                    response.put("error", "Usuario inactivo");
                }
            }
            
        } catch (Exception e) {
            response.put("error", e.getMessage());
        }
        return response;
    }
    
    @PostMapping("/test-auth-manager")
    public Map<String, Object> testAuthManager(@RequestBody Map<String, String> loginRequest) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = loginRequest.get("email");
            String password = loginRequest.get("password");
            
            response.put("email", email);
            response.put("password", password);
            
            // Probar AuthenticationManager directamente
            try {
                org.springframework.security.authentication.UsernamePasswordAuthenticationToken token = 
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(email, password);
                
                org.springframework.security.core.Authentication authentication = authenticationManager.authenticate(token);
                
                response.put("authManagerAvailable", true);
                response.put("authenticationSuccess", true);
                response.put("principal", authentication.getPrincipal().toString());
                response.put("authorities", authentication.getAuthorities().toString());
                
            } catch (Exception e) {
                response.put("authManagerAvailable", true);
                response.put("authenticationSuccess", false);
                response.put("authError", e.getMessage());
                response.put("errorType", e.getClass().getSimpleName());
            }
            
        } catch (Exception e) {
            response.put("error", e.getMessage());
        }
        return response;
    }
    
    @PostMapping("/test-full-login")
    public Map<String, Object> testFullLogin(@RequestBody Map<String, String> loginRequest) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = loginRequest.get("email");
            String password = loginRequest.get("password");
            
            response.put("email", email);
            response.put("password", password);
            
            // Simular exactamente lo que hace AuthService.login()
            try {
                // 1. Verificar que el usuario existe
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));
                
                response.put("step1_userFound", true);
                response.put("step1_userActive", user.getActivo());
                
                // 2. Verificar que el usuario esté activo
                if (!user.getActivo()) {
                    response.put("step2_userActive", false);
                    response.put("error", "Usuario inactivo");
                    return response;
                }
                response.put("step2_userActive", true);
                
                // 3. Autenticar usuario
                org.springframework.security.authentication.UsernamePasswordAuthenticationToken token = 
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(email, password);
                
                org.springframework.security.core.Authentication authentication = authenticationManager.authenticate(token);
                response.put("step3_authenticationSuccess", true);
                
                // 4. Obtener UserDetails
                org.springframework.security.core.userdetails.UserDetails userDetails = 
                    (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();
                response.put("step4_userDetails", userDetails.getUsername());
                
                // 5. Simular generación de token (sin JwtService)
                response.put("step5_tokenSimulated", true);
                response.put("loginSuccess", true);
                
            } catch (Exception e) {
                response.put("error", e.getMessage());
                response.put("errorType", e.getClass().getSimpleName());
                response.put("loginSuccess", false);
            }
            
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("loginSuccess", false);
        }
        return response;
    }
    
    @PostMapping("/test-auth-service")
    public Map<String, Object> testAuthService(@RequestBody Map<String, String> loginRequest) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = loginRequest.get("email");
            String password = loginRequest.get("password");
            
            response.put("email", email);
            response.put("password", password);
            
            // Simular exactamente lo que hace AuthService.login() paso a paso
            try {
                // 1. Verificar que el usuario existe
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));
                
                response.put("step1_userFound", true);
                response.put("step1_userActive", user.getActivo());
                response.put("step1_userPassword", user.getPassword().substring(0, 10) + "...");
                
                // 2. Verificar que el usuario esté activo
                if (!user.getActivo()) {
                    response.put("step2_userActive", false);
                    response.put("error", "Usuario inactivo");
                    return response;
                }
                response.put("step2_userActive", true);
                
                // 3. Verificar contraseña manualmente
                boolean passwordMatches = passwordEncoder.matches(password, user.getPassword());
                response.put("step3_passwordMatches", passwordMatches);
                
                if (!passwordMatches) {
                    response.put("error", "Contraseña incorrecta");
                    response.put("loginSuccess", false);
                    return response;
                }
                
                // 4. Autenticar usuario con AuthenticationManager
                org.springframework.security.authentication.UsernamePasswordAuthenticationToken token = 
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(email, password);
                
                org.springframework.security.core.Authentication authentication = authenticationManager.authenticate(token);
                response.put("step4_authenticationSuccess", true);
                
                // 5. Obtener UserDetails
                org.springframework.security.core.userdetails.UserDetails userDetails = 
                    (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();
                response.put("step5_userDetails", userDetails.getUsername());
                response.put("step5_authorities", userDetails.getAuthorities().toString());
                
                // 6. Simular generación de token (sin JwtService)
                response.put("step6_tokenSimulated", true);
                response.put("loginSuccess", true);
                
            } catch (Exception e) {
                response.put("error", e.getMessage());
                response.put("errorType", e.getClass().getSimpleName());
                response.put("errorStackTrace", e.getStackTrace()[0].toString());
                response.put("loginSuccess", false);
            }
            
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("loginSuccess", false);
        }
        return response;
    }
    
    @PostMapping("/test-jwt-service")
    public Map<String, Object> testJwtService(@RequestBody Map<String, String> loginRequest) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = loginRequest.get("email");
            String password = loginRequest.get("password");
            
            response.put("email", email);
            response.put("password", password);
            
            try {
                // 1. Autenticar usuario
                org.springframework.security.authentication.UsernamePasswordAuthenticationToken token = 
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(email, password);
                
                org.springframework.security.core.Authentication authentication = authenticationManager.authenticate(token);
                response.put("step1_authenticationSuccess", true);
                
                // 2. Obtener UserDetails
                org.springframework.security.core.userdetails.UserDetails userDetails = 
                    (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();
                response.put("step2_userDetails", userDetails.getUsername());
                
                // 3. Generar token JWT
                String jwtToken = jwtService.generateToken(userDetails);
                response.put("step3_jwtTokenGenerated", true);
                response.put("step3_jwtToken", jwtToken.substring(0, 50) + "...");
                
                // 4. Verificar token JWT
                String extractedUsername = jwtService.extractUsername(jwtToken);
                response.put("step4_usernameExtracted", extractedUsername);
                response.put("step4_usernameMatches", extractedUsername.equals(email));
                
                // 5. Verificar si el token es válido
                boolean isTokenValid = jwtService.isTokenValid(jwtToken, userDetails);
                response.put("step5_tokenValid", isTokenValid);
                
                response.put("loginSuccess", true);
                
            } catch (Exception e) {
                response.put("error", e.getMessage());
                response.put("errorType", e.getClass().getSimpleName());
                response.put("errorStackTrace", e.getStackTrace()[0].toString());
                response.put("loginSuccess", false);
            }
            
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("loginSuccess", false);
        }
        return response;
    }
    
    @PostMapping("/test-auth-service-exact")
    public Map<String, Object> testAuthServiceExact(@RequestBody Map<String, String> loginRequest) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = loginRequest.get("email");
            String password = loginRequest.get("password");
            
            response.put("email", email);
            response.put("password", password);
            
            try {
                // Simular EXACTAMENTE lo que hace AuthService.login()
                
                // 1. Verificar que el usuario existe antes de autenticar
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));
                
                response.put("step1_userFound", true);
                response.put("step1_userActive", user.getActivo());
                response.put("step1_userPassword", user.getPassword().substring(0, 10) + "...");
                
                // 2. Verificar que el usuario esté activo
                if (!user.getActivo()) {
                    response.put("step2_userActive", false);
                    response.put("error", "Usuario inactivo");
                    return response;
                }
                response.put("step2_userActive", true);
                
                // 3. Autenticar usuario
                org.springframework.security.authentication.UsernamePasswordAuthenticationToken token = 
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(email, password);
                
                org.springframework.security.core.Authentication authentication = authenticationManager.authenticate(token);
                response.put("step3_authenticationSuccess", true);
                
                // 4. Obtener UserDetails
                org.springframework.security.core.userdetails.UserDetails userDetails = 
                    (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();
                response.put("step4_userDetails", userDetails.getUsername());
                
                // 5. Generar token JWT
                String jwtToken = jwtService.generateToken(userDetails);
                response.put("step5_jwtTokenGenerated", true);
                response.put("step5_jwtToken", jwtToken.substring(0, 50) + "...");
                
                // 6. Obtener tiempo de expiración
                long expirationTime = jwtService.getExpirationTimeInSeconds();
                response.put("step6_expirationTime", expirationTime);
                
                // 7. Simular LoginResponse
                response.put("step7_loginResponseSimulated", true);
                response.put("loginSuccess", true);
                
            } catch (Exception e) {
                response.put("error", e.getMessage());
                response.put("errorType", e.getClass().getSimpleName());
                response.put("errorStackTrace", e.getStackTrace()[0].toString());
                response.put("loginSuccess", false);
            }
            
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("loginSuccess", false);
        }
        return response;
    }
    
    @PostMapping("/test-auth-service-direct")
    public Map<String, Object> testAuthServiceDirect(@RequestBody Map<String, String> loginRequest) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = loginRequest.get("email");
            String password = loginRequest.get("password");
            
            response.put("email", email);
            response.put("password", password);
            
            try {
                // Probar el AuthService directamente
                response.put("step1_authServiceInjected", authService != null);
                
                // Crear LoginRequest
                com.agrocloud.dto.LoginRequest loginReq = new com.agrocloud.dto.LoginRequest();
                loginReq.setEmail(email);
                loginReq.setPassword(password);
                
                response.put("step2_loginRequestCreated", true);
                
                // Llamar al AuthService.login() directamente
                com.agrocloud.dto.LoginResponse loginResponse = authService.login(loginReq);
                
                response.put("step3_authServiceLoginSuccess", true);
                response.put("step3_token", loginResponse.getToken().substring(0, 50) + "...");
                response.put("step3_expiration", loginResponse.getExpiresIn());
                response.put("step3_userEmail", loginResponse.getUser().getEmail());
                
                response.put("loginSuccess", true);
                
            } catch (Exception e) {
                response.put("error", e.getMessage());
                response.put("errorType", e.getClass().getSimpleName());
                response.put("errorStackTrace", e.getStackTrace()[0].toString());
                response.put("loginSuccess", false);
            }
            
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("loginSuccess", false);
        }
        return response;
    }
    
    @PostMapping("/login-working")
    public ResponseEntity<Map<String, Object>> loginWorking(@RequestBody Map<String, String> loginRequest) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = loginRequest.get("email");
            String password = loginRequest.get("password");
            
            // Crear LoginRequest
            com.agrocloud.dto.LoginRequest loginReq = new com.agrocloud.dto.LoginRequest();
            loginReq.setEmail(email);
            loginReq.setPassword(password);
            
            // Llamar al AuthService.login() directamente
            com.agrocloud.dto.LoginResponse loginResponse = authService.login(loginReq);
            
            // Construir respuesta
            Map<String, Object> loginData = new HashMap<>();
            loginData.put("token", loginResponse.getToken());
            loginData.put("expirationTime", loginResponse.getExpiresIn());
            loginData.put("user", loginResponse.getUser());
            
            response.put("success", true);
            response.put("data", loginData);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @GetMapping("/admin-roles")
    public ResponseEntity<Map<String, Object>> testAdminRoles(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (authentication == null) {
                response.put("error", "No hay autenticación");
                response.put("authentication", "NULL");
                response.put("debug", "El objeto Authentication es null");
                return ResponseEntity.ok(response);
            }

            String username = authentication.getName();
            response.put("username", username);
            response.put("authentication", "PRESENTE");
            response.put("authenticationType", authentication.getClass().getSimpleName());
            response.put("authorities", authentication.getAuthorities().toString());
            
            // Buscar usuario por username
            User user = userService.findByEmailWithAllRelations(username);
            if (user == null) {
                response.put("error", "Usuario no encontrado");
                return ResponseEntity.ok(response);
            }

            response.put("userId", user.getId());
            response.put("firstName", user.getFirstName());
            response.put("lastName", user.getLastName());
            response.put("email", user.getEmail());
            response.put("activo", user.getActivo());
            
            // Verificar roles
            if (user.getRoles() == null) {
                response.put("roles", "NULL - No hay roles cargados");
                response.put("rolesCount", 0);
            } else {
                response.put("roles", user.getRoles().stream()
                    .map(role -> role.getNombre())
                    .toList());
                response.put("rolesCount", user.getRoles().size());
            }

            // Verificar si es admin
            boolean esAdmin = user.getRoles() != null && 
                user.getRoles().stream()
                    .anyMatch(role -> "ADMINISTRADOR".equals(role.getNombre()) || "SUPERADMIN".equals(role.getNombre()));
            
            response.put("esAdmin", esAdmin);
            response.put("success", true);

        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("success", false);
            e.printStackTrace();
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/test-simple")
    public ResponseEntity<Map<String, Object>> testSimple() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Endpoint de prueba funcionando");
        response.put("timestamp", java.time.LocalDateTime.now());
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
}
