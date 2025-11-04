package com.agrocloud.service;

import com.agrocloud.dto.DosisAplicacionDTO;
import com.agrocloud.model.entity.DosisAplicacion;
import com.agrocloud.model.entity.Insumo;
import com.agrocloud.model.enums.TipoAplicacion;
import com.agrocloud.repository.DosisAplicacionRepository;
import com.agrocloud.repository.InsumoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class DosisAplicacionService {

    @Autowired
    private DosisAplicacionRepository dosisAplicacionRepository;

    @Autowired
    private InsumoRepository insumoRepository;

    /**
     * Obtener todas las dosis de aplicación de un insumo
     */
    public List<DosisAplicacionDTO> getDosisByInsumo(Long insumoId) {
        Insumo insumo = insumoRepository.findById(insumoId)
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado con ID: " + insumoId));
        
        List<DosisAplicacion> dosis = dosisAplicacionRepository.findByInsumoAndActivoTrue(insumo);
        return dosis.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener una dosis específica por insumo y tipo de aplicación
     */
    public Optional<DosisAplicacionDTO> getDosisByInsumoAndTipo(Long insumoId, TipoAplicacion tipoAplicacion) {
        Insumo insumo = insumoRepository.findById(insumoId)
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado con ID: " + insumoId));
        
        Optional<DosisAplicacion> dosis = dosisAplicacionRepository.findByInsumoAndTipoAplicacionAndActivoTrue(insumo, tipoAplicacion);
        return dosis.map(this::convertToDTO);
    }

    /**
     * Crear una nueva dosis de aplicación
     */
    public DosisAplicacionDTO createDosis(DosisAplicacionDTO dto) {
        Insumo insumo = insumoRepository.findById(dto.getInsumoId())
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado con ID: " + dto.getInsumoId()));
        
        // Verificar si ya existe una dosis para este insumo y tipo de aplicación
        Optional<DosisAplicacion> dosisExistente = dosisAplicacionRepository
                .findByInsumoAndTipoAplicacionAndActivoTrue(insumo, dto.getTipoAplicacion());
        
        if (dosisExistente.isPresent()) {
            throw new RuntimeException("Ya existe una dosis activa para este insumo y tipo de aplicación");
        }
        
        DosisAplicacion dosis = new DosisAplicacion();
        dosis.setInsumo(insumo);
        dosis.setTipoAplicacion(dto.getTipoAplicacion());
        dosis.setDosisPorHa(dto.getDosisPorHa());
        dosis.setUnidadMedida(dto.getUnidadMedida());
        dosis.setDescripcion(dto.getDescripcion());
        dosis.setActivo(true);
        
        DosisAplicacion saved = dosisAplicacionRepository.save(dosis);
        return convertToDTO(saved);
    }

    /**
     * Actualizar una dosis de aplicación existente
     */
    public DosisAplicacionDTO updateDosis(Long id, DosisAplicacionDTO dto) {
        DosisAplicacion dosis = dosisAplicacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dosis de aplicación no encontrada con ID: " + id));
        
        dosis.setDosisPorHa(dto.getDosisPorHa());
        dosis.setUnidadMedida(dto.getUnidadMedida());
        dosis.setDescripcion(dto.getDescripcion());
        
        DosisAplicacion saved = dosisAplicacionRepository.save(dosis);
        return convertToDTO(saved);
    }

    /**
     * Eliminar una dosis de aplicación (eliminación lógica)
     */
    public boolean deleteDosis(Long id) {
        DosisAplicacion dosis = dosisAplicacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dosis de aplicación no encontrada con ID: " + id));
        
        dosis.setActivo(false);
        dosisAplicacionRepository.save(dosis);
        return true;
    }

    /**
     * Sugerir dosis por defecto si no existe configuración
     */
    public DosisAplicacionDTO sugerirDosis(Long insumoId, TipoAplicacion tipoAplicacion) {
        Insumo insumo = insumoRepository.findById(insumoId)
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado con ID: " + insumoId));
        
        // Buscar si ya existe una dosis configurada
        Optional<DosisAplicacion> dosisExistente = dosisAplicacionRepository
                .findByInsumoAndTipoAplicacionAndActivoTrue(insumo, tipoAplicacion);
        
        if (dosisExistente.isPresent()) {
            return convertToDTO(dosisExistente.get());
        }
        
        // Sugerir dosis por defecto según el tipo de insumo
        DosisAplicacionDTO sugerencia = new DosisAplicacionDTO();
        sugerencia.setInsumoId(insumoId);
        sugerencia.setInsumoNombre(insumo.getNombre());
        sugerencia.setTipoAplicacion(tipoAplicacion);
        sugerencia.setUnidadMedida(insumo.getUnidadMedida());
        
        // Valores por defecto según tipo de aplicación
        switch (tipoAplicacion) {
            case PRE_EMERGENCIA:
                sugerencia.setDosisPorHa(new java.math.BigDecimal("2.0"));
                sugerencia.setDescripcion("Dosis sugerida para aplicación pre-emergencia");
                break;
            case POS_EMERGENCIA:
                sugerencia.setDosisPorHa(new java.math.BigDecimal("5.0"));
                sugerencia.setDescripcion("Dosis sugerida para aplicación pos-emergencia");
                break;
            case COBERTURA:
                sugerencia.setDosisPorHa(new java.math.BigDecimal("1.5"));
                sugerencia.setDescripcion("Dosis sugerida para aplicación de cobertura");
                break;
        }
        
        return sugerencia;
    }

    /**
     * Convertir entidad a DTO
     */
    private DosisAplicacionDTO convertToDTO(DosisAplicacion dosis) {
        DosisAplicacionDTO dto = new DosisAplicacionDTO();
        dto.setId(dosis.getId());
        dto.setInsumoId(dosis.getInsumo().getId());
        dto.setInsumoNombre(dosis.getInsumo().getNombre());
        dto.setTipoAplicacion(dosis.getTipoAplicacion());
        dto.setDosisPorHa(dosis.getDosisPorHa());
        dto.setUnidadMedida(dosis.getUnidadMedida());
        dto.setDescripcion(dosis.getDescripcion());
        dto.setActivo(dosis.getActivo());
        return dto;
    }
}

