package com.agrocloud.core.infrastructure;

import com.agrocloud.core.domain.AmbitoCalendarioSerie;
import com.agrocloud.core.domain.SerieTareaRecurrenteCalendario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SerieTareaRecurrenteCalendarioRepository extends JpaRepository<SerieTareaRecurrenteCalendario, Long> {

    List<SerieTareaRecurrenteCalendario> findByUsuarioIdAndActivoTrueOrderByFechaInicioAsc(Long usuarioId);

    @Query("""
            SELECT s FROM SerieTareaRecurrenteCalendario s
            WHERE s.usuario.id = :usuarioId AND s.activo = true
              AND s.fechaInicio <= :hastaRango
              AND (s.fechaFin IS NULL OR s.fechaFin >= :desdeRango)
              AND s.ambitoCalendario = :ambito
            ORDER BY s.fechaInicio ASC
            """)
    List<SerieTareaRecurrenteCalendario> findActivasSolapandoRangoPorAmbito(
            @Param("usuarioId") Long usuarioId,
            @Param("desdeRango") LocalDate desdeRango,
            @Param("hastaRango") LocalDate hastaRango,
            @Param("ambito") AmbitoCalendarioSerie ambito);
}
