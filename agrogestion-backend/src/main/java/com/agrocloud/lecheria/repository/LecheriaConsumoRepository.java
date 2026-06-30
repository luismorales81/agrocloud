package com.agrocloud.lecheria.repository;

import com.agrocloud.lecheria.model.entity.LecheriaConsumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface LecheriaConsumoRepository extends JpaRepository<LecheriaConsumo, Long> {

    @Query("SELECT c FROM LecheriaConsumo c WHERE c.empresaId = :empresaId ORDER BY c.fecha DESC")
    List<LecheriaConsumo> listarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT c FROM LecheriaConsumo c WHERE c.id = :id AND c.empresaId = :empresaId")
    Optional<LecheriaConsumo> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    @Query("SELECT c FROM LecheriaConsumo c WHERE c.rodeo.id = :rodeoId ORDER BY c.fecha DESC")
    List<LecheriaConsumo> listarPorRodeoId(@Param("rodeoId") Long rodeoId);

    @Query("SELECT COALESCE(SUM(c.cantidadKg), 0) FROM LecheriaConsumo c WHERE c.rodeo.id = :rodeoId AND c.campanaId = :campanaId")
    BigDecimal sumarCantidadKgPorRodeoYCampana(@Param("rodeoId") Long rodeoId, @Param("campanaId") Long campanaId);
}
