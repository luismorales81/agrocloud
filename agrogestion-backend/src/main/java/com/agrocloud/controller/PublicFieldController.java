package com.agrocloud.controller;

import com.agrocloud.dto.FieldDTO;
import com.agrocloud.model.entity.Field;
import com.agrocloud.service.FieldService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/campos")
@CrossOrigin(origins = "*")
public class PublicFieldController {

    @Autowired
    private FieldService fieldService;

    @Autowired
    private ObjectMapper objectMapper;

    // Métodos de conversión
    private FieldDTO convertToDTO(Field field) {
        FieldDTO dto = new FieldDTO();
        dto.setId(field.getId());
        dto.setNombre(field.getNombre());
        dto.setDescripcion(field.getDescripcion());
        dto.setUbicacion(field.getUbicacion());
        dto.setAreaHectareas(field.getAreaHectareas());
        dto.setTipoSuelo(field.getTipoSuelo());
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

    // Obtener todos los campos (público)
    @GetMapping
    public ResponseEntity<List<FieldDTO>> getAllFields() {
        try {
            List<Field> fields = fieldService.getAllFields();
            List<FieldDTO> dtos = fields.stream().map(this::convertToDTO).toList();
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            // Retornar lista vacía en lugar de error 500
            System.err.println("Error en getAllFields: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(List.of());
        }
    }

    // Endpoint de prueba
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("PublicFieldController funcionando correctamente");
    }

    // Obtener campo por ID (público)
    @GetMapping("/{id}")
    public ResponseEntity<FieldDTO> getFieldById(@PathVariable Long id) {
        try {
            Field field = fieldService.getFieldById(id);
            if (field != null) {
                return ResponseEntity.ok(convertToDTO(field));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
