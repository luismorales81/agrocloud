package com.agrocloud.controller;

import com.agrocloud.model.entity.Plot;
import com.agrocloud.service.PlotService;
import com.agrocloud.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controlador de compatibilidad para /api/lotes
 * Redirige las peticiones al controlador principal /api/v1/lotes
 */
@RestController
@RequestMapping("/api/lotes")
@CrossOrigin(origins = "*")
public class LoteController {

    @Autowired
    private PlotService plotService;

    @Autowired
    private UserService userService;

    // Obtener todos los lotes accesibles por el usuario
    @GetMapping
    public ResponseEntity<List<Plot>> getAllLotes(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            System.out.println("[LOTE_CONTROLLER] Iniciando getAllLotes para usuario: " + (userDetails != null ? userDetails.getUsername() : "null"));
            
            if (userDetails == null) {
                System.err.println("[LOTE_CONTROLLER] ERROR: UserDetails es null");
                return ResponseEntity.status(401).build();
            }
            
            com.agrocloud.model.entity.User user = userService.findByEmailWithRelations(userDetails.getUsername());
            if (user == null) {
                System.err.println("[LOTE_CONTROLLER] ERROR: Usuario no encontrado: " + userDetails.getUsername());
                return ResponseEntity.status(404).build();
            }
            
            System.out.println("[LOTE_CONTROLLER] Usuario encontrado: " + user.getEmail() + ", roles: " + user.getRoles().size());
            
            List<Plot> lotes = plotService.getLotesByUser(user);
            System.out.println("[LOTE_CONTROLLER] Lotes obtenidos: " + (lotes != null ? lotes.size() : "null"));
            
            return ResponseEntity.ok(lotes);
        } catch (Exception e) {
            System.err.println("[LOTE_CONTROLLER] ERROR en getAllLotes: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // Obtener lote por ID
    @GetMapping("/{id}")
    public ResponseEntity<Plot> getLoteById(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            com.agrocloud.model.entity.User user = userService.findByEmailWithRelations(userDetails.getUsername());
            Optional<Plot> lote = plotService.getLoteById(id, user);
            
            return lote.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Crear nuevo lote
    @PostMapping
    public ResponseEntity<Plot> createLote(@RequestBody Plot lote, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            com.agrocloud.model.entity.User user = userService.findByEmailWithRelations(userDetails.getUsername());
            lote.setUser(user);
            Plot savedLote = plotService.saveLote(lote);
            return ResponseEntity.ok(savedLote);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Actualizar lote existente
    @PutMapping("/{id}")
    public ResponseEntity<Plot> updateLote(@PathVariable Long id, @RequestBody Plot lote, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            com.agrocloud.model.entity.User user = userService.findByEmailWithRelations(userDetails.getUsername());
            Optional<Plot> existingLote = plotService.getLoteById(id, user);
            
            if (existingLote.isPresent()) {
                lote.setId(id);
                lote.setUser(user);
                Plot updatedLote = plotService.saveLote(lote);
                return ResponseEntity.ok(updatedLote);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Eliminar lote lógicamente
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLote(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            com.agrocloud.model.entity.User user = userService.findByEmailWithRelations(userDetails.getUsername());
            boolean deleted = plotService.deleteLote(id, user);
            
            if (deleted) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
