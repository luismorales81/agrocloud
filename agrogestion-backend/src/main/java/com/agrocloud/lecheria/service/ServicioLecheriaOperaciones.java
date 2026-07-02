package com.agrocloud.lecheria.service;

import com.agrocloud.core.application.CampanaContextService;
import com.agrocloud.core.inventory.application.InventoryService;
import com.agrocloud.core.inventory.domain.InventoryOrigin;
import com.agrocloud.core.inventory.domain.InventoryResult;
import com.agrocloud.core.security.ServicioSeguridadContexto;
import com.agrocloud.exception.ResourceNotFoundException;
import com.agrocloud.lecheria.model.dto.LecheriaBajaAnimalRespuesta;
import com.agrocloud.lecheria.model.dto.LecheriaBajaAnimalSolicitud;
import com.agrocloud.lecheria.model.dto.LecheriaConsumoRespuesta;
import com.agrocloud.lecheria.model.dto.LecheriaConsumoSolicitud;
import com.agrocloud.lecheria.model.dto.LecheriaEventoSanitarioRespuesta;
import com.agrocloud.lecheria.model.dto.LecheriaEventoSanitarioSolicitud;
import com.agrocloud.lecheria.model.dto.LecheriaScoreCorporalRespuesta;
import com.agrocloud.lecheria.model.dto.LecheriaScoreCorporalSolicitud;
import com.agrocloud.lecheria.model.dto.LecheriaVentaLecheRespuesta;
import com.agrocloud.lecheria.model.dto.LecheriaVentaLecheSolicitud;
import com.agrocloud.lecheria.model.entity.LecheriaAnimal;
import com.agrocloud.lecheria.model.entity.LecheriaBajaAnimal;
import com.agrocloud.lecheria.model.entity.LecheriaConsumo;
import com.agrocloud.lecheria.model.entity.LecheriaEventoSanitario;
import com.agrocloud.lecheria.model.entity.LecheriaMotivoBaja;
import com.agrocloud.lecheria.model.entity.LecheriaRodeo;
import com.agrocloud.lecheria.model.entity.LecheriaScoreCorporal;
import com.agrocloud.lecheria.model.entity.LecheriaVentaLeche;
import com.agrocloud.lecheria.repository.LecheriaBajaAnimalRepository;
import com.agrocloud.lecheria.repository.LecheriaConsumoRepository;
import com.agrocloud.lecheria.repository.LecheriaEventoSanitarioRepository;
import com.agrocloud.lecheria.repository.LecheriaLactanciaRepository;
import com.agrocloud.lecheria.repository.LecheriaMotivoBajaRepository;
import com.agrocloud.lecheria.repository.LecheriaRodeoRepository;
import com.agrocloud.lecheria.repository.LecheriaScoreCorporalRepository;
import com.agrocloud.lecheria.repository.LecheriaVentaLecheRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServicioLecheriaOperaciones {

    private final ServicioSeguridadContexto servicioSeguridadContexto;
    private final CampanaContextService campanaContextService;
    private final ServicioLecheriaAnimales servicioAnimales;
    private final LecheriaRodeoRepository rodeoRepository;
    private final LecheriaEventoSanitarioRepository eventoSanitarioRepository;
    private final LecheriaScoreCorporalRepository scoreCorporalRepository;
    private final LecheriaConsumoRepository consumoRepository;
    private final LecheriaVentaLecheRepository ventaLecheRepository;
    private final LecheriaBajaAnimalRepository bajaAnimalRepository;
    private final LecheriaMotivoBajaRepository motivoBajaRepository;
    private final LecheriaLactanciaRepository lactanciaRepository;
    private final InventoryService inventoryService;

    public ServicioLecheriaOperaciones(
            ServicioSeguridadContexto servicioSeguridadContexto,
            @Qualifier("campanaContextServiceCore") CampanaContextService campanaContextService,
            ServicioLecheriaAnimales servicioAnimales,
            LecheriaRodeoRepository rodeoRepository,
            LecheriaEventoSanitarioRepository eventoSanitarioRepository,
            LecheriaScoreCorporalRepository scoreCorporalRepository,
            LecheriaConsumoRepository consumoRepository,
            LecheriaVentaLecheRepository ventaLecheRepository,
            LecheriaBajaAnimalRepository bajaAnimalRepository,
            LecheriaMotivoBajaRepository motivoBajaRepository,
            LecheriaLactanciaRepository lactanciaRepository,
            InventoryService inventoryService) {
        this.servicioSeguridadContexto = servicioSeguridadContexto;
        this.campanaContextService = campanaContextService;
        this.servicioAnimales = servicioAnimales;
        this.rodeoRepository = rodeoRepository;
        this.eventoSanitarioRepository = eventoSanitarioRepository;
        this.scoreCorporalRepository = scoreCorporalRepository;
        this.consumoRepository = consumoRepository;
        this.ventaLecheRepository = ventaLecheRepository;
        this.bajaAnimalRepository = bajaAnimalRepository;
        this.motivoBajaRepository = motivoBajaRepository;
        this.lactanciaRepository = lactanciaRepository;
        this.inventoryService = inventoryService;
    }

    @Transactional(readOnly = true)
    public List<LecheriaEventoSanitarioRespuesta> listarEventosSanitarios(Long animalId) {
        Long empresaId = empresaActual();
        servicioAnimales.obtenerEntidadAnimal(empresaId, animalId);
        return eventoSanitarioRepository.listarPorAnimalId(animalId).stream()
                .map(this::aEventoSanitarioRespuesta).collect(Collectors.toList());
    }

    @Transactional
    public LecheriaEventoSanitarioRespuesta registrarEventoSanitario(Long animalId, LecheriaEventoSanitarioSolicitud solicitud) {
        Long empresaId = empresaActual();
        Long usuarioId = servicioSeguridadContexto.obtenerUsuarioIdActual();
        LecheriaAnimal animal = servicioAnimales.obtenerEntidadAnimal(empresaId, animalId);
        if (solicitud.getTipo() == null || solicitud.getFecha() == null) {
            throw new IllegalArgumentException("Tipo y fecha son obligatorios");
        }
        LecheriaEventoSanitario e = new LecheriaEventoSanitario();
        e.setAnimal(animal);
        e.setEmpresaId(empresaId);
        e.setTipo(solicitud.getTipo());
        e.setFecha(solicitud.getFecha());
        e.setDescripcion(solicitud.getDescripcion());
        e.setInsumoId(solicitud.getInsumoId());
        e.setCantidad(solicitud.getCantidad());
        e.setDiasRetiro(solicitud.getDiasRetiro());
        e = eventoSanitarioRepository.saveAndFlush(e);

        if (solicitud.getInsumoId() != null && solicitud.getCantidad() != null
                && solicitud.getCantidad().compareTo(BigDecimal.ZERO) > 0) {
            InventoryResult resultado = inventoryService.consumir(
                    empresaId, solicitud.getInsumoId(), solicitud.getCantidad(),
                    InventoryOrigin.LECHERIA, e.getId(), usuarioId);
            if (!resultado.exito()) {
                throw new IllegalStateException("No se pudo descontar inventario: " + resultado.mensaje());
            }
        }
        return aEventoSanitarioRespuesta(e);
    }

    @Transactional(readOnly = true)
    public List<LecheriaScoreCorporalRespuesta> listarScoresCorporales(Long animalId) {
        servicioAnimales.obtenerEntidadAnimal(empresaActual(), animalId);
        return scoreCorporalRepository.listarPorAnimalId(animalId).stream()
                .map(this::aScoreRespuesta).collect(Collectors.toList());
    }

    @Transactional
    public LecheriaScoreCorporalRespuesta registrarScoreCorporal(Long animalId, LecheriaScoreCorporalSolicitud solicitud) {
        Long empresaId = empresaActual();
        LecheriaAnimal animal = servicioAnimales.obtenerEntidadAnimal(empresaId, animalId);
        if (solicitud.getFecha() == null || solicitud.getValor() == null) {
            throw new IllegalArgumentException("Fecha y valor son obligatorios");
        }
        LecheriaScoreCorporal s = new LecheriaScoreCorporal();
        s.setAnimal(animal);
        s.setEmpresaId(empresaId);
        s.setFecha(solicitud.getFecha());
        s.setValor(solicitud.getValor());
        s.setObservaciones(solicitud.getObservaciones());
        return aScoreRespuesta(scoreCorporalRepository.save(s));
    }

    @Transactional(readOnly = true)
    public List<LecheriaConsumoRespuesta> listarConsumos(Long rodeoId) {
        requerirRodeo(rodeoId);
        return consumoRepository.listarPorRodeoId(rodeoId).stream()
                .map(this::aConsumoRespuesta).collect(Collectors.toList());
    }

    @Transactional
    public LecheriaConsumoRespuesta registrarConsumo(Long rodeoId, LecheriaConsumoSolicitud solicitud) {
        Long empresaId = empresaActual();
        Long usuarioId = servicioSeguridadContexto.obtenerUsuarioIdActual();
        LecheriaRodeo rodeo = requerirRodeo(rodeoId);
        if (solicitud.getInsumoId() == null || solicitud.getFecha() == null || solicitud.getCantidadKg() == null) {
            throw new IllegalArgumentException("Insumo, fecha y cantidad son obligatorios");
        }
        LecheriaConsumo c = new LecheriaConsumo();
        c.setRodeo(rodeo);
        c.setEmpresaId(empresaId);
        c.setCampanaId(campanaContextService.resolverCampanaIdActiva(empresaId));
        c.setInsumoId(solicitud.getInsumoId());
        c.setFecha(solicitud.getFecha());
        c.setCantidadKg(solicitud.getCantidadKg());
        c.setObservaciones(solicitud.getObservaciones());
        c = consumoRepository.saveAndFlush(c);

        InventoryResult resultado = inventoryService.consumir(
                empresaId, solicitud.getInsumoId(), solicitud.getCantidadKg(),
                InventoryOrigin.LECHERIA, c.getId(), usuarioId);
        if (!resultado.exito()) {
            throw new IllegalStateException("No se pudo descontar inventario: " + resultado.mensaje());
        }
        return aConsumoRespuesta(c);
    }

    @Transactional(readOnly = true)
    public List<LecheriaVentaLecheRespuesta> listarVentasLeche() {
        return ventaLecheRepository.listarPorEmpresaId(empresaActual()).stream()
                .map(this::aVentaRespuesta).collect(Collectors.toList());
    }

    @Transactional
    public LecheriaVentaLecheRespuesta registrarVentaLeche(LecheriaVentaLecheSolicitud solicitud) {
        Long empresaId = empresaActual();
        if (solicitud.getFecha() == null || solicitud.getLitros() == null) {
            throw new IllegalArgumentException("Fecha y litros son obligatorios");
        }
        LecheriaVentaLeche v = new LecheriaVentaLeche();
        v.setEmpresaId(empresaId);
        v.setCampanaId(campanaContextService.resolverCampanaIdActiva(empresaId));
        v.setFecha(solicitud.getFecha());
        v.setLitros(solicitud.getLitros());
        v.setPrecioLitro(solicitud.getPrecioLitro());
        v.setComprador(solicitud.getComprador());
        v.setObservaciones(solicitud.getObservaciones());
        if (solicitud.getPrecioLitro() != null) {
            v.setTotal(solicitud.getLitros().multiply(solicitud.getPrecioLitro())
                    .setScale(2, RoundingMode.HALF_UP));
        }
        return aVentaRespuesta(ventaLecheRepository.save(v));
    }

    @Transactional
    public LecheriaBajaAnimalRespuesta registrarBaja(Long animalId, LecheriaBajaAnimalSolicitud solicitud) {
        Long empresaId = empresaActual();
        LecheriaAnimal animal = servicioAnimales.obtenerEntidadAnimal(empresaId, animalId);
        if (!Boolean.TRUE.equals(animal.getActivo())) {
            throw new IllegalStateException("El animal ya está dado de baja");
        }
        if (solicitud.getFecha() == null) {
            throw new IllegalArgumentException("La fecha de baja es obligatoria");
        }
        lactanciaRepository.buscarActivaPorAnimalId(animalId).ifPresent(l -> {
            l.setActiva(false);
            l.setFechaSecado(solicitud.getFecha());
            lactanciaRepository.save(l);
        });
        animal.setActivo(false);
        animal.setFechaBaja(solicitud.getFecha());

        LecheriaBajaAnimal baja = new LecheriaBajaAnimal();
        baja.setAnimal(animal);
        baja.setEmpresaId(empresaId);
        baja.setFecha(solicitud.getFecha());
        baja.setObservaciones(solicitud.getObservaciones());
        if (solicitud.getMotivoId() != null) {
            LecheriaMotivoBaja motivo = motivoBajaRepository.buscarPorIdYEmpresaId(solicitud.getMotivoId(), empresaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Motivo de baja no encontrado"));
            baja.setMotivo(motivo);
        }
        return aBajaRespuesta(bajaAnimalRepository.save(baja));
    }

    private Long empresaActual() {
        return servicioSeguridadContexto.obtenerEmpresaIdActual();
    }

    private LecheriaRodeo requerirRodeo(Long rodeoId) {
        return rodeoRepository.buscarPorIdYEmpresaId(rodeoId, empresaActual())
                .orElseThrow(() -> new ResourceNotFoundException("Rodeo no encontrado"));
    }

    private LecheriaEventoSanitarioRespuesta aEventoSanitarioRespuesta(LecheriaEventoSanitario e) {
        LecheriaEventoSanitarioRespuesta dto = new LecheriaEventoSanitarioRespuesta();
        dto.setId(e.getId());
        dto.setAnimalId(e.getAnimal().getId());
        dto.setTipo(e.getTipo());
        dto.setFecha(e.getFecha());
        dto.setDescripcion(e.getDescripcion());
        dto.setInsumoId(e.getInsumoId());
        dto.setCantidad(e.getCantidad());
        dto.setDiasRetiro(e.getDiasRetiro());
        dto.setCreatedAt(e.getCreatedAt());
        return dto;
    }

    private LecheriaScoreCorporalRespuesta aScoreRespuesta(LecheriaScoreCorporal s) {
        LecheriaScoreCorporalRespuesta dto = new LecheriaScoreCorporalRespuesta();
        dto.setId(s.getId());
        dto.setAnimalId(s.getAnimal().getId());
        dto.setFecha(s.getFecha());
        dto.setValor(s.getValor());
        dto.setObservaciones(s.getObservaciones());
        dto.setCreatedAt(s.getCreatedAt());
        return dto;
    }

    private LecheriaConsumoRespuesta aConsumoRespuesta(LecheriaConsumo c) {
        LecheriaConsumoRespuesta dto = new LecheriaConsumoRespuesta();
        dto.setId(c.getId());
        dto.setRodeoId(c.getRodeo().getId());
        dto.setCampanaId(c.getCampanaId());
        dto.setInsumoId(c.getInsumoId());
        dto.setFecha(c.getFecha());
        dto.setCantidadKg(c.getCantidadKg());
        dto.setObservaciones(c.getObservaciones());
        dto.setCreatedAt(c.getCreatedAt());
        return dto;
    }

    private LecheriaVentaLecheRespuesta aVentaRespuesta(LecheriaVentaLeche v) {
        LecheriaVentaLecheRespuesta dto = new LecheriaVentaLecheRespuesta();
        dto.setId(v.getId());
        dto.setEmpresaId(v.getEmpresaId());
        dto.setCampanaId(v.getCampanaId());
        dto.setFecha(v.getFecha());
        dto.setLitros(v.getLitros());
        dto.setPrecioLitro(v.getPrecioLitro());
        dto.setTotal(v.getTotal());
        dto.setComprador(v.getComprador());
        dto.setObservaciones(v.getObservaciones());
        dto.setCreatedAt(v.getCreatedAt());
        return dto;
    }

    private LecheriaBajaAnimalRespuesta aBajaRespuesta(LecheriaBajaAnimal b) {
        LecheriaBajaAnimalRespuesta dto = new LecheriaBajaAnimalRespuesta();
        dto.setId(b.getId());
        dto.setAnimalId(b.getAnimal().getId());
        dto.setFecha(b.getFecha());
        if (b.getMotivo() != null) {
            dto.setMotivoId(b.getMotivo().getId());
            dto.setMotivoNombre(b.getMotivo().getNombre());
        }
        dto.setObservaciones(b.getObservaciones());
        dto.setCreatedAt(b.getCreatedAt());
        return dto;
    }
}
