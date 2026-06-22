package com.agrocloud.cultivos.application;

import com.agrocloud.cultivos.domain.EstadoLoteConfig;
import com.agrocloud.cultivos.domain.Labor;
import com.agrocloud.cultivos.domain.Plot;
import com.agrocloud.cultivos.domain.TareaPorEstadoConfig;
import com.agrocloud.cultivos.domain.TipoCultivo;
import com.agrocloud.cultivos.domain.TransicionEstadoConfig;
import com.agrocloud.cultivos.util.MapeadorEstadoLoteConfig;
import com.agrocloud.dto.ProgresoEstadoLoteDTO;
import com.agrocloud.cultivos.infrastructure.LaborRepository;
import com.agrocloud.cultivos.infrastructure.PlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Calcula el progreso visual del lote en su camino de estados configurados.
 */
@Service
public class ProgresoEstadoLoteService {

    private final PlotRepository plotRepository;
    private final LaborRepository laborRepository;
    private final ConfiguracionEstadosService configuracionEstadosService;
    private final EstadoLoteUpdater estadoLoteUpdater;

    public ProgresoEstadoLoteService(PlotRepository plotRepository,
                                       LaborRepository laborRepository,
                                       ConfiguracionEstadosService configuracionEstadosService,
                                       EstadoLoteUpdater estadoLoteUpdater) {
        this.plotRepository = plotRepository;
        this.laborRepository = laborRepository;
        this.configuracionEstadosService = configuracionEstadosService;
        this.estadoLoteUpdater = estadoLoteUpdater;
    }

    @Transactional
    public ProgresoEstadoLoteDTO calcularProgreso(Long loteId, Long empresaId) {
        Plot lote = plotRepository.findById(loteId)
            .orElseThrow(() -> new IllegalArgumentException("Lote no encontrado"));

        ProgresoEstadoLoteDTO dto = new ProgresoEstadoLoteDTO();
        dto.setLoteId(loteId);
        dto.setLoteNombre(lote.getNombre());

        Long tipoCultivoId = resolverTipoCultivoId(lote);
        if (tipoCultivoId == null || empresaId == null) {
            dto.setUsaConfiguracion(false);
            dto.setMensajeAvance("Asigne un tipo de cultivo con estados configurados para ver el camino del lote.");
            if (lote.getEstado() != null) {
                ProgresoEstadoLoteDTO.EstadoResumenDTO actual = new ProgresoEstadoLoteDTO.EstadoResumenDTO();
                actual.setNombre(lote.getEstado().getDescripcion());
                dto.setEstadoActual(actual);
            }
            return dto;
        }

        if (lote.getEstadoConfigurado() == null) {
            estadoLoteUpdater.recalcularEstado(loteId);
            lote = plotRepository.findById(loteId).orElse(lote);
        }

        List<EstadoLoteConfig> estados = configuracionEstadosService
            .obtenerEstadosPorTipoCultivo(tipoCultivoId, empresaId)
            .stream()
            .sorted(Comparator.comparing(EstadoLoteConfig::getOrden))
            .toList();

        if (estados.isEmpty()) {
            dto.setUsaConfiguracion(false);
            dto.setMensajeAvance("Configure estados para este tipo de cultivo en Cultivos → Configuración.");
            return dto;
        }

        dto.setUsaConfiguracion(true);
        List<Labor> labores = laborRepository.findByLoteId(loteId);
        List<Labor> completadas = labores.stream()
            .filter(l -> Boolean.TRUE.equals(l.getActivo()))
            .filter(l -> l.getEstado() == Labor.EstadoLabor.COMPLETADA)
            .toList();

        EstadoLoteConfig actualConfig = lote.getEstadoConfigurado();
        int indiceActual = actualConfig != null
            ? estados.indexOf(actualConfig)
            : estados.stream().filter(EstadoLoteConfig::getEsEstadoInicial).findFirst()
                .map(estados::indexOf).orElse(0);

        if (indiceActual < 0) {
            indiceActual = 0;
        }

        for (int i = 0; i < estados.size(); i++) {
            EstadoLoteConfig e = estados.get(i);
            ProgresoEstadoLoteDTO.PasoCaminoDTO paso = new ProgresoEstadoLoteDTO.PasoCaminoDTO();
            paso.setId(e.getId());
            paso.setNombre(e.getNombre());
            paso.setColor(e.getColor());
            paso.setIcono(e.getIcono());
            paso.setOrden(e.getOrden());
            paso.setActual(i == indiceActual);
            paso.setCompletado(i < indiceActual);
            dto.getCaminoEstados().add(paso);
        }

        if (actualConfig != null) {
            dto.setEstadoActual(resumirEstado(actualConfig));
        } else if (lote.getEstado() != null) {
            ProgresoEstadoLoteDTO.EstadoResumenDTO resumen = new ProgresoEstadoLoteDTO.EstadoResumenDTO();
            resumen.setNombre(lote.getEstado().getDescripcion());
            dto.setEstadoActual(resumen);
        }

        Long diasSiembra = lote.getFechaSiembra() != null
            ? ChronoUnit.DAYS.between(lote.getFechaSiembra(), LocalDate.now()) : null;
        dto.setDiasDesdeSiembra(diasSiembra);

        List<TransicionEstadoConfig> transiciones = actualConfig != null
            ? configuracionEstadosService.obtenerTransicionesValidasDesdeEstado(actualConfig.getId(), empresaId)
            : List.of();

        transiciones.stream()
            .filter(t -> t.getEstadoDestino() != null)
            .sorted(Comparator.comparing(t -> t.getEstadoDestino().getOrden()))
            .forEach(t -> {
                ProgresoEstadoLoteDTO.TransicionDisponibleDTO tr = new ProgresoEstadoLoteDTO.TransicionDisponibleDTO();
                tr.setDestinoId(t.getEstadoDestino().getId());
                tr.setDestinoNombre(t.getEstadoDestino().getNombre());
                tr.setDestinoColor(t.getEstadoDestino().getColor());
                tr.setRequiereMotivo(Boolean.TRUE.equals(t.getRequiereMotivo()));
                dto.getTransicionesDisponibles().add(tr);
            });

        if (!dto.getTransicionesDisponibles().isEmpty()) {
            TransicionEstadoConfig primera = transiciones.stream()
                .filter(t -> t.getEstadoDestino() != null)
                .min(Comparator.comparing(t -> t.getEstadoDestino().getOrden()))
                .orElse(null);
            if (primera != null) {
                dto.setProximoEstado(resumirEstado(primera.getEstadoDestino()));
            }
        } else if (indiceActual >= 0 && indiceActual + 1 < estados.size()) {
            dto.setProximoEstado(resumirEstado(estados.get(indiceActual + 1)));
        }

        if (actualConfig != null) {
            List<TareaPorEstadoConfig> tareasConfig = configuracionEstadosService
                .obtenerTareasPorEstado(actualConfig.getId(), empresaId);
            for (TareaPorEstadoConfig tarea : tareasConfig) {
                ProgresoEstadoLoteDTO.TareaProgresoDTO tp = new ProgresoEstadoLoteDTO.TareaProgresoDTO();
                tp.setTipoLabor(tarea.getTipoLabor());
                tp.setNombreTarea(tarea.getNombreTarea());
                tp.setEsObligatoria(Boolean.TRUE.equals(tarea.getEsObligatoria()));
                tp.setCompletada(completadas.stream().anyMatch(l ->
                    l.getTipoLabor() != null && l.getTipoLabor().name().equalsIgnoreCase(tarea.getTipoLabor())));
                dto.getTareas().add(tp);
            }
        }

        dto.setMensajeAvance(construirMensajeAvance(dto, actualConfig, diasSiembra));
        if (dto.getProximoEstado() != null && dto.getProximoEstado().getDiasMinimos() != null && diasSiembra != null) {
            long faltan = dto.getProximoEstado().getDiasMinimos() - diasSiembra;
            dto.setDiasParaProximoEstado(faltan > 0 ? faltan : 0L);
        }

        return dto;
    }

    private ProgresoEstadoLoteDTO.EstadoResumenDTO resumirEstado(EstadoLoteConfig e) {
        ProgresoEstadoLoteDTO.EstadoResumenDTO r = new ProgresoEstadoLoteDTO.EstadoResumenDTO();
        r.setId(e.getId());
        r.setNombre(e.getNombre());
        r.setColor(e.getColor());
        r.setIcono(e.getIcono());
        r.setDiasMinimos(e.getDiasMinimos());
        r.setModoAvance(e.getModoAvance() != null ? e.getModoAvance().name() : "MIXTO");
        return r;
    }

    private String construirMensajeAvance(ProgresoEstadoLoteDTO dto, EstadoLoteConfig actual, Long diasSiembra) {
        List<String> partes = new ArrayList<>();

        if (actual != null && MapeadorEstadoLoteConfig.esEstadoDerivadoPorEvento(actual)) {
            partes.add("Este estado se actualiza al registrar siembra o cosecha (no se cambia manualmente).");
        }

        List<ProgresoEstadoLoteDTO.TareaProgresoDTO> pendientesObligatorias = dto.getTareas().stream()
            .filter(t -> t.isEsObligatoria() && !t.isCompletada())
            .toList();
        List<ProgresoEstadoLoteDTO.TareaProgresoDTO> pendientesTodas = dto.getTareas().stream()
            .filter(t -> !t.isCompletada())
            .toList();

        if (!pendientesObligatorias.isEmpty()) {
            partes.add("Para avanzar, complete: " + pendientesObligatorias.stream()
                .map(ProgresoEstadoLoteDTO.TareaProgresoDTO::getNombreTarea)
                .reduce((a, b) -> a + ", " + b).orElse(""));
        } else if (!pendientesTodas.isEmpty() && dto.getTareas().stream().noneMatch(ProgresoEstadoLoteDTO.TareaProgresoDTO::isEsObligatoria)) {
            partes.add("Complete las tareas del estado: " + pendientesTodas.stream()
                .map(ProgresoEstadoLoteDTO.TareaProgresoDTO::getNombreTarea)
                .reduce((a, b) -> a + ", " + b).orElse(""));
        }

        if (dto.getDiasParaProximoEstado() != null && dto.getDiasParaProximoEstado() > 0) {
            partes.add("Faltan aproximadamente " + dto.getDiasParaProximoEstado() + " días desde siembra para el próximo estado.");
        } else if (diasSiembra != null && dto.getProximoEstado() != null
                && dto.getProximoEstado().getDiasMinimos() != null
                && diasSiembra >= dto.getProximoEstado().getDiasMinimos()) {
            partes.add("Ya cumplió los días requeridos; el estado avanzará al registrar una labor o recalcular.");
        }

        if (partes.isEmpty()) {
            if (dto.getProximoEstado() != null) {
                partes.add("Próximo estado: " + dto.getProximoEstado().getNombre()
                    + ". El avance es automático al registrar labores o cumplir el tiempo configurado.");
            } else {
                partes.add("El lote está en el último estado del ciclo configurado.");
            }
        }

        return String.join(" ", partes);
    }

    private Long resolverTipoCultivoId(Plot lote) {
        if (lote.getTipoCultivo() != null) {
            return lote.getTipoCultivo().getId();
        }
        if (lote.getCultivo() != null) {
            String tipo = lote.getCultivo().getTipo();
            String nombre = lote.getCultivo().getNombre();
            return configuracionEstadosService.obtenerTodosLosTiposCultivo().stream()
                .filter(tc -> tc.getNombre() != null && (
                    (tipo != null && tc.getNombre().equalsIgnoreCase(tipo)) ||
                    (nombre != null && tc.getNombre().equalsIgnoreCase(nombre))))
                .map(TipoCultivo::getId)
                .findFirst()
                .orElse(null);
        }
        return null;
    }
}
