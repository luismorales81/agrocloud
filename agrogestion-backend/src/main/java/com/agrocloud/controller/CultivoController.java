package com.agrocloud.controller;

import com.agrocloud.model.entity.Cultivo;
import com.agrocloud.model.entity.User;
import com.agrocloud.service.CultivoService;
import com.agrocloud.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cultivos")
@CrossOrigin(origins = "*")
public class CultivoController {

    @Autowired
    private CultivoService cultivoService;

    @Autowired
    private UserService userService;

    // Obtener todos los cultivos accesibles por el usuario
    @GetMapping
    public ResponseEntity<List<Cultivo>> getAllCultivos(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            List<Cultivo> cultivos = cultivoService.getCultivosByUser(user);
            return ResponseEntity.ok(cultivos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Obtener cultivo por ID
    @GetMapping("/{id}")
    public ResponseEntity<Cultivo> getCultivoById(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            Optional<Cultivo> cultivo = cultivoService.getCultivoById(id, user);
            
            return cultivo.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Crear nuevo cultivo
    @PostMapping
    public ResponseEntity<Cultivo> createCultivo(@RequestBody Cultivo cultivo, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            cultivo.setUsuario(user);
            
            // Establecer la empresa del usuario si no está establecida
            if (cultivo.getEmpresa() == null && !user.getUserCompanyRoles().isEmpty()) {
                // Obtener la primera empresa del usuario
                cultivo.setEmpresa(user.getUserCompanyRoles().get(0).getEmpresa());
            }
            
            Cultivo savedCultivo = cultivoService.saveCultivo(cultivo);
            return ResponseEntity.ok(savedCultivo);
        } catch (Exception e) {
            e.printStackTrace(); // Agregar logging para debug
            return ResponseEntity.internalServerError().build();
        }
    }

    // Actualizar cultivo existente
    @PutMapping("/{id}")
    public ResponseEntity<Cultivo> updateCultivo(@PathVariable Long id, @RequestBody Cultivo cultivo, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            Optional<Cultivo> existingCultivo = cultivoService.getCultivoById(id, user);
            
            if (existingCultivo.isPresent()) {
                cultivo.setId(id);
                cultivo.setUsuario(user);
                Cultivo updatedCultivo = cultivoService.saveCultivo(cultivo);
                return ResponseEntity.ok(updatedCultivo);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Eliminar cultivo lógicamente
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCultivo(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            boolean eliminado = cultivoService.deleteCultivo(id, user);
            
            if (eliminado) {
                return ResponseEntity.ok("Cultivo eliminado exitosamente (eliminación lógica)");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Eliminar cultivo físicamente (solo administradores)
    @DeleteMapping("/{id}/fisico")
    public ResponseEntity<String> deleteCultivoFisicamente(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            boolean eliminado = cultivoService.deleteCultivoFisicamente(id, user);
            
            if (eliminado) {
                return ResponseEntity.ok("Cultivo eliminado permanentemente");
            } else {
                return ResponseEntity.badRequest().body("No se pudo eliminar el cultivo o no tiene permisos");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Buscar cultivos por nombre
    @GetMapping("/buscar")
    public ResponseEntity<List<Cultivo>> searchCultivos(@RequestParam String nombre, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            List<Cultivo> cultivos = cultivoService.searchCultivosByNombre(nombre, user);
            return ResponseEntity.ok(cultivos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Obtener cultivos eliminados lógicamente (solo administradores)
    @GetMapping("/eliminados")
    public ResponseEntity<List<Cultivo>> getCultivosEliminados(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            List<Cultivo> cultivos = cultivoService.getCultivosEliminados(user);
            return ResponseEntity.ok(cultivos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Restaurar cultivo eliminado lógicamente (solo administradores)
    @PutMapping("/{id}/restaurar")
    public ResponseEntity<String> restaurarCultivo(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            boolean restaurado = cultivoService.restaurarCultivo(id, user);
            
            if (restaurado) {
                return ResponseEntity.ok("Cultivo restaurado exitosamente");
            } else {
                return ResponseEntity.badRequest().body("No se pudo restaurar el cultivo o no tiene permisos");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
