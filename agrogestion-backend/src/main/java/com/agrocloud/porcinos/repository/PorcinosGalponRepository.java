package com.agrocloud.porcinos.repository;

import com.agrocloud.porcinos.model.entity.PorcinosGalpon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PorcinosGalponRepository extends JpaRepository<PorcinosGalpon, Long> {

    @Query("SELECT g FROM PorcinosGalpon g WHERE g.establecimiento.id = :establecimientoId AND g.activo = true ORDER BY g.nombre, g.id")
    List<PorcinosGalpon> listarPorEstablecimientoId(@Param("establecimientoId") Long establecimientoId);

    @Query("SELECT COUNT(g) > 0 FROM PorcinosGalpon g WHERE g.establecimiento.id = :establecimientoId "
            + "AND g.activo = true AND LOWER(TRIM(g.nombre)) = LOWER(TRIM(:nombre))")
    boolean existeActivoConNombre(
            @Param("establecimientoId") Long establecimientoId,
            @Param("nombre") String nombre);

    @Query("SELECT g FROM PorcinosGalpon g WHERE g.id = :id AND g.establecimiento.id = :establecimientoId")
    Optional<PorcinosGalpon> buscarPorIdYEstablecimientoId(@Param("id") Long id, @Param("establecimientoId") Long establecimientoId);

    @Query("SELECT g FROM PorcinosGalpon g WHERE g.id = :id AND g.establecimiento.empresaId = :empresaId")
    Optional<PorcinosGalpon> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);
}
