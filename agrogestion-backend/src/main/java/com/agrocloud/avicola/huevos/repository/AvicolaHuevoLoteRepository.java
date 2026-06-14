package com.agrocloud.avicola.huevos.repository;

import com.agrocloud.avicola.huevos.model.entity.AvicolaHuevoLote;
import com.agrocloud.avicola.huevos.model.enums.AvicolaHuevoLoteEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AvicolaHuevoLoteRepository extends JpaRepository<AvicolaHuevoLote, Long> {

    @Query("SELECT l FROM AvicolaHuevoLote l WHERE l.empresaId = :empresaId ORDER BY l.fechaInicio DESC, l.id DESC")
    List<AvicolaHuevoLote> listarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT l FROM AvicolaHuevoLote l WHERE l.id = :id AND l.empresaId = :empresaId")
    Optional<AvicolaHuevoLote> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    @Query("SELECT l FROM AvicolaHuevoLote l WHERE l.empresaId = :empresaId AND l.estado = :estado ORDER BY l.fechaInicio DESC")
    List<AvicolaHuevoLote> listarPorEmpresaIdYEstado(@Param("empresaId") Long empresaId, @Param("estado") AvicolaHuevoLoteEstado estado);
}
