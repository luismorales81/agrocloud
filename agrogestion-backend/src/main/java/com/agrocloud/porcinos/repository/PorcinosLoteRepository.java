package com.agrocloud.porcinos.repository;

import com.agrocloud.porcinos.model.entity.PorcinosLote;
import com.agrocloud.porcinos.model.enums.PorcinosLoteEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PorcinosLoteRepository extends JpaRepository<PorcinosLote, Long> {

    @Query("SELECT l FROM PorcinosLote l WHERE l.empresaId = :empresaId ORDER BY l.fechaIngreso DESC, l.id DESC")
    List<PorcinosLote> listarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT l FROM PorcinosLote l WHERE l.id = :id AND l.empresaId = :empresaId")
    Optional<PorcinosLote> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    @Query("SELECT l FROM PorcinosLote l WHERE l.empresaId = :empresaId AND l.estado = :estado ORDER BY l.fechaIngreso DESC")
    List<PorcinosLote> listarPorEmpresaIdYEstado(@Param("empresaId") Long empresaId, @Param("estado") PorcinosLoteEstado estado);

    @Query("SELECT l FROM PorcinosLote l WHERE l.empresaId = :empresaId AND l.campanaId = :campanaId ORDER BY l.fechaIngreso DESC")
    List<PorcinosLote> listarPorEmpresaIdYCampanaId(@Param("empresaId") Long empresaId, @Param("campanaId") Long campanaId);

    @Query("SELECT l FROM PorcinosLote l WHERE l.empresaId = :empresaId AND l.galpon.id = :galponId ORDER BY l.fechaIngreso DESC")
    List<PorcinosLote> listarPorEmpresaIdYGalponId(@Param("empresaId") Long empresaId, @Param("galponId") Long galponId);

    @Query("SELECT COUNT(l) > 0 FROM PorcinosLote l WHERE l.galpon.id = :galponId AND l.estado = :estado")
    boolean existsByGalponIdAndEstado(@Param("galponId") Long galponId, @Param("estado") PorcinosLoteEstado estado);

    @Query("SELECT COALESCE(SUM(l.cabezasActuales), 0) FROM PorcinosLote l WHERE l.empresaId = :empresaId AND l.campanaId = :campanaId AND l.estado = :estado")
    Long sumarCabezasActualesPorEmpresaCampanaYEstado(
            @Param("empresaId") Long empresaId,
            @Param("campanaId") Long campanaId,
            @Param("estado") PorcinosLoteEstado estado);
}
