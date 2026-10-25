package com.agrocloud.core.inventory.infrastructure;

import com.agrocloud.core.inventory.domain.Insumo;
import com.agrocloud.core.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository("insumoRepositoryInventario")
public interface InsumoRepository extends JpaRepository<Insumo, Long> {

    List<Insumo> findByUserId(Long userId);

    List<Insumo> findByUserIdAndActivoTrue(Long userId);

    List<Insumo> findByEmpresaIdAndActivoTrue(Long empresaId);

    @Query("SELECT i FROM Insumo i JOIN FETCH i.empresa WHERE i.id = :id")
    java.util.Optional<Insumo> buscarPorIdConEmpresa(@Param("id") Long id);

    @Query(value = "SELECT i.id, i.nombre, i.descripcion, i.tipo, i.unidad_medida, "
            + "i.precio_unitario, i.stock_actual, i.stock_minimo, i.proveedor, i.activo "
            + "FROM cultivo_insumos i "
            + "WHERE (i.activo IS NULL OR i.activo = TRUE) "
            + "AND i.empresa_id = :empresaId "
            + "ORDER BY i.nombre",
            nativeQuery = true)
    List<Object[]> listarFilasPorEmpresa(@Param("empresaId") Long empresaId);

    @Query(value = "SELECT i.id, i.nombre, i.descripcion, i.tipo, i.unidad_medida, "
            + "i.precio_unitario, i.stock_actual, i.stock_minimo, i.proveedor, i.activo "
            + "FROM cultivo_insumos i "
            + "WHERE (i.activo IS NULL OR i.activo = TRUE) "
            + "ORDER BY i.nombre",
            nativeQuery = true)
    List<Object[]> listarFilasActivas();

    List<Insumo> findByUserIdAndTipo(Long userId, Insumo.TipoInsumo tipo);

    @Query("SELECT i FROM Insumo i WHERE i.user.id = :userId AND i.stockActual <= i.stockMinimo")
    List<Insumo> findStockBajoByUserId(@Param("userId") Long userId);

    @Query("SELECT i FROM Insumo i WHERE i.user.id = :userId AND i.fechaVencimiento IS NOT NULL AND i.fechaVencimiento <= :fechaLimite")
    List<Insumo> findProximosAVencerByUserId(@Param("userId") Long userId, @Param("fechaLimite") LocalDate fechaLimite);

    @Query("SELECT i FROM Insumo i WHERE " +
           "i.user = :user OR " +
           "i.user IN (SELECT u FROM User u WHERE u.parentUser = :user) OR " +
           "i.user IN (SELECT u FROM User u WHERE u.parentUser IN (SELECT c FROM User c WHERE c.parentUser = :user))")
    List<Insumo> findAccessibleByUser(@Param("user") User user);

    @Query("SELECT i FROM Insumo i WHERE i.user.id = :userId AND i.nombre LIKE %:nombre%")
    List<Insumo> findByUserIdAndNombreContaining(@Param("userId") Long userId, @Param("nombre") String nombre);

    long countByUserId(Long userId);

    long countByUserIdAndActivoTrue(Long userId);

    List<Insumo> findByActivoTrue();
    List<Insumo> findByActivoFalse();
    List<Insumo> findByUserIdAndActivoFalse(Long userId);

    List<Insumo> findByNombreContainingIgnoreCase(String nombre);
    List<Insumo> findByTipo(Insumo.TipoInsumo tipo);
    List<Insumo> findByProveedor(String proveedor);

    @Query("SELECT i FROM Insumo i WHERE i.activo = true AND i.tipo IN ('HERBICIDA', 'FUNGICIDA', 'INSECTICIDA', 'FERTILIZANTE')")
    List<Insumo> findAgroquimicosActivos();

    List<Insumo> findByTipoInAndActivoTrue(Insumo.TipoInsumo... tipos);

    @Query("SELECT i FROM Insumo i WHERE i.activo = true AND i.principioActivo IS NOT NULL AND i.principioActivo != ''")
    List<Insumo> findAgroquimicosConPropiedades();

    List<Insumo> findByPrincipioActivoContainingIgnoreCaseAndActivoTrue(String principioActivo);

    List<Insumo> findByClaseQuimicaContainingIgnoreCaseAndActivoTrue(String claseQuimica);

    @Query("SELECT i FROM Insumo i WHERE i.activo = true AND i.tipo IN ('HERBICIDA', 'FUNGICIDA', 'INSECTICIDA', 'FERTILIZANTE') " +
           "AND i.stockActual >= :cantidadMinima")
    List<Insumo> findAgroquimicosConStockSuficiente(@Param("cantidadMinima") Double cantidadMinima);
}
