package com.agrocloud.config;

import com.agrocloud.cultivos.application.RecalculoEstadosLoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Job diario que recalcula estados derivados de lotes con cultivo (tiempo fenológico, tareas, eventos).
 * Se ejecuta a las 01:00, después del consumo diario porcino (00:30).
 */
@Component
public class RecalculoEstadosLoteScheduler {

    private static final Logger log = LoggerFactory.getLogger(RecalculoEstadosLoteScheduler.class);

    private final RecalculoEstadosLoteService recalculoEstadosLoteService;

    public RecalculoEstadosLoteScheduler(RecalculoEstadosLoteService recalculoEstadosLoteService) {
        this.recalculoEstadosLoteService = recalculoEstadosLoteService;
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void ejecutarRecalculoDiario() {
        try {
            log.debug("Iniciando recálculo diario de estados de lotes");
            recalculoEstadosLoteService.recalcularTodosLosLotesActivos();
        } catch (Exception e) {
            log.error("Error en recálculo diario de estados de lotes", e);
        }
    }
}
