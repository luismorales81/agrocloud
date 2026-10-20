package com.agrocloud.feedlot.repository;

import com.agrocloud.feedlot.model.entity.FeedlotPerfilLecturaManga;
import com.agrocloud.feedlot.model.enums.FeedlotTipoSesionManga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FeedlotPerfilLecturaMangaRepository extends JpaRepository<FeedlotPerfilLecturaManga, Long> {

    @Query("SELECT p FROM FeedlotPerfilLecturaManga p WHERE p.empresaId = :empresaId ORDER BY p.tipo, p.nombre")
    List<FeedlotPerfilLecturaManga> listarPorEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT p FROM FeedlotPerfilLecturaManga p WHERE p.empresaId = :empresaId AND p.tipo = :tipo ORDER BY p.predeterminado DESC, p.nombre")
    List<FeedlotPerfilLecturaManga> listarPorEmpresaYTipo(
            @Param("empresaId") Long empresaId,
            @Param("tipo") FeedlotTipoSesionManga tipo);

    @Query("SELECT p FROM FeedlotPerfilLecturaManga p WHERE p.id = :id AND p.empresaId = :empresaId")
    Optional<FeedlotPerfilLecturaManga> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    @Modifying
    @Query("UPDATE FeedlotPerfilLecturaManga p SET p.predeterminado = false WHERE p.empresaId = :empresaId AND p.tipo = :tipo AND p.id <> :exceptoId")
    void quitarPredeterminados(
            @Param("empresaId") Long empresaId,
            @Param("tipo") FeedlotTipoSesionManga tipo,
            @Param("exceptoId") Long exceptoId);
}
