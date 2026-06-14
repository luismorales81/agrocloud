package com.agrocloud.cultivos.infrastructure;

import com.agrocloud.cultivos.domain.Labor;
import com.agrocloud.cultivos.domain.LaborMaquinaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("laborMaquinariaRepositoryCultivos")
public interface LaborMaquinariaRepository extends JpaRepository<LaborMaquinaria, Long> {
    
    /**
     * Buscar todas las maquinarias de una labor específica
     */
    List<LaborMaquinaria> findByLabor(Labor labor);
    
    /**
     * Buscar todas las maquinarias de una labor por ID
     */
    @Query("SELECT lm FROM LaborMaquinaria lm WHERE lm.labor.id = :laborId")
    List<LaborMaquinaria> findByLaborId(@Param("laborId") Long laborId);

    @Query("SELECT lm FROM LaborMaquinaria lm WHERE lm.labor.id IN :laborIds")
    List<LaborMaquinaria> findByLaborIdIn(@Param("laborIds") List<Long> laborIds);
    
    /**
     * Calcular el costo total de maquinaria para una labor
     */
    @Query("SELECT COALESCE(SUM(lm.costo), 0) FROM LaborMaquinaria lm WHERE lm.labor.id = :laborId")
    Double calcularCostoTotalMaquinaria(@Param("laborId") Long laborId);
    
    /**
     * Buscar maquinarias por proveedor
     */
    List<LaborMaquinaria> findByProveedorContainingIgnoreCase(String proveedor);
    
    /**
     * Eliminar todas las maquinarias de una labor
     */
    void deleteByLabor(Labor labor);
}
