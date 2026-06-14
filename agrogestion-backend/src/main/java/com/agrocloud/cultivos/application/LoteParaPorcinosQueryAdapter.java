package com.agrocloud.cultivos.application;

import com.agrocloud.core.application.port.LoteMinimoDTO;
import com.agrocloud.core.application.port.LoteParaPorcinosQuery;
import com.agrocloud.cultivos.domain.Plot;
import com.agrocloud.cultivos.infrastructure.PlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación del puerto {@link LoteParaPorcinosQuery} que delega en PlotService.
 * Permite que el controlador de Porcinos (catálogos/lotes) no dependa directamente
 * de la entidad Plot ni de PlotService; solo del puerto en Core.
 */
@Service
public class LoteParaPorcinosQueryAdapter implements LoteParaPorcinosQuery {

    @Autowired
    private PlotService plotService;

    @Autowired
    private PlotRepository plotRepository;

    @Override
    public List<LoteMinimoDTO> listarLotesPorcinosPorEmpresa(Long empresaId) {
        List<Plot> lotes = plotService.getLotesPorcinosByEmpresaId(empresaId);
        return lotes.stream()
                .map(p -> new LoteMinimoDTO(
                        p.getId(),
                        p.getNombre(),
                        p.getAreaHectareas() != null ? p.getAreaHectareas().doubleValue() : null))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<LoteMinimoDTO> obtenerPorId(Long loteId) {
        if (loteId == null) return Optional.empty();
        return plotRepository.findById(loteId)
                .filter(p -> p.getTipoUso() == Plot.TipoUsoLote.PORCINO)
                .map(p -> new LoteMinimoDTO(
                        p.getId(),
                        p.getNombre(),
                        p.getAreaHectareas() != null ? p.getAreaHectareas().doubleValue() : null));
    }
}
