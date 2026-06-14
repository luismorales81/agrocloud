package com.agrocloud.avicola.huevos.repository;

import com.agrocloud.avicola.huevos.model.entity.AvicolaHuevoConsumo;
import com.agrocloud.core.inventory.domain.Insumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AvicolaHuevoConsumoRepository extends JpaRepository<AvicolaHuevoConsumo, Long> {

    @Query("SELECT c FROM AvicolaHuevoConsumo c WHERE c.lote.id = :loteId AND c.empresaId = :empresaId ORDER BY c.fecha DESC, c.id DESC")
    List<AvicolaHuevoConsumo> listarPorLoteIdYEmpresaId(@Param("loteId") Long loteId, @Param("empresaId") Long empresaId);

    @Query("SELECT c FROM AvicolaHuevoConsumo c WHERE c.id = :id AND c.empresaId = :empresaId")
    Optional<AvicolaHuevoConsumo> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    @Query("SELECT COALESCE(SUM(c.cantidad), 0) FROM AvicolaHuevoConsumo c WHERE c.lote.id = :loteId AND c.empresaId = :empresaId")
    BigDecimal sumarCantidadConsumidaPorLoteYEmpresa(@Param("loteId") Long loteId, @Param("empresaId") Long empresaId);

    @Query("SELECT COALESCE(SUM(c.cantidad), 0) FROM AvicolaHuevoConsumo c WHERE c.lote.id = :loteId AND c.empresaId = :empresaId AND c.fecha BETWEEN :desde AND :hasta")
    BigDecimal sumarConsumoLoteEnRango(
            @Param("loteId") Long loteId,
            @Param("empresaId") Long empresaId,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);

    @Query("SELECT COALESCE(SUM(c.cantidad * COALESCE(i.precioUnitario, 0)), 0) FROM AvicolaHuevoConsumo c, Insumo i "
            + "WHERE c.insumoId = i.id AND c.lote.id = :loteId AND c.empresaId = :empresaId AND c.fecha BETWEEN :desde AND :hasta")
    BigDecimal sumarCostoConsumosLoteEnRango(
            @Param("loteId") Long loteId,
            @Param("empresaId") Long empresaId,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);

    @Query("SELECT c.fecha, COALESCE(SUM(c.cantidad * COALESCE(i.precioUnitario, 0)), 0) FROM AvicolaHuevoConsumo c, Insumo i "
            + "WHERE c.insumoId = i.id AND c.lote.id = :loteId AND c.empresaId = :empresaId AND c.fecha BETWEEN :desde AND :hasta "
            + "GROUP BY c.fecha ORDER BY c.fecha")
    List<Object[]> listarCostoConsumoPorDia(
            @Param("loteId") Long loteId,
            @Param("empresaId") Long empresaId,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);
}
