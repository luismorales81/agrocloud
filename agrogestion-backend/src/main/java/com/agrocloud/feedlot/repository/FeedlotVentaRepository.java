package com.agrocloud.feedlot.repository;

import com.agrocloud.feedlot.model.entity.FeedlotVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface FeedlotVentaRepository extends JpaRepository<FeedlotVenta, Long> {

    @Query("SELECT v FROM FeedlotVenta v WHERE v.lote.id = :loteId AND v.empresaId = :empresaId ORDER BY v.fecha DESC, v.id DESC")
    List<FeedlotVenta> listarPorLoteIdYEmpresaId(@Param("loteId") Long loteId, @Param("empresaId") Long empresaId);

    @Query("SELECT v FROM FeedlotVenta v WHERE v.id = :id AND v.empresaId = :empresaId")
    Optional<FeedlotVenta> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    @Query("SELECT COALESCE(SUM(v.total), 0) FROM FeedlotVenta v WHERE v.lote.id = :loteId AND v.empresaId = :empresaId")
    BigDecimal sumarTotalPorLoteIdYEmpresaId(@Param("loteId") Long loteId, @Param("empresaId") Long empresaId);

    @Query("SELECT COALESCE(SUM(v.cabezas), 0) FROM FeedlotVenta v WHERE v.lote.id = :loteId AND v.empresaId = :empresaId")
    Integer sumarCabezasVendidasPorLoteIdYEmpresaId(@Param("loteId") Long loteId, @Param("empresaId") Long empresaId);
}
