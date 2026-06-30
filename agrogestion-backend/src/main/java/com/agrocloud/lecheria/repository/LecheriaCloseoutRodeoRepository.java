package com.agrocloud.lecheria.repository;

import com.agrocloud.lecheria.model.entity.LecheriaCloseoutRodeo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LecheriaCloseoutRodeoRepository extends JpaRepository<LecheriaCloseoutRodeo, Long> {

    @Query("SELECT c FROM LecheriaCloseoutRodeo c WHERE c.empresaId = :empresaId")
    List<LecheriaCloseoutRodeo> listarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT c FROM LecheriaCloseoutRodeo c WHERE c.id = :id AND c.empresaId = :empresaId")
    Optional<LecheriaCloseoutRodeo> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    @Query("SELECT c FROM LecheriaCloseoutRodeo c WHERE c.rodeo.id = :rodeoId AND c.campanaId = :campanaId ORDER BY c.fechaCierre DESC")
    List<LecheriaCloseoutRodeo> listarPorRodeoYCampana(@Param("rodeoId") Long rodeoId, @Param("campanaId") Long campanaId);
}
