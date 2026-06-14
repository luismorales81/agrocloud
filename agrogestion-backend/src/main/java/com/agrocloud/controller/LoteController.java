package com.agrocloud.controller;

import com.agrocloud.cultivos.domain.Plot;
import com.agrocloud.cultivos.application.PlotService;
import com.agrocloud.core.application.UserService;
import com.agrocloud.core.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controlador de compatibilidad para /api/lotes (tabla cultivo_lotes).
 */
@RestController
@RequestMapping("/api/lotes")
public class LoteController {

    @Autowired
    @Qualifier("plotServicioCultivos")
    private PlotService plotService;

    @Autowired
    @Qualifier("userServiceCore")
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<Plot>> getAllLotes(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(401).build();
            }
            User user = userService.findByEmailWithAllRelations(userDetails.getUsername());
            if (user == null) {
                return ResponseEntity.status(404).build();
            }
            return ResponseEntity.ok(plotService.getLotesByUser(user));
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Plot> getLoteById(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmailWithAllRelations(userDetails.getUsername());
            Optional<Plot> lote = plotService.getLoteById(id, user);
            return lote.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<Plot> createLote(@RequestBody Plot lote, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmailWithAllRelations(userDetails.getUsername());
            lote.setUser(user);
            return ResponseEntity.ok(plotService.saveLote(lote));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Plot> updateLote(@PathVariable Long id, @RequestBody Plot lote, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmailWithAllRelations(userDetails.getUsername());
            Optional<Plot> updated = plotService.updateLote(id, lote, user);
            return updated.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLote(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmailWithAllRelations(userDetails.getUsername());
            return plotService.deleteLote(id, user) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
