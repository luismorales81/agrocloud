package com.agrocloud.cultivos.infrastructure;
import com.agrocloud.core.domain.Empresa;

import com.agrocloud.cultivos.domain.TareaPorEstadoConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TareaPorEstadoConfigRepository extends JpaRepository<TareaPorEstadoConfig, Long> {

    // Buscar tareas por estado
    @Query("SELECT t FROM TareaPorEstadoConfig t WHERE " +
           "t.estado.id = :estadoId AND " +
           "t.activo = true " +
           "ORDER BY t.orden ASC")
    List<TareaPorEstadoConfig> findByEstadoIdAndActivoTrueOrderByOrdenAsc(@Param("estadoId") Long estadoId);

    // Buscar tareas por tipo de cultivo (plantillas)
    @Query("SELECT t FROM TareaPorEstadoConfig t WHERE " +
           "t.tipoCultivo.id = :tipoCultivoId AND " +
           "t.empresa IS NULL AND " +
           "t.activo = true " +
           "ORDER BY t.orden ASC")
    List<TareaPorEstadoConfig> findPlantillasByTipoCultivoId(@Param("tipoCultivoId") Long tipoCultivoId);

    // Buscar tareas personalizadas por empresa
    @Query("SELECT t FROM TareaPorEstadoConfig t WHERE " +
           "t.tipoCultivo.id = :tipoCultivoId AND " +
           "t.empresa.id = :empresaId AND " +
           "t.activo = true " +
           "ORDER BY t.orden ASC")
    List<TareaPorEstadoConfig> findByTipoCultivoIdAndEmpresaIdAndActivoTrueOrderByOrdenAsc(
        @Param("tipoCultivoId") Long tipoCultivoId,
        @Param("empresaId") Long empresaId
    );

    // Buscar tareas por estado y tipo de labor
    @Query("SELECT t FROM TareaPorEstadoConfig t WHERE " +
           "t.estado.id = :estadoId AND " +
           "t.tipoLabor = :tipoLabor AND " +
           "t.activo = true")
    Optional<TareaPorEstadoConfig> findByEstadoIdAndTipoLaborAndActivoTrue(
        @Param("estadoId") Long estadoId,
        @Param("tipoLabor") String tipoLabor
    );

    // Buscar tareas obligatorias por estado
    @Query("SELECT t FROM TareaPorEstadoConfig t WHERE " +
           "t.estado.id = :estadoId AND " +
           "t.esObligatoria = true AND " +
           "t.activo = true " +
           "ORDER BY t.orden ASC")
    List<TareaPorEstadoConfig> findByEstadoIdAndEsObligatoriaTrueAndActivoTrueOrderByOrdenAsc(@Param("estadoId") Long estadoId);

    // Buscar todas las tareas disponibles para un estado (plantilla empresa_id NULL o personalización de la empresa)
    // Incluye plantilla para que las tareas configuradas en tipos de cultivo se devuelvan aunque la empresa no tenga copia
    @Query("SELECT t FROM TareaPorEstadoConfig t " +
           "LEFT JOIN FETCH t.estado " +
           "WHERE t.estado.id = :estadoId AND " +
           "(t.empresa IS NULL OR t.empresa.id = :empresaId) AND " +
           "t.activo = true " +
           "ORDER BY t.orden ASC")
    List<TareaPorEstadoConfig> findTareasDisponiblesPorEstado(
        @Param("estadoId") Long estadoId,
        @Param("empresaId") Long empresaId
    );
}

