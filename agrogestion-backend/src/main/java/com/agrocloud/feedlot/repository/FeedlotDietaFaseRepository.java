package com.agrocloud.feedlot.repository;

import com.agrocloud.feedlot.model.entity.FeedlotDietaFase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FeedlotDietaFaseRepository extends JpaRepository<FeedlotDietaFase, Long> {

    @Query("SELECT f FROM FeedlotDietaFase f WHERE f.dieta.id = :dietaId ORDER BY f.diasDesdeIngreso ASC, f.id ASC")
    List<FeedlotDietaFase> listarPorDietaId(@Param("dietaId") Long dietaId);

    @Query("SELECT f FROM FeedlotDietaFase f WHERE f.id = :id AND f.dieta.id = :dietaId AND f.dieta.empresaId = :empresaId")
    Optional<FeedlotDietaFase> buscarPorIdYDietaIdYEmpresaId(
            @Param("id") Long id, @Param("dietaId") Long dietaId, @Param("empresaId") Long empresaId);
}
