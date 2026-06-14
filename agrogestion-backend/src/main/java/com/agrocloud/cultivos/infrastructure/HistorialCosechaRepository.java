package com.agrocloud.cultivos.infrastructure;

import com.agrocloud.core.domain.User;
import com.agrocloud.cultivos.domain.Plot;
import com.agrocloud.cultivos.domain.HistorialCosecha;
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
@Repository("historialCosechaRepositoryCultivos")
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



    @Query("SELECT h FROM HistorialCosecha h JOIN FETCH h.lote JOIN FETCH h.cultivo WHERE h.lote.id = :loteId ORDER BY h.fechaCosecha DESC")

    List<HistorialCosecha> findByLoteIdConLoteYCultivo(@Param("loteId") Long loteId);



    @Query("SELECT h FROM HistorialCosecha h JOIN FETCH h.lote JOIN FETCH h.cultivo WHERE h.lote.id = :loteId AND (" +

           "h.usuario = :user OR " +

           "h.usuario IN (SELECT u FROM User u WHERE u.parentUser = :user) OR " +

           "h.usuario IN (SELECT u FROM User u WHERE u.parentUser IN (SELECT c FROM User c WHERE c.parentUser = :user))) " +

           "ORDER BY h.fechaCosecha DESC")

    List<HistorialCosecha> findAccessibleByUserAndLoteConLoteYCultivo(@Param("user") User user, @Param("loteId") Long loteId);



    @Query("SELECT h FROM HistorialCosecha h JOIN FETCH h.lote JOIN FETCH h.cultivo ORDER BY h.fechaCosecha DESC")

    List<HistorialCosecha> findAllConLoteYCultivo();



    @Query("SELECT h FROM HistorialCosecha h JOIN FETCH h.lote JOIN FETCH h.cultivo WHERE " +

           "h.usuario = :user OR " +

           "h.usuario IN (SELECT u FROM User u WHERE u.parentUser = :user) OR " +

           "h.usuario IN (SELECT u FROM User u WHERE u.parentUser IN (SELECT c FROM User c WHERE c.parentUser = :user)) " +

           "ORDER BY h.fechaCosecha DESC")

    List<HistorialCosecha> findAccessibleByUserConLoteYCultivo(@Param("user") User user);



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

    @Query("SELECT h FROM HistorialCosecha h " +
           "JOIN FETCH h.lote l " +
           "JOIN FETCH l.campo c " +
           "JOIN FETCH c.empresa " +
           "JOIN FETCH h.cultivo " +
           "WHERE h.id = :id")
    Optional<HistorialCosecha> findByIdConLoteCampoEmpresa(@Param("id") Long id);

}

