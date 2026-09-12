package com.agrocloud.porcinos.repository;

import com.agrocloud.porcinos.model.entity.PorcinosRaza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PorcinosRazaRepository extends JpaRepository<PorcinosRaza, Long> {

    @Query("SELECT r FROM PorcinosRaza r WHERE r.empresaId = :empresaId ORDER BY r.nombre")
    List<PorcinosRaza> listarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT r FROM PorcinosRaza r WHERE r.id = :id AND r.empresaId = :empresaId")
    Optional<PorcinosRaza> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);
}
