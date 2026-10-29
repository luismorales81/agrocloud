package com.agrocloud.avicola.huevos.repository;

import com.agrocloud.avicola.huevos.model.entity.AvicolaHuevoVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AvicolaHuevoVentaRepository extends JpaRepository<AvicolaHuevoVenta, Long> {

    @Query("""
            SELECT v FROM AvicolaHuevoVenta v
            WHERE v.lote.id = :loteId AND v.empresaId = :empresaId
            ORDER BY v.fecha DESC, v.id DESC
            """)
    List<AvicolaHuevoVenta> listarPorLoteIdYEmpresaId(@Param("loteId") Long loteId, @Param("empresaId") Long empresaId);

    @Query("""
            SELECT COALESCE(SUM(v.cantidad), 0) FROM AvicolaHuevoVenta v
            WHERE v.lote.id = :loteId AND v.empresaId = :empresaId
            """)
    Long sumarCantidadVendida(@Param("loteId") Long loteId, @Param("empresaId") Long empresaId);
}
