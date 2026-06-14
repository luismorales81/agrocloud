package com.agrocloud.porcinos.infrastructure;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.porcinos.domain.ConsumoDiarioAutomatico;
import com.agrocloud.porcinos.domain.DiaAlimentacion;
import com.agrocloud.porcinos.domain.Recria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConsumoDiarioAutomaticoRepository extends JpaRepository<ConsumoDiarioAutomatico, Long> {

    @Query("SELECT c FROM ConsumoDiarioAutomatico c JOIN FETCH c.diaAlimentacion d JOIN FETCH d.empresa "
        + "LEFT JOIN FETCH c.recria LEFT JOIN FETCH c.madre JOIN FETCH c.receta WHERE c.id = :id")
    Optional<ConsumoDiarioAutomatico> findByIdConDiaYEmpresa(@Param("id") Long id);

    @Query("SELECT c FROM ConsumoDiarioAutomatico c WHERE c.diaAlimentacion = :diaAlimentacion " +
           "ORDER BY c.etapaAlimentacion ASC, c.id ASC")
    List<ConsumoDiarioAutomatico> findByDiaAlimentacionOrderByEtapaAlimentacionAsc(
        @Param("diaAlimentacion") DiaAlimentacion diaAlimentacion);

    List<ConsumoDiarioAutomatico> findByDiaAlimentacion(DiaAlimentacion diaAlimentacion);

    List<ConsumoDiarioAutomatico> findByRecria(Recria recria);

    List<ConsumoDiarioAutomatico> findByProcesadoFalse();

    @Query("SELECT c FROM ConsumoDiarioAutomatico c WHERE c.diaAlimentacion.empresa = :empresa " +
           "AND c.diaAlimentacion.fecha BETWEEN :fechaDesde AND :fechaHasta " +
           "ORDER BY c.diaAlimentacion.fecha DESC, c.etapaAlimentacion ASC, c.id ASC")
    List<ConsumoDiarioAutomatico> findByEmpresaAndFechaBetween(
        @Param("empresa") Empresa empresa,
        @Param("fechaDesde") LocalDate fechaDesde,
        @Param("fechaHasta") LocalDate fechaHasta);

    long countByDiaAlimentacion(DiaAlimentacion diaAlimentacion);

    long countByDiaAlimentacionAndTipoRegistroConsumo(
        DiaAlimentacion diaAlimentacion,
        ConsumoDiarioAutomatico.TipoRegistroConsumo tipoRegistroConsumo);
}
