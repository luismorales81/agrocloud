package com.agrocloud.repository;

import com.agrocloud.model.entity.Insumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface InsumoRepository extends JpaRepository<Insumo, Long> {
    
    // Buscar insumos activos
    List<Insumo> findByActivoTrueOrderByNombreAsc();
    
    // Buscar insumos por tipo
    List<Insumo> findByTipoAndActivoTrueOrderByNombreAsc(String tipo);
    
    // Buscar insumos por proveedor
    List<Insumo> findByProveedorAndActivoTrueOrderByNombreAsc(String proveedor);
    
    // Buscar insumos con stock bajo
    @Query("SELECT i FROM Insumo i WHERE i.stockActual <= i.stockMinimo AND i.activo = true ORDER BY i.stockActual ASC")
    List<Insumo> findInsumosConStockBajo();
    
    // Buscar insumos próximos a vencer
    @Query("SELECT i FROM Insumo i WHERE i.fechaVencimiento IS NOT NULL AND i.fechaVencimiento <= :fechaLimite AND i.activo = true ORDER BY i.fechaVencimiento ASC")
    List<Insumo> findInsumosProximosAVencer(@Param("fechaLimite") LocalDate fechaLimite);
    
    // Buscar insumos vencidos
    @Query("SELECT i FROM Insumo i WHERE i.fechaVencimiento IS NOT NULL AND i.fechaVencimiento < :hoy AND i.activo = true ORDER BY i.fechaVencimiento ASC")
    List<Insumo> findInsumosVencidos(@Param("hoy") LocalDate hoy);
    
    // Contar insumos con stock bajo
    @Query("SELECT COUNT(i) FROM Insumo i WHERE i.stockActual <= i.stockMinimo AND i.activo = true")
    Long countInsumosConStockBajo();
    
    // Contar insumos próximos a vencer
    @Query("SELECT COUNT(i) FROM Insumo i WHERE i.fechaVencimiento IS NOT NULL AND i.fechaVencimiento <= :fechaLimite AND i.activo = true")
    Long countInsumosProximosAVencer(@Param("fechaLimite") LocalDate fechaLimite);
    
    // Calcular valor total del inventario
    @Query("SELECT SUM(i.stockActual * i.precioUnitario) FROM Insumo i WHERE i.activo = true")
    BigDecimal calcularValorTotalInventario();
    
    // Buscar insumos por rango de stock
    @Query("SELECT i FROM Insumo i WHERE i.stockActual BETWEEN :stockMin AND :stockMax AND i.activo = true ORDER BY i.stockActual ASC")
    List<Insumo> findByRangoStock(@Param("stockMin") BigDecimal stockMin, @Param("stockMax") BigDecimal stockMax);
    
    // Buscar insumos por rango de precios
    @Query("SELECT i FROM Insumo i WHERE i.precioUnitario BETWEEN :precioMin AND :precioMax AND i.activo = true ORDER BY i.precioUnitario ASC")
    List<Insumo> findByRangoPrecio(@Param("precioMin") BigDecimal precioMin, @Param("precioMax") BigDecimal precioMax);
}
