package com.agrocloud.avicola.huevos.repository;

import com.agrocloud.avicola.huevos.model.entity.AvicolaHuevoProduccionDiaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AvicolaHuevoProduccionDiariaRepository extends JpaRepository<AvicolaHuevoProduccionDiaria, Long> {

    @Query("SELECT p FROM AvicolaHuevoProduccionDiaria p WHERE p.lote.id = :loteId AND p.empresaId = :empresaId ORDER BY p.fecha DESC, p.id DESC")
    List<AvicolaHuevoProduccionDiaria> listarPorLoteIdYEmpresaId(@Param("loteId") Long loteId, @Param("empresaId") Long empresaId);

    @Query("SELECT p FROM AvicolaHuevoProduccionDiaria p WHERE p.lote.id = :loteId AND p.empresaId = :empresaId AND p.fecha = :fecha")
    Optional<AvicolaHuevoProduccionDiaria> buscarPorLoteEmpresaYFecha(
            @Param("loteId") Long loteId,
            @Param("empresaId") Long empresaId,
            @Param("fecha") LocalDate fecha);

    @Query("SELECT p FROM AvicolaHuevoProduccionDiaria p WHERE p.id = :id AND p.empresaId = :empresaId")
    Optional<AvicolaHuevoProduccionDiaria> buscarPorIdYEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    @Query("SELECT COALESCE(SUM(p.totalHuevosDia), 0) FROM AvicolaHuevoProduccionDiaria p WHERE p.lote.id = :loteId AND p.empresaId = :empresaId")
    Long sumarCantidadHuevosPorLoteYEmpresa(@Param("loteId") Long loteId, @Param("empresaId") Long empresaId);

    @Query("SELECT COALESCE(SUM(p.totalHuevosDia), 0) FROM AvicolaHuevoProduccionDiaria p WHERE p.empresaId = :empresaId AND p.fecha BETWEEN :desde AND :hasta")
    Long sumarTotalHuevosEmpresaEnRango(
            @Param("empresaId") Long empresaId,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);

    @Query("SELECT COALESCE(SUM(p.totalHuevosDia), 0) FROM AvicolaHuevoProduccionDiaria p WHERE p.lote.id = :loteId AND p.empresaId = :empresaId AND p.fecha BETWEEN :desde AND :hasta")
    Long sumarTotalHuevosLoteEnRango(
            @Param("loteId") Long loteId,
            @Param("empresaId") Long empresaId,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);

    @Query("SELECT AVG(p.temperaturaDia) FROM AvicolaHuevoProduccionDiaria p WHERE p.lote.id = :loteId AND p.empresaId = :empresaId AND p.fecha BETWEEN :desde AND :hasta AND p.temperaturaDia IS NOT NULL")
    Double promedioTemperaturaLoteEnRango(
            @Param("loteId") Long loteId,
            @Param("empresaId") Long empresaId,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);

    @Query("SELECT AVG(p.humedadDia) FROM AvicolaHuevoProduccionDiaria p WHERE p.lote.id = :loteId AND p.empresaId = :empresaId AND p.fecha BETWEEN :desde AND :hasta AND p.humedadDia IS NOT NULL")
    Double promedioHumedadLoteEnRango(
            @Param("loteId") Long loteId,
            @Param("empresaId") Long empresaId,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);

    @Query("SELECT p FROM AvicolaHuevoProduccionDiaria p WHERE p.lote.id = :loteId AND p.empresaId = :empresaId AND p.fecha BETWEEN :desde AND :hasta ORDER BY p.fecha ASC, p.id ASC")
    List<AvicolaHuevoProduccionDiaria> listarPorLoteYEmpresaEnRango(
            @Param("loteId") Long loteId,
            @Param("empresaId") Long empresaId,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);
}
