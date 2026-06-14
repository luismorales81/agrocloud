package com.agrocloud.core.infrastructure;

import com.agrocloud.core.domain.LogAcceso;
import com.agrocloud.core.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository("logAccesoRepositoryCore")
public interface LogAccesoRepository extends JpaRepository<LogAcceso, Long> {

    List<LogAcceso> findByUsuarioOrderByFechaAccesoDesc(User usuario);
    Page<LogAcceso> findByUsuarioOrderByFechaAccesoDesc(User usuario, Pageable pageable);
    List<LogAcceso> findByTipoAccesoOrderByFechaAccesoDesc(String tipoAcceso);
    List<LogAcceso> findByResultadoOrderByFechaAccesoDesc(String resultado);

    @Query("SELECT l FROM LogAcceso l WHERE l.fechaAcceso BETWEEN :fechaInicio AND :fechaFin ORDER BY l.fechaAcceso DESC")
    List<LogAcceso> findByFechaAccesoBetween(@Param("fechaInicio") LocalDateTime fechaInicio, @Param("fechaFin") LocalDateTime fechaFin);

    @Query("SELECT l FROM LogAcceso l WHERE l.usuario = :usuario AND l.fechaAcceso BETWEEN :fechaInicio AND :fechaFin ORDER BY l.fechaAcceso DESC")
    List<LogAcceso> findByUsuarioAndFechaAccesoBetween(@Param("usuario") User usuario, @Param("fechaInicio") LocalDateTime fechaInicio, @Param("fechaFin") LocalDateTime fechaFin);

    long countByUsuario(User usuario);
    long countByUsuarioAndResultado(User usuario, String resultado);
}
