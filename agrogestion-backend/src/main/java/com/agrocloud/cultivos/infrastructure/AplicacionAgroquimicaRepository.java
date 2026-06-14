package com.agrocloud.cultivos.infrastructure;

import com.agrocloud.cultivos.domain.AplicacionAgroquimica;
import com.agrocloud.core.inventory.domain.Insumo;
import com.agrocloud.cultivos.domain.Labor;
import com.agrocloud.model.enums.TipoAplicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository("aplicacionAgroquimicaRepositoryCultivos")
public interface AplicacionAgroquimicaRepository extends JpaRepository<AplicacionAgroquimica, Long> {
    
    /**
     * Buscar todas las aplicaciones de una labor (tarea)
     */
    List<AplicacionAgroquimica> findByLaborAndActivoTrue(Labor labor);
    
    /**
     * Buscar todas las aplicaciones de un insumo
     */
    List<AplicacionAgroquimica> findByInsumoAndActivoTrue(Insumo insumo);
    
    /**
     * Buscar aplicaciones por tipo de aplicación
     */
    List<AplicacionAgroquimica> findByTipoAplicacionAndActivoTrue(TipoAplicacion tipoAplicacion);
    
    /**
     * Buscar aplicaciones activas en un rango de fechas
     */
    @Query("SELECT a FROM AplicacionAgroquimica a WHERE a.fechaAplicacion BETWEEN :fechaInicio AND :fechaFin AND a.activo = true")
    List<AplicacionAgroquimica> findByFechaAplicacionBetween(
        @Param("fechaInicio") LocalDateTime fechaInicio,
        @Param("fechaFin") LocalDateTime fechaFin
    );
    
    /**
     * Buscar aplicaciones de un insumo en un rango de fechas
     */
    @Query("SELECT a FROM AplicacionAgroquimica a WHERE a.insumo = :insumo AND a.fechaAplicacion BETWEEN :fechaInicio AND :fechaFin AND a.activo = true")
    List<AplicacionAgroquimica> findByInsumoAndFechaAplicacionBetween(
        @Param("insumo") Insumo insumo,
        @Param("fechaInicio") LocalDateTime fechaInicio,
        @Param("fechaFin") LocalDateTime fechaFin
    );
    
    /**
     * Contar aplicaciones activas de un insumo
     */
    long countByInsumoAndActivoTrue(Insumo insumo);
    
    /**
     * Sumar la cantidad total aplicada de un insumo
     */
    @Query("SELECT COALESCE(SUM(a.cantidadTotalAplicar), 0) FROM AplicacionAgroquimica a WHERE a.insumo = :insumo AND a.activo = true")
    Double sumCantidadTotalByInsumo(@Param("insumo") Insumo insumo);
    
    /**
     * Buscar todas las aplicaciones activas
     */
    List<AplicacionAgroquimica> findByActivoTrue();
    
    /**
     * Buscar aplicaciones por labor (incluyendo inactivas)
     */
    List<AplicacionAgroquimica> findByLabor(Labor labor);

    @Query("SELECT a FROM AplicacionAgroquimica a WHERE a.labor.id IN :laborIds AND a.activo = true")
    List<AplicacionAgroquimica> findActivasByLaborIdIn(@Param("laborIds") List<Long> laborIds);
}

