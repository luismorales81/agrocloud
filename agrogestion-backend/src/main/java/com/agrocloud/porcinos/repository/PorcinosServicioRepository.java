package com.agrocloud.porcinos.repository;

import com.agrocloud.porcinos.model.entity.PorcinosServicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PorcinosServicioRepository extends JpaRepository<PorcinosServicio, Long> {

    @Query("SELECT s FROM PorcinosServicio s WHERE s.madre.id = :madreId ORDER BY s.fecha DESC, s.id DESC")
    List<PorcinosServicio> listarPorMadreId(@Param("madreId") Long madreId);

    @Query("SELECT s FROM PorcinosServicio s WHERE s.madre.empresaId = :empresaId ORDER BY s.fecha DESC, s.id DESC")
    List<PorcinosServicio> listarPorEmpresaId(@Param("empresaId") Long empresaId);
}
