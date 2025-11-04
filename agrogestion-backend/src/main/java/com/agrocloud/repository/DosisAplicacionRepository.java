package com.agrocloud.repository;

import com.agrocloud.model.entity.DosisAplicacion;
import com.agrocloud.model.entity.Insumo;
import com.agrocloud.model.enums.TipoAplicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DosisAplicacionRepository extends JpaRepository<DosisAplicacion, Long> {
    
    /**
     * Buscar todas las dosis de aplicación de un insumo
     */
    List<DosisAplicacion> findByInsumoAndActivoTrue(Insumo insumo);
    
    /**
     * Buscar una dosis específica de un insumo por tipo de aplicación
     */
    Optional<DosisAplicacion> findByInsumoAndTipoAplicacionAndActivoTrue(
        Insumo insumo, 
        TipoAplicacion tipoAplicacion
    );
    
    /**
     * Buscar todas las dosis activas
     */
    List<DosisAplicacion> findByActivoTrue();
    
    /**
     * Buscar dosis por insumo (incluyendo inactivas)
     */
    List<DosisAplicacion> findByInsumo(Insumo insumo);
    
    /**
     * Buscar dosis por tipo de aplicación
     */
    List<DosisAplicacion> findByTipoAplicacionAndActivoTrue(TipoAplicacion tipoAplicacion);
    
    /**
     * Contar dosis activas de un insumo
     */
    long countByInsumoAndActivoTrue(Insumo insumo);
}

