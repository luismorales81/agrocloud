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
        try {
            System.out.println("[FIELD_CONTROLLER] Iniciando getAllFields para usuario: " + userDetails.getUsername());
            
            User user = userService.findByEmail(userDetails.getUsername());
            System.out.println("[FIELD_CONTROLLER] Usuario encontrado: " + (user != null ? user.getEmail() : "null"));
            
            List<Field> fields = fieldService.getFieldsByUser(user);
            System.out.println("[FIELD_CONTROLLER] Campos encontrados: " + (fields != null ? fields.size() : "null"));
            
            if (fields != null) {
                for (int i = 0; i < fields.size(); i++) {
                    Field field = fields.get(i);
                    System.out.println("[FIELD_CONTROLLER] Campo " + i + ": " + (field != null ? "ID=" + field.getId() + ", nombre=" + field.getNombre() : "null"));
                }
            }
            
            List<FieldDTO> dtos = fields.stream()
                .filter(field -> {
                    boolean isNotNull = field != null;
                    if (!isNotNull) {
                        System.out.println("[FIELD_CONTROLLER] Filtrando campo nulo");
                    }
                    return isNotNull;
                })
                .map(field -> {
                    try {
                        System.out.println("[FIELD_CONTROLLER] Convirtiendo campo ID: " + field.getId());
                        return this.convertToDTO(field);
                    } catch (Exception e) {
                        System.err.println("[FIELD_CONTROLLER] Error convirtiendo campo ID " + field.getId() + ": " + e.getMessage());
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(dto -> {
                    boolean isNotNull = dto != null;
                    if (!isNotNull) {
                        System.out.println("[FIELD_CONTROLLER] Filtrando DTO nulo");
                    }
                    return isNotNull;
                })
                .toList();
                
            System.out.println("[FIELD_CONTROLLER] DTOs finales: " + dtos.size());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            System.err.println("Error in getAllFields: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    // Obtener campo por ID
    @GetMapping("/{id}")
    public ResponseEntity<FieldDTO> getFieldById(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        Optional<Field> field = fieldService.getFieldById(id, user);
        
        return field.map(this::convertToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Crear nuevo campo
    @PostMapping
    public ResponseEntity<FieldDTO> createField(@RequestBody FieldDTO fieldDTO, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            System.out.println("[FIELD_CONTROLLER] Recibida petición para crear campo: " + fieldDTO.getNombre());
            
            User user = userService.findByEmail(userDetails.getUsername());
            System.out.println("[FIELD_CONTROLLER] Usuario encontrado: " + user.getEmail());
            
            Field field = convertToEntity(fieldDTO);
            System.out.println("[FIELD_CONTROLLER] Campo convertido a entidad");
            
            Field createdField = fieldService.createField(field, user);
            System.out.println("[FIELD_CONTROLLER] Campo creado exitosamente con ID: " + createdField.getId());
            
            return ResponseEntity.ok(convertToDTO(createdField));
        } catch (Exception e) {
            System.err.println("[FIELD_CONTROLLER] Error creando campo: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // Actualizar campo
    @PutMapping("/{id}")
    public ResponseEntity<FieldDTO> updateField(@PathVariable Long id, @RequestBody FieldDTO fieldDTO, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        Field field = convertToEntity(fieldDTO);
        Optional<Field> updatedField = fieldService.updateField(id, field, user);
        
        return updatedField.map(this::convertToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Eliminar campo
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteField(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            System.out.println("🔍 [CAMPOS] Eliminando campo con ID: " + id);
            System.out.println("🔍 [CAMPOS] Usuario autenticado: " + (userDetails != null ? userDetails.getUsername() : "null"));
            
            User user = userService.findByEmail(userDetails.getUsername());
            System.out.println("🔍 [CAMPOS] Usuario encontrado: " + (user != null ? user.getEmail() : "null"));
            
            boolean deleted = fieldService.deleteField(id, user);
            System.out.println("🔍 [CAMPOS] Resultado de eliminación: " + deleted);
            
            if (deleted) {
                System.out.println("✅ [CAMPOS] Campo eliminado exitosamente con ID: " + id);
                return ResponseEntity.noContent().build();
            } else {
                System.out.println("❌ [CAMPOS] Campo no encontrado o sin permisos para eliminar ID: " + id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("❌ [CAMPOS] Error eliminando campo: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // Buscar campo por nombre
    @GetMapping("/search")
    public ResponseEntity<List<FieldDTO>> searchField(@RequestParam String nombre, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        List<Field> fields = fieldService.searchFieldByName(nombre, user);
        List<FieldDTO> dtos = fields.stream().map(this::convertToDTO).toList();
        return ResponseEntity.ok(dtos);
    }

    // Obtener estadísticas de campos
    @GetMapping("/stats")
    public ResponseEntity<FieldService.FieldStats> getFieldStats(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        FieldService.FieldStats stats = fieldService.getFieldStats(user);
        return ResponseEntity.ok(stats);
    }

    // Obtener campos por estado
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<FieldDTO>> getFieldsByEstado(@PathVariable String estado, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        List<FieldDTO> dtos = fieldService.getFieldsByUser(user).stream()
                .filter(f -> estado.equals(f.getEstado()))
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }
}
