package com.agrocloud.avicola.crianza.repository;

import com.agrocloud.avicola.crianza.model.entity.AvicolaVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Ventas y faena por lote; toda consulta explícita filtra por empresa y lote.
 */
public interface AvicolaVentaRepository extends JpaRepository<AvicolaVenta, Long> {

    @Query("SELECT v FROM AvicolaVenta v WHERE v.lote.id = :loteId AND v.empresaId = :empresaId ORDER BY v.fecha DESC, v.id DESC")
    List<AvicolaVenta> listarPorLoteIdYEmpresaId(@Param("loteId") Long loteId, @Param("empresaId") Long empresaId);

    @Query("SELECT v FROM AvicolaVenta v WHERE v.id = :id AND v.empresaId = :empresaId")
    Optional<AvicolaVenta> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);
}
