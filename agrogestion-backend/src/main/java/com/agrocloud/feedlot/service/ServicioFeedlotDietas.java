package com.agrocloud.feedlot.service;

import com.agrocloud.core.security.ServicioSeguridadContexto;
import com.agrocloud.exception.ResourceNotFoundException;
import com.agrocloud.feedlot.model.dto.*;
import com.agrocloud.feedlot.model.entity.FeedlotDieta;
import com.agrocloud.feedlot.model.entity.FeedlotDietaFase;
import com.agrocloud.feedlot.model.entity.FeedlotLote;
import com.agrocloud.feedlot.repository.FeedlotConsumoRepository;
import com.agrocloud.feedlot.repository.FeedlotDietaFaseRepository;
import com.agrocloud.feedlot.repository.FeedlotDietaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServicioFeedlotDietas {

    private final ServicioSeguridadContexto servicioSeguridadContexto;
    private final ServicioFeedlotLotes servicioLotes;
    private final FeedlotDietaRepository dietaRepository;
    private final FeedlotDietaFaseRepository faseRepository;
    private final FeedlotConsumoRepository consumoRepository;

    public ServicioFeedlotDietas(
            ServicioSeguridadContexto servicioSeguridadContexto,
            ServicioFeedlotLotes servicioLotes,
            FeedlotDietaRepository dietaRepository,
            FeedlotDietaFaseRepository faseRepository,
            FeedlotConsumoRepository consumoRepository) {
        this.servicioSeguridadContexto = servicioSeguridadContexto;
        this.servicioLotes = servicioLotes;
        this.dietaRepository = dietaRepository;
        this.faseRepository = faseRepository;
        this.consumoRepository = consumoRepository;
    }

    @Transactional(readOnly = true)
    public List<FeedlotDietaRespuesta> listarDietas() {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        return dietaRepository.listarPorEmpresaId(empresaId).stream()
                .map(this::aDietaRespuesta)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FeedlotDietaRespuesta obtenerDieta(Long dietaId) {
        return aDietaRespuesta(obtenerEntidadDieta(dietaId));
    }

    @Transactional
    public FeedlotDietaRespuesta crearDieta(FeedlotDietaSolicitud solicitud) {
        validarNombreDieta(solicitud);
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        FeedlotDieta dieta = new FeedlotDieta();
        dieta.setEmpresaId(empresaId);
        dieta.setNombre(solicitud.getNombre().trim());
        dieta.setActivo(solicitud.getActivo() != null ? solicitud.getActivo() : Boolean.TRUE);
        return aDietaRespuesta(dietaRepository.save(dieta));
    }

    @Transactional
    public FeedlotDietaRespuesta actualizarDieta(Long dietaId, FeedlotDietaSolicitud solicitud) {
        FeedlotDieta dieta = obtenerEntidadDieta(dietaId);
        if (solicitud.getNombre() != null && !solicitud.getNombre().isBlank()) {
            dieta.setNombre(solicitud.getNombre().trim());
        }
        if (solicitud.getActivo() != null) {
            dieta.setActivo(solicitud.getActivo());
        }
        return aDietaRespuesta(dietaRepository.save(dieta));
    }

    @Transactional
    public FeedlotDietaFaseRespuesta crearFase(Long dietaId, FeedlotDietaFaseSolicitud solicitud) {
        FeedlotDieta dieta = obtenerEntidadDieta(dietaId);
        validarFase(solicitud);
        FeedlotDietaFase fase = new FeedlotDietaFase();
        fase.setDieta(dieta);
        fase.setNombreFase(solicitud.getNombreFase().trim());
        fase.setDiasDesdeIngreso(solicitud.getDiasDesdeIngreso() != null ? solicitud.getDiasDesdeIngreso() : 0);
        fase.setKgMsCabezaDia(solicitud.getKgMsCabezaDia());
        fase.setInsumoId(solicitud.getInsumoId());
        return aFaseRespuesta(faseRepository.save(fase));
    }

    @Transactional
    public FeedlotDietaFaseRespuesta actualizarFase(Long dietaId, Long faseId, FeedlotDietaFaseSolicitud solicitud) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        FeedlotDietaFase fase = faseRepository.buscarPorIdYDietaIdYEmpresaId(faseId, dietaId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Fase de dieta no encontrada"));
        if (solicitud.getNombreFase() != null && !solicitud.getNombreFase().isBlank()) {
            fase.setNombreFase(solicitud.getNombreFase().trim());
        }
        if (solicitud.getDiasDesdeIngreso() != null) {
            fase.setDiasDesdeIngreso(solicitud.getDiasDesdeIngreso());
        }
        if (solicitud.getKgMsCabezaDia() != null) {
            fase.setKgMsCabezaDia(solicitud.getKgMsCabezaDia());
        }
        if (solicitud.getInsumoId() != null) {
            fase.setInsumoId(solicitud.getInsumoId());
        }
        return aFaseRespuesta(faseRepository.save(fase));
    }

    @Transactional(readOnly = true)
    public FeedlotConsumoTeoricoRespuesta calcularConsumoTeorico(
            Long loteId, LocalDate fechaDesde, LocalDate fechaHasta) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        FeedlotLote lote = servicioLotes.obtenerEntidadLote(empresaId, loteId);
        if (lote.getDieta() == null) {
            throw new IllegalStateException("El lote no tiene dieta asignada");
        }
        FeedlotDieta dieta = obtenerEntidadDieta(lote.getDieta().getId());
        LocalDate desde = fechaDesde != null ? fechaDesde : lote.getFechaIngreso();
        LocalDate hasta = fechaHasta != null ? fechaHasta : LocalDate.now();
        if (hasta.isBefore(desde)) {
            throw new IllegalArgumentException("La fecha hasta no puede ser anterior a la fecha desde");
        }

        FeedlotConsumoTeoricoRespuesta resp = new FeedlotConsumoTeoricoRespuesta();
        resp.setLoteId(loteId);
        resp.setDietaId(dieta.getId());
        resp.setDietaNombre(dieta.getNombre());
        resp.setFechaDesde(desde);
        resp.setFechaHasta(hasta);

        List<FeedlotDietaFase> fases = dieta.getFases().stream()
                .sorted(Comparator.comparing(FeedlotDietaFase::getDiasDesdeIngreso).reversed())
                .toList();

        for (LocalDate fecha = desde; !fecha.isAfter(hasta); fecha = fecha.plusDays(1)) {
            FeedlotDietaFase fase = resolverFase(fases, lote.getFechaIngreso(), fecha);
            int cabezas = lote.getCabezasActuales() != null ? lote.getCabezasActuales() : 0;
            BigDecimal teorico = BigDecimal.ZERO;
            String nombreFase = null;
            if (fase != null && cabezas > 0) {
                teorico = fase.getKgMsCabezaDia()
                        .multiply(BigDecimal.valueOf(cabezas))
                        .setScale(3, RoundingMode.HALF_UP);
                nombreFase = fase.getNombreFase();
            }
            BigDecimal real = sumarConsumoMsDia(loteId, empresaId, fecha);
            FeedlotConsumoTeoricoRespuesta.DiaConsumo dia = new FeedlotConsumoTeoricoRespuesta.DiaConsumo();
            dia.setFecha(fecha);
            dia.setFaseNombre(nombreFase);
            dia.setKgMsTeorico(teorico);
            dia.setKgReal(real);
            dia.setDiferenciaKg(real.subtract(teorico));
            resp.getDias().add(dia);
        }
        return resp;
    }

    private BigDecimal sumarConsumoMsDia(Long loteId, Long empresaId, LocalDate fecha) {
        BigDecimal total = consumoRepository.sumarMateriaSecaKgPorLoteIdEmpresaIdYFecha(loteId, empresaId, fecha);
        if (total != null && total.compareTo(BigDecimal.ZERO) > 0) {
            return total;
        }
        BigDecimal kg = consumoRepository.sumarCantidadKgPorLoteIdEmpresaIdYFecha(loteId, empresaId, fecha);
        return kg != null ? kg : BigDecimal.ZERO;
    }

    FeedlotDietaFase resolverFase(List<FeedlotDietaFase> fasesOrdenDesc, LocalDate fechaIngreso, LocalDate fecha) {
        long dias = ChronoUnit.DAYS.between(fechaIngreso, fecha);
        for (FeedlotDietaFase fase : fasesOrdenDesc) {
            if (dias >= fase.getDiasDesdeIngreso()) {
                return fase;
            }
        }
        return null;
    }

    FeedlotDieta obtenerEntidadDieta(Long dietaId) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        return dietaRepository.buscarPorIdYEmpresaId(dietaId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Dieta no encontrada"));
    }

    private static void validarNombreDieta(FeedlotDietaSolicitud solicitud) {
        if (solicitud.getNombre() == null || solicitud.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre de la dieta es obligatorio");
        }
    }

    private static void validarFase(FeedlotDietaFaseSolicitud solicitud) {
        if (solicitud.getNombreFase() == null || solicitud.getNombreFase().isBlank()) {
            throw new IllegalArgumentException("El nombre de la fase es obligatorio");
        }
        if (solicitud.getKgMsCabezaDia() == null || solicitud.getKgMsCabezaDia().signum() <= 0) {
            throw new IllegalArgumentException("Los kg MS/cabeza/día deben ser mayores a cero");
        }
    }

    private FeedlotDietaRespuesta aDietaRespuesta(FeedlotDieta d) {
        FeedlotDietaRespuesta dto = new FeedlotDietaRespuesta();
        dto.setId(d.getId());
        dto.setEmpresaId(d.getEmpresaId());
        dto.setNombre(d.getNombre());
        dto.setActivo(d.getActivo());
        dto.setCreatedAt(d.getCreatedAt());
        dto.setUpdatedAt(d.getUpdatedAt());
        dto.setFases(d.getFases().stream().map(this::aFaseRespuesta).collect(Collectors.toList()));
        return dto;
    }

    private FeedlotDietaFaseRespuesta aFaseRespuesta(FeedlotDietaFase f) {
        FeedlotDietaFaseRespuesta dto = new FeedlotDietaFaseRespuesta();
        dto.setId(f.getId());
        dto.setDietaId(f.getDieta() != null ? f.getDieta().getId() : null);
        dto.setNombreFase(f.getNombreFase());
        dto.setDiasDesdeIngreso(f.getDiasDesdeIngreso());
        dto.setKgMsCabezaDia(f.getKgMsCabezaDia());
        dto.setInsumoId(f.getInsumoId());
        dto.setCreatedAt(f.getCreatedAt());
        dto.setUpdatedAt(f.getUpdatedAt());
        return dto;
    }
}
