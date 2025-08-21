package com.agrocloud.controller;

import com.agrocloud.model.entity.Field;
import com.agrocloud.model.entity.User;
import com.agrocloud.service.FieldService;
import com.agrocloud.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/campos")
@CrossOrigin(origins = "*")
public class FieldController {

    @Autowired
    private FieldService fieldService;

    @Autowired
    private UserService userService;

    // Obtener todos los campos accesibles por el usuario
    @GetMapping
    public ResponseEntity<List<Field>> getAllFields(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        List<Field> fields = fieldService.getFieldsByUser(user);
        return ResponseEntity.ok(fields);
    }

    // Obtener campo por ID
    @GetMapping("/{id}")
    public ResponseEntity<Field> getFieldById(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        Optional<Field> field = fieldService.getFieldById(id, user);
        
        return field.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Crear nuevo campo
    @PostMapping
    public ResponseEntity<Field> createField(@RequestBody Field field, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        Field createdField = fieldService.createField(field, user);
        return ResponseEntity.ok(createdField);
    }

    // Actualizar campo
    @PutMapping("/{id}")
    public ResponseEntity<Field> updateField(@PathVariable Long id, @RequestBody Field field, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        Optional<Field> updatedField = fieldService.updateField(id, field, user);
        
        return updatedField.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Eliminar campo
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteField(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        boolean deleted = fieldService.deleteField(id, user);
        
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Buscar campo por nombre
    @GetMapping("/search")
    public ResponseEntity<List<Field>> searchField(@RequestParam String nombre, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        List<Field> fields = fieldService.searchFieldByName(nombre, user);
        return ResponseEntity.ok(fields);
    }

    // Obtener estadísticas de campos
    @GetMapping("/stats")
    public ResponseEntity<FieldService.FieldStats> getFieldStats(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        FieldService.FieldStats stats = fieldService.getFieldStats(user);
        return ResponseEntity.ok(stats);
    }

    // Obtener campos por estado
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<Field>> getFieldsByEstado(@PathVariable String estado, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        List<Field> fields = fieldService.getFieldsByUser(user).stream()
                .filter(f -> estado.equals(f.getEstado()))
                .toList();
        return ResponseEntity.ok(fields);
    }
}
