package com.agrocloud.cultivos.infrastructure;
import com.agrocloud.core.domain.Empresa;

import com.agrocloud.cultivos.domain.TransicionEstadoConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransicionEstadoConfigRepository extends JpaRepository<TransicionEstadoConfig, Long> {

    // Buscar transiciones por estado origen
    @Query("SELECT t FROM TransicionEstadoConfig t WHERE " +
           "t.estadoOrigen.id = :estadoOrigenId AND " +
           "t.activo = true")
    List<TransicionEstadoConfig> findByEstadoOrigenIdAndActivoTrue(@Param("estadoOrigenId") Long estadoOrigenId);

    // Buscar transiciones por estado destino
    @Query("SELECT t FROM TransicionEstadoConfig t WHERE " +
           "t.estadoDestino.id = :estadoDestinoId AND " +
           "t.activo = true")
    List<TransicionEstadoConfig> findByEstadoDestinoIdAndActivoTrue(@Param("estadoDestinoId") Long estadoDestinoId);

    // Buscar transiciones por tipo de cultivo
    @Query("SELECT t FROM TransicionEstadoConfig t " +
           "LEFT JOIN FETCH t.estadoOrigen " +
           "LEFT JOIN FETCH t.estadoDestino " +
           "WHERE t.tipoCultivo.id = :tipoCultivoId AND " +
           "t.empresa IS NULL AND " +
           "t.activo = true")
    List<TransicionEstadoConfig> findPlantillasByTipoCultivoId(@Param("tipoCultivoId") Long tipoCultivoId);

    // Buscar transiciones personalizadas por empresa
    @Query("SELECT t FROM TransicionEstadoConfig t " +
           "LEFT JOIN FETCH t.estadoOrigen " +
           "LEFT JOIN FETCH t.estadoDestino " +
           "WHERE t.tipoCultivo.id = :tipoCultivoId AND " +
           "t.empresa.id = :empresaId AND " +
           "t.activo = true")
    List<TransicionEstadoConfig> findByTipoCultivoIdAndEmpresaIdAndActivoTrue(
        @Param("tipoCultivoId") Long tipoCultivoId,
        @Param("empresaId") Long empresaId
    );

    // Verificar si existe una transición específica
    @Query("SELECT t FROM TransicionEstadoConfig t WHERE " +
           "t.estadoOrigen.id = :estadoOrigenId AND " +
           "t.estadoDestino.id = :estadoDestinoId AND " +
           "(:empresaId IS NULL AND t.empresa IS NULL OR t.empresa.id = :empresaId) AND " +
           "t.activo = true")
    Optional<TransicionEstadoConfig> findByEstadoOrigenIdAndEstadoDestinoIdAndEmpresaId(
        @Param("estadoOrigenId") Long estadoOrigenId,
        @Param("estadoDestinoId") Long estadoDestinoId,
        @Param("empresaId") Long empresaId
    );

    // Buscar todas las transiciones válidas desde un estado
    @Query("SELECT t FROM TransicionEstadoConfig t " +
           "LEFT JOIN FETCH t.estadoOrigen " +
           "LEFT JOIN FETCH t.estadoDestino " +
           "WHERE t.estadoOrigen.id = :estadoOrigenId AND " +
           "(:empresaId IS NULL AND t.empresa IS NULL OR t.empresa.id = :empresaId) AND " +
           "t.activo = true")
    List<TransicionEstadoConfig> findTransicionesValidasDesdeEstado(
        @Param("estadoOrigenId") Long estadoOrigenId,
        @Param("empresaId") Long empresaId
    );
}

