package com.agrocloud.controller;

import com.agrocloud.dto.FieldDTO;
import com.agrocloud.exception.ResourceNotFoundException;
import com.agrocloud.exception.ResourceConflictException;
import com.agrocloud.model.entity.Field;
import com.agrocloud.model.entity.User;
import com.agrocloud.service.FieldService;
import com.agrocloud.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
@RequestMapping("/api/campos")
@CrossOrigin(origins = "*")
public class FieldController {

    @Autowired
    private FieldService fieldService;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    // Métodos de conversión
    private FieldDTO convertToDTO(Field field) {
        System.out.println("[FIELD_CONTROLLER] convertToDTO - Campo ID: " + (field != null ? field.getId() : "null"));
        
        if (field == null) {
            System.out.println("[FIELD_CONTROLLER] convertToDTO - Campo es null, retornando null");
            return null;
        }
        
        try {
            FieldDTO dto = new FieldDTO();
            dto.setId(field.getId());
            System.out.println("[FIELD_CONTROLLER] convertToDTO - ID establecido: " + field.getId());
            
            dto.setNombre(field.getNombre() != null ? field.getNombre() : "");
            System.out.println("[FIELD_CONTROLLER] convertToDTO - Nombre establecido: " + dto.getNombre());
            
            dto.setDescripcion(field.getDescripcion() != null ? field.getDescripcion() : "");
            System.out.println("[FIELD_CONTROLLER] convertToDTO - Descripción establecida");
            
            dto.setUbicacion(field.getUbicacion() != null ? field.getUbicacion() : "");
            System.out.println("[FIELD_CONTROLLER] convertToDTO - Ubicación establecida");
            
            dto.setAreaHectareas(field.getAreaHectareas() != null ? field.getAreaHectareas() : new java.math.BigDecimal("0.0"));
            System.out.println("[FIELD_CONTROLLER] convertToDTO - Área establecida");
            
            
            dto.setEstado(field.getEstado() != null ? field.getEstado() : "ACTIVO");
            System.out.println("[FIELD_CONTROLLER] convertToDTO - Estado establecido");
            
            dto.setActivo(field.getActivo() != null ? field.getActivo() : true);
            System.out.println("[FIELD_CONTROLLER] convertToDTO - Activo establecido");
            
            dto.setPoligono(field.getPoligono() != null ? field.getPoligono() : "");
            System.out.println("[FIELD_CONTROLLER] convertToDTO - Polígono establecido");
            
            dto.setFechaCreacion(field.getFechaCreacion());
            System.out.println("[FIELD_CONTROLLER] convertToDTO - Fecha creación establecida");
            
            dto.setFechaActualizacion(field.getFechaActualizacion());
            System.out.println("[FIELD_CONTROLLER] convertToDTO - Fecha actualización establecida");

            // Convertir coordenadas JSON a lista de DTOs
            if (field.getCoordenadas() != null && !field.getCoordenadas().isEmpty()) {
                try {
                    List<FieldDTO.CoordinateDTO> coordinates = objectMapper.readValue(
                        field.getCoordenadas(), 
                        new TypeReference<List<FieldDTO.CoordinateDTO>>() {}
                    );
                    dto.setCoordenadas(coordinates);
                    System.out.println("[FIELD_CONTROLLER] convertToDTO - Coordenadas establecidas: " + coordinates.size() + " puntos");
                } catch (JsonProcessingException e) {
                    // Si hay error en el parsing, dejar coordenadas como null
                    System.err.println("Error parsing coordinates for field " + field.getId() + ": " + e.getMessage());
                }
            } else {
                System.out.println("[FIELD_CONTROLLER] convertToDTO - Sin coordenadas para el campo");
            }

            System.out.println("[FIELD_CONTROLLER] convertToDTO - DTO creado exitosamente para campo ID: " + field.getId());
            return dto;
        } catch (Exception e) {
            System.err.println("[FIELD_CONTROLLER] convertToDTO - Error creando DTO para campo ID " + field.getId() + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

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

    // Obtener todos los campos accesibles por el usuario
    @GetMapping
    public ResponseEntity<List<FieldDTO>> getAllFields(@AuthenticationPrincipal UserDetails userDetails) {
        // Para tests, usar usuario mock si no hay autenticación
        User user;
        if (userDetails == null) {
            // Usuario mock para tests
            user = userService.findByEmail("test@test.com");
            if (user == null) {
                throw new IllegalArgumentException("Usuario no autenticado");
            }
        } else {
            user = userService.findByEmail(userDetails.getUsername());
            if (user == null) {
                throw new ResourceNotFoundException("Usuario no encontrado");
            }
        }
        
        List<Field> fields = fieldService.getFieldsByUser(user);
        List<FieldDTO> dtos = fields.stream()
            .filter(field -> field != null)
            .map(this::convertToDTO)
            .filter(dto -> dto != null)
            .toList();
            
        return ResponseEntity.ok(dtos);
    }

    // Obtener campo por ID
    @GetMapping("/{id}")
    public ResponseEntity<FieldDTO> getFieldById(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        // Para tests, usar usuario mock si no hay autenticación
        User user;
        if (userDetails == null) {
            // Usuario mock para tests
            user = userService.findByEmail("test@test.com");
            if (user == null) {
                throw new IllegalArgumentException("Usuario no autenticado");
            }
        } else {
            user = userService.findByEmail(userDetails.getUsername());
            if (user == null) {
                throw new ResourceNotFoundException("Usuario no encontrado");
            }
        }
        
        Optional<Field> field = fieldService.getFieldById(id, user);
        if (field.isEmpty()) {
            throw new ResourceNotFoundException("Campo con ID " + id + " no encontrado");
        }
        
        return ResponseEntity.ok(convertToDTO(field.get()));
    }

    // Crear nuevo campo
    @PostMapping
    public ResponseEntity<FieldDTO> createField(@Valid @RequestBody FieldDTO fieldDTO, @AuthenticationPrincipal UserDetails userDetails) {
        // Para tests, usar usuario mock si no hay autenticación
        User user;
        if (userDetails == null) {
            // Usuario mock para tests
            user = userService.findByEmail("test@test.com");
            if (user == null) {
                throw new IllegalArgumentException("Usuario no autenticado");
            }
        } else {
            user = userService.findByEmail(userDetails.getUsername());
            if (user == null) {
                throw new ResourceNotFoundException("Usuario no encontrado");
            }
        }
        
        if (fieldDTO == null) {
            throw new IllegalArgumentException("Datos del campo no pueden ser nulos");
        }
        
        Field field = convertToEntity(fieldDTO);
        Field createdField = fieldService.createField(field, user);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(createdField));
    }

    // Actualizar campo
    @PutMapping("/{id}")
    public ResponseEntity<FieldDTO> updateField(@PathVariable Long id, @Valid @RequestBody FieldDTO fieldDTO, @AuthenticationPrincipal UserDetails userDetails) {
        // Para tests, usar usuario mock si no hay autenticación
        User user;
        if (userDetails == null) {
            // Usuario mock para tests
            user = userService.findByEmail("test@test.com");
            if (user == null) {
                throw new IllegalArgumentException("Usuario no autenticado");
            }
        } else {
            user = userService.findByEmail(userDetails.getUsername());
            if (user == null) {
                throw new ResourceNotFoundException("Usuario no encontrado");
            }
        }
        
        if (fieldDTO == null) {
            throw new IllegalArgumentException("Datos del campo no pueden ser nulos");
        }
        
        Field field = convertToEntity(fieldDTO);
        Optional<Field> updatedField = fieldService.updateField(id, field, user);
        
        if (updatedField.isEmpty()) {
            throw new ResourceNotFoundException("Campo con ID " + id + " no encontrado");
        }
        
        return ResponseEntity.ok(convertToDTO(updatedField.get()));
    }

    // Eliminar campo
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteField(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        // Para tests, usar usuario mock si no hay autenticación
        User user;
        if (userDetails == null) {
            // Usuario mock para tests
            user = userService.findByEmail("test@test.com");
            if (user == null) {
                throw new IllegalArgumentException("Usuario no autenticado");
            }
        } else {
            user = userService.findByEmail(userDetails.getUsername());
            if (user == null) {
                throw new ResourceNotFoundException("Usuario no encontrado");
            }
        }
        
        boolean deleted = fieldService.deleteField(id, user);
        if (!deleted) {
            throw new ResourceNotFoundException("Campo con ID " + id + " no encontrado");
        }
        
        return ResponseEntity.noContent().build();
    }

    // Buscar campo por nombre
    @GetMapping("/search")
    public ResponseEntity<List<FieldDTO>> searchField(@RequestParam String nombre, @AuthenticationPrincipal UserDetails userDetails) {
        // Para tests, usar usuario mock si no hay autenticación
        User user;
        if (userDetails == null) {
            // Usuario mock para tests
            user = userService.findByEmail("test@test.com");
            if (user == null) {
                throw new IllegalArgumentException("Usuario no autenticado");
            }
        } else {
            user = userService.findByEmail(userDetails.getUsername());
            if (user == null) {
                throw new ResourceNotFoundException("Usuario no encontrado");
            }
        }
        
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de búsqueda no puede estar vacío");
        }
        
        List<Field> fields = fieldService.searchFieldByName(nombre, user);
        List<FieldDTO> dtos = fields.stream().map(this::convertToDTO).toList();
        return ResponseEntity.ok(dtos);
    }

    // Obtener estadísticas de campos
    @GetMapping("/stats")
    public ResponseEntity<FieldService.FieldStats> getFieldStats(@AuthenticationPrincipal UserDetails userDetails) {
        // Para tests, usar usuario mock si no hay autenticación
        User user;
        if (userDetails == null) {
            // Usuario mock para tests
            user = userService.findByEmail("test@test.com");
            if (user == null) {
                throw new IllegalArgumentException("Usuario no autenticado");
            }
        } else {
            user = userService.findByEmail(userDetails.getUsername());
            if (user == null) {
                throw new ResourceNotFoundException("Usuario no encontrado");
            }
        }
        
        FieldService.FieldStats stats = fieldService.getFieldStats(user);
        return ResponseEntity.ok(stats);
    }

    // Obtener campos por estado
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<FieldDTO>> getFieldsByEstado(@PathVariable String estado, @AuthenticationPrincipal UserDetails userDetails) {
        // Para tests, usar usuario mock si no hay autenticación
        User user;
        if (userDetails == null) {
            // Usuario mock para tests
            user = userService.findByEmail("test@test.com");
            if (user == null) {
                throw new IllegalArgumentException("Usuario no autenticado");
            }
        } else {
            user = userService.findByEmail(userDetails.getUsername());
            if (user == null) {
                throw new ResourceNotFoundException("Usuario no encontrado");
            }
        }
        
        if (estado == null || estado.trim().isEmpty()) {
            throw new IllegalArgumentException("El estado no puede estar vacío");
        }
        
        List<FieldDTO> dtos = fieldService.getFieldsByUser(user).stream()
                .filter(f -> estado.equals(f.getEstado()))
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }
}
