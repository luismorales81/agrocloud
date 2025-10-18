package com.agrocloud.controller;

import com.agrocloud.dto.FieldDTO;
import com.agrocloud.model.entity.Field;
import com.agrocloud.model.entity.User;
import com.agrocloud.service.FieldService;
import com.agrocloud.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controlador alias para /api/fields que redirige a la funcionalidad de campos
 * Mantiene compatibilidad con endpoints en inglés
 */
@RestController
@RequestMapping("/api/fields")
@CrossOrigin(origins = "*")
public class FieldAliasController {

    @Autowired
    private FieldService fieldService;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    // Métodos de conversión (copiados de FieldController)
    private FieldDTO convertToDTO(Field field) {
        FieldDTO dto = new FieldDTO();
        dto.setId(field.getId());
        dto.setNombre(field.getNombre());
        dto.setDescripcion(field.getDescripcion());
        dto.setUbicacion(field.getUbicacion());
        dto.setAreaHectareas(field.getAreaHectareas());
        dto.setEstado(field.getEstado());
        dto.setActivo(field.getActivo());
        dto.setPoligono(field.getPoligono());
        dto.setFechaCreacion(field.getFechaCreacion());
        dto.setFechaActualizacion(field.getFechaActualizacion());

        // Convertir coordenadas JSON a lista de DTOs
        if (field.getCoordenadas() != null && !field.getCoordenadas().isEmpty()) {
            try {
                List<FieldDTO.CoordinateDTO> coordinates = objectMapper.readValue(
                    field.getCoordenadas(), 
                    new TypeReference<List<FieldDTO.CoordinateDTO>>() {}
                );
                dto.setCoordenadas(coordinates);
            } catch (JsonProcessingException e) {
                // Si hay error en el parsing, dejar coordenadas como null
            }
        }

        return dto;
    }

    // Obtener todos los campos accesibles por el usuario
    @GetMapping
    public ResponseEntity<List<FieldDTO>> getAllFields(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmailWithAllRelations(userDetails.getUsername());
            List<Field> fields = fieldService.getFieldsByUser(user);
            List<FieldDTO> dtos = fields.stream().map(this::convertToDTO).toList();
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            System.err.println("Error en getAllFields (alias): " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    // Obtener campo por ID
    @GetMapping("/{id}")
    public ResponseEntity<FieldDTO> getFieldById(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmailWithAllRelations(userDetails.getUsername());
            Optional<Field> field = fieldService.getFieldById(id, user);
            
            return field.map(this::convertToDTO)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            System.err.println("Error en getFieldById (alias): " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    // Crear nuevo campo
    @PostMapping
    public ResponseEntity<FieldDTO> createField(@RequestBody FieldDTO fieldDTO, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmailWithAllRelations(userDetails.getUsername());
            Field field = convertToEntity(fieldDTO);
            Field createdField = fieldService.createField(field, user);
            return ResponseEntity.ok(convertToDTO(createdField));
        } catch (Exception e) {
            System.err.println("Error en createField (alias): " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    // Actualizar campo
    @PutMapping("/{id}")
    public ResponseEntity<FieldDTO> updateField(@PathVariable Long id, @RequestBody FieldDTO fieldDTO, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmailWithAllRelations(userDetails.getUsername());
            Field field = convertToEntity(fieldDTO);
            Optional<Field> updatedField = fieldService.updateField(id, field, user);
            
            return updatedField.map(this::convertToDTO)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            System.err.println("Error en updateField (alias): " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    // Eliminar campo
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteField(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmailWithAllRelations(userDetails.getUsername());
            boolean deleted = fieldService.deleteField(id, user);
            
            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Error en deleteField (alias): " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // Buscar campo por nombre
    @GetMapping("/search")
    public ResponseEntity<List<FieldDTO>> searchField(@RequestParam String nombre, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmailWithAllRelations(userDetails.getUsername());
            List<Field> fields = fieldService.searchFieldByName(nombre, user);
            List<FieldDTO> dtos = fields.stream().map(this::convertToDTO).toList();
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            System.err.println("Error en searchField (alias): " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    // Método auxiliar para convertir DTO a Entity
    private Field convertToEntity(FieldDTO dto) {
        Field field = new Field();
        field.setId(dto.getId());
        field.setNombre(dto.getNombre());
        field.setDescripcion(dto.getDescripcion());
        field.setUbicacion(dto.getUbicacion());
        field.setAreaHectareas(dto.getAreaHectareas());
        field.setEstado(dto.getEstado());
        field.setActivo(dto.getActivo());
        field.setPoligono(dto.getPoligono());

        // Convertir coordenadas a JSON
        if (dto.getCoordenadas() != null && !dto.getCoordenadas().isEmpty()) {
            try {
                field.setCoordenadas(objectMapper.writeValueAsString(dto.getCoordenadas()));
            } catch (JsonProcessingException e) {
                // Si hay error en la serialización, dejar coordenadas como null
            }
        }

        return field;
    }
}
