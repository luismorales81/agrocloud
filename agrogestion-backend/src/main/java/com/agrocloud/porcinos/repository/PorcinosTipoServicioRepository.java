package com.agrocloud.porcinos.repository;

import com.agrocloud.porcinos.model.entity.PorcinosTipoServicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PorcinosTipoServicioRepository extends JpaRepository<PorcinosTipoServicio, Long> {

    @Query("SELECT t FROM PorcinosTipoServicio t WHERE t.empresaId = :empresaId ORDER BY t.nombre")
    List<PorcinosTipoServicio> listarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT t FROM PorcinosTipoServicio t WHERE t.id = :id AND t.empresaId = :empresaId")
    Optional<PorcinosTipoServicio> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);
}
