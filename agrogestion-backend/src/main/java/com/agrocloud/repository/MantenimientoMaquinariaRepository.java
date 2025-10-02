package com.agrocloud.repository;

import com.agrocloud.model.entity.MantenimientoMaquinaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio para la entidad MantenimientoMaquinaria.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
/**
 * @deprecated Repositorio deprecado. La tabla mantenimiento_maquinaria será eliminada en V1_13.
 * Funcionalidad no implementada. Puede reimplementarse después si se necesita.
 */
@Deprecated
@Repository
public interface MantenimientoMaquinariaRepository extends JpaRepository<MantenimientoMaquinaria, Long> {

    /**
     * Busca mantenimientos por usuario.
     */
    List<MantenimientoMaquinaria> findByUsuarioIdOrderByFechaRealizadaDesc(Long usuarioId);

    /**
     * Busca mantenimientos por maquinaria.
     */
    List<MantenimientoMaquinaria> findByMaquinariaIdOrderByFechaRealizadaDesc(Long maquinariaId);

    /**
     * Busca mantenimientos por usuario y rango de fechas (fecha realizada).
     */
    List<MantenimientoMaquinaria> findByUsuarioIdAndFechaRealizadaBetween(Long usuarioId, LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Busca mantenimientos por maquinaria y rango de fechas (fecha realizada).
     */
    List<MantenimientoMaquinaria> findByMaquinariaIdAndFechaRealizadaBetween(Long maquinariaId, LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Busca mantenimientos por tipo.
     */
    List<MantenimientoMaquinaria> findByTipoMantenimientoOrderByFechaRealizadaDesc(MantenimientoMaquinaria.TipoMantenimiento tipoMantenimiento);

    /**
     * Busca mantenimientos por estado.
     */
    List<MantenimientoMaquinaria> findByEstadoOrderByFechaRealizadaDesc(MantenimientoMaquinaria.EstadoMantenimiento estado);

    /**
     * Busca mantenimientos por usuario y rango de fechas (fecha programada).
     */
    List<MantenimientoMaquinaria> findByUsuarioIdAndFechaProgramadaBetween(Long usuarioId, LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Busca mantenimientos por maquinaria y rango de fechas (fecha programada).
     */
    List<MantenimientoMaquinaria> findByMaquinariaIdAndFechaProgramadaBetween(Long maquinariaId, LocalDate fechaInicio, LocalDate fechaFin);
}
