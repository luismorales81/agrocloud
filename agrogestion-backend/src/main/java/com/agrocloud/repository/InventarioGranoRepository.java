package com.agrocloud.repository;

import com.agrocloud.model.entity.InventarioGrano;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar inventario de granos cosechados.
 */
@Repository
public interface InventarioGranoRepository extends JpaRepository<InventarioGrano, Long> {

    // Buscar por usuario
    List<InventarioGrano> findByUsuarioIdOrderByFechaIngresoDesc(Long usuarioId);

    // Buscar por cultivo
    List<InventarioGrano> findByCultivoIdOrderByFechaIngresoDesc(Long cultivoId);

    // Buscar por lote
    List<InventarioGrano> findByLoteIdOrderByFechaIngresoDesc(Long loteId);

    // Buscar por estado
    List<InventarioGrano> findByEstadoOrderByFechaIngresoDesc(String estado);

    // Buscar disponibles por usuario
    List<InventarioGrano> findByUsuarioIdAndEstadoOrderByFechaIngresoDesc(Long usuarioId, String estado);

    // Buscar por cosecha
    Optional<InventarioGrano> findByCosechaId(Long cosechaId);

    // Query para obtener stock total por cultivo
    @Query("SELECT ig.cultivo.id, ig.cultivo.nombre, SUM(ig.cantidadDisponible), ig.unidadMedida " +
           "FROM InventarioGrano ig " +
           "WHERE ig.usuario.id = :usuarioId AND ig.estado = 'DISPONIBLE' " +
           "GROUP BY ig.cultivo.id, ig.cultivo.nombre, ig.unidadMedida")
    List<Object[]> obtenerStockPorCultivo(@Param("usuarioId") Long usuarioId);

    // Query para obtener valor total del inventario
    @Query("SELECT SUM(ig.cantidadDisponible * ig.costoUnitario) " +
           "FROM InventarioGrano ig " +
           "WHERE ig.usuario.id = :usuarioId AND ig.estado = 'DISPONIBLE'")
    BigDecimal obtenerValorTotalInventario(@Param("usuarioId") Long usuarioId);

    // Contar registros disponibles
    long countByUsuarioIdAndEstado(Long usuarioId, String estado);
}

