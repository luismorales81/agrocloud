package com.agrocloud.avicola.ponedoras.service;

import com.agrocloud.avicola.ponedoras.model.dto.AvicolaPonedorasRazaRespuesta;
import com.agrocloud.avicola.ponedoras.model.dto.AvicolaPonedorasRazaSolicitud;
import com.agrocloud.avicola.ponedoras.model.entity.AvicolaPonedorasRaza;
import com.agrocloud.avicola.ponedoras.repository.AvicolaPonedorasRazaRepository;
import com.agrocloud.core.security.ServicioSeguridadContexto;
import com.agrocloud.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServicioAvicolaPonedorasCatalogo {

    private final ServicioSeguridadContexto servicioSeguridadContexto;
    private final AvicolaPonedorasRazaRepository razaRepository;

    public ServicioAvicolaPonedorasCatalogo(
            ServicioSeguridadContexto servicioSeguridadContexto,
            AvicolaPonedorasRazaRepository razaRepository) {
        this.servicioSeguridadContexto = servicioSeguridadContexto;
        this.razaRepository = razaRepository;
    }

    @Transactional(readOnly = true)
    public List<AvicolaPonedorasRazaRespuesta> listarRazas() {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        return razaRepository.listarPorEmpresaId(empresaId).stream()
                .map(this::aRazaRespuesta)
                .collect(Collectors.toList());
    }

    @Transactional
    public AvicolaPonedorasRazaRespuesta crearRaza(AvicolaPonedorasRazaSolicitud solicitud) {
        if (solicitud.getNombre() == null || solicitud.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre de la raza es obligatorio");
        }
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        AvicolaPonedorasRaza raza = new AvicolaPonedorasRaza();
        raza.setEmpresaId(empresaId);
        raza.setNombre(solicitud.getNombre().trim());
        if (solicitud.getActivo() != null) {
            raza.setActivo(solicitud.getActivo());
        }
        return aRazaRespuesta(razaRepository.save(raza));
    }

    @Transactional
    public AvicolaPonedorasRazaRespuesta actualizarRaza(Long id, AvicolaPonedorasRazaSolicitud solicitud) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        AvicolaPonedorasRaza raza = razaRepository.buscarPorIdYEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Raza de ponedoras no encontrada"));
        if (solicitud.getNombre() != null) {
            if (solicitud.getNombre().isBlank()) {
                throw new IllegalArgumentException("El nombre de la raza es obligatorio");
            }
            raza.setNombre(solicitud.getNombre().trim());
        }
        if (solicitud.getActivo() != null) {
            raza.setActivo(solicitud.getActivo());
        }
        return aRazaRespuesta(razaRepository.save(raza));
    }

    private AvicolaPonedorasRazaRespuesta aRazaRespuesta(AvicolaPonedorasRaza raza) {
        AvicolaPonedorasRazaRespuesta dto = new AvicolaPonedorasRazaRespuesta();
        dto.setId(raza.getId());
        dto.setEmpresaId(raza.getEmpresaId());
        dto.setNombre(raza.getNombre());
        dto.setActivo(raza.getActivo());
        dto.setCreatedAt(raza.getCreatedAt());
        dto.setUpdatedAt(raza.getUpdatedAt());
        return dto;
    }
}
