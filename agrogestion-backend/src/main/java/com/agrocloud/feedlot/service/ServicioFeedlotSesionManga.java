package com.agrocloud.feedlot.service;

import com.agrocloud.core.domain.User;
import com.agrocloud.core.security.ServicioSeguridadContexto;
import com.agrocloud.exception.ResourceConflictException;
import com.agrocloud.exception.ResourceNotFoundException;
import com.agrocloud.feedlot.model.dto.*;
import com.agrocloud.feedlot.model.entity.FeedlotAnimal;
import com.agrocloud.feedlot.model.entity.FeedlotLote;
import com.agrocloud.feedlot.model.entity.FeedlotPesadaAnimal;
import com.agrocloud.feedlot.model.entity.FeedlotSesionManga;
import com.agrocloud.feedlot.model.entity.FeedlotSesionMangaFila;
import com.agrocloud.feedlot.model.enums.*;
import com.agrocloud.feedlot.repository.FeedlotAnimalRepository;
import com.agrocloud.feedlot.repository.FeedlotPesadaAnimalRepository;
import com.agrocloud.feedlot.repository.FeedlotSesionMangaFilaRepository;
import com.agrocloud.feedlot.repository.FeedlotSesionMangaRepository;
import com.agrocloud.feedlot.service.manga.ContextoConfirmacionManga;
import com.agrocloud.feedlot.service.manga.EstrategiaConfirmacionManga;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ServicioFeedlotSesionManga {

    private final ServicioSeguridadContexto servicioSeguridadContexto;
    private final ServicioFeedlotLotes servicioLotes;
    private final ServicioFeedlotImportacionManga importacionManga;
    private final ServicioFeedlotAnimales servicioAnimales;
    private final ServicioFeedlotPerfilLecturaManga servicioPerfilLectura;
    private final FeedlotSesionMangaRepository sesionRepository;
    private final FeedlotSesionMangaFilaRepository filaRepository;
    private final FeedlotAnimalRepository animalRepository;
    private final FeedlotPesadaAnimalRepository pesadaAnimalRepository;
    private final Map<FeedlotTipoSesionManga, EstrategiaConfirmacionManga> estrategias;

    public static final BigDecimal PESO_MINIMO_KG = new BigDecimal("50");
    public static final BigDecimal PESO_MAXIMO_KG = new BigDecimal("900");

    public ServicioFeedlotSesionManga(
            ServicioSeguridadContexto servicioSeguridadContexto,
            ServicioFeedlotLotes servicioLotes,
            ServicioFeedlotImportacionManga importacionManga,
            ServicioFeedlotAnimales servicioAnimales,
            ServicioFeedlotPerfilLecturaManga servicioPerfilLectura,
            FeedlotSesionMangaRepository sesionRepository,
            FeedlotSesionMangaFilaRepository filaRepository,
            FeedlotAnimalRepository animalRepository,
            FeedlotPesadaAnimalRepository pesadaAnimalRepository,
            List<EstrategiaConfirmacionManga> estrategiasLista) {
        this.servicioSeguridadContexto = servicioSeguridadContexto;
        this.servicioLotes = servicioLotes;
        this.importacionManga = importacionManga;
        this.servicioAnimales = servicioAnimales;
        this.servicioPerfilLectura = servicioPerfilLectura;
        this.sesionRepository = sesionRepository;
        this.filaRepository = filaRepository;
        this.animalRepository = animalRepository;
        this.pesadaAnimalRepository = pesadaAnimalRepository;
        this.estrategias = new EnumMap<>(FeedlotTipoSesionManga.class);
        for (EstrategiaConfirmacionManga e : estrategiasLista) {
            this.estrategias.put(e.tipo(), e);
        }
    }

    @Transactional
    public FeedlotSesionMangaRespuesta crearBorrador(FeedlotSesionMangaSolicitud solicitud, User usuario) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        if (solicitud.getLoteId() == null || solicitud.getTipo() == null || solicitud.getFecha() == null) {
            throw new IllegalArgumentException("Lote, tipo y fecha son obligatorios");
        }
        FeedlotLote lote = loteActivo(empresaId, solicitud.getLoteId());
        FeedlotSesionManga s = new FeedlotSesionManga();
        s.setEmpresaId(empresaId);
        s.setLote(lote);
        s.setTipo(solicitud.getTipo());
        s.setFecha(solicitud.getFecha());
        s.setEstado(FeedlotEstadoSesionManga.BORRADOR);
        s.setUsuarioId(usuario != null ? usuario.getId() : null);
        aplicarCabecera(s, solicitud);
        aplicarDefaultsSanidad(s);
        return aRespuesta(sesionRepository.save(s), false);
    }

    @Transactional
    public FeedlotSesionMangaRespuesta actualizarCabecera(Long id, FeedlotSesionMangaSolicitud solicitud) {
        FeedlotSesionManga s = obtenerBorrador(id);
        aplicarCabecera(s, solicitud);
        aplicarDefaultsSanidad(s);
        return aRespuesta(sesionRepository.save(s), true);
    }

    @Transactional
    public FeedlotSesionMangaRespuesta cargarArchivo(Long id, MultipartFile archivo, FeedlotMapeoColumnas mapeo) {
        FeedlotSesionManga s = obtenerBorrador(id);
        FeedlotMapeoColumnas efectivo = aplicarPerfilSiCorresponde(mapeo, s.getPerfilLecturaId(), s.getTipo());
        importacionManga.cargarArchivo(s, archivo, efectivo);
        sesionRepository.save(s);
        recalcularFilas(s);
        return aRespuesta(sesionRepository.save(s), true);
    }

    @Transactional(readOnly = true)
    public FeedlotResumenArchivoManga analizarArchivo(MultipartFile archivo, FeedlotMapeoColumnas mapeo) {
        return analizarArchivo(archivo, mapeo, null, null, false);
    }

    @Transactional(readOnly = true)
    public FeedlotResumenArchivoManga analizarArchivo(
            MultipartFile archivo,
            FeedlotMapeoColumnas mapeo,
            Long perfilLecturaId,
            FeedlotTipoSesionManga tipo,
            boolean cargaManualPeso) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        FeedlotMapeoColumnas efectivo = aplicarPerfilSiCorresponde(mapeo, perfilLecturaId, tipo);
        return importacionManga.analizar(archivo, efectivo, empresaId, tipo, cargaManualPeso, perfilLecturaId);
    }

    private FeedlotMapeoColumnas aplicarPerfilSiCorresponde(
            FeedlotMapeoColumnas mapeo,
            Long perfilLecturaId,
            FeedlotTipoSesionManga tipo) {
        FeedlotMapeoColumnas efectivo = mapeo == null ? new FeedlotMapeoColumnas() : mapeo;
        if (efectivo.getDelimitador() == null) {
            efectivo.setDelimitador("AUTO");
        }
        if (perfilLecturaId == null) {
            return efectivo;
        }
        var perfil = servicioPerfilLectura.entidad(perfilLecturaId);
        if (tipo != null && perfil.getTipo() != tipo) {
            throw new IllegalArgumentException("Ese perfil es de otro tipo de evento");
        }
        importacionManga.aplicarAlias(efectivo, servicioPerfilLectura.aliasDe(perfil));
        return efectivo;
    }

    @Transactional(readOnly = true)
    public FeedlotSesionMangaRespuesta obtener(Long id) {
        return aRespuesta(obtenerSesion(id), true);
    }

    @Transactional(readOnly = true)
    public List<FeedlotSesionMangaRespuesta> listarPorLote(Long loteId) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        servicioLotes.obtenerEntidadLote(empresaId, loteId);
        return sesionRepository.listarPorLoteIdYEmpresaId(loteId, empresaId).stream()
                .map(s -> aRespuesta(s, false))
                .collect(Collectors.toList());
    }

    @Transactional
    public FeedlotSesionMangaRespuesta confirmar(Long id, FeedlotConfirmarSesionSolicitud solicitud, User usuario) {
        FeedlotSesionManga s = obtenerBorrador(id);
        if (solicitud != null) {
            if (solicitud.getAjustarPlantel() != null) {
                s.setAjustarPlantel(solicitud.getAjustarPlantel());
            }
            if (solicitud.getCantidadTotal() != null) {
                s.setCantidadTotal(solicitud.getCantidadTotal());
            }
        }
        recalcularFilas(s);
        validarCabeceraConfirmacion(s);
        List<FeedlotSesionMangaFila> filas = filaRepository.listarPorSesionId(s.getId());
        List<FeedlotSesionMangaFila> ok = filas.stream()
                .filter(f -> f.getResultado() == FeedlotResultadoFilaManga.OK)
                .toList();
        if (s.getTipo() != FeedlotTipoSesionManga.RECUENTO && ok.isEmpty()) {
            throw new IllegalArgumentException("No hay filas válidas para confirmar");
        }
        EstrategiaConfirmacionManga est = estrategias.get(s.getTipo());
        if (est == null) {
            throw new IllegalStateException("Tipo de sesión no soportado");
        }
        List<FeedlotSesionMangaFila> paraEstrategia =
                s.getTipo() == FeedlotTipoSesionManga.RECUENTO ? filas : ok;
        est.confirmar(s, paraEstrategia, new ContextoConfirmacionManga(usuario, solicitud));
        s.setEstado(FeedlotEstadoSesionManga.CONFIRMADA);
        if (usuario != null) {
            s.setUsuarioId(usuario.getId());
        }
        return aRespuesta(sesionRepository.save(s), true);
    }

    @Transactional
    public FeedlotSesionMangaRespuesta anular(Long id) {
        FeedlotSesionManga s = obtenerSesion(id);
        if (s.getEstado() != FeedlotEstadoSesionManga.CONFIRMADA) {
            throw new ResourceConflictException("Solo se puede anular una sesión confirmada");
        }
        EstrategiaConfirmacionManga est = estrategias.get(s.getTipo());
        est.revertir(s);
        s.setEstado(FeedlotEstadoSesionManga.ANULADA);
        return aRespuesta(sesionRepository.save(s), true);
    }

    void recalcularFilas(FeedlotSesionManga sesion) {
        List<FeedlotSesionMangaFila> filas = filaRepository.listarPorSesionId(sesion.getId());
        int ok = 0;
        int error = 0;
        StringBuilder detalle = new StringBuilder();
        for (FeedlotSesionMangaFila fila : filas) {
            evaluarFila(sesion, fila);
            filaRepository.save(fila);
            if (fila.getResultado() == FeedlotResultadoFilaManga.ERROR) {
                error++;
                detalle.append("Línea ").append(fila.getNumeroLinea()).append(": ").append(fila.getMensaje()).append('\n');
            } else if (fila.getResultado() == FeedlotResultadoFilaManga.OK) {
                ok++;
            }
        }
        sesion.setFilasOk(ok);
        sesion.setFilasError(error);
        sesion.setDetalleErrores(detalle.isEmpty() ? null : detalle.toString());
    }

    private void evaluarFila(FeedlotSesionManga sesion, FeedlotSesionMangaFila fila) {
        fila.setAnimal(null);
        fila.setMensaje(null);
        String eid = fila.getEidNormalizado();
        String visual = fila.getCaravanaVisual() == null ? null : fila.getCaravanaVisual().trim();
        if ((eid == null || eid.isBlank()) && (visual == null || visual.isBlank())) {
            if (sesion.getTipo() == FeedlotTipoSesionManga.ENROLAMIENTO) {
                marcar(fila, FeedlotResultadoFilaManga.ERROR, "Falta EID o caravana visual");
                return;
            }
        }

        Optional<FeedlotAnimal> porEid = (eid == null || eid.isBlank())
                ? Optional.empty()
                : animalRepository.buscarPorEmpresaYEid(sesion.getEmpresaId(), eid);
        Optional<FeedlotAnimal> porVisual = (visual == null || visual.isBlank())
                ? Optional.empty()
                : animalRepository.buscarPorEmpresaYCaravanaVisual(sesion.getEmpresaId(), visual);

        if (porEid.isPresent() && porVisual.isPresent() && !porEid.get().getId().equals(porVisual.get().getId())) {
            marcar(fila, FeedlotResultadoFilaManga.ERROR, "Conflicto entre EID y caravana visual");
            return;
        }
        if (porEid.isPresent() && visual != null && !visual.isBlank()
                && porEid.get().getCaravanaVisual() != null
                && !porEid.get().getCaravanaVisual().equalsIgnoreCase(visual)) {
            marcar(fila, FeedlotResultadoFilaManga.ERROR, "El EID pertenece a otra caravana visual");
            return;
        }

        Optional<FeedlotAnimal> match = porEid.isPresent() ? porEid : porVisual;

        if (sesion.getTipo() == FeedlotTipoSesionManga.ENROLAMIENTO) {
            evaluarEnrolamiento(sesion, fila, match);
            return;
        }
        if (sesion.getTipo() == FeedlotTipoSesionManga.RECUENTO) {
            evaluarRecuento(sesion, fila, match);
            return;
        }
        if (match.isEmpty()) {
            marcar(fila, FeedlotResultadoFilaManga.ERROR, "Animal no enrolado");
            return;
        }
        FeedlotAnimal animal = match.get();
        if (animal.getEstado() != FeedlotEstadoAnimal.ACTIVO
                || animal.getLote() == null
                || !animal.getLote().getId().equals(sesion.getLote().getId())) {
            marcar(fila, FeedlotResultadoFilaManga.ERROR, "El animal no está activo en este lote");
            return;
        }
        fila.setAnimal(animal);
        if (sesion.getTipo() == FeedlotTipoSesionManga.PESADA) {
            if (fila.getPesoKg() == null || fila.getPesoKg().signum() <= 0) {
                marcar(fila, FeedlotResultadoFilaManga.ERROR, "Peso inválido");
                return;
            }
            if (fila.getPesoKg().compareTo(PESO_MINIMO_KG) < 0 || fila.getPesoKg().compareTo(PESO_MAXIMO_KG) > 0) {
                marcar(fila, FeedlotResultadoFilaManga.ERROR, "Peso fuera de rango (50 a 900 kg)");
                return;
            }
            List<FeedlotPesadaAnimal> anteriores = pesadaAnimalRepository.listarPorAnimalId(animal.getId());
            if (!anteriores.isEmpty() && fila.getPesoKg().compareTo(anteriores.get(0).getPesoKg()) < 0) {
                marcar(fila, FeedlotResultadoFilaManga.OK, "Peso menor que la pesada anterior");
                return;
            }
        }
        if (sesion.getTipo() == FeedlotTipoSesionManga.BAJA
                && sesion.getSubtipoBaja() == FeedlotSubtipoBaja.FAENA
                && servicioAnimales.retiroVigente(animal.getId(), sesion.getFecha())) {
            marcar(fila, FeedlotResultadoFilaManga.ERROR, "Animal en retiro sanitario; no se puede faenar");
            return;
        }
        marcar(fila, FeedlotResultadoFilaManga.OK, null);
    }

    private void evaluarEnrolamiento(FeedlotSesionManga sesion, FeedlotSesionMangaFila fila, Optional<FeedlotAnimal> match) {
        if (match.isEmpty()) {
            marcar(fila, FeedlotResultadoFilaManga.OK, "Se dará de alta");
            return;
        }
        FeedlotAnimal animal = match.get();
        if (animal.getEstado() != FeedlotEstadoAnimal.ACTIVO) {
            marcar(fila, FeedlotResultadoFilaManga.ERROR, "El animal existe pero no está activo");
            return;
        }
        if (animal.getLote() != null && animal.getLote().getId().equals(sesion.getLote().getId())) {
            fila.setAnimal(animal);
            marcar(fila, FeedlotResultadoFilaManga.OK, "Ya enrolado en este lote");
            return;
        }
        marcar(fila, FeedlotResultadoFilaManga.ERROR, "El animal está activo en otro lote");
    }

    private void evaluarRecuento(FeedlotSesionManga sesion, FeedlotSesionMangaFila fila, Optional<FeedlotAnimal> match) {
        if (match.isEmpty()) {
            fila.setOrigenRecuento(FeedlotOrigenFilaRecuento.DESCONOCIDO);
            marcar(fila, FeedlotResultadoFilaManga.ADVERTENCIA, "Desconocido: no está en el padrón");
            return;
        }
        FeedlotAnimal animal = match.get();
        fila.setAnimal(animal);
        if (animal.getEstado() == FeedlotEstadoAnimal.ACTIVO
                && animal.getLote() != null
                && animal.getLote().getId().equals(sesion.getLote().getId())) {
            fila.setOrigenRecuento(FeedlotOrigenFilaRecuento.PRESENTE);
            marcar(fila, FeedlotResultadoFilaManga.OK, "Presente");
            return;
        }
        if (animal.getEstado() == FeedlotEstadoAnimal.ACTIVO && animal.getLote() != null) {
            fila.setOrigenRecuento(FeedlotOrigenFilaRecuento.OTRO_LOTE);
            String loteNom = animal.getLote().getNombre();
            String corralNom = animal.getLote().getCorral() != null ? animal.getLote().getCorral().getNombre() : "—";
            marcar(fila, FeedlotResultadoFilaManga.ADVERTENCIA,
                    "Está en lote " + loteNom + " / corral " + corralNom);
            return;
        }
        fila.setOrigenRecuento(FeedlotOrigenFilaRecuento.INACTIVO);
        marcar(fila, FeedlotResultadoFilaManga.ADVERTENCIA,
                "Inactivo (" + animal.getEstado() + ")");
    }

    private static void marcar(FeedlotSesionMangaFila fila, FeedlotResultadoFilaManga resultado, String mensaje) {
        fila.setResultado(resultado);
        fila.setMensaje(mensaje);
    }

    private void aplicarCabecera(FeedlotSesionManga s, FeedlotSesionMangaSolicitud solicitud) {
        if (solicitud.getPerfilLecturaId() != null) {
            s.setPerfilLecturaId(solicitud.getPerfilLecturaId());
        }
        if (solicitud.getAjustarPlantel() != null) {
            s.setAjustarPlantel(solicitud.getAjustarPlantel());
        }
        if (solicitud.getTipoEvento() != null) {
            s.setTipoEvento(solicitud.getTipoEvento());
        }
        if (solicitud.getInsumoId() != null) {
            s.setInsumoId(solicitud.getInsumoId());
        }
        if (solicitud.getDosisPorAnimal() != null) {
            s.setDosisPorAnimal(solicitud.getDosisPorAnimal());
        }
        if (solicitud.getUnidadDosis() != null) {
            s.setUnidadDosis(solicitud.getUnidadDosis());
        }
        if (solicitud.getCantidadTotal() != null) {
            s.setCantidadTotal(solicitud.getCantidadTotal());
        }
        if (solicitud.getDiasRetiro() != null) {
            s.setDiasRetiro(solicitud.getDiasRetiro());
        }
        if (solicitud.getAplicador() != null) {
            s.setAplicador(solicitud.getAplicador());
        }
        if (solicitud.getAplicadorNombre() != null) {
            s.setAplicadorNombre(solicitud.getAplicadorNombre());
        }
        if (solicitud.getOrigenInsumo() != null) {
            s.setOrigenInsumo(solicitud.getOrigenInsumo());
        }
        if (solicitud.getDescripcion() != null) {
            s.setDescripcion(solicitud.getDescripcion());
        }
        if (solicitud.getSubtipoBaja() != null) {
            s.setSubtipoBaja(solicitud.getSubtipoBaja());
        }
        if (solicitud.getMotivoMuerteId() != null) {
            s.setMotivoMuerteId(solicitud.getMotivoMuerteId());
        }
        if (solicitud.getPrecioKg() != null) {
            s.setPrecioKg(solicitud.getPrecioKg());
        }
    }

    private void aplicarDefaultsSanidad(FeedlotSesionManga s) {
        if (s.getTipo() != FeedlotTipoSesionManga.SANIDAD) {
            return;
        }
        if (s.getAplicador() == null) {
            s.setAplicador(FeedlotAplicador.PERSONAL_ESTABLECIMIENTO);
        }
        if (s.getOrigenInsumo() == null) {
            s.setOrigenInsumo(s.getAplicador() == FeedlotAplicador.VETERINARIO
                    ? FeedlotOrigenInsumo.PROVEIDO_POR_APLICADOR
                    : FeedlotOrigenInsumo.STOCK_EMPRESA);
        }
    }

    private FeedlotLote loteActivo(Long empresaId, Long loteId) {
        FeedlotLote lote = servicioLotes.obtenerEntidadLote(empresaId, loteId);
        if (lote.getEstado() == FeedlotLoteEstado.CERRADO) {
            throw new ResourceConflictException("El lote ya está cerrado");
        }
        return lote;
    }

    private FeedlotSesionManga obtenerSesion(Long id) {
        Long empresaId = servicioSeguridadContexto.obtenerEmpresaIdActual();
        return sesionRepository.buscarPorIdYEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Sesión de manga no encontrada"));
    }

    private FeedlotSesionManga obtenerBorrador(Long id) {
        FeedlotSesionManga s = obtenerSesion(id);
        if (s.getEstado() != FeedlotEstadoSesionManga.BORRADOR) {
            throw new ResourceConflictException("La sesión no está en borrador");
        }
        loteActivo(s.getEmpresaId(), s.getLote().getId());
        return s;
    }

    private FeedlotSesionMangaRespuesta aRespuesta(FeedlotSesionManga s, boolean incluirFilas) {
        FeedlotSesionMangaRespuesta dto = new FeedlotSesionMangaRespuesta();
        dto.setId(s.getId());
        dto.setLoteId(s.getLote() != null ? s.getLote().getId() : null);
        dto.setLoteNombre(s.getLote() != null ? s.getLote().getNombre() : null);
        dto.setTipo(s.getTipo());
        dto.setFecha(s.getFecha());
        dto.setEstado(s.getEstado());
        dto.setNombreArchivo(s.getNombreArchivo());
        dto.setFilasOk(s.getFilasOk());
        dto.setFilasError(s.getFilasError());
        dto.setAjustarPlantel(s.getAjustarPlantel());
        dto.setAplicador(s.getAplicador());
        dto.setOrigenInsumo(s.getOrigenInsumo());
        dto.setTipoEvento(s.getTipoEvento());
        dto.setInsumoId(s.getInsumoId());
        dto.setDosisPorAnimal(s.getDosisPorAnimal());
        dto.setCantidadTotal(s.getCantidadTotal());
        dto.setDiasRetiro(s.getDiasRetiro());
        dto.setSubtipoBaja(s.getSubtipoBaja());
        if (incluirFilas && s.getId() != null) {
            List<FeedlotSesionMangaFila> filas = filaRepository.listarPorSesionId(s.getId());
            dto.setFilas(filas.stream().map(this::aFila).toList());
            if (s.getTipo() == FeedlotTipoSesionManga.RECUENTO) {
                dto.setResumenRecuento(armarRecuento(s, filas));
            }
        }
        completarSemaforo(s, dto);
        return dto;
    }

    private void validarCabeceraConfirmacion(FeedlotSesionManga s) {
        List<String> bloqueantes = calcularBloqueantes(s);
        if (!bloqueantes.isEmpty()) {
            throw new IllegalArgumentException(bloqueantes.get(0));
        }
    }

    private void completarSemaforo(FeedlotSesionManga s, FeedlotSesionMangaRespuesta dto) {
        List<String> avisos = new ArrayList<>();
        if (dto.getFilas() != null) {
            long menores = dto.getFilas().stream()
                    .filter(f -> f.getMensaje() != null && f.getMensaje().contains("Peso menor"))
                    .count();
            if (menores > 0) {
                avisos.add(menores + " lectura(s) con peso menor que la pesada anterior.");
            }
            boolean derivaFecha = dto.getFilas().stream()
                    .anyMatch(f -> f.getFechaHoraLectura() != null && s.getFecha() != null
                            && Math.abs(f.getFechaHoraLectura().toLocalDate().toEpochDay()
                            - s.getFecha().toEpochDay()) > 1);
            if (derivaFecha) {
                avisos.add("La fecha del archivo difiere más de un día de la fecha operativa.");
            }
        }
        dto.setAvisos(avisos);
        dto.setBloqueantes(calcularBloqueantes(s));
        boolean recuento = s.getTipo() == FeedlotTipoSesionManga.RECUENTO;
        boolean hayOk = s.getFilasOk() != null && s.getFilasOk() > 0;
        dto.setPuedeConfirmar(dto.getBloqueantes().isEmpty()
                && s.getEstado() == FeedlotEstadoSesionManga.BORRADOR
                && (recuento || hayOk));
    }

    private List<String> calcularBloqueantes(FeedlotSesionManga s) {
        List<String> out = new ArrayList<>();
        if (s.getTipo() == FeedlotTipoSesionManga.BAJA && s.getSubtipoBaja() == null) {
            out.add("Indicá qué pasó (muerte, faena, venta en pie o descarte)");
        }
        if (s.getTipo() == FeedlotTipoSesionManga.SANIDAD) {
            if (s.getTipoEvento() == null) {
                out.add("El tipo de evento sanitario es obligatorio");
            }
            if (s.getAplicador() == null) {
                out.add("El aplicador es obligatorio");
            }
            FeedlotTipoEventoSanitario tipo = s.getTipoEvento();
            if ((tipo == FeedlotTipoEventoSanitario.VACUNA || tipo == FeedlotTipoEventoSanitario.TRATAMIENTO)
                    && s.getOrigenInsumo() == null) {
                out.add("El origen del insumo es obligatorio para vacuna o tratamiento");
            }
            if (s.getOrigenInsumo() == FeedlotOrigenInsumo.STOCK_EMPRESA) {
                if (s.getInsumoId() == null) {
                    out.add("Elegí el insumo del depósito");
                }
                if (s.getDosisPorAnimal() == null && s.getCantidadTotal() == null) {
                    out.add("Indicá la dosis o la cantidad total a descontar");
                }
            }
        }
        if (s.getTipo() != FeedlotTipoSesionManga.RECUENTO
                && (s.getFilasOk() == null || s.getFilasOk() == 0)
                && s.getNombreArchivo() != null) {
            out.add("No hay filas válidas para confirmar");
        }
        return out;
    }

    private FeedlotResumenRecuento armarRecuento(FeedlotSesionManga s, List<FeedlotSesionMangaFila> filas) {
        FeedlotResumenRecuento r = new FeedlotResumenRecuento();
        Set<Long> presentes = filas.stream()
                .filter(f -> f.getResultado() == FeedlotResultadoFilaManga.OK && f.getAnimal() != null)
                .map(f -> f.getAnimal().getId())
                .collect(Collectors.toSet());
        r.setPresentes(presentes.size());
        List<FeedlotAnimal> activos = animalRepository.listarPorLoteYEstado(
                s.getLote().getId(), s.getEmpresaId(), FeedlotEstadoAnimal.ACTIVO);
        r.setFaltantes(activos.stream()
                .filter(a -> !presentes.contains(a.getId()))
                .map(a -> new FeedlotIdentificacionAnimal(a.getId(), a.getEid(), a.getCaravanaVisual()))
                .toList());
        List<FeedlotIdentificacionAnimal> desconocidos = new ArrayList<>();
        List<FeedlotIdentificacionAnimal> otroLote = new ArrayList<>();
        List<FeedlotIdentificacionAnimal> inactivos = new ArrayList<>();
        for (FeedlotSesionMangaFila f : filas) {
            if (f.getOrigenRecuento() == FeedlotOrigenFilaRecuento.DESCONOCIDO) {
                FeedlotIdentificacionAnimal d = new FeedlotIdentificacionAnimal(
                        null, f.getEidNormalizado(), f.getCaravanaVisual());
                d.setFilaId(f.getId());
                desconocidos.add(d);
            } else if (f.getOrigenRecuento() == FeedlotOrigenFilaRecuento.OTRO_LOTE) {
                FeedlotIdentificacionAnimal d = new FeedlotIdentificacionAnimal(
                        f.getAnimal() != null ? f.getAnimal().getId() : null,
                        f.getEidNormalizado(), f.getCaravanaVisual());
                if (f.getAnimal() != null && f.getAnimal().getLote() != null) {
                    d.setLoteId(f.getAnimal().getLote().getId());
                    d.setLoteNombre(f.getAnimal().getLote().getNombre());
                    if (f.getAnimal().getLote().getCorral() != null) {
                        d.setCorralNombre(f.getAnimal().getLote().getCorral().getNombre());
                    }
                }
                otroLote.add(d);
            } else if (f.getOrigenRecuento() == FeedlotOrigenFilaRecuento.INACTIVO) {
                FeedlotIdentificacionAnimal d = new FeedlotIdentificacionAnimal(
                        f.getAnimal() != null ? f.getAnimal().getId() : null,
                        f.getEidNormalizado(), f.getCaravanaVisual());
                if (f.getAnimal() != null) {
                    d.setEstado(f.getAnimal().getEstado().name());
                }
                inactivos.add(d);
            }
        }
        r.setDesconocidos(desconocidos);
        r.setOtroLote(otroLote);
        r.setInactivos(inactivos);
        List<FeedlotIdentificacionAnimal> intrusos = new ArrayList<>();
        intrusos.addAll(desconocidos);
        intrusos.addAll(otroLote);
        intrusos.addAll(inactivos);
        r.setIntrusos(intrusos);
        int cabezas = s.getLote().getCabezasActuales() != null ? s.getLote().getCabezasActuales() : 0;
        r.setCabezasSinEnrolar(Math.max(0, cabezas - activos.size()));
        return r;
    }

    private FeedlotSesionMangaFilaRespuesta aFila(FeedlotSesionMangaFila f) {
        FeedlotSesionMangaFilaRespuesta dto = new FeedlotSesionMangaFilaRespuesta();
        dto.setId(f.getId());
        dto.setNumeroLinea(f.getNumeroLinea());
        dto.setEidCrudo(f.getEidCrudo());
        dto.setEidNormalizado(f.getEidNormalizado());
        dto.setCaravanaVisual(f.getCaravanaVisual());
        dto.setPesoKg(f.getPesoKg());
        dto.setFechaHoraLectura(f.getFechaHoraLectura());
        dto.setAnimalId(f.getAnimal() != null ? f.getAnimal().getId() : null);
        dto.setResultado(f.getResultado());
        dto.setMensaje(f.getMensaje());
        dto.setOrigenRecuento(f.getOrigenRecuento());
        dto.setAccionDesconocido(f.getAccionDesconocido());
        dto.setLoteDestinoId(f.getLoteDestinoId());
        return dto;
    }

    @Transactional
    public FeedlotSesionMangaRespuesta generarBajaFaltantes(
            Long id, FeedlotGenerarBajaFaltantesSolicitud solicitud, User usuario) {
        FeedlotSesionManga origen = obtenerSesion(id);
        if (origen.getTipo() != FeedlotTipoSesionManga.RECUENTO) {
            throw new IllegalArgumentException("Solo se puede generar baja desde un recuento");
        }
        if (origen.getEstado() != FeedlotEstadoSesionManga.BORRADOR
                && origen.getEstado() != FeedlotEstadoSesionManga.CONFIRMADA) {
            throw new ResourceConflictException("El recuento no admite generar baja en este estado");
        }
        if (solicitud == null || solicitud.getAnimalIds() == null || solicitud.getAnimalIds().isEmpty()) {
            throw new IllegalArgumentException("Elegí al menos un faltante");
        }
        loteActivo(origen.getEmpresaId(), origen.getLote().getId());
        List<FeedlotSesionMangaFila> filasRecuento = filaRepository.listarPorSesionId(origen.getId());
        Set<Long> presentes = filasRecuento.stream()
                .filter(f -> f.getResultado() == FeedlotResultadoFilaManga.OK && f.getAnimal() != null)
                .map(f -> f.getAnimal().getId())
                .collect(Collectors.toSet());
        FeedlotSesionManga baja = new FeedlotSesionManga();
        baja.setEmpresaId(origen.getEmpresaId());
        baja.setLote(origen.getLote());
        baja.setTipo(FeedlotTipoSesionManga.BAJA);
        baja.setFecha(origen.getFecha());
        baja.setEstado(FeedlotEstadoSesionManga.BORRADOR);
        baja.setSubtipoBaja(solicitud.getSubtipoBaja() != null
                ? solicitud.getSubtipoBaja() : FeedlotSubtipoBaja.MUERTE);
        baja.setUsuarioId(usuario != null ? usuario.getId() : null);
        baja.setDescripcion("Generada desde recuento " + origen.getId());
        baja = sesionRepository.save(baja);
        int linea = 0;
        List<FeedlotSesionMangaFila> filasBaja = new ArrayList<>();
        for (Long animalId : solicitud.getAnimalIds()) {
            FeedlotAnimal animal = animalRepository.buscarPorIdYEmpresaId(animalId, origen.getEmpresaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Animal no encontrado"));
            if (animal.getEstado() != FeedlotEstadoAnimal.ACTIVO
                    || animal.getLote() == null
                    || !animal.getLote().getId().equals(origen.getLote().getId())) {
                throw new ResourceConflictException("El animal " + animalId + " no es un faltante activo de este lote");
            }
            if (presentes.contains(animal.getId())) {
                throw new ResourceConflictException("El animal " + animalId + " estuvo presente en el recuento");
            }
            linea++;
            FeedlotSesionMangaFila fila = new FeedlotSesionMangaFila();
            fila.setSesion(baja);
            fila.setEmpresaId(origen.getEmpresaId());
            fila.setNumeroLinea(linea);
            fila.setEidNormalizado(animal.getEid());
            fila.setEidCrudo(animal.getEid());
            fila.setCaravanaVisual(animal.getCaravanaVisual());
            fila.setAnimal(animal);
            fila.setResultado(FeedlotResultadoFilaManga.OK);
            fila.setMensaje("Faltante del recuento " + origen.getId());
            filasBaja.add(fila);
        }
        filaRepository.saveAll(filasBaja);
        baja.setFilasOk(filasBaja.size());
        baja.setFilasError(0);
        return aRespuesta(sesionRepository.save(baja), true);
    }
}
