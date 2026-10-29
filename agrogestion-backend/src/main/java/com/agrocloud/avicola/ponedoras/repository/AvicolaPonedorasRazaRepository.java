package com.agrocloud.avicola.ponedoras.repository;

import com.agrocloud.avicola.ponedoras.model.entity.AvicolaPonedorasRaza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AvicolaPonedorasRazaRepository extends JpaRepository<AvicolaPonedorasRaza, Long> {

    @Query("SELECT r FROM AvicolaPonedorasRaza r WHERE r.empresaId = :empresaId ORDER BY r.nombre ASC")
    List<AvicolaPonedorasRaza> listarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT r FROM AvicolaPonedorasRaza r WHERE r.id = :id AND r.empresaId = :empresaId")
    Optional<AvicolaPonedorasRaza> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    @Query("SELECT r FROM AvicolaPonedorasRaza r WHERE r.empresaId = :empresaId AND r.id IN :ids")
    List<AvicolaPonedorasRaza> buscarPorIdsYEmpresaId(
            @Param("ids") Collection<Long> ids, @Param("empresaId") Long empresaId);
}
