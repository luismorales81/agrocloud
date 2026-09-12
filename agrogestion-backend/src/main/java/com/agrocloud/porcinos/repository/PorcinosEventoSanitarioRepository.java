package com.agrocloud.porcinos.repository;

import com.agrocloud.porcinos.model.entity.PorcinosEventoSanitario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PorcinosEventoSanitarioRepository extends JpaRepository<PorcinosEventoSanitario, Long> {

    @Query("SELECT e FROM PorcinosEventoSanitario e JOIN FETCH e.lote WHERE e.empresaId = :empresaId ORDER BY e.fecha DESC, e.id DESC")
    List<PorcinosEventoSanitario> listarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT e FROM PorcinosEventoSanitario e JOIN FETCH e.lote WHERE e.lote.id = :loteId AND e.empresaId = :empresaId ORDER BY e.fecha DESC, e.id DESC")
    List<PorcinosEventoSanitario> listarPorLoteIdYEmpresaId(@Param("loteId") Long loteId, @Param("empresaId") Long empresaId);

    @Query("SELECT e FROM PorcinosEventoSanitario e WHERE e.id = :id AND e.empresaId = :empresaId")
    Optional<PorcinosEventoSanitario> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);
}
