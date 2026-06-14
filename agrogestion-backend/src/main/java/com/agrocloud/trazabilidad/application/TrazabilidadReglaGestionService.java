package com.agrocloud.trazabilidad.application;

import com.agrocloud.trazabilidad.domain.TrazabilidadReglaCertificacion;
import com.agrocloud.trazabilidad.infrastructure.TrazabilidadReglaCertificacionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TrazabilidadReglaGestionService {

    private final TrazabilidadReglaCertificacionRepository reglaRepository;
    private final ObjectMapper objectMapper;

    public TrazabilidadReglaGestionService(
            TrazabilidadReglaCertificacionRepository reglaRepository,
            ObjectMapper objectMapper) {
        this.reglaRepository = reglaRepository;
        this.objectMapper = objectMapper;
    }

    public void actualizarParametrosJson(Long idRegla, String parametrosJson) {
        TrazabilidadReglaCertificacion regla = reglaRepository.findById(idRegla)
                .orElseThrow(() -> new IllegalArgumentException("Regla no encontrada: " + idRegla));
        try {
            objectMapper.readTree(parametrosJson);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("parametrosJson no es un JSON valido: " + e.getOriginalMessage());
        }
        regla.setParametrosJson(parametrosJson);
        reglaRepository.save(regla);
    }
}
