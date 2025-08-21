package com.agrocloud.repository;

import com.agrocloud.model.entity.Plot;
import com.agrocloud.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlotRepository extends JpaRepository<Plot, Long> {

    // Buscar lotes por usuario propietario
    List<Plot> findByUserId(Long userId);

    // Buscar lotes por usuario y estado activo
    List<Plot> findByUserIdAndActivoTrue(Long userId);

    // Buscar lotes por usuario y estado
    List<Plot> findByUserIdAndEstado(Long userId, String estado);

    // Buscar lotes por campo y usuario
    List<Plot> findByCampoIdAndUserId(Long campoId, Long userId);

    // Query personalizada para buscar lotes accesibles por un usuario
    @Query("SELECT p FROM Plot p WHERE " +
           "p.user = :user OR " +
           "p.user IN (SELECT u FROM User u WHERE u.parentUser = :user) OR " +
           "p.user IN (SELECT u FROM User u WHERE u.parentUser IN (SELECT c FROM User c WHERE c.parentUser = :user))")
    List<Plot> findAccessibleByUser(@Param("user") User user);

    // Query para buscar lotes por nombre (filtrado por usuario)
    @Query("SELECT p FROM Plot p WHERE p.user.id = :userId AND p.nombre LIKE %:nombre%")
    List<Plot> findByUserIdAndNombreContaining(@Param("userId") Long userId, @Param("nombre") String nombre);

    // Contar lotes por usuario
    long countByUserId(Long userId);

    // Contar lotes activos por usuario
    long countByUserIdAndActivoTrue(Long userId);
}
