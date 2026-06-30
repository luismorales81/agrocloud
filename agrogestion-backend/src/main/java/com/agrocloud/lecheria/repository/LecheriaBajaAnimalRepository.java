package com.agrocloud.lecheria.repository;

import com.agrocloud.lecheria.model.entity.LecheriaBajaAnimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LecheriaBajaAnimalRepository extends JpaRepository<LecheriaBajaAnimal, Long> {

    @Query("SELECT b FROM LecheriaBajaAnimal b WHERE b.empresaId = :empresaId")
    List<LecheriaBajaAnimal> listarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT b FROM LecheriaBajaAnimal b WHERE b.id = :id AND b.empresaId = :empresaId")
    Optional<LecheriaBajaAnimal> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);
}
