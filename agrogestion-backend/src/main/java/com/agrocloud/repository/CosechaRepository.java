package com.agrocloud.repository;

import com.agrocloud.model.entity.Cosecha;
import com.agrocloud.model.entity.Cultivo;
import com.agrocloud.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio para la gestión de cosechas
 * 
 * @deprecated Este repositorio está deprecado. Usar {@link HistorialCosechaRepository} en su lugar.
 * La tabla 'cosechas' será eliminada en la migración V1_12.
 * Razón: Sistema unificado con historial_cosechas que tiene campos completos para análisis y reportes.
 */
@Deprecated
@Repository
public interface CosechaRepository extends JpaRepository<Cosecha, Long> {

    // Buscar cosechas por cultivo
    List<Cosecha> findByCultivo(Cultivo cultivo);

    // Buscar cosechas por cultivo ID
    List<Cosecha> findByCultivoId(Long cultivoId);

    // Buscar cosechas por usuario
    List<Cosecha> findByUsuario(User usuario);

    // Buscar cosechas por usuario ID
    List<Cosecha> findByUsuarioId(Long usuarioId);

    // Buscar cosechas por cultivo y usuario (para verificar permisos)
    List<Cosecha> findByCultivoIdAndUsuarioId(Long cultivoId, Long usuarioId);

    // Buscar cosechas por rango de fechas
    List<Cosecha> findByFechaBetween(LocalDate fechaInicio, LocalDate fechaFin);

    // Buscar cosechas por cultivo y rango de fechas
    List<Cosecha> findByCultivoIdAndFechaBetween(Long cultivoId, LocalDate fechaInicio, LocalDate fechaFin);

    // Buscar cosechas por usuario y rango de fechas
    List<Cosecha> findByUsuarioIdAndFechaBetween(Long usuarioId, LocalDate fechaInicio, LocalDate fechaFin);

    // Contar cosechas por cultivo
    long countByCultivoId(Long cultivoId);

    // Contar cosechas por usuario
    long countByUsuarioId(Long usuarioId);

    // Obtener cosechas ordenadas por fecha descendente
    List<Cosecha> findByCultivoIdOrderByFechaDesc(Long cultivoId);

    // Obtener cosechas por usuario ordenadas por fecha descendente
    List<Cosecha> findByUsuarioIdOrderByFechaDesc(Long usuarioId);

    // Calcular rendimiento promedio por cultivo
    @Query("SELECT AVG(c.cantidadToneladas) FROM Cosecha c WHERE c.cultivo.id = :cultivoId")
    Double calcularRendimientoPromedioPorCultivo(@Param("cultivoId") Long cultivoId);

    // Calcular rendimiento promedio por usuario
    @Query("SELECT AVG(c.cantidadToneladas) FROM Cosecha c WHERE c.usuario.id = :usuarioId")
    Double calcularRendimientoPromedioPorUsuario(@Param("usuarioId") Long usuarioId);

    // Obtener estadísticas de cosechas por cultivo
    @Query("SELECT " +
           "COUNT(c) as totalCosechas, " +
           "AVG(c.cantidadToneladas) as rendimientoPromedio, " +
           "MIN(c.cantidadToneladas) as rendimientoMinimo, " +
           "MAX(c.cantidadToneladas) as rendimientoMaximo, " +
           "SUM(c.cantidadToneladas) as cantidadTotal " +
           "FROM Cosecha c WHERE c.cultivo.id = :cultivoId")
    Object[] obtenerEstadisticasPorCultivo(@Param("cultivoId") Long cultivoId);

    // Obtener estadísticas de cosechas por usuario
    @Query("SELECT " +
           "COUNT(c) as totalCosechas, " +
           "AVG(c.cantidadToneladas) as rendimientoPromedio, " +
           "MIN(c.cantidadToneladas) as rendimientoMinimo, " +
           "MAX(c.cantidadToneladas) as rendimientoMaximo, " +
           "SUM(c.cantidadToneladas) as cantidadTotal " +
           "FROM Cosecha c WHERE c.usuario.id = :usuarioId")
    Object[] obtenerEstadisticasPorUsuario(@Param("usuarioId") Long usuarioId);

    // Buscar cosechas con observaciones que contengan texto específico
    @Query("SELECT c FROM Cosecha c WHERE c.usuario.id = :usuarioId AND c.observaciones LIKE %:texto%")
    List<Cosecha> findByUsuarioIdAndObservacionesContaining(@Param("usuarioId") Long usuarioId, @Param("texto") String texto);

    // Obtener cosechas del año actual por usuario
    @Query("SELECT c FROM Cosecha c WHERE c.usuario.id = :usuarioId AND YEAR(c.fecha) = YEAR(CURRENT_DATE)")
    List<Cosecha> findCosechasAnioActualPorUsuario(@Param("usuarioId") Long usuarioId);

    // Obtener cosechas del año actual por cultivo
    @Query("SELECT c FROM Cosecha c WHERE c.cultivo.id = :cultivoId AND YEAR(c.fecha) = YEAR(CURRENT_DATE)")
    List<Cosecha> findCosechasAnioActualPorCultivo(@Param("cultivoId") Long cultivoId);
    
    // Método faltante para los tests
    List<Cosecha> findByLoteId(Long loteId);
}
