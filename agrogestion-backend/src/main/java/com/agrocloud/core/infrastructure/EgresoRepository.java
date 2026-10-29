package com.agrocloud.core.infrastructure;

import com.agrocloud.core.domain.Egreso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository("egresoRepositoryCore")
public interface EgresoRepository extends JpaRepository<Egreso, Long> {

    List<Egreso> findByUserIdAndFechaBetweenOrderByFechaDesc(
            Long userId, LocalDate fechaInicio, LocalDate fechaFin);

    List<Egreso> findByLoteIdAndFechaBetweenOrderByFechaDesc(
            Long loteId, LocalDate fechaInicio, LocalDate fechaFin);

    List<Egreso> findByTipoAndUserIdOrderByFechaDesc(
            Egreso.TipoEgreso tipo, Long userId);

    @Query("SELECT COALESCE(SUM(e.costoTotal), 0) FROM Egreso e WHERE e.user.id = :usuarioId AND e.fecha BETWEEN :fechaInicio AND :fechaFin")
    BigDecimal calcularTotalEgresosPorUsuarioYFecha(
            @Param("usuarioId") Long usuarioId,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin);

    @Query("SELECT COALESCE(SUM(e.costoTotal), 0) FROM Egreso e WHERE e.lote.id = :loteId AND e.fecha BETWEEN :fechaInicio AND :fechaFin")
    BigDecimal calcularTotalEgresosPorLoteYFecha(
            @Param("loteId") Long loteId,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin);

    @Query("SELECT COALESCE(SUM(e.costoTotal), 0) FROM Egreso e WHERE e.campanaId = :campanaId AND e.fecha BETWEEN :fechaInicio AND :fechaFin")
    BigDecimal calcularTotalEgresosPorCampanaYFecha(@Param("campanaId") Long campanaId,
                                                    @Param("fechaInicio") LocalDate fechaInicio,
                                                    @Param("fechaFin") LocalDate fechaFin);

    List<Egreso> findByCampanaIdAndFechaBetweenOrderByFechaDesc(Long campanaId, LocalDate fechaInicio, LocalDate fechaFin);

    @Query("SELECT COALESCE(SUM(e.costoTotal), 0) FROM Egreso e WHERE e.tipo = :tipoEgreso AND e.user.id = :usuarioId AND e.fecha BETWEEN :fechaInicio AND :fechaFin")
    BigDecimal calcularTotalEgresosPorTipoYUsuario(
            @Param("tipoEgreso") Egreso.TipoEgreso tipoEgreso,
            @Param("usuarioId") Long usuarioId,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin);

    long countByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(e.costoTotal), 0) FROM Egreso e")
    BigDecimal sumAllEgresos();

    @Query("SELECT COALESCE(SUM(e.costoTotal), 0) FROM Egreso e WHERE e.user.id = :usuarioId")
    BigDecimal sumEgresosByUsuarioId(@Param("usuarioId") Long usuarioId);

    List<Egreso> findByUserIdAndActivoTrue(Long userId);
    long countByUserIdAndActivoTrue(Long userId);
    List<Egreso> findByActivoTrue();

    List<Egreso> findByUserId(Long userId);

    @Query("""
            SELECT e FROM Egreso e
            WHERE e.empresaId = :empresaId
            AND (:modulo IS NULL OR e.moduloOrigen = :modulo
                 OR (:incluirCompartidas = true AND e.imputacion = com.agrocloud.core.domain.ImputacionEconomica.COMPARTIDA))
            ORDER BY e.fecha DESC, e.id DESC
            """)
    List<Egreso> listarPorEmpresa(@Param("empresaId") Long empresaId,
                                  @Param("modulo") String modulo,
                                  @Param("incluirCompartidas") boolean incluirCompartidas);

    @Query("""
            SELECT COALESCE(SUM(e.costoTotal), 0) FROM Egreso e
            WHERE e.empresaId = :empresaId
            AND e.fecha BETWEEN :fechaInicio AND :fechaFin
            AND (e.activo = true OR e.activo IS NULL)
            AND e.estado <> com.agrocloud.core.domain.Egreso.EstadoEgreso.CANCELADO
            """)
    BigDecimal sumarPorEmpresaYFecha(@Param("empresaId") Long empresaId,
                                     @Param("fechaInicio") LocalDate fechaInicio,
                                     @Param("fechaFin") LocalDate fechaFin);
}
