package com.agrocloud.controller;

import com.agrocloud.model.entity.Plot;
import com.agrocloud.model.entity.User;
import com.agrocloud.service.PlotService;
import com.agrocloud.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/lotes")
@CrossOrigin(origins = "*")
public class PlotController {

    @Autowired
    private PlotService plotService;

    @Autowired
    private UserService userService;

    // Obtener todos los lotes accesibles por el usuario
    @GetMapping
    public ResponseEntity<List<Plot>> getAllLotes(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            List<Plot> lotes = plotService.getLotesByUser(user);
            return ResponseEntity.ok(lotes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Obtener lote por ID
    @GetMapping("/{id}")
    public ResponseEntity<Plot> getLoteById(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmail(userDetails.getUsername());
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
            User user = userService.findByEmail(userDetails.getUsername());
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
            User user = userService.findByEmail(userDetails.getUsername());
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
            User user = userService.findByEmail(userDetails.getUsername());
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
    
    // Eliminar lote físicamente (solo administradores)
    @DeleteMapping("/{id}/fisico")
    public ResponseEntity<Void> deleteLoteFisicamente(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            boolean deleted = plotService.deleteLoteFisicamente(id, user);
            
            if (deleted) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Obtener todos los lotes (incluyendo inactivos) - solo administradores
    @GetMapping("/todos")
    public ResponseEntity<List<Plot>> getAllLotesIncludingInactive(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            List<Plot> lotes = plotService.getAllLotesIncludingInactive(user);
            return ResponseEntity.ok(lotes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Buscar lotes por nombre
    @GetMapping("/buscar")
    public ResponseEntity<List<Plot>> searchLotes(@RequestParam String nombre, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            List<Plot> lotes = plotService.searchLotesByNombre(nombre, user);
            return ResponseEntity.ok(lotes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
