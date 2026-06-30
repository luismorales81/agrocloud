package com.agrocloud.lecheria.service;

import com.agrocloud.core.security.ServicioSeguridadContexto;
import com.agrocloud.exception.ResourceNotFoundException;
import com.agrocloud.lecheria.model.dto.*;
import com.agrocloud.lecheria.model.entity.LecheriaAnimal;
import com.agrocloud.lecheria.model.entity.LecheriaLactancia;
import com.agrocloud.lecheria.model.entity.LecheriaRegistroOrdene;
import com.agrocloud.lecheria.model.enums.LecheriaEstadoAnimal;
import com.agrocloud.lecheria.repository.LecheriaAnimalRepository;
import com.agrocloud.lecheria.repository.LecheriaLactanciaRepository;
import com.agrocloud.lecheria.repository.LecheriaRegistroOrdeneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ServicioLecheriaReportes {

    private final ServicioSeguridadContexto servicioSeguridadContexto;
    private final LecheriaAnimalRepository animalRepository;
    private final LecheriaLactanciaRepository lactanciaRepository;
    private final LecheriaRegistroOrdeneRepository ordeneRepository;

    public ServicioLecheriaReportes(
            ServicioSeguridadContexto servicioSeguridadContexto,
            LecheriaAnimalRepository animalRepository,
            LecheriaLactanciaRepository lactanciaRepository,
            LecheriaRegistroOrdeneRepository ordeneRepository) {
        this.servicioSeguridadContexto = servicioSeguridadContexto;
        this.animalRepository = animalRepository;
        this.lactanciaRepository = lactanciaRepository;
        this.ordeneRepository = ordeneRepository;
    }

    @Transactional(readOnly = true)
    public List<LecheriaCurvaLactanciaRespuesta> curvasLactancia(Long animalId) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        if (animalId != null) {
            LecheriaAnimal animal = animalRepository.buscarPorIdYEmpresaId(animalId, empresaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Animal no encontrado"));
            return List.of(construirCurva(animal));
        }
        return animalRepository.listarPorEmpresaId(empresaId).stream()
                .filter(a -> a.getEstado() == LecheriaEstadoAnimal.LACTANDO)
                .map(this::construirCurva)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LecheriaRankingProduccionRespuesta rankingProduccion(LocalDate desde, LocalDate hasta) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        LocalDate fin = hasta != null ? hasta : LocalDate.now();
        LocalDate ini = desde != null ? desde : fin.minusDays(30);

        Map<Long, BigDecimal> totales = new HashMap<>();
        Map<Long, String> identificaciones = new HashMap<>();
        for (LecheriaAnimal a : animalRepository.listarPorEmpresaId(empresaId)) {
            identificaciones.put(a.getId(), a.getIdentificacion());
            List<LecheriaRegistroOrdene> ordenes = ordeneRepository.listarPorAnimalId(a.getId());
            BigDecimal suma = ordenes.stream()
                    .filter(o -> !o.getFecha().isBefore(ini) && !o.getFecha().isAfter(fin))
                    .map(LecheriaRegistroOrdene::getLitros)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (suma.compareTo(BigDecimal.ZERO) > 0) {
                totales.put(a.getId(), suma);
            }
        }

        LecheriaRankingProduccionRespuesta resp = new LecheriaRankingProduccionRespuesta();
        totales.entrySet().stream()
                .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
                .forEach(e -> resp.getItems().add(new LecheriaRankingProduccionRespuesta.ItemRanking(
                        e.getKey(), identificaciones.get(e.getKey()), e.getValue())));
        return resp;
    }

    @Transactional(readOnly = true)
    public LecheriaClimaProduccionRespuesta climaProduccion(LocalDate desde, LocalDate hasta) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        LocalDate fin = hasta != null ? hasta : LocalDate.now();
        LocalDate ini = desde != null ? desde : fin.minusDays(30);

        Map<LocalDate, LecheriaClimaProduccionRespuesta.PuntoClima> porFecha = new TreeMap<>();
        for (LecheriaRegistroOrdene o : ordeneRepository.listarPorEmpresaId(empresaId)) {
            if (o.getFecha().isBefore(ini) || o.getFecha().isAfter(fin)) continue;
            LecheriaClimaProduccionRespuesta.PuntoClima punto = porFecha.computeIfAbsent(o.getFecha(),
                    f -> new LecheriaClimaProduccionRespuesta.PuntoClima(f, null, null, BigDecimal.ZERO));
            punto.setLitros(punto.getLitros().add(o.getLitros()));
            if (o.getTemperaturaAmbiente() != null) punto.setTemperatura(o.getTemperaturaAmbiente());
            if (o.getHumedadAmbiente() != null) punto.setHumedad(o.getHumedadAmbiente());
        }
        LecheriaClimaProduccionRespuesta resp = new LecheriaClimaProduccionRespuesta();
        resp.getPuntos().addAll(porFecha.values());
        return resp;
    }

    private LecheriaCurvaLactanciaRespuesta construirCurva(LecheriaAnimal animal) {
        LecheriaCurvaLactanciaRespuesta curva = new LecheriaCurvaLactanciaRespuesta();
        curva.setAnimalId(animal.getId());
        curva.setIdentificacion(animal.getIdentificacion());
        lactanciaRepository.buscarActivaPorAnimalId(animal.getId()).ifPresent(l -> {
            for (LecheriaRegistroOrdene o : ordeneRepository.listarPorLactanciaId(l.getId())) {
                int dim = ServicioLecheriaAnimales.calcularDim(l);
                if (l.getFechaParto() != null) {
                    dim = (int) java.time.temporal.ChronoUnit.DAYS.between(l.getFechaParto(), o.getFecha());
                }
                curva.getPuntos().add(new LecheriaCurvaLactanciaRespuesta.PuntoDimLitros(dim, o.getLitros(), o.getFecha()));
            }
        });
        return curva;
    }
}
