package com.agrocloud.core.application;

import com.agrocloud.config.CampanaRequestContext;
import com.agrocloud.core.domain.Campana;
import com.agrocloud.exception.BadRequestException;
import com.agrocloud.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Resuelve la campaña activa del request (header X-Campaign-Id o default de la empresa).
 */
@Service("campanaContextServiceCore")
@Transactional(readOnly = true)
public class CampanaContextService {

    @Autowired
    @Qualifier("campanaServiceCore")
    private CampanaService campanaService;

    @Autowired
    @Qualifier("campanaRepositoryCore")
    private com.agrocloud.core.infrastructure.CampanaRepository campanaRepository;

    /**
     * Campaña del contexto HTTP para la empresa indicada.
     */
    public Campana resolverCampanaActiva(Long empresaId) {
        Long campanaHeader = CampanaRequestContext.getCampanaId();
        if (campanaHeader != null) {
            Campana campana = campanaRepository.findById(campanaHeader)
                    .orElseThrow(() -> new ResourceNotFoundException("Campaña no encontrada"));
            if (!campana.getEmpresaId().equals(empresaId)) {
                throw new BadRequestException("La campaña no pertenece a la empresa del contexto");
            }
            return campana;
        }
        return campanaService.asegurarCampanaActivaPorDefecto(empresaId);
    }

    public Long resolverCampanaIdActiva(Long empresaId) {
        return resolverCampanaActiva(empresaId).getId();
    }

    /**
     * Valida que la campaña activa permita escrituras (no CERRADA).
     */
    public void validarCampanaEditable(Long empresaId) {
        Campana campana = resolverCampanaActiva(empresaId);
        campanaService.validarCampanaEditable(campana);
    }
}
