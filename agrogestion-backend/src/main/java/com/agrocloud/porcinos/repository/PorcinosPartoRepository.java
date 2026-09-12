package com.agrocloud.porcinos.repository;

import com.agrocloud.porcinos.model.entity.PorcinosParto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PorcinosPartoRepository extends JpaRepository<PorcinosParto, Long> {

    @Query("SELECT p FROM PorcinosParto p WHERE p.madre.id = :madreId ORDER BY p.fecha DESC, p.id DESC")
    List<PorcinosParto> listarPorMadreId(@Param("madreId") Long madreId);

    @Query("SELECT p FROM PorcinosParto p WHERE p.madre.empresaId = :empresaId ORDER BY p.fecha DESC, p.id DESC")
    List<PorcinosParto> listarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT p FROM PorcinosParto p WHERE p.gestacion.id = :gestacionId")
    Optional<PorcinosParto> buscarPorGestacionId(@Param("gestacionId") Long gestacionId);

    @Query("SELECT p FROM PorcinosParto p WHERE p.id = :id AND p.madre.empresaId = :empresaId")
    Optional<PorcinosParto> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);
}
