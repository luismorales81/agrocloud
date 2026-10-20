package com.agrocloud.feedlot.repository;

import com.agrocloud.feedlot.model.entity.FeedlotSesionMangaFila;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FeedlotSesionMangaFilaRepository extends JpaRepository<FeedlotSesionMangaFila, Long> {

    @Query("SELECT f FROM FeedlotSesionMangaFila f WHERE f.sesion.id = :sesionId ORDER BY f.numeroLinea")
    List<FeedlotSesionMangaFila> listarPorSesionId(@Param("sesionId") Long sesionId);

    @Modifying
    @Query("DELETE FROM FeedlotSesionMangaFila f WHERE f.sesion.id = :sesionId")
    void eliminarPorSesionId(@Param("sesionId") Long sesionId);
}
