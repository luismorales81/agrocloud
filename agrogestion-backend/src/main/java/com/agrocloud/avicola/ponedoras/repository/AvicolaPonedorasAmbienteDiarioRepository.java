package com.agrocloud.avicola.ponedoras.repository;

import com.agrocloud.avicola.ponedoras.model.entity.AvicolaPonedorasAmbienteDiario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AvicolaPonedorasAmbienteDiarioRepository extends JpaRepository<AvicolaPonedorasAmbienteDiario, Long> {

    @Query("SELECT a FROM AvicolaPonedorasAmbienteDiario a WHERE a.galpon.id = :galponId AND a.empresaId = :empresaId "
            + "AND a.fecha BETWEEN :desde AND :hasta ORDER BY a.fecha")
    List<AvicolaPonedorasAmbienteDiario> listarPorGalponYFechas(
            Long galponId, Long empresaId, LocalDate desde, LocalDate hasta);

    @Query("SELECT a FROM AvicolaPonedorasAmbienteDiario a WHERE a.galpon.id = :galponId AND a.empresaId = :empresaId AND a.fecha = :fecha")
    Optional<AvicolaPonedorasAmbienteDiario> buscarPorGalponYFecha(Long galponId, Long empresaId, LocalDate fecha);
}
