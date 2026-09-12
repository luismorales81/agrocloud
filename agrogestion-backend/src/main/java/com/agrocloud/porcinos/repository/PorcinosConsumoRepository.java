package com.agrocloud.porcinos.repository;

import com.agrocloud.porcinos.model.entity.PorcinosConsumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PorcinosConsumoRepository extends JpaRepository<PorcinosConsumo, Long> {

    @Query("SELECT c FROM PorcinosConsumo c WHERE c.empresaId = :empresaId ORDER BY c.fecha DESC, c.id DESC")
    List<PorcinosConsumo> listarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT c FROM PorcinosConsumo c WHERE c.lote.id = :loteId AND c.empresaId = :empresaId ORDER BY c.fecha DESC, c.id DESC")
    List<PorcinosConsumo> listarPorLoteIdYEmpresaId(@Param("loteId") Long loteId, @Param("empresaId") Long empresaId);

    @Query("SELECT c FROM PorcinosConsumo c WHERE c.id = :id AND c.empresaId = :empresaId")
    Optional<PorcinosConsumo> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    @Query("SELECT COALESCE(SUM(c.cantidadKg), 0) FROM PorcinosConsumo c WHERE c.empresaId = :empresaId AND c.campanaId = :campanaId")
    java.math.BigDecimal sumarCantidadKgPorEmpresaIdYCampanaId(
            @Param("empresaId") Long empresaId,
            @Param("campanaId") Long campanaId);
}
