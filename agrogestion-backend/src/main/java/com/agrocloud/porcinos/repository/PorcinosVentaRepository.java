package com.agrocloud.porcinos.repository;

import com.agrocloud.porcinos.model.entity.PorcinosVenta;
import com.agrocloud.porcinos.model.enums.PorcinosVentaTipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PorcinosVentaRepository extends JpaRepository<PorcinosVenta, Long> {

    @Query("SELECT v FROM PorcinosVenta v JOIN FETCH v.lote WHERE v.empresaId = :empresaId ORDER BY v.fecha DESC, v.id DESC")
    List<PorcinosVenta> listarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT v FROM PorcinosVenta v JOIN FETCH v.lote WHERE v.lote.id = :loteId AND v.empresaId = :empresaId ORDER BY v.fecha DESC, v.id DESC")
    List<PorcinosVenta> listarPorLoteIdYEmpresaId(@Param("loteId") Long loteId, @Param("empresaId") Long empresaId);

    @Query("SELECT v FROM PorcinosVenta v WHERE v.id = :id AND v.empresaId = :empresaId")
    Optional<PorcinosVenta> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    @Query("SELECT v FROM PorcinosVenta v WHERE v.empresaId = :empresaId AND v.tipo = :tipo ORDER BY v.fecha DESC, v.id DESC")
    List<PorcinosVenta> listarPorEmpresaIdYTipo(@Param("empresaId") Long empresaId, @Param("tipo") PorcinosVentaTipo tipo);

    @Query("SELECT COALESCE(SUM(v.total), 0) FROM PorcinosVenta v WHERE v.lote.id = :loteId AND v.empresaId = :empresaId")
    BigDecimal sumarTotalPorLoteIdYEmpresaId(@Param("loteId") Long loteId, @Param("empresaId") Long empresaId);
}
