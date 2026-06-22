package com.agrocloud.cultivos.infrastructure;
import com.agrocloud.core.domain.Empresa;

import com.agrocloud.cultivos.domain.TipoCultivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipoCultivoRepository extends JpaRepository<TipoCultivo, Long> {

    // Buscar por nombre
    Optional<TipoCultivo> findByNombre(String nombre);

    // Buscar por nombre sin distinguir mayúsculas
    Optional<TipoCultivo> findByNombreIgnoreCase(String nombre);

    // Buscar todos los activos ordenados por nombre
    @Query("SELECT t FROM TipoCultivo t WHERE t.activo = true ORDER BY t.nombre ASC")
    List<TipoCultivo> findByActivoTrue();

    // Buscar plantillas globales
    List<TipoCultivo> findByEsPlantillaTrueAndActivoTrue();

    // Buscar personalizaciones por empresa
    @Query("SELECT t FROM TipoCultivo t WHERE t.esPlantilla = false AND t.activo = true")
    List<TipoCultivo> findPersonalizaciones();

    // Buscar por nombre y activo
    Optional<TipoCultivo> findByNombreAndActivoTrue(String nombre);
}

