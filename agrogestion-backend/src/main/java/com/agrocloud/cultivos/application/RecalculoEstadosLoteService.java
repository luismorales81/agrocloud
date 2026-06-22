package com.agrocloud.cultivos.application;

import com.agrocloud.cultivos.domain.Plot;
import com.agrocloud.cultivos.infrastructure.PlotRepository;
import com.agrocloud.dto.ResultadoRecalculoEstadosDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Recálculo masivo de estados derivados para lotes con cultivo asignado.
 */
@Service
public class RecalculoEstadosLoteService {

    private static final Logger log = LoggerFactory.getLogger(RecalculoEstadosLoteService.class);

    private final PlotRepository plotRepository;
    private final EstadoLoteUpdater estadoLoteUpdater;

    public RecalculoEstadosLoteService(PlotRepository plotRepository, EstadoLoteUpdater estadoLoteUpdater) {
        this.plotRepository = plotRepository;
        this.estadoLoteUpdater = estadoLoteUpdater;
    }

    /**
     * Recalcula estados de todos los lotes activos con cultivo/tipo de cultivo.
     */
    @Transactional
    public ResultadoRecalculoEstadosDTO recalcularTodosLosLotesActivos() {
        return recalcularLotes(plotRepository.findActivosConCultivoParaRecalcularEstado());
    }

    /**
     * Recalcula estados de lotes activos de una empresa.
     */
    @Transactional
    public ResultadoRecalculoEstadosDTO recalcularLotesPorEmpresa(Long empresaId) {
        return recalcularLotes(plotRepository.findActivosConCultivoPorEmpresa(empresaId));
    }

    private ResultadoRecalculoEstadosDTO recalcularLotes(List<Plot> lotes) {
        ResultadoRecalculoEstadosDTO resultado = new ResultadoRecalculoEstadosDTO();
        resultado.setFechaEjecucion(LocalDateTime.now());
        resultado.setLotesProcesados(lotes.size());

        for (Plot lote : lotes) {
            try {
                Long estadoConfigIdAntes = lote.getEstadoConfigurado() != null
                    ? lote.getEstadoConfigurado().getId() : null;
                var estadoEnumAntes = lote.getEstado();

                estadoLoteUpdater.recalcularEstado(lote.getId());

                Plot actualizado = plotRepository.findById(lote.getId()).orElse(lote);
                Long estadoConfigIdDespues = actualizado.getEstadoConfigurado() != null
                    ? actualizado.getEstadoConfigurado().getId() : null;
                var estadoEnumDespues = actualizado.getEstado();

                boolean cambio = !Objects.equals(estadoConfigIdAntes, estadoConfigIdDespues)
                    || estadoEnumAntes != estadoEnumDespues;

                if (cambio) {
                    resultado.setLotesActualizados(resultado.getLotesActualizados() + 1);
                    resultado.getLotesCambiados().add(lote.getId());
                    log.info("Recálculo diario: lote {} cambió de estado", lote.getNombre());
                }
            } catch (Exception e) {
                resultado.setLotesConError(resultado.getLotesConError() + 1);
                String msg = "Lote " + lote.getId() + " (" + lote.getNombre() + "): " + e.getMessage();
                resultado.getErrores().add(msg);
                log.warn("Error recalculando estado del lote {}: {}", lote.getId(), e.getMessage());
            }
        }

        log.info("Recálculo de estados finalizado: {} procesados, {} actualizados, {} errores",
            resultado.getLotesProcesados(), resultado.getLotesActualizados(), resultado.getLotesConError());
        return resultado;
    }
}
