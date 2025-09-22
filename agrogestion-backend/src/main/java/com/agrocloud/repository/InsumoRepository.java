package com.agrocloud.repository;

import com.agrocloud.model.entity.Insumo;
import com.agrocloud.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InsumoRepository extends JpaRepository<Insumo, Long> {

    // Buscar insumos por usuario propietario
    List<Insumo> findByUserId(Long userId);

    // Buscar insumos por usuario y estado activo
    List<Insumo> findByUserIdAndActivoTrue(Long userId);

    // Buscar insumos por usuario y tipo
    List<Insumo> findByUserIdAndTipo(Long userId, Insumo.TipoInsumo tipo);

    // Buscar insumos por usuario y stock bajo
    @Query("SELECT i FROM Insumo i WHERE i.user.id = :userId AND i.stockActual <= i.stockMinimo")
    List<Insumo> findStockBajoByUserId(@Param("userId") Long userId);

    // Buscar insumos por usuario y próximos a vencer
    @Query("SELECT i FROM Insumo i WHERE i.user.id = :userId AND i.fechaVencimiento IS NOT NULL AND i.fechaVencimiento <= :fechaLimite")
    List<Insumo> findProximosAVencerByUserId(@Param("userId") Long userId, @Param("fechaLimite") LocalDate fechaLimite);

    // Query personalizada para buscar insumos accesibles por un usuario
    @Query("SELECT i FROM Insumo i WHERE " +
           "i.user = :user OR " +
           "i.user IN (SELECT u FROM User u WHERE u.parentUser = :user) OR " +
           "i.user IN (SELECT u FROM User u WHERE u.parentUser IN (SELECT c FROM User c WHERE c.parentUser = :user))")
    List<Insumo> findAccessibleByUser(@Param("user") User user);

    // Query para buscar insumos por nombre (filtrado por usuario)
    @Query("SELECT i FROM Insumo i WHERE i.user.id = :userId AND i.nombre LIKE %:nombre%")
    List<Insumo> findByUserIdAndNombreContaining(@Param("userId") Long userId, @Param("nombre") String nombre);

    // Contar insumos por usuario
    long countByUserId(Long userId);

    // Contar insumos activos por usuario
    long countByUserIdAndActivoTrue(Long userId);

    // Métodos para eliminación lógica
    List<Insumo> findByActivoTrue();
    List<Insumo> findByActivoFalse();
    List<Insumo> findByUserIdAndActivoFalse(Long userId);
}
