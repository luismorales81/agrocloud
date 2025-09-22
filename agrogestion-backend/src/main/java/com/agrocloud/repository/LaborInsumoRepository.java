package com.agrocloud.repository;

import com.agrocloud.model.entity.LaborInsumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad LaborInsumo.
 * Proporciona métodos para acceder a los datos de insumos utilizados en las labores.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@Repository
public interface LaborInsumoRepository extends JpaRepository<LaborInsumo, Long> {
    
    /**
     * Busca todos los insumos utilizados en una labor específica.
     * 
     * @param laborId ID de la labor
     * @return Lista de insumos utilizados en la labor
     */
    List<LaborInsumo> findByLaborId(Long laborId);
    
    /**
     * Busca todos los insumos utilizados en una labor específica con la información del insumo cargada.
     * 
     * @param laborId ID de la labor
     * @return Lista de insumos utilizados en la labor con información completa del insumo
     */
    @Query("SELECT li FROM LaborInsumo li JOIN FETCH li.insumo WHERE li.labor.id = :laborId")
    List<LaborInsumo> findByLaborIdWithInsumo(@Param("laborId") Long laborId);
    
    /**
     * Busca todos los insumos utilizados en una labor específica con la información de la labor cargada.
     * 
     * @param laborId ID de la labor
     * @return Lista de insumos utilizados en la labor con información completa de la labor
     */
    @Query("SELECT li FROM LaborInsumo li JOIN FETCH li.labor WHERE li.labor.id = :laborId")
    List<LaborInsumo> findByLaborIdWithLabor(@Param("laborId") Long laborId);
    
    /**
     * Busca todos los insumos utilizados en una labor específica con toda la información relacionada.
     * 
     * @param laborId ID de la labor
     * @return Lista de insumos utilizados en la labor con información completa
     */
    @Query("SELECT li FROM LaborInsumo li JOIN FETCH li.insumo JOIN FETCH li.labor WHERE li.labor.id = :laborId")
    List<LaborInsumo> findByLaborIdWithAllDetails(@Param("laborId") Long laborId);
    
    /**
     * Elimina todos los insumos utilizados en una labor específica.
     * 
     * @param laborId ID de la labor
     */
    void deleteByLaborId(Long laborId);
    
    /**
     * Cuenta cuántos insumos se utilizaron en una labor específica.
     * 
     * @param laborId ID de la labor
     * @return Número de insumos utilizados en la labor
     */
    long countByLaborId(Long laborId);
    
    /**
     * Busca todos los insumos utilizados por un usuario específico.
     * 
     * @param userId ID del usuario
     * @return Lista de insumos utilizados por el usuario
     */
    @Query("SELECT li FROM LaborInsumo li JOIN FETCH li.labor l WHERE l.usuario.id = :userId")
    List<LaborInsumo> findByUserId(@Param("userId") Long userId);
    
    /**
     * Busca todos los insumos utilizados en un lote específico.
     * 
     * @param loteId ID del lote
     * @return Lista de insumos utilizados en el lote
     */
    @Query("SELECT li FROM LaborInsumo li JOIN FETCH li.labor l WHERE l.lote.id = :loteId")
    List<LaborInsumo> findByLoteId(@Param("loteId") Long loteId);
}
