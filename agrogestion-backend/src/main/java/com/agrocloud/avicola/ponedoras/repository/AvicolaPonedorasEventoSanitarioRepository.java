package com.agrocloud.avicola.ponedoras.repository;

import com.agrocloud.avicola.ponedoras.model.entity.AvicolaPonedorasEventoSanitario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Eventos sanitarios del galpón. Solo consultas con {@code empresaId}.
 */
public interface AvicolaPonedorasEventoSanitarioRepository extends JpaRepository<AvicolaPonedorasEventoSanitario, Long> {

    @Query("SELECT e FROM AvicolaPonedorasEventoSanitario e WHERE e.galpon.id = :galponId AND e.empresaId = :empresaId ORDER BY e.fecha DESC, e.id DESC")
    List<AvicolaPonedorasEventoSanitario> buscarPorGalponIdYEmpresaId(
            @Param("galponId") Long galponId,
            @Param("empresaId") Long empresaId);
}
