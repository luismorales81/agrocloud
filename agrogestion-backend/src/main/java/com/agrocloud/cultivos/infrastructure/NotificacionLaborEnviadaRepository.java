package com.agrocloud.cultivos.infrastructure;

import com.agrocloud.cultivos.domain.NotificacionLaborEnviada;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository("notificacionLaborEnviadaRepositoryCultivos")
public interface NotificacionLaborEnviadaRepository extends JpaRepository<NotificacionLaborEnviada, Long> {

    Optional<NotificacionLaborEnviada> findByLaborIdAndTipoAndFechaEnvio(
            Long laborId,
            NotificacionLaborEnviada.TipoNotificacion tipo,
            LocalDate fechaEnvio);
}
