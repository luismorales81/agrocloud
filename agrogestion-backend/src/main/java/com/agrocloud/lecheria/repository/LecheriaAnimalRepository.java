package com.agrocloud.lecheria.repository;

import com.agrocloud.lecheria.model.entity.LecheriaAnimal;
import com.agrocloud.lecheria.model.enums.LecheriaEstadoAnimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LecheriaAnimalRepository extends JpaRepository<LecheriaAnimal, Long> {

    @Query("SELECT a FROM LecheriaAnimal a WHERE a.empresaId = :empresaId AND a.activo = true ORDER BY a.identificacion")
    List<LecheriaAnimal> listarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT a FROM LecheriaAnimal a WHERE a.id = :id AND a.empresaId = :empresaId")
    Optional<LecheriaAnimal> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    @Query("SELECT COUNT(a) FROM LecheriaAnimal a WHERE a.empresaId = :empresaId AND a.activo = true AND a.estado = :estado")
    Long contarPorEmpresaIdYEstado(@Param("empresaId") Long empresaId, @Param("estado") LecheriaEstadoAnimal estado);

    boolean existsByEmpresaIdAndIdentificacion(Long empresaId, String identificacion);
}
