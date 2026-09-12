package com.agrocloud.porcinos.repository;

import com.agrocloud.porcinos.model.entity.PorcinosGestacion;
import com.agrocloud.porcinos.model.enums.PorcinosGestacionEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PorcinosGestacionRepository extends JpaRepository<PorcinosGestacion, Long> {

    @Query("SELECT g FROM PorcinosGestacion g WHERE g.madre.id = :madreId ORDER BY g.fechaInicio DESC, g.id DESC")
    List<PorcinosGestacion> listarPorMadreId(@Param("madreId") Long madreId);

    @Query("SELECT g FROM PorcinosGestacion g WHERE g.madre.empresaId = :empresaId ORDER BY g.fechaInicio DESC, g.id DESC")
    List<PorcinosGestacion> listarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT g FROM PorcinosGestacion g WHERE g.madre.id = :madreId AND g.estado = :estado AND g.activo = true")
    Optional<PorcinosGestacion> buscarActivaPorMadreId(
            @Param("madreId") Long madreId,
            @Param("estado") PorcinosGestacionEstado estado);

    @Query("SELECT g FROM PorcinosGestacion g WHERE g.id = :id AND g.madre.empresaId = :empresaId")
    Optional<PorcinosGestacion> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);
}
