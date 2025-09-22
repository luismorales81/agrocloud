package com.agrocloud.repository;

import com.agrocloud.model.entity.Labor;
import com.agrocloud.model.entity.LaborManoObra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LaborManoObraRepository extends JpaRepository<LaborManoObra, Long> {
    
    /**
     * Buscar toda la mano de obra de una labor específica
     */
    List<LaborManoObra> findByLabor(Labor labor);
    
    /**
     * Buscar toda la mano de obra de una labor por ID
     */
    @Query("SELECT lmo FROM LaborManoObra lmo WHERE lmo.labor.id = :laborId")
    List<LaborManoObra> findByLaborId(@Param("laborId") Long laborId);
    
    /**
     * Calcular el costo total de mano de obra para una labor
     */
    @Query("SELECT COALESCE(SUM(lmo.costoTotal), 0) FROM LaborManoObra lmo WHERE lmo.labor.id = :laborId")
    Double calcularCostoTotalManoObra(@Param("laborId") Long laborId);
    
    /**
     * Buscar mano de obra por proveedor
     */
    List<LaborManoObra> findByProveedorContainingIgnoreCase(String proveedor);
    
    /**
     * Buscar mano de obra por cantidad de personas
     */
    List<LaborManoObra> findByCantidadPersonas(Integer cantidadPersonas);
    
    /**
     * Eliminar toda la mano de obra de una labor
     */
    void deleteByLabor(Labor labor);
}
