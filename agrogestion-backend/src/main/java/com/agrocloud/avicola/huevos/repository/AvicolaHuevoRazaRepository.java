package com.agrocloud.avicola.huevos.repository;

import com.agrocloud.avicola.huevos.model.entity.AvicolaHuevoRaza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AvicolaHuevoRazaRepository extends JpaRepository<AvicolaHuevoRaza, Long> {

    @Query("SELECT r FROM AvicolaHuevoRaza r WHERE r.empresaId = :empresaId ORDER BY r.nombre ASC")
    List<AvicolaHuevoRaza> listarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT r FROM AvicolaHuevoRaza r WHERE r.id = :id AND r.empresaId = :empresaId")
    Optional<AvicolaHuevoRaza> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);
}
