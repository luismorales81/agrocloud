package com.agrocloud.core.infrastructure;

import com.agrocloud.core.domain.Ingreso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository("ingresoRepositoryCore")
public interface IngresoRepository extends JpaRepository<Ingreso, Long> {

    List<Ingreso> findByUserIdOrderByFechaDesc(Long userId);

    List<Ingreso> findByLoteIdOrderByFechaDesc(Long loteId);

    List<Ingreso> findByTipoIngresoOrderByFechaDesc(Ingreso.TipoIngreso tipoIngreso);

    List<Ingreso> findByFechaBetweenOrderByFechaDesc(LocalDate fechaInicio, LocalDate fechaFin);

    List<Ingreso> findByUserIdAndFechaBetweenOrderByFechaDesc(
            Long userId, LocalDate fechaInicio, LocalDate fechaFin);

    @Query("SELECT COALESCE(SUM(i.monto), 0) FROM Ingreso i WHERE i.user.id = :usuarioId AND i.fecha BETWEEN :fechaInicio AND :fechaFin")
    BigDecimal calcularTotalIngresosPorUsuarioYFecha(@Param("usuarioId") Long usuarioId,
                                                     @Param("fechaInicio") LocalDate fechaInicio,
                                                     @Param("fechaFin") LocalDate fechaFin);

    @Query("SELECT COALESCE(SUM(i.monto), 0) FROM Ingreso i WHERE i.lote.id = :loteId AND i.fecha BETWEEN :fechaInicio AND :fechaFin")
    BigDecimal calcularTotalIngresosPorLoteYFecha(@Param("loteId") Long loteId,
                                                  @Param("fechaInicio") LocalDate fechaInicio,
                                                  @Param("fechaFin") LocalDate fechaFin);

    @Query("SELECT COALESCE(SUM(i.monto), 0) FROM Ingreso i WHERE i.campanaId = :campanaId AND i.fecha BETWEEN :fechaInicio AND :fechaFin")
    BigDecimal calcularTotalIngresosPorCampanaYFecha(@Param("campanaId") Long campanaId,
                                                     @Param("fechaInicio") LocalDate fechaInicio,
                                                     @Param("fechaFin") LocalDate fechaFin);

    List<Ingreso> findByCampanaIdAndFechaBetweenOrderByFechaDesc(Long campanaId, LocalDate fechaInicio, LocalDate fechaFin);

    @Query("SELECT COALESCE(SUM(i.monto), 0) FROM Ingreso i WHERE i.tipoIngreso = :tipoIngreso AND i.fecha BETWEEN :fechaInicio AND :fechaFin")
    BigDecimal calcularTotalIngresosPorTipoYFecha(@Param("tipoIngreso") Ingreso.TipoIngreso tipoIngreso,
                                                  @Param("fechaInicio") LocalDate fechaInicio,
                                                  @Param("fechaFin") LocalDate fechaFin);

    @Query("SELECT YEAR(i.fecha) as año, MONTH(i.fecha) as mes, SUM(i.monto) as total " +
           "FROM Ingreso i WHERE i.user.id = :usuarioId " +
           "GROUP BY YEAR(i.fecha), MONTH(i.fecha) " +
           "ORDER BY año DESC, mes DESC")
    List<Object[]> obtenerEstadisticasIngresosPorMes(@Param("usuarioId") Long usuarioId);

    long countByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(i.monto), 0) FROM Ingreso i")
    BigDecimal sumAllIngresos();

    @Query("SELECT COALESCE(SUM(i.monto), 0) FROM Ingreso i WHERE i.user.id = :usuarioId")
    BigDecimal sumIngresosByUsuarioId(@Param("usuarioId") Long usuarioId);

    List<Ingreso> findByUserIdAndActivoTrue(Long userId);
    long countByUserIdAndActivoTrue(Long userId);
    List<Ingreso> findByActivoTrue();

    List<Ingreso> findByUserId(Long userId);
}
