package com.agrocloud.feedlot.repository;

import com.agrocloud.feedlot.model.entity.FeedlotConsumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FeedlotConsumoRepository extends JpaRepository<FeedlotConsumo, Long> {

    @Query("SELECT c FROM FeedlotConsumo c WHERE c.lote.id = :loteId AND c.empresaId = :empresaId ORDER BY c.fecha DESC, c.id DESC")
    List<FeedlotConsumo> listarPorLoteIdYEmpresaId(@Param("loteId") Long loteId, @Param("empresaId") Long empresaId);

    @Query("SELECT c FROM FeedlotConsumo c WHERE c.id = :id AND c.empresaId = :empresaId")
    Optional<FeedlotConsumo> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    @Query("SELECT COALESCE(SUM(c.cantidadKg), 0) FROM FeedlotConsumo c WHERE c.lote.id = :loteId AND c.empresaId = :empresaId")
    BigDecimal sumarCantidadKgPorLoteIdYEmpresaId(@Param("loteId") Long loteId, @Param("empresaId") Long empresaId);

    @Query("SELECT COALESCE(SUM(c.cantidadKg * c.materiaSecaPct / 100), 0) FROM FeedlotConsumo c "
            + "WHERE c.lote.id = :loteId AND c.empresaId = :empresaId AND c.materiaSecaPct IS NOT NULL")
    BigDecimal sumarMateriaSecaKgPorLoteIdYEmpresaId(@Param("loteId") Long loteId, @Param("empresaId") Long empresaId);

    @Query("SELECT COALESCE(SUM(c.cantidadKg), 0) FROM FeedlotConsumo c "
            + "WHERE c.empresaId = :empresaId AND c.campanaId = :campanaId")
    BigDecimal sumarCantidadKgPorEmpresaIdYCampanaId(@Param("empresaId") Long empresaId, @Param("campanaId") Long campanaId);

    @Query("SELECT COALESCE(SUM(c.cantidadKg), 0) FROM FeedlotConsumo c "
            + "WHERE c.lote.id = :loteId AND c.empresaId = :empresaId AND c.fecha = :fecha")
    BigDecimal sumarCantidadKgPorLoteIdEmpresaIdYFecha(
            @Param("loteId") Long loteId, @Param("empresaId") Long empresaId, @Param("fecha") LocalDate fecha);

    @Query("SELECT COALESCE(SUM(c.cantidadKg * c.materiaSecaPct / 100), 0) FROM FeedlotConsumo c "
            + "WHERE c.lote.id = :loteId AND c.empresaId = :empresaId AND c.fecha = :fecha "
            + "AND c.materiaSecaPct IS NOT NULL")
    BigDecimal sumarMateriaSecaKgPorLoteIdEmpresaIdYFecha(
            @Param("loteId") Long loteId, @Param("empresaId") Long empresaId, @Param("fecha") LocalDate fecha);
}
