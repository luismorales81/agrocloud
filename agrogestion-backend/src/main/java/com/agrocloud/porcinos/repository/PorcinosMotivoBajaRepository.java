package com.agrocloud.porcinos.repository;

import com.agrocloud.porcinos.model.entity.PorcinosMotivoBaja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PorcinosMotivoBajaRepository extends JpaRepository<PorcinosMotivoBaja, Long> {

    @Query("SELECT m FROM PorcinosMotivoBaja m WHERE m.empresaId = :empresaId ORDER BY m.nombre")
    List<PorcinosMotivoBaja> listarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT m FROM PorcinosMotivoBaja m WHERE m.id = :id AND m.empresaId = :empresaId")
    Optional<PorcinosMotivoBaja> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);
}
