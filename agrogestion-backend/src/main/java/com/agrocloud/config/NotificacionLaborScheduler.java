package com.agrocloud.config;

import com.agrocloud.cultivos.application.NotificacionLaborService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Job que deriva notificaciones de labores (spec SDD). Sin entidad Reminder.
 * Ejecuta diariamente y procesa labores con fecha_planificada hoy o mañana.
 */
@Component
public class NotificacionLaborScheduler {

    private static final Logger log = LoggerFactory.getLogger(NotificacionLaborScheduler.class);

    private final NotificacionLaborService notificacionLaborService;

    public NotificacionLaborScheduler(
            @org.springframework.beans.factory.annotation.Qualifier("notificacionLaborServiceCultivos")
            NotificacionLaborService notificacionLaborService) {
        this.notificacionLaborService = notificacionLaborService;
    }

    /** Todos los días a las 08:00. */
    @Scheduled(cron = "0 0 8 * * ?")
    public void ejecutarNotificacionesLabores() {
        try {
            log.debug("Ejecutando notificaciones derivadas de labores (spec SDD)");
            notificacionLaborService.enviarNotificacionesPendientes();
        } catch (Exception e) {
            log.error("Error en notificaciones de labores", e);
        }
    }
}
