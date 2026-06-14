package com.agrocloud.avicola.ponedoras.repository;

import com.agrocloud.avicola.ponedoras.model.entity.AvicolaPonedorasGalpon;
import com.agrocloud.avicola.ponedoras.model.enums.AvicolaPonedorasGalponEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Galpones ponedoras. Solo métodos con filtro {@code empresaId}; no usar {@code findAll()} sin empresa.
 */
public interface AvicolaPonedorasGalponRepository extends JpaRepository<AvicolaPonedorasGalpon, Long> {

    @Query("SELECT g FROM AvicolaPonedorasGalpon g WHERE g.empresaId = :empresaId ORDER BY g.fechaIngreso DESC, g.id DESC")
    List<AvicolaPonedorasGalpon> buscarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT g FROM AvicolaPonedorasGalpon g WHERE g.id = :id AND g.empresaId = :empresaId")
    Optional<AvicolaPonedorasGalpon> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    @Query("SELECT g FROM AvicolaPonedorasGalpon g WHERE g.empresaId = :empresaId AND g.estado = :estado ORDER BY g.fechaIngreso DESC, g.id DESC")
    List<AvicolaPonedorasGalpon> buscarPorEmpresaIdYEstado(
            @Param("empresaId") Long empresaId,
            @Param("estado") AvicolaPonedorasGalponEstado estado);
}
