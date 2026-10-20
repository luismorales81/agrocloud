package com.agrocloud.feedlot.service;

import com.agrocloud.core.security.ServicioSeguridadContexto;
import com.agrocloud.exception.ResourceNotFoundException;
import com.agrocloud.feedlot.model.dto.FeedlotAnimalDetalleRespuesta;
import com.agrocloud.feedlot.model.dto.FeedlotAnimalRespuesta;
import com.agrocloud.feedlot.model.dto.FeedlotEventoSanitarioRespuesta;
import com.agrocloud.feedlot.model.dto.FeedlotPesadaAnimalRespuesta;
import com.agrocloud.feedlot.model.entity.FeedlotAnimal;
import com.agrocloud.feedlot.model.entity.FeedlotEventoSanitario;
import com.agrocloud.feedlot.model.entity.FeedlotPesadaAnimal;
import com.agrocloud.feedlot.repository.FeedlotAnimalRepository;
import com.agrocloud.feedlot.repository.FeedlotEventoSanitarioRepository;
import com.agrocloud.feedlot.repository.FeedlotPesadaAnimalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServicioFeedlotAnimales {

    private final ServicioSeguridadContexto servicioSeguridadContexto;
    private final ServicioFeedlotLotes servicioLotes;
    private final FeedlotAnimalRepository animalRepository;
    private final FeedlotPesadaAnimalRepository pesadaAnimalRepository;
    private final FeedlotEventoSanitarioRepository eventoSanitarioRepository;

    public ServicioFeedlotAnimales(
            ServicioSeguridadContexto servicioSeguridadContexto,
            ServicioFeedlotLotes servicioLotes,
            FeedlotAnimalRepository animalRepository,
            FeedlotPesadaAnimalRepository pesadaAnimalRepository,
            FeedlotEventoSanitarioRepository eventoSanitarioRepository) {
        this.servicioSeguridadContexto = servicioSeguridadContexto;
        this.servicioLotes = servicioLotes;
        this.animalRepository = animalRepository;
        this.pesadaAnimalRepository = pesadaAnimalRepository;
        this.eventoSanitarioRepository = eventoSanitarioRepository;
    }

    @Transactional(readOnly = true)
    public List<FeedlotAnimalRespuesta> listarPorLote(Long loteId) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        servicioLotes.obtenerEntidadLote(empresaId, loteId);
        LocalDate hoy = LocalDate.now();
        return animalRepository.listarPorLoteIdYEmpresaId(loteId, empresaId).stream()
                .map(a -> aRespuesta(a, retiroVigente(a.getId(), hoy)))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FeedlotAnimalDetalleRespuesta obtener(Long id) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        FeedlotAnimal a = animalRepository.buscarPorIdYEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Animal no encontrado"));
        FeedlotAnimalDetalleRespuesta dto = new FeedlotAnimalDetalleRespuesta();
        copiar(a, dto, retiroVigente(a.getId(), LocalDate.now()));
        dto.setPesadas(pesadaAnimalRepository.listarPorAnimalId(a.getId()).stream().map(this::aPesada).toList());
        dto.setEventosSanitarios(eventoSanitarioRepository.listarPorAnimalId(a.getId()).stream()
                .map(this::aEvento).toList());
        return dto;
    }

    public boolean retiroVigente(Long animalId, LocalDate fecha) {
        if (animalId == null || fecha == null) {
            return false;
        }
        return eventoSanitarioRepository.listarPorAnimalId(animalId).stream()
                .anyMatch(e -> e.getDiasRetiro() != null && e.getDiasRetiro() > 0
                        && e.getFecha() != null
                        && !fecha.isBefore(e.getFecha())
                        && !fecha.isAfter(e.getFecha().plusDays(e.getDiasRetiro())));
    }

    FeedlotAnimalRespuesta aRespuesta(FeedlotAnimal a, boolean retiro) {
        FeedlotAnimalRespuesta dto = new FeedlotAnimalRespuesta();
        copiar(a, dto, retiro);
        return dto;
    }

    private void copiar(FeedlotAnimal a, FeedlotAnimalRespuesta dto, boolean retiro) {
        dto.setId(a.getId());
        dto.setLoteId(a.getLote() != null ? a.getLote().getId() : null);
        dto.setEid(a.getEid());
        dto.setCaravanaVisual(a.getCaravanaVisual());
        dto.setSexo(a.getSexo());
        dto.setEstado(a.getEstado());
        dto.setFechaAlta(a.getFechaAlta());
        dto.setFechaBaja(a.getFechaBaja());
        dto.setPesoIngresoKg(a.getPesoIngresoKg());
        dto.setRetiroVigente(retiro);
    }

    private FeedlotPesadaAnimalRespuesta aPesada(FeedlotPesadaAnimal p) {
        FeedlotPesadaAnimalRespuesta dto = new FeedlotPesadaAnimalRespuesta();
        dto.setId(p.getId());
        dto.setFecha(p.getFecha());
        dto.setPesoKg(p.getPesoKg());
        return dto;
    }

    private FeedlotEventoSanitarioRespuesta aEvento(FeedlotEventoSanitario e) {
        FeedlotEventoSanitarioRespuesta dto = new FeedlotEventoSanitarioRespuesta();
        dto.setId(e.getId());
        dto.setLoteId(e.getLote() != null ? e.getLote().getId() : null);
        dto.setEmpresaId(e.getEmpresaId());
        dto.setFecha(e.getFecha());
        dto.setTipo(e.getTipo());
        dto.setDescripcion(e.getDescripcion());
        dto.setInsumoId(e.getInsumoId());
        dto.setDiasRetiro(e.getDiasRetiro());
        dto.setObservaciones(e.getObservaciones());
        dto.setCreatedAt(e.getCreatedAt());
        return dto;
    }
}
