package com.agrocloud.lecheria.repository;

import com.agrocloud.lecheria.model.entity.LecheriaVentaLeche;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface LecheriaVentaLecheRepository extends JpaRepository<LecheriaVentaLeche, Long> {

    @Query("SELECT v FROM LecheriaVentaLeche v WHERE v.empresaId = :empresaId ORDER BY v.fecha DESC")
    List<LecheriaVentaLeche> listarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT v FROM LecheriaVentaLeche v WHERE v.id = :id AND v.empresaId = :empresaId")
    Optional<LecheriaVentaLeche> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    @Query("SELECT COALESCE(SUM(v.total), 0) FROM LecheriaVentaLeche v WHERE v.empresaId = :empresaId AND v.campanaId = :campanaId")
    BigDecimal sumarTotalPorEmpresaYCampana(@Param("empresaId") Long empresaId, @Param("campanaId") Long campanaId);
}
