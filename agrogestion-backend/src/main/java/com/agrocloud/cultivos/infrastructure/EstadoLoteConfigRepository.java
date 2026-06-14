package com.agrocloud.cultivos.infrastructure;
import com.agrocloud.core.domain.Empresa;

import com.agrocloud.cultivos.domain.EstadoLoteConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstadoLoteConfigRepository extends JpaRepository<EstadoLoteConfig, Long> {

    // Buscar por tipo de cultivo
    @Query("SELECT e FROM EstadoLoteConfig e WHERE " +
           "e.tipoCultivo.id = :tipoCultivoId AND " +
           "e.activo = true " +
           "ORDER BY e.orden ASC")
    List<EstadoLoteConfig> findByTipoCultivoIdAndActivoTrueOrderByOrdenAsc(@Param("tipoCultivoId") Long tipoCultivoId);

    // Buscar por empresa
    @Query("SELECT e FROM EstadoLoteConfig e WHERE " +
           "e.empresa.id = :empresaId AND " +
           "e.activo = true " +
           "ORDER BY e.orden ASC")
    List<EstadoLoteConfig> findByEmpresaIdAndActivoTrueOrderByOrdenAsc(@Param("empresaId") Long empresaId);

    // Buscar por tipo de cultivo y empresa (personalizaciones)
    @Query("SELECT e FROM EstadoLoteConfig e WHERE " +
           "e.tipoCultivo.id = :tipoCultivoId AND " +
           "e.empresa.id = :empresaId AND " +
           "e.activo = true " +
           "ORDER BY e.orden ASC")
    List<EstadoLoteConfig> findByTipoCultivoIdAndEmpresaIdAndActivoTrueOrderByOrdenAsc(
        @Param("tipoCultivoId") Long tipoCultivoId,
        @Param("empresaId") Long empresaId
    );

    // Buscar plantillas globales por tipo de cultivo
    @Query("SELECT e FROM EstadoLoteConfig e WHERE " +
           "e.tipoCultivo.id = :tipoCultivoId AND " +
           "e.empresa IS NULL AND " +
           "e.activo = true " +
           "ORDER BY e.orden ASC")
    List<EstadoLoteConfig> findPlantillasByTipoCultivoId(@Param("tipoCultivoId") Long tipoCultivoId);

    // Buscar estados iniciales
    List<EstadoLoteConfig> findByEsEstadoInicialTrueAndActivoTrue();

    // Buscar estados finales
    List<EstadoLoteConfig> findByEsEstadoFinalTrueAndActivoTrue();

    // Buscar por nombre, tipo de cultivo y empresa
    @Query("SELECT e FROM EstadoLoteConfig e WHERE " +
           "e.nombre = :nombre AND " +
           "e.tipoCultivo.id = :tipoCultivoId AND " +
           "(:empresaId IS NULL AND e.empresa IS NULL OR e.empresa.id = :empresaId)")
    Optional<EstadoLoteConfig> findByNombreAndTipoCultivoIdAndEmpresaId(
        @Param("nombre") String nombre,
        @Param("tipoCultivoId") Long tipoCultivoId,
        @Param("empresaId") Long empresaId
    );

    // Contar estados por tipo de cultivo
    @Query("SELECT COUNT(e) FROM EstadoLoteConfig e WHERE " +
           "e.tipoCultivo.id = :tipoCultivoId AND " +
           "e.activo = true")
    long countByTipoCultivoIdAndActivoTrue(@Param("tipoCultivoId") Long tipoCultivoId);

    // Verificar si hay estados en uso (con lotes)
    @Query("SELECT COUNT(p) > 0 FROM Plot p WHERE p.estadoConfigurado.id = :estadoId")
    boolean isEstadoEnUso(@Param("estadoId") Long estadoId);
}

