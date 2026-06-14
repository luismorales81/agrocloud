package com.agrocloud.core.inventory.infrastructure;

import com.agrocloud.core.inventory.domain.InventarioGrano;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository("inventarioGranoRepositoryInventario")
public interface InventarioGranoRepository extends JpaRepository<InventarioGrano, Long> {

    List<InventarioGrano> findByUsuarioIdOrderByFechaIngresoDesc(Long usuarioId);

    List<InventarioGrano> findByCultivoIdOrderByFechaIngresoDesc(Long cultivoId);

    List<InventarioGrano> findByLoteIdOrderByFechaIngresoDesc(Long loteId);

    List<InventarioGrano> findByEstadoOrderByFechaIngresoDesc(String estado);

    List<InventarioGrano> findByUsuarioIdAndEstadoOrderByFechaIngresoDesc(Long usuarioId, String estado);

    Optional<InventarioGrano> findByCosechaId(Long cosechaId);

    @Query("SELECT ig.cultivo.id, ig.cultivo.nombre, SUM(ig.cantidadDisponible), ig.unidadMedida " +
           "FROM InventarioGrano ig " +
           "WHERE ig.usuario.id = :usuarioId AND ig.estado = 'DISPONIBLE' " +
           "GROUP BY ig.cultivo.id, ig.cultivo.nombre, ig.unidadMedida")
    List<Object[]> obtenerStockPorCultivo(@Param("usuarioId") Long usuarioId);

    @Query("SELECT SUM(ig.cantidadDisponible * ig.costoUnitario) " +
           "FROM InventarioGrano ig " +
           "WHERE ig.usuario.id = :usuarioId AND ig.estado = 'DISPONIBLE'")
    BigDecimal obtenerValorTotalInventario(@Param("usuarioId") Long usuarioId);

    long countByUsuarioIdAndEstado(Long usuarioId, String estado);
}
