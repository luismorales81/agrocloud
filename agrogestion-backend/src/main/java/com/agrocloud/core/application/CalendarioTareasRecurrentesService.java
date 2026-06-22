package com.agrocloud.core.application;

import com.agrocloud.core.domain.AmbitoCalendarioSerie;
import com.agrocloud.core.domain.CumplimientoSerieTareaRecurrente;
import com.agrocloud.core.domain.SerieTareaRecurrenteCalendario;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.infrastructure.CumplimientoSerieTareaRecurrenteRepository;
import com.agrocloud.core.infrastructure.SerieTareaRecurrenteCalendarioRepository;
import com.agrocloud.dto.calendario.ActualizarSerieTareaRecurrenteSolicitud;
import com.agrocloud.dto.calendario.CrearSerieTareaRecurrenteSolicitud;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CalendarioTareasRecurrentesService {

    private final SerieTareaRecurrenteCalendarioRepository serieRepository;
    private final CumplimientoSerieTareaRecurrenteRepository cumplimientoRepository;

    public CalendarioTareasRecurrentesService(
            SerieTareaRecurrenteCalendarioRepository serieRepository,
            CumplimientoSerieTareaRecurrenteRepository cumplimientoRepository) {
        this.serieRepository = serieRepository;
        this.cumplimientoRepository = cumplimientoRepository;
    }

    public List<SerieTareaRecurrenteCalendario> listarSeriesUsuario(Long usuarioId) {
        return serieRepository.findByUsuarioIdAndActivoTrueOrderByFechaInicioAsc(usuarioId);
    }

    @Transactional
    public SerieTareaRecurrenteCalendario crear(User usuario, CrearSerieTareaRecurrenteSolicitud solicitud) {
        validarSolicitudCreacion(solicitud);
        SerieTareaRecurrenteCalendario s = new SerieTareaRecurrenteCalendario();
        s.setUsuario(usuario);
        s.setTitulo(solicitud.getTitulo().trim());
        s.setDescripcion(solicitud.getDescripcion() != null ? solicitud.getDescripcion().trim() : null);
        s.setFechaInicio(solicitud.getFechaInicio());
        s.setFechaFin(solicitud.getFechaFin());
        s.setTipoRepeticion(solicitud.getTipoRepeticion());
        s.setAmbitoCalendario(solicitud.getAmbitoCalendario() != null
                ? solicitud.getAmbitoCalendario()
                : AmbitoCalendarioSerie.GENERAL);
        s.setActivo(true);
        return serieRepository.save(s);
    }

    @Transactional
    public SerieTareaRecurrenteCalendario actualizar(Long id, Long usuarioId, ActualizarSerieTareaRecurrenteSolicitud solicitud) {
        SerieTareaRecurrenteCalendario s = serieRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Serie no encontrada"));
        if (!s.getUsuario().getId().equals(usuarioId) || Boolean.FALSE.equals(s.getActivo())) {
            throw new IllegalArgumentException("Serie no encontrada");
        }
        if (solicitud.getTitulo() != null && !solicitud.getTitulo().isBlank()) {
            s.setTitulo(solicitud.getTitulo().trim());
        }
        if (solicitud.getDescripcion() != null) {
            s.setDescripcion(solicitud.getDescripcion().trim());
        }
        if (solicitud.getFechaInicio() != null) {
            s.setFechaInicio(solicitud.getFechaInicio());
        }
        if (solicitud.getFechaFin() != null) {
            s.setFechaFin(solicitud.getFechaFin());
        }
        if (solicitud.getTipoRepeticion() != null) {
            s.setTipoRepeticion(solicitud.getTipoRepeticion());
        }
        if (solicitud.getAmbitoCalendario() != null) {
            s.setAmbitoCalendario(solicitud.getAmbitoCalendario());
        }
        validarFechasSerie(s.getFechaInicio(), s.getFechaFin());
        return serieRepository.save(s);
    }

    /**
     * Elimina la serie y los cumplimientos asociados (FK en BD con ON DELETE CASCADE).
     */
    @Transactional
    public void eliminarSerie(Long id, Long usuarioId) {
        SerieTareaRecurrenteCalendario s = serieRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Serie no encontrada"));
        if (!s.getUsuario().getId().equals(usuarioId)) {
            throw new IllegalArgumentException("Serie no encontrada");
        }
        serieRepository.delete(s);
    }

    @Transactional
    public void marcarCumplimiento(Long serieId, Long usuarioId, LocalDate fechaOcurrencia, boolean cumplida) {
        SerieTareaRecurrenteCalendario s = serieRepository.findById(serieId)
                .orElseThrow(() -> new IllegalArgumentException("Serie no encontrada"));
        if (!s.getUsuario().getId().equals(usuarioId) || Boolean.FALSE.equals(s.getActivo())) {
            throw new IllegalArgumentException("Serie no encontrada");
        }
        if (fechaOcurrencia == null) {
            throw new IllegalArgumentException("La fecha de ocurrencia es obligatoria");
        }
        Optional<CumplimientoSerieTareaRecurrente> existente =
                cumplimientoRepository.findBySerieIdAndFechaOcurrencia(serieId, fechaOcurrencia);
        if (cumplida) {
            CumplimientoSerieTareaRecurrente c = existente.orElseGet(CumplimientoSerieTareaRecurrente::new);
            c.setSerie(s);
            c.setFechaOcurrencia(fechaOcurrencia);
            c.setCumplida(true);
            c.setFechaMarcado(LocalDateTime.now());
            cumplimientoRepository.save(c);
        } else {
            existente.ifPresent(cumplimientoRepository::delete);
        }
    }

    /**
     * Eventos listos para fusionar en {@code todos} del calendario (tipo TAREA_RECURRENTE).
     */
    public List<Map<String, Object>> construirEventosEnRango(Long usuarioId, LocalDate desde, LocalDate hasta) {
        return construirEventosEnRangoPorAmbito(usuarioId, desde, hasta, AmbitoCalendarioSerie.GENERAL);
    }

    /**
     * Tareas recurrentes del calendario del módulo avícola huevos.
     */
    public List<Map<String, Object>> construirEventosEnRangoCalendarioHuevos(Long usuarioId, LocalDate desde, LocalDate hasta) {
        return construirEventosEnRangoPorAmbito(usuarioId, desde, hasta, AmbitoCalendarioSerie.AVICOLA_HUEVOS);
    }

    /**
     * Tareas recurrentes del calendario del módulo feedlot.
     */
    public List<Map<String, Object>> construirEventosEnRangoCalendarioFeedlot(Long usuarioId, LocalDate desde, LocalDate hasta) {
        return construirEventosEnRangoPorAmbito(usuarioId, desde, hasta, AmbitoCalendarioSerie.FEEDLOT);
    }

    private List<Map<String, Object>> construirEventosEnRangoPorAmbito(
            Long usuarioId, LocalDate desde, LocalDate hasta, AmbitoCalendarioSerie ambito) {
        List<SerieTareaRecurrenteCalendario> series =
                serieRepository.findActivasSolapandoRangoPorAmbito(usuarioId, desde, hasta, ambito);
        if (series.isEmpty()) {
            return List.of();
        }
        List<Long> ids = series.stream().map(SerieTareaRecurrenteCalendario::getId).collect(Collectors.toList());
        List<CumplimientoSerieTareaRecurrente> cumplimientos =
                cumplimientoRepository.findBySerieIdInAndFechaOcurrenciaBetween(ids, desde, hasta);
        Map<String, Boolean> cumplidaPorClave = new HashMap<>();
        for (CumplimientoSerieTareaRecurrente c : cumplimientos) {
            if (Boolean.TRUE.equals(c.getCumplida())) {
                cumplidaPorClave.put(c.getSerie().getId() + "_" + c.getFechaOcurrencia(), true);
            }
        }
        List<Map<String, Object>> eventos = new ArrayList<>();
        for (SerieTareaRecurrenteCalendario serie : series) {
            List<LocalDate> ocurrencias = ExpansionOcurrenciasSerieTareaRecurrente.expandir(serie, desde, hasta);
            for (LocalDate fecha : ocurrencias) {
                String clave = serie.getId() + "_" + fecha;
                boolean cumplida = cumplidaPorClave.containsKey(clave);
                Map<String, Object> evento = new HashMap<>();
                evento.put("id", "tarea_recurrente_" + serie.getId() + "_" + fecha);
                evento.put("tipo", "TAREA_RECURRENTE");
                evento.put("titulo", serie.getTitulo());
                evento.put("descripcion", serie.getDescripcion());
                evento.put("fecha", fecha.toString());
                evento.put("serieId", serie.getId());
                evento.put("tipoRepeticion", serie.getTipoRepeticion().name());
                evento.put("cumplida", cumplida);
                evento.put("completado", cumplida);
                eventos.add(evento);
            }
        }
        return eventos;
    }

    private void validarSolicitudCreacion(CrearSerieTareaRecurrenteSolicitud solicitud) {
        if (solicitud.getTitulo() == null || solicitud.getTitulo().isBlank()) {
            throw new IllegalArgumentException("El título es obligatorio");
        }
        if (solicitud.getFechaInicio() == null) {
            throw new IllegalArgumentException("La fecha de inicio es obligatoria");
        }
        if (solicitud.getTipoRepeticion() == null) {
            throw new IllegalArgumentException("El tipo de repetición es obligatorio");
        }
        validarFechasSerie(solicitud.getFechaInicio(), solicitud.getFechaFin());
    }

    private void validarFechasSerie(LocalDate inicio, LocalDate fin) {
        if (fin != null && fin.isBefore(inicio)) {
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }
    }
}
