package com.agrocloud.controller;

import com.agrocloud.dto.CreateUserRequest;
import com.agrocloud.dto.LoginRequest;
import com.agrocloud.dto.LoginResponse;
import com.agrocloud.dto.UserDto;
import com.agrocloud.dto.PasswordResetRequest;
import com.agrocloud.dto.PasswordResetConfirm;
import com.agrocloud.dto.ChangePasswordRequest;
import com.agrocloud.service.AuthService;
import com.agrocloud.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Endpoints para autenticación y gestión de usuarios")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:5173", "http://127.0.0.1:3000", "http://127.0.0.1:3001", "http://127.0.0.1:5173"})
public class AuthController {
    
    private AuthService authService;
    
    private RoleService roleService;
    
    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autenticar usuario y obtener token JWT")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            System.out.println("🔧 [AuthController] Intentando login para: " + loginRequest.getEmail());
            LoginResponse response = authService.login(loginRequest);
            System.out.println("✅ [AuthController] Login exitoso para: " + loginRequest.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ [AuthController] Error en login para " + loginRequest.getEmail() + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(null);
        }
    }
    
    @PostMapping("/register")
    @Operation(summary = "Registrar usuario", description = "Crear nuevo usuario (solo administradores)")
    @PreAuthorize("hasAuthority('USER_CREATE')")
    public ResponseEntity<UserDto> register(@Valid @RequestBody CreateUserRequest request) {
        try {
            UserDto user = authService.createUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/users")
    @Operation(summary = "Obtener usuarios", description = "Listar todos los usuarios (solo administradores)")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        try {
            List<UserDto> users = authService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/users/filter")
    @Operation(summary = "Filtrar usuarios", description = "Buscar usuarios con filtros")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<List<UserDto>> getUsersWithFilters(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) Boolean active) {
        try {
            List<UserDto> users = authService.getUsersWithFilters(name, email, roleName, active);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/users/{id}")
    @Operation(summary = "Obtener usuario por ID", description = "Obtener información de un usuario específico")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        try {
            UserDto user = authService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/users/{id}")
    @Operation(summary = "Actualizar usuario", description = "Actualizar información de un usuario")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @Valid @RequestBody CreateUserRequest request) {
        try {
            UserDto user = authService.updateUser(id, request);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PatchMapping("/users/{id}/toggle-status")
    @Operation(summary = "Activar/desactivar usuario", description = "Cambiar estado activo/inactivo de un usuario")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<UserDto> toggleUserStatus(@PathVariable Long id) {
        try {
            UserDto user = authService.toggleUserStatus(id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/users/{id}")
    @Operation(summary = "Eliminar usuario", description = "Eliminar un usuario del sistema")
    @PreAuthorize("hasAuthority('USER_DELETE')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            authService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/roles")
    @Operation(summary = "Obtener roles", description = "Listar todos los roles disponibles")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ResponseEntity<List<Map<String, Object>>> getAllRoles() {
        try {
            List<Map<String, Object>> roles = roleService.getAllRoles().stream()
                    .map(role -> {
                        Map<String, Object> roleMap = new java.util.HashMap<>();
                        roleMap.put("id", role.getId());
                        roleMap.put("name", role.getName());
                        roleMap.put("description", role.getDescription());
                        roleMap.put("permissions", new java.util.ArrayList<>());
                        roleMap.put("userCount", 0);
                        return roleMap;
                    })
                    .toList();
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/permissions")
    @Operation(summary = "Obtener permisos", description = "Listar todos los permisos disponibles")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ResponseEntity<List<String>> getAvailablePermissions() {
        try {
            List<String> permissions = roleService.getAvailablePermissions();
            return ResponseEntity.ok(permissions);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/stats")
    @Operation(summary = "Estadísticas", description = "Obtener estadísticas de usuarios y roles")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<Map<String, Object>> getStats() {
        try {
            Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("users", authService.getUserStats());
            stats.put("roles", roleService.getRoleStats());
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/verify-email")
    @Operation(summary = "Verificar email", description = "Verificar email con token")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam String token) {
        try {
            boolean verified = authService.verifyEmail(token);
            if (verified) {
                return ResponseEntity.ok(Map.of("message", "Email verificado correctamente"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Token inválido"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/request-password-reset")
    @Operation(summary = "Solicitar reset de contraseña", description = "Solicitar token para resetear contraseña")
    public ResponseEntity<Map<String, String>> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        try {
            authService.requestPasswordReset(request.getEmail());
            return ResponseEntity.ok(Map.of("message", "Se ha enviado un email con las instrucciones para recuperar tu contraseña"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/reset-password")
    @Operation(summary = "Resetear contraseña", description = "Resetear contraseña con token")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody PasswordResetConfirm request) {
        try {
            authService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Contraseña actualizada correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/change-password")
    @Operation(summary = "Cambiar contraseña", description = "Cambiar contraseña desde el perfil del usuario")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Verificar que las contraseñas coincidan
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Las contraseñas no coinciden"));
            }
            
            authService.changePassword(userDetails.getUsername(), request.getCurrentPassword(), request.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Contraseña cambiada correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
