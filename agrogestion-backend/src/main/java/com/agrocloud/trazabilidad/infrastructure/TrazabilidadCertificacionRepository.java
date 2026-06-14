package com.agrocloud.trazabilidad.infrastructure;

import com.agrocloud.trazabilidad.domain.TrazabilidadCertificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrazabilidadCertificacionRepository extends JpaRepository<TrazabilidadCertificacion, Long> {

    Optional<TrazabilidadCertificacion> findByCodigoAndActivaTrue(String codigo);

    @Query("SELECT DISTINCT c FROM TrazabilidadCertificacion c LEFT JOIN FETCH c.reglas r WHERE c.codigo = :codigo AND c.activa = true")
    Optional<TrazabilidadCertificacion> findByCodigoConReglas(@Param("codigo") String codigo);

    @Query("SELECT DISTINCT c FROM TrazabilidadCertificacion c LEFT JOIN FETCH c.reglas WHERE c.activa = true ORDER BY c.codigo")
    List<TrazabilidadCertificacion> findActivasConReglasOrdenadas();
}
