package com.agrocloud.trazabilidad.application;

import com.agrocloud.trazabilidad.domain.TrazabilidadCertificacion;
import com.agrocloud.trazabilidad.infrastructure.TrazabilidadCertificacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Carga de definiciones y reglas de certificación desde almacenamiento.
 */
@Service
@Transactional(readOnly = true)
public class CertificacionTrazabilidadService {

    @Autowired
    private TrazabilidadCertificacionRepository certificacionRepository;

    public TrazabilidadCertificacion obtenerCertificacionConReglas(String codigo) {
        return certificacionRepository.findByCodigoConReglas(codigo)
                .orElseThrow(() -> new IllegalArgumentException("Certificación desconocida o inactiva: " + codigo));
    }
}
