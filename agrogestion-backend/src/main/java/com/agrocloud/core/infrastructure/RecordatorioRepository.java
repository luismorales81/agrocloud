package com.agrocloud.core.infrastructure;

import com.agrocloud.core.domain.Recordatorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RecordatorioRepository extends JpaRepository<Recordatorio, Long> {

    List<Recordatorio> findByUsuarioIdAndActivoTrueOrderByFechaAsc(Long usuarioId);
    List<Recordatorio> findByUsuarioIdAndFechaAndActivoTrue(Long usuarioId, LocalDate fecha);

    @Query("SELECT r FROM Recordatorio r WHERE r.usuario.id = :usuarioId AND r.fecha BETWEEN :fechaInicio AND :fechaFin AND r.activo = true ORDER BY r.fecha ASC")
    List<Recordatorio> findByUsuarioIdAndRangoFechas(@Param("usuarioId") Long usuarioId, @Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);

    @Query("SELECT r FROM Recordatorio r WHERE r.usuario.id = :usuarioId AND r.completado = false AND r.activo = true AND r.fecha >= :hoy ORDER BY r.fecha ASC")
    List<Recordatorio> findPendientesByUsuarioId(@Param("usuarioId") Long usuarioId, @Param("hoy") LocalDate hoy);

    List<Recordatorio> findByUsuarioIdAndTipoAndActivoTrueOrderByFechaAsc(Long usuarioId, Recordatorio.TipoRecordatorio tipo);
    List<Recordatorio> findByLaborIdAndActivoTrue(Long laborId);
    List<Recordatorio> findByLoteIdAndActivoTrue(Long loteId);

    @Query("SELECT r FROM Recordatorio r WHERE (r.servicioId IS NOT NULL OR r.gestacionId IS NOT NULL OR r.partoId IS NOT NULL) AND r.activo = true")
    List<Recordatorio> findRecordatoriosPorcinos();

    @Query("SELECT r FROM Recordatorio r WHERE (r.servicioId IS NOT NULL OR r.gestacionId IS NOT NULL OR r.partoId IS NOT NULL) AND r.activo = true AND r.fecha BETWEEN :fechaInicio AND :fechaFin")
    List<Recordatorio> findRecordatoriosPorcinosPorRango(@Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);
}
