package com.agrocloud.feedlot.repository;

import com.agrocloud.feedlot.model.entity.FeedlotProveedorOrigen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FeedlotProveedorOrigenRepository extends JpaRepository<FeedlotProveedorOrigen, Long> {

    @Query("SELECT p FROM FeedlotProveedorOrigen p WHERE p.empresaId = :empresaId ORDER BY p.nombre")
    List<FeedlotProveedorOrigen> listarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT p FROM FeedlotProveedorOrigen p WHERE p.id = :id AND p.empresaId = :empresaId")
    Optional<FeedlotProveedorOrigen> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);
}
