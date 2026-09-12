package com.agrocloud.porcinos.service;

import com.agrocloud.core.security.ServicioSeguridadContexto;
import com.agrocloud.exception.ResourceConflictException;
import com.agrocloud.exception.ResourceNotFoundException;
import com.agrocloud.porcinos.model.dto.*;
import com.agrocloud.porcinos.model.entity.*;
import com.agrocloud.porcinos.model.enums.PorcinosGestacionEstado;
import com.agrocloud.porcinos.model.enums.PorcinosMadreEstado;
import com.agrocloud.porcinos.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServicioPorcinosReproduccion {

    private final ServicioSeguridadContexto servicioSeguridadContexto;
    private final ServicioPorcinosCatalogos servicioCatalogos;
    private final ServicioPorcinosLotes servicioLotes;
    private final PorcinosMadreRepository madreRepository;
    private final PorcinosPadrilloRepository padrilloRepository;
    private final PorcinosRazaRepository razaRepository;
    private final PorcinosGalponRepository galponRepository;
    private final PorcinosServicioRepository servicioRepository;
    private final PorcinosGestacionRepository gestacionRepository;
    private final PorcinosPartoRepository partoRepository;
    private final PorcinosDesteteRepository desteteRepository;
    private final PorcinosTipoServicioRepository tipoServicioRepository;

    public ServicioPorcinosReproduccion(
            ServicioSeguridadContexto servicioSeguridadContexto,
            ServicioPorcinosCatalogos servicioCatalogos,
            ServicioPorcinosLotes servicioLotes,
            PorcinosMadreRepository madreRepository,
            PorcinosPadrilloRepository padrilloRepository,
            PorcinosRazaRepository razaRepository,
            PorcinosGalponRepository galponRepository,
            PorcinosServicioRepository servicioRepository,
            PorcinosGestacionRepository gestacionRepository,
            PorcinosPartoRepository partoRepository,
            PorcinosDesteteRepository desteteRepository,
            PorcinosTipoServicioRepository tipoServicioRepository) {
        this.servicioSeguridadContexto = servicioSeguridadContexto;
        this.servicioCatalogos = servicioCatalogos;
        this.servicioLotes = servicioLotes;
        this.madreRepository = madreRepository;
        this.padrilloRepository = padrilloRepository;
        this.razaRepository = razaRepository;
        this.galponRepository = galponRepository;
        this.servicioRepository = servicioRepository;
        this.gestacionRepository = gestacionRepository;
        this.partoRepository = partoRepository;
        this.desteteRepository = desteteRepository;
        this.tipoServicioRepository = tipoServicioRepository;
    }

    @Transactional(readOnly = true)
    public List<PorcinosMadreRespuesta> listarMadres() {
        return madreRepository.listarPorEmpresaId(empresaActual()).stream()
                .map(this::aMadreRespuesta)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PorcinosMadreRespuesta obtenerMadre(Long id) {
        return aMadreRespuesta(requerirMadre(id));
    }

    @Transactional
    public PorcinosMadreRespuesta crearMadre(PorcinosMadreSolicitud solicitud) {
        if (solicitud.getCaravana() == null || solicitud.getCaravana().isBlank()) {
            throw new IllegalArgumentException("La caravana es obligatoria");
        }
        if (solicitud.getFechaIngreso() == null) {
            throw new IllegalArgumentException("La fecha de ingreso es obligatoria");
        }
        PorcinosMadre m = new PorcinosMadre();
        m.setEmpresaId(empresaActual());
        m.setCaravana(solicitud.getCaravana().trim());
        asignarRaza(m, solicitud.getRazaId());
        asignarGalpon(m, solicitud.getGalponId());
        m.setEstado(solicitud.getEstado() != null ? solicitud.getEstado() : PorcinosMadreEstado.CACHORRA);
        m.setFechaIngreso(solicitud.getFechaIngreso());
        m.setFechaNacimiento(solicitud.getFechaNacimiento());
        if (solicitud.getActivo() != null) {
            m.setActivo(solicitud.getActivo());
        }
        return aMadreRespuesta(madreRepository.save(m));
    }

    @Transactional
    public PorcinosMadreRespuesta actualizarMadre(Long id, PorcinosMadreSolicitud solicitud) {
        PorcinosMadre m = requerirMadre(id);
        if (solicitud.getCaravana() != null) {
            m.setCaravana(solicitud.getCaravana().trim());
        }
        if (solicitud.getRazaId() != null) {
            asignarRaza(m, solicitud.getRazaId());
        }
        if (solicitud.getGalponId() != null) {
            asignarGalpon(m, solicitud.getGalponId());
        }
        if (solicitud.getEstado() != null) {
            m.setEstado(solicitud.getEstado());
        }
        if (solicitud.getFechaNacimiento() != null) {
            m.setFechaNacimiento(solicitud.getFechaNacimiento());
        }
        if (solicitud.getActivo() != null) {
            m.setActivo(solicitud.getActivo());
        }
        return aMadreRespuesta(madreRepository.save(m));
    }

    @Transactional(readOnly = true)
    public List<PorcinosPadrilloRespuesta> listarPadrillos() {
        return padrilloRepository.listarPorEmpresaId(empresaActual()).stream()
                .map(this::aPadrilloRespuesta)
                .collect(Collectors.toList());
    }

    @Transactional
    public PorcinosPadrilloRespuesta crearPadrillo(PorcinosPadrilloSolicitud solicitud) {
        if (solicitud.getNombre() == null || solicitud.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        PorcinosPadrillo p = new PorcinosPadrillo();
        p.setEmpresaId(empresaActual());
        p.setNombre(solicitud.getNombre().trim());
        if (solicitud.getRazaId() != null) {
            p.setRaza(razaRepository.buscarPorIdYEmpresaId(solicitud.getRazaId(), empresaActual())
                    .orElseThrow(() -> new ResourceNotFoundException("Raza no encontrada")));
        }
        if (solicitud.getActivo() != null) {
            p.setActivo(solicitud.getActivo());
        }
        return aPadrilloRespuesta(padrilloRepository.save(p));
    }

    @Transactional
    public PorcinosServicioRespuesta registrarServicio(Long madreId, PorcinosServicioSolicitud solicitud) {
        if (solicitud.getFecha() == null) {
            throw new IllegalArgumentException("La fecha del servicio es obligatoria");
        }
        PorcinosMadre madre = requerirMadre(madreId);
        if (!Boolean.TRUE.equals(madre.getActivo())) {
            throw new IllegalStateException("La madre no está activa");
        }
        gestacionRepository.buscarActivaPorMadreId(madreId, PorcinosGestacionEstado.EN_CURSO)
                .ifPresent(g -> {
                    throw new ResourceConflictException("La madre ya tiene una gestación en curso");
                });

        PorcinosServicio servicio = new PorcinosServicio();
        servicio.setMadre(madre);
        if (solicitud.getPadrilloId() != null) {
            servicio.setPadrillo(padrilloRepository.buscarPorIdYEmpresaId(solicitud.getPadrilloId(), empresaActual())
                    .orElseThrow(() -> new ResourceNotFoundException("Padrillo no encontrado")));
        }
        if (solicitud.getTipoServicioId() != null) {
            servicio.setTipoServicio(tipoServicioRepository.buscarPorIdYEmpresaId(
                            solicitud.getTipoServicioId(), empresaActual())
                    .orElseThrow(() -> new ResourceNotFoundException("Tipo de servicio no encontrado")));
        }
        servicio.setFecha(solicitud.getFecha());
        servicio.setObservaciones(solicitud.getObservaciones());
        servicio = servicioRepository.save(servicio);

        int diasGestacion = servicioCatalogos.resolverDiasGestacion(madre);
        PorcinosGestacion gestacion = new PorcinosGestacion();
        gestacion.setMadre(madre);
        gestacion.setServicio(servicio);
        gestacion.setFechaInicio(solicitud.getFecha());
        gestacion.setFechaProbableParto(solicitud.getFecha().plusDays(diasGestacion));
        gestacion.setEstado(PorcinosGestacionEstado.EN_CURSO);
        gestacion = gestacionRepository.save(gestacion);

        madre.setEstado(PorcinosMadreEstado.GESTACION);
        madreRepository.save(madre);

        return aServicioRespuesta(servicio, gestacion);
    }

    @Transactional
    public PorcinosPartoRespuesta registrarParto(Long gestacionId, PorcinosPartoSolicitud solicitud) {
        if (solicitud.getFecha() == null) {
            throw new IllegalArgumentException("La fecha del parto es obligatoria");
        }
        PorcinosGestacion gestacion = gestacionRepository.buscarPorIdYEmpresaId(gestacionId, empresaActual())
                .orElseThrow(() -> new ResourceNotFoundException("Gestación no encontrada"));
        if (gestacion.getEstado() != PorcinosGestacionEstado.EN_CURSO) {
            throw new IllegalStateException("La gestación no está en curso");
        }
        if (partoRepository.buscarPorGestacionId(gestacionId).isPresent()) {
            throw new ResourceConflictException("Ya existe un parto registrado para esta gestación");
        }

        PorcinosParto parto = new PorcinosParto();
        parto.setGestacion(gestacion);
        parto.setMadre(gestacion.getMadre());
        parto.setFecha(solicitud.getFecha());
        parto.setNacidosVivos(solicitud.getNacidosVivos() != null ? solicitud.getNacidosVivos() : 0);
        parto.setNacidosMuertos(solicitud.getNacidosMuertos() != null ? solicitud.getNacidosMuertos() : 0);
        parto.setMomificados(solicitud.getMomificados() != null ? solicitud.getMomificados() : 0);
        parto.setTemperaturaAmbiente(solicitud.getTemperaturaAmbiente());
        parto.setHumedadAmbiente(solicitud.getHumedadAmbiente());
        parto.setObservaciones(solicitud.getObservaciones());
        parto = partoRepository.save(parto);

        gestacion.setEstado(PorcinosGestacionEstado.FINALIZADA);
        gestacion.setActivo(false);
        gestacionRepository.save(gestacion);

        PorcinosMadre madre = gestacion.getMadre();
        madre.setEstado(PorcinosMadreEstado.LACTANCIA);
        madreRepository.save(madre);

        return aPartoRespuesta(parto);
    }

    @Transactional
    public PorcinosDesteteRespuesta registrarDestete(Long partoId, PorcinosDesteteSolicitud solicitud) {
        if (solicitud.getFecha() == null) {
            throw new IllegalArgumentException("La fecha del destete es obligatoria");
        }
        if (solicitud.getCantidadDestetados() == null || solicitud.getCantidadDestetados() <= 0) {
            throw new IllegalArgumentException("La cantidad destetada debe ser mayor a cero");
        }
        if (solicitud.getGalponId() == null) {
            throw new IllegalArgumentException("El galpón destino es obligatorio");
        }
        if (solicitud.getPesoPromedioKg() == null || solicitud.getPesoPromedioKg().signum() <= 0) {
            throw new IllegalArgumentException("El peso promedio debe ser mayor a cero");
        }

        PorcinosParto parto = partoRepository.buscarPorIdYEmpresaId(partoId, empresaActual())
                .orElseThrow(() -> new ResourceNotFoundException("Parto no encontrado"));
        if (desteteRepository.buscarPorPartoId(partoId).isPresent()) {
            throw new ResourceConflictException("Ya existe un destete registrado para este parto");
        }

        Long empresaId = empresaActual();
        PorcinosGalpon galpon = galponRepository.buscarPorIdYEmpresaId(solicitud.getGalponId(), empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Galpón no encontrado"));

        String nombreLote = solicitud.getLoteNombre() != null && !solicitud.getLoteNombre().isBlank()
                ? solicitud.getLoteNombre().trim()
                : "Destete-" + parto.getMadre().getCaravana() + "-" + solicitud.getFecha();

        PorcinosLote lote = servicioLotes.crearLoteDesdeDestete(
                galpon,
                empresaId,
                nombreLote,
                solicitud.getFecha(),
                solicitud.getCantidadDestetados(),
                solicitud.getPesoPromedioKg());

        PorcinosDestete destete = new PorcinosDestete();
        destete.setParto(parto);
        destete.setFecha(solicitud.getFecha());
        destete.setCantidadDestetados(solicitud.getCantidadDestetados());
        destete.setPesoPromedioKg(solicitud.getPesoPromedioKg());
        destete.setLote(lote);
        destete = desteteRepository.save(destete);

        lote.setDestete(destete);
        servicioLotes.guardarLote(lote);

        PorcinosMadre madre = parto.getMadre();
        madre.setEstado(PorcinosMadreEstado.ADULTA);
        madreRepository.save(madre);

        return aDesteteRespuesta(destete);
    }

    @Transactional(readOnly = true)
    public PorcinosGestacionRespuesta obtenerGestacionActivaMadre(Long madreId) {
        requerirMadre(madreId);
        PorcinosGestacion gestacion = gestacionRepository
                .buscarActivaPorMadreId(madreId, PorcinosGestacionEstado.EN_CURSO)
                .orElseThrow(() -> new ResourceNotFoundException("No hay gestación activa para esta madre"));
        return aGestacionRespuesta(gestacion);
    }

    @Transactional(readOnly = true)
    public PorcinosPartoRespuesta obtenerPartoPendienteDestete(Long madreId) {
        requerirMadre(madreId);
        for (PorcinosParto parto : partoRepository.listarPorMadreId(madreId)) {
            if (desteteRepository.buscarPorPartoId(parto.getId()).isEmpty()) {
                return aPartoRespuesta(parto);
            }
        }
        throw new ResourceNotFoundException("No hay parto pendiente de destete para esta madre");
    }

    @Transactional(readOnly = true)
    public PorcinosReproduccionResumen resumenReproduccion() {
        Long empresaId = empresaActual();
        PorcinosReproduccionResumen res = new PorcinosReproduccionResumen();
        res.setMadresActivas(madreRepository.listarPorEmpresaId(empresaId).stream()
                .filter(m -> Boolean.TRUE.equals(m.getActivo())).count());
        res.setGestacionesEnCurso(gestacionRepository.listarPorEmpresaId(empresaId).stream()
                .filter(g -> g.getEstado() == PorcinosGestacionEstado.EN_CURSO && Boolean.TRUE.equals(g.getActivo()))
                .count());
        res.setMadresEnLactancia(madreRepository.listarPorEmpresaIdYEstado(empresaId, PorcinosMadreEstado.LACTANCIA).stream()
                .count());
        res.setPartosPeriodo((long) partoRepository.listarPorEmpresaId(empresaId).size());
        res.setDestetesPeriodo(desteteRepository.count());
        return res;
    }

    private Long empresaActual() {
        return servicioSeguridadContexto.obtenerEmpresaIdActual();
    }

    private PorcinosMadre requerirMadre(Long id) {
        return madreRepository.buscarPorIdYEmpresaId(id, empresaActual())
                .orElseThrow(() -> new ResourceNotFoundException("Madre no encontrada"));
    }

    private void asignarRaza(PorcinosMadre m, Long razaId) {
        if (razaId == null) {
            m.setRaza(null);
            return;
        }
        m.setRaza(razaRepository.buscarPorIdYEmpresaId(razaId, empresaActual())
                .orElseThrow(() -> new ResourceNotFoundException("Raza no encontrada")));
    }

    private void asignarGalpon(PorcinosMadre m, Long galponId) {
        if (galponId == null) {
            m.setGalpon(null);
            return;
        }
        m.setGalpon(galponRepository.buscarPorIdYEmpresaId(galponId, empresaActual())
                .orElseThrow(() -> new ResourceNotFoundException("Galpón no encontrado")));
    }

    private PorcinosMadreRespuesta aMadreRespuesta(PorcinosMadre m) {
        PorcinosMadreRespuesta dto = new PorcinosMadreRespuesta();
        dto.setId(m.getId());
        dto.setEmpresaId(m.getEmpresaId());
        dto.setCaravana(m.getCaravana());
        if (m.getRaza() != null) {
            dto.setRazaId(m.getRaza().getId());
            dto.setRazaNombre(m.getRaza().getNombre());
        }
        if (m.getGalpon() != null) {
            dto.setGalponId(m.getGalpon().getId());
            dto.setGalponNombre(m.getGalpon().getNombre());
        }
        dto.setEstado(m.getEstado());
        dto.setFechaIngreso(m.getFechaIngreso());
        dto.setFechaNacimiento(m.getFechaNacimiento());
        dto.setActivo(m.getActivo());
        dto.setCreatedAt(m.getCreatedAt());
        dto.setUpdatedAt(m.getUpdatedAt());
        return dto;
    }

    private PorcinosPadrilloRespuesta aPadrilloRespuesta(PorcinosPadrillo p) {
        PorcinosPadrilloRespuesta dto = new PorcinosPadrilloRespuesta();
        dto.setId(p.getId());
        dto.setEmpresaId(p.getEmpresaId());
        dto.setNombre(p.getNombre());
        if (p.getRaza() != null) {
            dto.setRazaId(p.getRaza().getId());
            dto.setRazaNombre(p.getRaza().getNombre());
        }
        dto.setActivo(p.getActivo());
        dto.setCreatedAt(p.getCreatedAt());
        dto.setUpdatedAt(p.getUpdatedAt());
        return dto;
    }

    private PorcinosServicioRespuesta aServicioRespuesta(PorcinosServicio s, PorcinosGestacion g) {
        PorcinosServicioRespuesta dto = new PorcinosServicioRespuesta();
        dto.setId(s.getId());
        dto.setMadreId(s.getMadre().getId());
        dto.setMadreCaravana(s.getMadre().getCaravana());
        if (s.getPadrillo() != null) {
            dto.setPadrilloId(s.getPadrillo().getId());
            dto.setPadrilloNombre(s.getPadrillo().getNombre());
        }
        if (s.getTipoServicio() != null) {
            dto.setTipoServicioId(s.getTipoServicio().getId());
            dto.setTipoServicioNombre(s.getTipoServicio().getNombre());
        }
        dto.setFecha(s.getFecha());
        dto.setObservaciones(s.getObservaciones());
        if (g != null) {
            dto.setGestacionId(g.getId());
            dto.setFechaProbableParto(g.getFechaProbableParto());
        }
        dto.setCreatedAt(s.getCreatedAt());
        return dto;
    }

    private PorcinosPartoRespuesta aPartoRespuesta(PorcinosParto p) {
        PorcinosPartoRespuesta dto = new PorcinosPartoRespuesta();
        dto.setId(p.getId());
        dto.setGestacionId(p.getGestacion().getId());
        dto.setMadreId(p.getMadre().getId());
        dto.setMadreCaravana(p.getMadre().getCaravana());
        dto.setFecha(p.getFecha());
        dto.setNacidosVivos(p.getNacidosVivos());
        dto.setNacidosMuertos(p.getNacidosMuertos());
        dto.setMomificados(p.getMomificados());
        dto.setTemperaturaAmbiente(p.getTemperaturaAmbiente());
        dto.setHumedadAmbiente(p.getHumedadAmbiente());
        dto.setObservaciones(p.getObservaciones());
        dto.setCreatedAt(p.getCreatedAt());
        return dto;
    }

    private PorcinosDesteteRespuesta aDesteteRespuesta(PorcinosDestete d) {
        PorcinosDesteteRespuesta dto = new PorcinosDesteteRespuesta();
        dto.setId(d.getId());
        dto.setPartoId(d.getParto().getId());
        dto.setFecha(d.getFecha());
        dto.setCantidadDestetados(d.getCantidadDestetados());
        dto.setPesoPromedioKg(d.getPesoPromedioKg());
        if (d.getLote() != null) {
            dto.setLoteId(d.getLote().getId());
            dto.setLoteNombre(d.getLote().getNombre());
        }
        dto.setCreatedAt(d.getCreatedAt());
        return dto;
    }

    private PorcinosGestacionRespuesta aGestacionRespuesta(PorcinosGestacion g) {
        PorcinosGestacionRespuesta dto = new PorcinosGestacionRespuesta();
        dto.setId(g.getId());
        dto.setMadreId(g.getMadre().getId());
        dto.setMadreCaravana(g.getMadre().getCaravana());
        if (g.getServicio() != null) {
            dto.setServicioId(g.getServicio().getId());
        }
        dto.setFechaInicio(g.getFechaInicio());
        dto.setFechaProbableParto(g.getFechaProbableParto());
        dto.setEstado(g.getEstado());
        dto.setCreatedAt(g.getCreatedAt());
        return dto;
    }
}
