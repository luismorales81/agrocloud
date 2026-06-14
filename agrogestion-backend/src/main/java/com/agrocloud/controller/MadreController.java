package com.agrocloud.controller;

import com.agrocloud.porcinos.domain.Madre;
import com.agrocloud.core.domain.User;
import com.agrocloud.porcinos.application.MadreService;
import com.agrocloud.core.application.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/porcinos/madres")
public class MadreController {

    @Autowired
    private MadreService madreService;

    @Autowired
    private UserService userService;

    private User obtenerUsuario(UserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }
        return userService.findByEmailWithAllRelations(userDetails.getUsername());
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<Madre>> getAllMadres(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String estado) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            
            List<Madre> madres;
            if (estado != null && !estado.isEmpty()) {
                try {
                    Madre.EstadoMadre estadoEnum = Madre.EstadoMadre.valueOf(estado.toUpperCase());
                    madres = madreService.getMadresByUserAndEstado(user, estadoEnum);
                } catch (IllegalArgumentException e) {
                    madres = madreService.getMadresByUser(user);
                }
            } else {
                madres = madreService.getMadresByUser(user);
            }
            
            return ResponseEntity.ok(madres);
        } catch (Exception e) {
            System.err.println("Error al obtener madres: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<Madre> getMadreById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            
            Optional<Madre> madre = madreService.getMadreById(id, user);
            return madre.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            System.err.println("Error al obtener madre: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> crearMadre(
            @RequestBody Madre madre,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Usuario no autenticado"));
            }
            
            Madre nuevaMadre = madreService.crearMadre(madre, user);
            return ResponseEntity.ok(nuevaMadre);
        } catch (IllegalArgumentException e) {
            // Errores de validaciÃ³n de negocio
            System.err.println("Error de validaciÃ³n al crear madre: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("Error al crear madre: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Error interno al crear la madre"));
        }
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<Madre> actualizarMadre(
            @PathVariable Long id,
            @RequestBody Madre madre,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            
            Madre madreActualizada = madreService.actualizarMadre(id, madre, user);
            return ResponseEntity.ok(madreActualizada);
        } catch (Exception e) {
            System.err.println("Error al actualizar madre: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMadre(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = obtenerUsuario(userDetails);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            
            madreService.eliminarMadre(id, user);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            System.err.println("Error al eliminar madre: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}





