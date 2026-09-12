package com.agrocloud.porcinos.repository;

import com.agrocloud.porcinos.model.entity.PorcinosDestete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PorcinosDesteteRepository extends JpaRepository<PorcinosDestete, Long> {

    @Query("SELECT d FROM PorcinosDestete d WHERE d.parto.id = :partoId")
    Optional<PorcinosDestete> buscarPorPartoId(@Param("partoId") Long partoId);
}
