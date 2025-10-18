package com.agrocloud.controller;

import com.agrocloud.model.entity.Maquinaria;
import com.agrocloud.model.entity.User;
import com.agrocloud.service.MaquinariaService;
import com.agrocloud.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/maquinaria")
@CrossOrigin(origins = "*")
public class MaquinariaController {

    @Autowired
    private MaquinariaService maquinariaService;

    @Autowired
    private UserService userService;

    // Obtener todas las maquinarias accesibles por el usuario
    @GetMapping
    public ResponseEntity<List<Maquinaria>> getAllMaquinarias(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            System.out.println("[MAQUINARIA_CONTROLLER] Iniciando getAllMaquinarias para usuario: " + (userDetails != null ? userDetails.getUsername() : "null"));
            
            if (userDetails == null) {
                System.err.println("[MAQUINARIA_CONTROLLER] ERROR: UserDetails es null");
                return ResponseEntity.status(401).build();
            }
            
            User user = userService.findByEmailWithRelations(userDetails.getUsername());
            if (user == null) {
                System.err.println("[MAQUINARIA_CONTROLLER] ERROR: Usuario no encontrado: " + userDetails.getUsername());
                return ResponseEntity.status(404).build();
            }
            
            System.out.println("[MAQUINARIA_CONTROLLER] Usuario encontrado: " + user.getEmail() + ", esAdmin: " + user.isAdmin());
            System.out.println("[MAQUINARIA_CONTROLLER] Roles del usuario: " + user.getRoles().stream().map(r -> r.getNombre()).toList());
            
            List<Maquinaria> maquinarias = maquinariaService.getMaquinariasByUser(user);
            System.out.println("[MAQUINARIA_CONTROLLER] Maquinarias encontradas: " + maquinarias.size());
            
            return ResponseEntity.ok(maquinarias);
        } catch (Exception e) {
            System.err.println("[MAQUINARIA_CONTROLLER] ERROR en getAllMaquinarias: " + e.getMessage());
            System.err.println("[MAQUINARIA_CONTROLLER] Stack trace completo:");
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // Obtener maquinaria por ID
    @GetMapping("/{id}")
    public ResponseEntity<Maquinaria> getMaquinariaById(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmailWithRelations(userDetails.getUsername());
        Optional<Maquinaria> maquinaria = maquinariaService.getMaquinariaById(id, user);
        
        return maquinaria.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Crear nueva maquinaria
    @PostMapping
    public ResponseEntity<Maquinaria> createMaquinaria(@RequestBody Maquinaria maquinaria, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmailWithRelations(userDetails.getUsername());
        Maquinaria createdMaquinaria = maquinariaService.createMaquinaria(maquinaria, user);
        return ResponseEntity.ok(createdMaquinaria);
    }

    // Actualizar maquinaria
    @PutMapping("/{id}")
    public ResponseEntity<Maquinaria> updateMaquinaria(@PathVariable Long id, @RequestBody Maquinaria maquinaria, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmailWithRelations(userDetails.getUsername());
        Optional<Maquinaria> updatedMaquinaria = maquinariaService.updateMaquinaria(id, maquinaria, user);
        
        return updatedMaquinaria.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Eliminar maquinaria
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMaquinaria(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmailWithRelations(userDetails.getUsername());
        boolean deleted = maquinariaService.deleteMaquinaria(id, user);
        
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Buscar maquinaria por nombre
    @GetMapping("/search")
    public ResponseEntity<List<Maquinaria>> searchMaquinaria(@RequestParam String nombre, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmailWithRelations(userDetails.getUsername());
        List<Maquinaria> maquinarias = maquinariaService.searchMaquinariaByName(nombre, user);
        return ResponseEntity.ok(maquinarias);
    }

    // Obtener estadísticas de maquinaria
    @GetMapping("/stats")
    public ResponseEntity<MaquinariaService.MaquinariaStats> getMaquinariaStats(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmailWithRelations(userDetails.getUsername());
        MaquinariaService.MaquinariaStats stats = maquinariaService.getMaquinariaStats(user);
        return ResponseEntity.ok(stats);
    }



    // Obtener maquinarias por estado
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<Maquinaria>> getMaquinariasByEstado(@PathVariable Maquinaria.EstadoMaquinaria estado, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmailWithRelations(userDetails.getUsername());
        List<Maquinaria> maquinarias = maquinariaService.getMaquinariasByUser(user).stream()
                .filter(m -> estado.equals(m.getEstado()))
                .toList();
        return ResponseEntity.ok(maquinarias);
    }
}
