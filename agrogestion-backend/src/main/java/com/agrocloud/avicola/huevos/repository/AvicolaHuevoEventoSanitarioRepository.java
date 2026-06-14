package com.agrocloud.avicola.huevos.repository;

import com.agrocloud.avicola.huevos.model.entity.AvicolaHuevoEventoSanitario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AvicolaHuevoEventoSanitarioRepository extends JpaRepository<AvicolaHuevoEventoSanitario, Long> {

    @Query("SELECT s FROM AvicolaHuevoEventoSanitario s WHERE s.lote.id = :loteId AND s.empresaId = :empresaId ORDER BY s.fecha DESC, s.id DESC")
    List<AvicolaHuevoEventoSanitario> listarPorLoteIdYEmpresaId(@Param("loteId") Long loteId, @Param("empresaId") Long empresaId);

    @Query("SELECT s FROM AvicolaHuevoEventoSanitario s WHERE s.id = :id AND s.empresaId = :empresaId")
    Optional<AvicolaHuevoEventoSanitario> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);
}
