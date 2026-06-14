package com.agrocloud.cultivos.infrastructure;

import com.agrocloud.cultivos.domain.Plot;
import com.agrocloud.core.domain.User;
import com.agrocloud.model.enums.EstadoLote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository("plotRepositoryCultivos")
public interface PlotRepository extends JpaRepository<Plot, Long> {

    // Buscar lotes por usuario propietario
    List<Plot> findByUserId(Long userId);

    // Buscar lotes por usuario y estado activo
    List<Plot> findByUserIdAndActivoTrue(Long userId);

    // Buscar lotes por usuario y estado
    List<Plot> findByUserIdAndEstado(Long userId, String estado);

    // Buscar lotes por campo y usuario
    @Query("SELECT p FROM Plot p WHERE p.campo.id = :campoId AND p.user.id = :userId")
    List<Plot> findByCampoIdAndUserId(@Param("campoId") Long campoId, @Param("userId") Long userId);

    // Query personalizada para buscar lotes accesibles por un usuario
    @Query("SELECT p FROM Plot p WHERE " +
           "p.user = :user OR " +
           "p.user IN (SELECT u FROM User u WHERE u.parentUser = :user) OR " +
           "p.user IN (SELECT u FROM User u WHERE u.parentUser IN (SELECT c FROM User c WHERE c.parentUser = :user))")
    List<Plot> findAccessibleByUser(@Param("user") User user);
    
    // Query personalizada para buscar lotes accesibles y activos por un usuario
    @Query("SELECT p FROM Plot p LEFT JOIN FETCH p.campo WHERE p.activo = true AND (" +
           "p.user = :user OR " +
           "p.user IN (SELECT u FROM User u WHERE u.parentUser = :user) OR " +
           "p.user IN (SELECT u FROM User u WHERE u.parentUser IN (SELECT c FROM User c WHERE c.parentUser = :user)))")
    List<Plot> findAccessibleByUserAndActivoTrue(@Param("user") User user);

    // Query para buscar lotes por nombre (filtrado por usuario)
    @Query("SELECT p FROM Plot p WHERE p.user.id = :userId AND p.nombre LIKE %:nombre%")
    List<Plot> findByUserIdAndNombreContaining(@Param("userId") Long userId, @Param("nombre") String nombre);

    // Contar lotes por usuario
    long countByUserId(Long userId);

    // Contar lotes activos por usuario
    long countByUserIdAndActivoTrue(Long userId);

    // Buscar todos los lotes activos
    @Query("SELECT p FROM Plot p LEFT JOIN FETCH p.campo WHERE p.activo = true")
    List<Plot> findByActivoTrue();

    // Métodos para eliminación lógica
    List<Plot> findByActivoFalse();
    List<Plot> findByUserIdAndActivoFalse(Long userId);
    
    // Métodos para buscar por estado
    List<Plot> findByEstado(EstadoLote estado);
    List<Plot> findByEstadoIn(List<EstadoLote> estados);
    
    // Método para buscar por usuario o usuario padre
    @Query("SELECT p FROM Plot p WHERE " +
           "p.user.id = :userId OR " +
           "p.user.parentUser.id = :userId OR " +
           "p.user.parentUser.id IN (SELECT u.id FROM User u WHERE u.parentUser.id = :userId)")
    List<Plot> findByUserIdOrParentUserId(@Param("userId") Long userId);
    
    // Métodos faltantes para los tests
    @Query("SELECT p FROM Plot p WHERE p.campo.id = :campoId")
    List<Plot> findByCampoId(@Param("campoId") Long campoId);
    List<Plot> findByNombreContainingIgnoreCase(String nombre);
    
    // Buscar lotes activos por campo
    @Query("SELECT p FROM Plot p WHERE p.campo.id = :campoId AND p.activo = true")
    List<Plot> findByCampoIdAndActivoTrue(@Param("campoId") Long campoId);
    
    // Query para calcular la superficie total ocupada por lotes activos de un campo
    @Query("SELECT COALESCE(SUM(p.areaHectareas), 0) FROM Plot p WHERE p.campo.id = :campoId AND p.activo = true")
    java.math.BigDecimal calcularSuperficieOcupadaPorCampo(@Param("campoId") Long campoId);

    /** Lotes de uso porcino por empresa (para LoteParaPorcinosQuery sin depender de User en el puerto). */
    List<Plot> findByCampo_Empresa_IdAndTipoUsoAndActivoTrue(Long empresaId, Plot.TipoUsoLote tipoUso);

    /** Lotes activos de todos los campos de una empresa (para mostrar lotes por empresa, no solo por user_id). */
    @Query("SELECT p FROM Plot p LEFT JOIN FETCH p.campo WHERE p.campo.empresa.id = :empresaId AND p.activo = true")
    List<Plot> findByCampo_Empresa_IdAndActivoTrue(@Param("empresaId") Long empresaId);

    @Query("SELECT DISTINCT p FROM Plot p LEFT JOIN FETCH p.campo WHERE p.id IN :ids AND p.activo = true " +
           "AND p.fechaCosechaEsperada IS NOT NULL AND p.fechaCosechaEsperada BETWEEN :desde AND :hasta")
    List<Plot> findByIdInAndFechaCosechaEsperadaBetween(
            @Param("ids") List<Long> ids,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);

    @Query("SELECT DISTINCT p FROM Plot p JOIN FETCH p.campo c JOIN FETCH c.empresa e WHERE p.id = :id")
    Optional<Plot> findByIdConCampoYEmpresa(@Param("id") Long id);
}
