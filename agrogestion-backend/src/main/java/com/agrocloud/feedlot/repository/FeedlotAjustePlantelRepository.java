package com.agrocloud.feedlot.repository;

import com.agrocloud.feedlot.model.entity.FeedlotAjustePlantel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FeedlotAjustePlantelRepository extends JpaRepository<FeedlotAjustePlantel, Long> {

    @Query("SELECT a FROM FeedlotAjustePlantel a WHERE a.lote.id = :loteId AND a.empresaId = :empresaId ORDER BY a.fecha DESC, a.id DESC")
    List<FeedlotAjustePlantel> listarPorLoteIdYEmpresaId(@Param("loteId") Long loteId, @Param("empresaId") Long empresaId);
}
