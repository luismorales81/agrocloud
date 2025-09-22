package com.agrocloud.repository;

import com.agrocloud.model.entity.HistorialCosecha;
import com.agrocloud.model.entity.Plot;
import com.agrocloud.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar el historial de cosechas
 */
@Repository
public interface HistorialCosechaRepository extends JpaRepository<HistorialCosecha, Long> {

    // Buscar historial por lote
    List<HistorialCosecha> findByLoteOrderByFechaCosechaDesc(Plot lote);

    // Buscar historial por lote ID
    List<HistorialCosecha> findByLoteIdOrderByFechaCosechaDesc(Long loteId);

    // Buscar historial por usuario
    List<HistorialCosecha> findByUsuarioOrderByFechaCosechaDesc(User usuario);

    // Buscar historial por usuario ID
    List<HistorialCosecha> findByUsuarioIdOrderByFechaCosechaDesc(Long usuarioId);

    // Buscar última cosecha de un lote
    Optional<HistorialCosecha> findFirstByLoteOrderByFechaCosechaDesc(Plot lote);

    // Buscar última cosecha de un lote por ID
    Optional<HistorialCosecha> findFirstByLoteIdOrderByFechaCosechaDesc(Long loteId);

    // Buscar cosechas en un rango de fechas
    List<HistorialCosecha> findByFechaCosechaBetweenOrderByFechaCosechaDesc(LocalDate fechaInicio, LocalDate fechaFin);

    // Buscar cosechas por cultivo
    List<HistorialCosecha> findByCultivoIdOrderByFechaCosechaDesc(Long cultivoId);

    // Buscar cosechas que requieren descanso
    List<HistorialCosecha> findByRequiereDescansoTrueOrderByFechaCosechaDesc();

    // Buscar cosechas por lote que requieren descanso
    List<HistorialCosecha> findByLoteIdAndRequiereDescansoTrueOrderByFechaCosechaDesc(Long loteId);

    // Contar cosechas por lote
    long countByLoteId(Long loteId);

    // Contar cosechas por usuario
    long countByUsuarioId(Long usuarioId);

    // Buscar cosechas por estado del suelo
    List<HistorialCosecha> findByEstadoSueloOrderByFechaCosechaDesc(String estadoSuelo);

    // Query personalizada para buscar historial accesible por usuario
    @Query("SELECT h FROM HistorialCosecha h WHERE " +
           "h.usuario = :user OR " +
           "h.usuario IN (SELECT u FROM User u WHERE u.parentUser = :user) OR " +
           "h.usuario IN (SELECT u FROM User u WHERE u.parentUser IN (SELECT c FROM User c WHERE c.parentUser = :user))")
    List<HistorialCosecha> findAccessibleByUser(@Param("user") User user);

    // Query para buscar historial por lote accesible por usuario
    @Query("SELECT h FROM HistorialCosecha h WHERE h.lote.id = :loteId AND (" +
           "h.usuario = :user OR " +
           "h.usuario IN (SELECT u FROM User u WHERE u.parentUser = :user) OR " +
           "h.usuario IN (SELECT u FROM User u WHERE u.parentUser IN (SELECT c FROM User c WHERE c.parentUser = :user))) " +
           "ORDER BY h.fechaCosecha DESC")
    List<HistorialCosecha> findAccessibleByUserAndLote(@Param("user") User user, @Param("loteId") Long loteId);

    // Query para obtener estadísticas de rendimiento por cultivo
    @Query("SELECT h.cultivo.nombre, AVG(h.rendimientoReal), COUNT(h) " +
           "FROM HistorialCosecha h WHERE h.usuario = :user " +
           "GROUP BY h.cultivo.id, h.cultivo.nombre " +
           "ORDER BY AVG(h.rendimientoReal) DESC")
    List<Object[]> getEstadisticasRendimientoPorCultivo(@Param("user") User user);

    // Query para obtener cosechas recientes (últimos 30 días)
    @Query("SELECT h FROM HistorialCosecha h WHERE h.fechaCosecha >= :fechaInicio AND " +
           "(h.usuario = :user OR " +
           "h.usuario IN (SELECT u FROM User u WHERE u.parentUser = :user) OR " +
           "h.usuario IN (SELECT u FROM User u WHERE u.parentUser IN (SELECT c FROM User c WHERE c.parentUser = :user))) " +
           "ORDER BY h.fechaCosecha DESC")
    List<HistorialCosecha> findCosechasRecientes(@Param("user") User user, @Param("fechaInicio") LocalDate fechaInicio);

    // Query para verificar si un lote puede ser liberado (sin cosechas recientes)
    @Query("SELECT COUNT(h) FROM HistorialCosecha h WHERE h.lote.id = :loteId AND h.fechaCosecha >= :fechaMinima")
    long countCosechasRecientesPorLote(@Param("loteId") Long loteId, @Param("fechaMinima") LocalDate fechaMinima);
}
