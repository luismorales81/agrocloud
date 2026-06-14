package com.agrocloud.porcinos.infrastructure;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.porcinos.domain.EventoSanitario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventoSanitarioRepository extends JpaRepository<EventoSanitario, Long> {

    List<EventoSanitario> findByEmpresaAndActivoTrue(Empresa empresa);

    List<EventoSanitario> findByEmpresaAndTipoEntidadAndEntidadIdAndActivoTrue(
        Empresa empresa, EventoSanitario.TipoEntidad tipoEntidad, Long entidadId);

    List<EventoSanitario> findByEmpresaAndFechaBetweenAndActivoTrue(
        Empresa empresa, LocalDate fechaInicio, LocalDate fechaFin);

    @Query("SELECT e FROM EventoSanitario e JOIN e.tipoEventoSanitario t " +
           "WHERE e.empresa = :empresa " +
           "AND t.requiereFechaRetiro = true " +
           "AND e.retiroCumplido = false " +
           "AND e.fechaRetiro IS NOT NULL " +
           "AND e.fechaRetiro <= :fechaLimite " +
           "AND e.activo = true")
    List<EventoSanitario> findRetirosVencidos(@Param("empresa") Empresa empresa,
                                               @Param("fechaLimite") LocalDate fechaLimite);

    @Query("SELECT e FROM EventoSanitario e JOIN e.tipoEventoSanitario t " +
           "WHERE e.empresa = :empresa " +
           "AND t.requiereFechaRetiro = true " +
           "AND e.retiroCumplido = false " +
           "AND e.fechaRetiro IS NOT NULL " +
           "AND e.fechaRetiro BETWEEN :fechaInicio AND :fechaFin " +
           "AND e.activo = true")
    List<EventoSanitario> findRetirosProximosAVencer(@Param("empresa") Empresa empresa,
                                                     @Param("fechaInicio") LocalDate fechaInicio,
                                                     @Param("fechaFin") LocalDate fechaFin);

    Optional<EventoSanitario> findByIdAndEmpresaAndActivoTrue(Long id, Empresa empresa);

    @org.springframework.data.jpa.repository.Query("SELECT e FROM EventoSanitario e JOIN FETCH e.tipoEventoSanitario t " +
           "WHERE e.empresa = :empresa AND e.tipoEntidad = :tipoEntidad AND e.entidadId = :entidadId AND e.activo = true")
    List<EventoSanitario> findByEmpresaYEntidadConTipo(
        @Param("empresa") Empresa empresa,
        @Param("tipoEntidad") EventoSanitario.TipoEntidad tipoEntidad,
        @Param("entidadId") Long entidadId);
}
