package com.agrocloud.trazabilidad.infrastructure;

import com.agrocloud.trazabilidad.domain.TrazabilidadReglaCertificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrazabilidadReglaCertificacionRepository extends JpaRepository<TrazabilidadReglaCertificacion, Long> {
}
