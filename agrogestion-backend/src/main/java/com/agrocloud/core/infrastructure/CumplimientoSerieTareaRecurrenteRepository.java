package com.agrocloud.core.infrastructure;

import com.agrocloud.core.domain.CumplimientoSerieTareaRecurrente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CumplimientoSerieTareaRecurrenteRepository extends JpaRepository<CumplimientoSerieTareaRecurrente, Long> {

    Optional<CumplimientoSerieTareaRecurrente> findBySerieIdAndFechaOcurrencia(Long serieId, LocalDate fechaOcurrencia);

    @Query("""
            SELECT c FROM CumplimientoSerieTareaRecurrente c
            WHERE c.serie.id IN :serieIds
              AND c.fechaOcurrencia BETWEEN :desde AND :hasta
            """)
    List<CumplimientoSerieTareaRecurrente> findBySerieIdInAndFechaOcurrenciaBetween(
            @Param("serieIds") Collection<Long> serieIds,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);
}
