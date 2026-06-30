package com.agrocloud.lecheria.service;

import com.agrocloud.core.application.CampanaContextService;
import com.agrocloud.core.security.ServicioSeguridadContexto;
import com.agrocloud.lecheria.model.dto.LecheriaAccionDiaRespuesta;
import com.agrocloud.lecheria.model.dto.LecheriaPanelRespuesta;
import com.agrocloud.lecheria.model.entity.LecheriaAnimal;
import com.agrocloud.lecheria.model.entity.LecheriaEventoSanitario;
import com.agrocloud.lecheria.model.entity.LecheriaLactancia;
import com.agrocloud.lecheria.model.entity.LecheriaParametroEspecie;
import com.agrocloud.lecheria.model.entity.LecheriaRegistroOrdene;
import com.agrocloud.lecheria.model.enums.LecheriaEstadoAnimal;
import com.agrocloud.lecheria.repository.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class ServicioLecheriaPanel {

    private static final int DIAS_VENTANA_PARTO = 7;
    private static final int DIM_UMBRAL_SERVICIO = 60;
    private static final Comparator<LecheriaRegistroOrdene> COMPARADOR_ORDENE_RECIENTE = Comparator
            .comparing(LecheriaRegistroOrdene::getFecha)
            .thenComparing(LecheriaRegistroOrdene::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()));

    private final ServicioSeguridadContexto servicioSeguridadContexto;
    private final CampanaContextService campanaContextService;
    private final LecheriaAnimalRepository animalRepository;
    private final LecheriaRegistroOrdeneRepository ordeneRepository;
    private final LecheriaEventoReproductivoRepository eventoReproductivoRepository;
    private final LecheriaEventoSanitarioRepository eventoSanitarioRepository;
    private final LecheriaLactanciaRepository lactanciaRepository;
    private final LecheriaParametroEspecieRepository parametroEspecieRepository;

    public ServicioLecheriaPanel(
            ServicioSeguridadContexto servicioSeguridadContexto,
            @Qualifier("campanaContextServiceCore") CampanaContextService campanaContextService,
            LecheriaAnimalRepository animalRepository,
            LecheriaRegistroOrdeneRepository ordeneRepository,
            LecheriaEventoReproductivoRepository eventoReproductivoRepository,
            LecheriaEventoSanitarioRepository eventoSanitarioRepository,
            LecheriaLactanciaRepository lactanciaRepository,
            LecheriaParametroEspecieRepository parametroEspecieRepository) {
        this.servicioSeguridadContexto = servicioSeguridadContexto;
        this.campanaContextService = campanaContextService;
        this.animalRepository = animalRepository;
        this.ordeneRepository = ordeneRepository;
        this.eventoReproductivoRepository = eventoReproductivoRepository;
        this.eventoSanitarioRepository = eventoSanitarioRepository;
        this.lactanciaRepository = lactanciaRepository;
        this.parametroEspecieRepository = parametroEspecieRepository;
    }

    @Transactional(readOnly = true)
    public LecheriaPanelRespuesta resumenPeriodoActivo() {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        Long campanaId = campanaContextService.resolverCampanaIdActiva(empresaId);
        LocalDate hoy = LocalDate.now();
        LocalDate inicioPeriodo = hoy.withDayOfMonth(1);

        BigDecimal litrosTotales = ordeneRepository.sumarLitrosPorEmpresaYFechas(empresaId, inicioPeriodo, hoy);
        if (litrosTotales == null) litrosTotales = BigDecimal.ZERO;

        Long lactando = animalRepository.contarPorEmpresaIdYEstado(empresaId, LecheriaEstadoAnimal.LACTANDO);
        Long secas = animalRepository.contarPorEmpresaIdYEstado(empresaId, LecheriaEstadoAnimal.SECA);
        Long prenadas = animalRepository.contarPorEmpresaIdYEstado(empresaId, LecheriaEstadoAnimal.PRENADA);

        BigDecimal litrosPorAnimal = lactando > 0
                ? litrosTotales.divide(BigDecimal.valueOf(lactando), 3, RoundingMode.HALF_UP) : null;

        BigDecimal promedioRcs = calcularPromedioRcs(empresaId);

        LecheriaPanelRespuesta panel = new LecheriaPanelRespuesta();
        panel.setCampanaId(campanaId);
        panel.setLitrosTotalesPeriodo(litrosTotales);
        panel.setLitrosPorAnimalLactante(litrosPorAnimal);
        panel.setPromedioRcs(promedioRcs);
        panel.setAnimalesLactando(lactando);
        panel.setAnimalesSecas(secas);
        panel.setAnimalesPrenadas(prenadas);
        panel.setAcciones(construirAcciones(empresaId, hoy));
        return panel;
    }

    private BigDecimal calcularPromedioRcs(Long empresaId) {
        List<LecheriaRegistroOrdene> ordenes = ordeneRepository.listarPorEmpresaId(empresaId);
        Optional<LecheriaRegistroOrdene> ultimoConRcs = ordenes.stream()
                .filter(o -> o.getRcs() != null)
                .max(COMPARADOR_ORDENE_RECIENTE);
        return ultimoConRcs.map(o -> BigDecimal.valueOf(o.getRcs())).orElse(null);
    }

    private List<LecheriaAccionDiaRespuesta> construirAcciones(Long empresaId, LocalDate hoy) {
        List<LecheriaAccionDiaRespuesta> acciones = new ArrayList<>();
        List<LecheriaAnimal> animales = animalRepository.listarPorEmpresaId(empresaId);

        for (LecheriaAnimal a : animales) {
            if (a.getEstado() == LecheriaEstadoAnimal.LACTANDO) {
                lactanciaRepository.buscarActivaPorAnimalId(a.getId()).ifPresent(l -> {
                    int dim = ServicioLecheriaAnimales.calcularDim(l);
                    LecheriaParametroEspecie param = parametroEspecieRepository.findById(a.getEspecie()).orElse(null);
                    if (param != null && dim > param.getDimObjetivoSecado()) {
                        acciones.add(accion("SECAR", a, "DIM " + dim + " supera objetivo de secado (" + param.getDimObjetivoSecado() + ")"));
                    }
                    if (a.getEstado() != LecheriaEstadoAnimal.PRENADA && dim > DIM_UMBRAL_SERVICIO) {
                        acciones.add(accion("SERVICIO", a, "Considerar servicio (DIM " + dim + ")"));
                    }
                });
            }
            if (a.getEstado() == LecheriaEstadoAnimal.PRENADA) {
                eventoReproductivoRepository.listarPorAnimalId(a.getId()).stream()
                        .filter(e -> e.getFechaPrevistaParto() != null)
                        .max(Comparator.comparing(e -> e.getFechaPrevistaParto()))
                        .ifPresent(e -> {
                            if (!e.getFechaPrevistaParto().isBefore(hoy.minusDays(DIAS_VENTANA_PARTO))
                                    && !e.getFechaPrevistaParto().isAfter(hoy.plusDays(DIAS_VENTANA_PARTO))) {
                                acciones.add(accion("PARIR", a, "Parto previsto: " + e.getFechaPrevistaParto()));
                            }
                        });
            }
        }

        for (LecheriaAnimal a : animales) {
            if (a.getEstado() == LecheriaEstadoAnimal.LACTANDO) {
                List<LecheriaRegistroOrdene> ordenes = ordeneRepository.listarPorAnimalId(a.getId());
                ordenes.stream().filter(o -> o.getRcs() != null).max(COMPARADOR_ORDENE_RECIENTE)
                        .ifPresent(o -> {
                            LecheriaParametroEspecie param = parametroEspecieRepository.findById(a.getEspecie()).orElse(null);
                            if (param != null && o.getRcs() > param.getUmbralRcsAlerta()) {
                                acciones.add(accion("RCS_ALTO", a, "RCS elevado: " + o.getRcs()));
                            }
                        });
            }
        }

        LocalDate desdeRetiro = hoy.minusDays(30);
        for (LecheriaEventoSanitario ev : eventoSanitarioRepository.listarConRetiroVigente(empresaId, desdeRetiro)) {
            if (ev.getDiasRetiro() != null && !ev.getFecha().plusDays(ev.getDiasRetiro()).isBefore(hoy)) {
                LecheriaAnimal a = ev.getAnimal();
                if (a != null) {
                    acciones.add(accion("RETIRO", a, "Retiro vigente hasta " + ev.getFecha().plusDays(ev.getDiasRetiro())));
                }
            }
        }
        return acciones;
    }

    private LecheriaAccionDiaRespuesta accion(String tipo, LecheriaAnimal a, String mensaje) {
        LecheriaAccionDiaRespuesta dto = new LecheriaAccionDiaRespuesta();
        dto.setTipo(tipo);
        dto.setAnimalId(a.getId());
        dto.setIdentificacion(a.getIdentificacion());
        dto.setMensaje(mensaje);
        return dto;
    }
}
