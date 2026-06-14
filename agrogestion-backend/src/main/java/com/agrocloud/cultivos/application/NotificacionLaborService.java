package com.agrocloud.cultivos.application;

import com.agrocloud.cultivos.domain.Labor;
import com.agrocloud.cultivos.domain.NotificacionLaborEnviada;
import com.agrocloud.cultivos.infrastructure.LaborRepository;
import com.agrocloud.cultivos.infrastructure.NotificacionLaborEnviadaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Notificaciones derivadas de labores (spec SDD). Comportamiento del sistema, no entidad Reminder.
 * Reglas: 24h antes → notificación; mismo día → notificación.
 * Este servicio solo registra el envío para no duplicar; el envío real (email/push) se puede integrar después.
 */
@Service("notificacionLaborServiceCultivos")
public class NotificacionLaborService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionLaborService.class);

    private final LaborRepository laborRepository;
    private final NotificacionLaborEnviadaRepository notificacionLaborEnviadaRepository;

    public NotificacionLaborService(
            @org.springframework.beans.factory.annotation.Qualifier("laborRepositoryCultivos") LaborRepository laborRepository,
            @org.springframework.beans.factory.annotation.Qualifier("notificacionLaborEnviadaRepositoryCultivos")
            NotificacionLaborEnviadaRepository notificacionLaborEnviadaRepository) {
        this.laborRepository = laborRepository;
        this.notificacionLaborEnviadaRepository = notificacionLaborEnviadaRepository;
    }

    /**
     * Envía notificaciones para labores planificadas con fecha hoy o mañana.
     * Hoy → tipo MISMO_DIA; mañana → tipo H_24_ANTES.
     */
    @Transactional
    public void enviarNotificacionesPendientes() {
        LocalDate hoy = LocalDate.now();
        LocalDate manana = hoy.plusDays(1);

        List<Labor> laboresProximas = laborRepository.findLaboresProximas(hoy, manana);
        laboresProximas = laboresProximas.stream()
                .filter(l -> Boolean.TRUE.equals(l.getActivo()))
                .toList();

        for (Labor labor : laboresProximas) {
            LocalDate fechaPlanificada = labor.getFechaInicio();
            if (fechaPlanificada == null) continue;

            if (fechaPlanificada.equals(manana)) {
                enviarSiNoEnviada(labor, NotificacionLaborEnviada.TipoNotificacion.H_24_ANTES, hoy);
            } else if (fechaPlanificada.equals(hoy)) {
                enviarSiNoEnviada(labor, NotificacionLaborEnviada.TipoNotificacion.MISMO_DIA, hoy);
            }
        }
    }

    private void enviarSiNoEnviada(Labor labor, NotificacionLaborEnviada.TipoNotificacion tipo, LocalDate fechaEnvio) {
        if (notificacionLaborEnviadaRepository.findByLaborIdAndTipoAndFechaEnvio(labor.getId(), tipo, fechaEnvio).isPresent()) {
            return;
        }
        // Envío real: aquí se puede integrar email/push. Por ahora solo log y registro.
        log.info("[NotificacionLabor] {} labor id={} lote={} fecha_planificada={}",
                tipo == NotificacionLaborEnviada.TipoNotificacion.H_24_ANTES ? "24h antes" : "Mismo día",
                labor.getId(),
                labor.getLote() != null ? labor.getLote().getNombre() : "-",
                labor.getFechaInicio());

        NotificacionLaborEnviada registro = new NotificacionLaborEnviada();
        registro.setLaborId(labor.getId());
        registro.setTipo(tipo);
        registro.setFechaEnvio(fechaEnvio);
        notificacionLaborEnviadaRepository.save(registro);
    }
}
