package com.agrocloud.lecheria.service;

import com.agrocloud.core.application.CampanaContextService;
import com.agrocloud.core.security.ServicioSeguridadContexto;
import com.agrocloud.exception.ResourceNotFoundException;
import com.agrocloud.lecheria.model.dto.LecheriaCloseoutRodeoRespuesta;
import com.agrocloud.lecheria.model.entity.LecheriaAnimal;
import com.agrocloud.lecheria.model.entity.LecheriaCloseoutRodeo;
import com.agrocloud.lecheria.model.entity.LecheriaRodeo;
import com.agrocloud.lecheria.model.enums.LecheriaEstadoAnimal;
import com.agrocloud.lecheria.repository.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class ServicioLecheriaCloseout {

    private final ServicioSeguridadContexto servicioSeguridadContexto;
    private final CampanaContextService campanaContextService;
    private final LecheriaRodeoRepository rodeoRepository;
    private final LecheriaAnimalRepository animalRepository;
    private final LecheriaRegistroOrdeneRepository ordeneRepository;
    private final LecheriaConsumoRepository consumoRepository;
    private final LecheriaVentaLecheRepository ventaLecheRepository;
    private final LecheriaCloseoutRodeoRepository closeoutRepository;

    public ServicioLecheriaCloseout(
            ServicioSeguridadContexto servicioSeguridadContexto,
            @Qualifier("campanaContextServiceCore") CampanaContextService campanaContextService,
            LecheriaRodeoRepository rodeoRepository,
            LecheriaAnimalRepository animalRepository,
            LecheriaRegistroOrdeneRepository ordeneRepository,
            LecheriaConsumoRepository consumoRepository,
            LecheriaVentaLecheRepository ventaLecheRepository,
            LecheriaCloseoutRodeoRepository closeoutRepository) {
        this.servicioSeguridadContexto = servicioSeguridadContexto;
        this.campanaContextService = campanaContextService;
        this.rodeoRepository = rodeoRepository;
        this.animalRepository = animalRepository;
        this.ordeneRepository = ordeneRepository;
        this.consumoRepository = consumoRepository;
        this.ventaLecheRepository = ventaLecheRepository;
        this.closeoutRepository = closeoutRepository;
    }

    @Transactional(readOnly = true)
    public LecheriaCloseoutRodeoRespuesta calcularCloseoutRodeo(Long rodeoId) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        Long campanaId = campanaContextService.resolverCampanaIdActiva(empresaId);
        LecheriaRodeo rodeo = rodeoRepository.buscarPorIdYEmpresaId(rodeoId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Rodeo no encontrado"));

        BigDecimal litrosTotales = BigDecimal.ZERO;
        List<LecheriaAnimal> animales = animalRepository.listarPorEmpresaId(empresaId);
        for (LecheriaAnimal a : animales) {
            if (a.getRodeo() != null && a.getRodeo().getId().equals(rodeoId)
                    && a.getEstado() == LecheriaEstadoAnimal.LACTANDO) {
                for (var o : ordeneRepository.listarPorAnimalId(a.getId())) {
                    litrosTotales = litrosTotales.add(o.getLitros());
                }
            }
        }

        BigDecimal costoAlimentacion = consumoRepository.sumarCantidadKgPorRodeoYCampana(rodeoId, campanaId);
        if (costoAlimentacion == null) costoAlimentacion = BigDecimal.ZERO;

        BigDecimal ingresos = ventaLecheRepository.sumarTotalPorEmpresaYCampana(empresaId, campanaId);
        if (ingresos == null) ingresos = BigDecimal.ZERO;

        BigDecimal margen = ingresos.subtract(costoAlimentacion).setScale(2, RoundingMode.HALF_UP);

        LecheriaCloseoutRodeoRespuesta resp = new LecheriaCloseoutRodeoRespuesta();
        resp.setRodeoId(rodeoId);
        resp.setRodeoNombre(rodeo.getNombre());
        resp.setCampanaId(campanaId);
        resp.setFechaCierre(LocalDate.now());
        resp.setLitrosTotales(litrosTotales);
        resp.setCostoAlimentacion(costoAlimentacion);
        resp.setIngresosLeche(ingresos);
        resp.setMargen(margen);
        return resp;
    }

    @Transactional
    public LecheriaCloseoutRodeoRespuesta guardarCloseoutRodeo(Long rodeoId, String observaciones) {
        LecheriaCloseoutRodeoRespuesta calc = calcularCloseoutRodeo(rodeoId);
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        LecheriaRodeo rodeo = rodeoRepository.buscarPorIdYEmpresaId(rodeoId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Rodeo no encontrado"));

        LecheriaCloseoutRodeo c = new LecheriaCloseoutRodeo();
        c.setRodeo(rodeo);
        c.setEmpresaId(empresaId);
        c.setCampanaId(calc.getCampanaId());
        c.setFechaCierre(calc.getFechaCierre());
        c.setLitrosTotales(calc.getLitrosTotales());
        c.setCostoAlimentacion(calc.getCostoAlimentacion());
        c.setIngresosLeche(calc.getIngresosLeche());
        c.setMargen(calc.getMargen());
        c.setObservaciones(observaciones);
        c = closeoutRepository.save(c);

        calc.setId(c.getId());
        calc.setObservaciones(observaciones);
        return calc;
    }
}
