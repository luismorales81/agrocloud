package com.agrocloud.feedlot.repository;

import com.agrocloud.feedlot.model.entity.FeedlotPesadaAnimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FeedlotPesadaAnimalRepository extends JpaRepository<FeedlotPesadaAnimal, Long> {

    @Query("SELECT p FROM FeedlotPesadaAnimal p WHERE p.animal.id = :animalId ORDER BY p.fecha DESC, p.id DESC")
    List<FeedlotPesadaAnimal> listarPorAnimalId(@Param("animalId") Long animalId);

    @Modifying
    @Query("DELETE FROM FeedlotPesadaAnimal p WHERE p.sesion.id = :sesionId")
    void eliminarPorSesionId(@Param("sesionId") Long sesionId);

    boolean existsBySesion_Id(Long sesionId);
}
