package com.agrocloud.repository;

import com.agrocloud.model.entity.LogAcceso;
import com.agrocloud.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LogAccesoRepository extends JpaRepository<LogAcceso, Long> {

    /**
     * Buscar logs por usuario
     */
    List<LogAcceso> findByUsuarioOrderByFechaAccesoDesc(User usuario);

    /**
     * Buscar logs por usuario con paginación
     */
    Page<LogAcceso> findByUsuarioOrderByFechaAccesoDesc(User usuario, Pageable pageable);

    /**
     * Buscar logs por tipo de acceso
     */
    List<LogAcceso> findByTipoAccesoOrderByFechaAccesoDesc(String tipoAcceso);

    /**
     * Buscar logs por resultado
     */
    List<LogAcceso> findByResultadoOrderByFechaAccesoDesc(String resultado);

    /**
     * Buscar logs por rango de fechas
     */
    @Query("SELECT l FROM LogAcceso l WHERE l.fechaAcceso BETWEEN :fechaInicio AND :fechaFin ORDER BY l.fechaAcceso DESC")
    List<LogAcceso> findByFechaAccesoBetween(@Param("fechaInicio") LocalDateTime fechaInicio,
                                             @Param("fechaFin") LocalDateTime fechaFin);

    /**
     * Buscar logs por usuario y rango de fechas
     */
    @Query("SELECT l FROM LogAcceso l WHERE l.usuario = :usuario AND l.fechaAcceso BETWEEN :fechaInicio AND :fechaFin ORDER BY l.fechaAcceso DESC")
    List<LogAcceso> findByUsuarioAndFechaAccesoBetween(@Param("usuario") User usuario,
                                                       @Param("fechaInicio") LocalDateTime fechaInicio,
                                                       @Param("fechaFin") LocalDateTime fechaFin);

    /**
     * Contar accesos por usuario
     */
    long countByUsuario(User usuario);

    /**
     * Contar accesos exitosos por usuario
     */
    long countByUsuarioAndResultado(User usuario, String resultado);
}
