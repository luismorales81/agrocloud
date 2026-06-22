package com.agrocloud.cultivos.application;

import com.agrocloud.core.domain.Campana;
import com.agrocloud.cultivos.domain.CicloCultivo;
import com.agrocloud.cultivos.domain.Cultivo;
import com.agrocloud.cultivos.domain.EstadoCicloCultivo;
import com.agrocloud.cultivos.domain.Plot;
import com.agrocloud.cultivos.infrastructure.CicloCultivoRepository;
import com.agrocloud.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service("cicloCultivoServiceCultivos")
@Transactional
public class CicloCultivoService {

    @Autowired
    @Qualifier("cicloCultivoRepositoryCultivos")
    private CicloCultivoRepository cicloCultivoRepository;

    public CicloCultivo abrirCiclo(Plot lote, Cultivo cultivo, Campana campana, LocalDate fechaSiembra) {
        cicloCultivoRepository.findCicloActivoPorLote(lote.getId())
                .ifPresent(c -> {
                    throw new BadRequestException("El lote ya tiene un ciclo activo");
                });
        CicloCultivo ciclo = new CicloCultivo();
        ciclo.setCampanaId(campana.getId());
        ciclo.setLote(lote);
        ciclo.setCultivo(cultivo);
        ciclo.setSuperficieHectareas(lote.getAreaHectareas());
        ciclo.setFechaSiembra(fechaSiembra);
        ciclo.setEstado(EstadoCicloCultivo.EN_CULTIVO);
        ciclo = cicloCultivoRepository.save(ciclo);
        lote.setCicloActivoId(ciclo.getId());
        return ciclo;
    }

    public CicloCultivo cerrarCiclo(Plot lote, LocalDate fechaCosecha) {
        CicloCultivo ciclo = obtenerCicloActivo(lote.getId())
                .orElseThrow(() -> new BadRequestException("No hay ciclo activo en el lote"));
        ciclo.setFechaCosecha(fechaCosecha);
        ciclo.setEstado(EstadoCicloCultivo.COSECHADO);
        lote.setCicloActivoId(null);
        return cicloCultivoRepository.save(ciclo);
    }

    public CicloCultivo abandonarCiclo(Plot lote) {
        Optional<CicloCultivo> cicloOpt = obtenerCicloActivo(lote.getId());
        if (cicloOpt.isEmpty()) {
            return null;
        }
        CicloCultivo ciclo = cicloOpt.get();
        ciclo.setEstado(EstadoCicloCultivo.ABANDONADO);
        ciclo.setFechaCosecha(LocalDate.now());
        lote.setCicloActivoId(null);
        return cicloCultivoRepository.save(ciclo);
    }

    public Optional<CicloCultivo> obtenerCicloActivo(Long loteId) {
        if (loteId == null) {
            return Optional.empty();
        }
        return cicloCultivoRepository.findCicloActivoPorLote(loteId);
    }

    @Transactional(readOnly = true)
    public List<CicloCultivo> listarPorCampana(Long campanaId) {
        return cicloCultivoRepository.findByCampanaIdOrderByFechaSiembraDesc(campanaId);
    }
}
