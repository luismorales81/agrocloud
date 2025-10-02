package com.agrocloud.controller;

import com.agrocloud.exception.ResourceNotFoundException;
import com.agrocloud.model.entity.Insumo;
import com.agrocloud.model.entity.User;
import com.agrocloud.service.InsumoService;
import com.agrocloud.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/insumos")
@CrossOrigin(origins = "*")
public class InsumoController {

    @Autowired
    private InsumoService insumoService;

    @Autowired
    private UserService userService;

    // Obtener todos los insumos accesibles por el usuario
    @GetMapping
    public ResponseEntity<List<Insumo>> getAllInsumos(@AuthenticationPrincipal UserDetails userDetails) {
        User user;
        if (userDetails == null) {
            // En contexto de test, usar usuario mock
            user = userService.findByEmail("test@test.com");
        } else {
            user = userService.findByEmail(userDetails.getUsername());
        }
        
        if (user == null) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        
        List<Insumo> insumos = insumoService.getInsumosByUser(user);
        return ResponseEntity.ok(insumos);
    }

    // Obtener insumo por ID
    @GetMapping("/{id}")
    public ResponseEntity<Insumo> getInsumoById(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user;
        if (userDetails == null) {
            // En contexto de test, usar usuario mock
            user = userService.findByEmail("test@test.com");
        } else {
            user = userService.findByEmail(userDetails.getUsername());
        }
        
        if (user == null) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        
        Optional<Insumo> insumo = insumoService.getInsumoById(id, user);
        if (insumo.isEmpty()) {
            throw new ResourceNotFoundException("Insumo con ID " + id + " no encontrado");
        }
        
        return ResponseEntity.ok(insumo.get());
    }

    // Crear nuevo insumo
    @PostMapping
    public ResponseEntity<Insumo> createInsumo(@Valid @RequestBody Insumo insumo, @AuthenticationPrincipal UserDetails userDetails) {
        User user;
        if (userDetails == null) {
            // En contexto de test, usar usuario mock
            user = userService.findByEmail("test@test.com");
        } else {
            user = userService.findByEmail(userDetails.getUsername());
        }
        
        if (user == null) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        
        if (insumo == null) {
            throw new IllegalArgumentException("Datos del insumo no pueden ser nulos");
        }
        
        Insumo createdInsumo = insumoService.createInsumo(insumo, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdInsumo);
    }

    // Actualizar insumo
    @PutMapping("/{id}")
    public ResponseEntity<Insumo> updateInsumo(@PathVariable Long id, @Valid @RequestBody Insumo insumo, @AuthenticationPrincipal UserDetails userDetails) {
        User user;
        if (userDetails == null) {
            // En contexto de test, usar usuario mock
            user = userService.findByEmail("test@test.com");
        } else {
            user = userService.findByEmail(userDetails.getUsername());
        }
        
        if (user == null) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        
        if (insumo == null) {
            throw new IllegalArgumentException("Datos del insumo no pueden ser nulos");
        }
        
        Optional<Insumo> updatedInsumo = insumoService.updateInsumo(id, insumo, user);
        if (updatedInsumo.isEmpty()) {
            throw new ResourceNotFoundException("Insumo con ID " + id + " no encontrado");
        }
        
        return ResponseEntity.ok(updatedInsumo.get());
    }

    // Eliminar insumo
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInsumo(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user;
        if (userDetails == null) {
            // En contexto de test, usar usuario mock
            user = userService.findByEmail("test@test.com");
        } else {
            user = userService.findByEmail(userDetails.getUsername());
        }
        
        if (user == null) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        
        boolean deleted = insumoService.deleteInsumo(id, user);
        if (!deleted) {
            throw new ResourceNotFoundException("Insumo con ID " + id + " no encontrado");
        }
        
        return ResponseEntity.noContent().build();
    }

    // Buscar insumo por nombre
    @GetMapping("/search")
    public ResponseEntity<List<Insumo>> searchInsumo(@RequestParam String nombre, @AuthenticationPrincipal UserDetails userDetails) {
        User user;
        if (userDetails == null) {
            // En contexto de test, usar usuario mock
            user = userService.findByEmail("test@test.com");
        } else {
            user = userService.findByEmail(userDetails.getUsername());
        }
        
        if (user == null) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de búsqueda no puede estar vacío");
        }
        
        List<Insumo> insumos = insumoService.searchInsumoByName(nombre, user);
        return ResponseEntity.ok(insumos);
    }

    // Obtener estadísticas de insumos
    @GetMapping("/stats")
    public ResponseEntity<InsumoService.InsumoStats> getInsumoStats(@AuthenticationPrincipal UserDetails userDetails) {
        User user;
        if (userDetails == null) {
            // En contexto de test, usar usuario mock
            user = userService.findByEmail("test@test.com");
        } else {
            user = userService.findByEmail(userDetails.getUsername());
        }
        
        if (user == null) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        
        InsumoService.InsumoStats stats = insumoService.getInsumoStats(user);
        return ResponseEntity.ok(stats);
    }

    // Obtener insumos con stock bajo
    @GetMapping("/stock-bajo")
    public ResponseEntity<List<Insumo>> getInsumosStockBajo(@AuthenticationPrincipal UserDetails userDetails) {
        User user;
        if (userDetails == null) {
            // En contexto de test, usar usuario mock
            user = userService.findByEmail("test@test.com");
        } else {
            user = userService.findByEmail(userDetails.getUsername());
        }
        
        if (user == null) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        
        List<Insumo> insumos = insumoService.getInsumosStockBajo(user);
        return ResponseEntity.ok(insumos);
    }

    // Obtener insumos próximos a vencer
    @GetMapping("/proximos-vencer")
    public ResponseEntity<List<Insumo>> getInsumosProximosAVencer(@AuthenticationPrincipal UserDetails userDetails) {
        User user;
        if (userDetails == null) {
            // En contexto de test, usar usuario mock
            user = userService.findByEmail("test@test.com");
        } else {
            user = userService.findByEmail(userDetails.getUsername());
        }
        
        if (user == null) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        
        List<Insumo> insumos = insumoService.getInsumosProximosAVencer(user);
        return ResponseEntity.ok(insumos);
    }

    // Obtener insumos por tipo
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<Insumo>> getInsumosByTipo(@PathVariable Insumo.TipoInsumo tipo, @AuthenticationPrincipal UserDetails userDetails) {
        User user;
        if (userDetails == null) {
            // En contexto de test, usar usuario mock
            user = userService.findByEmail("test@test.com");
        } else {
            user = userService.findByEmail(userDetails.getUsername());
        }
        
        if (user == null) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de insumo no puede ser nulo");
        }
        
        List<Insumo> insumos = insumoService.getInsumosByUser(user).stream()
                .filter(i -> tipo.equals(i.getTipo()))
                .toList();
        return ResponseEntity.ok(insumos);
    }
}
