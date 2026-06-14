package com.agrocloud.porcinos.infrastructure;

import com.agrocloud.porcinos.domain.ConsumoDiarioAutomatico;
import com.agrocloud.porcinos.domain.ConsumoDiarioDetalle;
import com.agrocloud.porcinos.domain.DiaAlimentacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsumoDiarioDetalleRepository extends JpaRepository<ConsumoDiarioDetalle, Long> {

    List<ConsumoDiarioDetalle> findByConsumoDiarioOrderByTipoComponenteAsc(ConsumoDiarioAutomatico consumoDiario);

    List<ConsumoDiarioDetalle> findByTieneDeficitTrue();

    @Query("SELECT d FROM ConsumoDiarioDetalle d WHERE d.consumoDiario.diaAlimentacion = :diaAlimentacion " +
           "AND d.tieneDeficit = true ORDER BY d.deficit DESC")
    List<ConsumoDiarioDetalle> findByDiaAlimentacionAndTieneDeficitTrue(
        @Param("diaAlimentacion") DiaAlimentacion diaAlimentacion);

    Long countByConsumoDiarioAndTieneDeficitTrue(ConsumoDiarioAutomatico consumoDiario);
}
