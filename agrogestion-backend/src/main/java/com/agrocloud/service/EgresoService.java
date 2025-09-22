package com.agrocloud.service;

import com.agrocloud.model.entity.*;
import com.agrocloud.repository.*;
import com.agrocloud.dto.CrearEgresoRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de egresos con integración automática
 */
@Service
public class EgresoService {

    @Autowired
    private EgresoRepository egresoRepository;

    @Autowired
    private InsumoRepository insumoRepository;

    @Autowired
    private MaquinariaRepository maquinariaRepository;

    @Autowired
    private AlquilerMaquinariaRepository alquilerRepository;

    @Autowired
    private PlotRepository plotRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Crea un egreso con lógica de integración automática
     */
    @Transactional
    public Egreso crearEgreso(CrearEgresoRequest request) {
        // Validar usuario
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Crear el egreso base
        Egreso egreso = new Egreso();
        egreso.setTipo(request.getTipoEgreso());
        egreso.setFecha(request.getFechaEgreso());
        egreso.setUser(user);
        egreso.setObservaciones(request.getObservaciones());
        egreso.setFechaCreacion(LocalDateTime.now());
        egreso.setFechaActualizacion(LocalDateTime.now());

        // Lógica específica según el tipo de egreso
        switch (request.getTipoEgreso()) {
            case INSUMO:
                return crearEgresoInsumo(egreso, request);
            case MAQUINARIA_COMPRA:
                return crearEgresoMaquinariaCompra(egreso, request);
            case MAQUINARIA_ALQUILER:
                return crearEgresoMaquinariaAlquiler(egreso, request);
            default:
                return crearEgresoGeneral(egreso, request);
        }
    }

    /**
     * Crea egreso de tipo INSUMO con cálculo automático de costos
     */
    private Egreso crearEgresoInsumo(Egreso egreso, CrearEgresoRequest request) {
        // Buscar el insumo
        Insumo insumo = insumoRepository.findById(request.getInsumoId())
            .orElseThrow(() -> new RuntimeException("Insumo no encontrado"));

        // Establecer referencia al insumo
        egreso.setReferenciaId(insumo.getId());

        // Calcular costo total automáticamente
        BigDecimal cantidad = request.getCantidad();
        BigDecimal precioUnitario = insumo.getPrecioUnitario() != null ? 
            insumo.getPrecioUnitario() : BigDecimal.ZERO;
        
        BigDecimal costoTotal = cantidad.multiply(precioUnitario);
        
        egreso.setCantidad(cantidad);
        egreso.setCostoUnitario(precioUnitario);
        egreso.setCostoTotal(costoTotal);

        // Asociar con lote si se especifica
        if (request.getLoteId() != null) {
            Plot lote = plotRepository.findById(request.getLoteId())
                .orElseThrow(() -> new RuntimeException("Lote no encontrado"));
            egreso.setLote(lote);
        }

        return egresoRepository.save(egreso);
    }

    /**
     * Crea egreso de tipo MAQUINARIA_COMPRA y registra la maquinaria
     */
    private Egreso crearEgresoMaquinariaCompra(Egreso egreso, CrearEgresoRequest request) {
        // Crear registro de maquinaria
        Maquinaria maquinaria = new Maquinaria();
        maquinaria.setNombre(request.getConcepto());
        maquinaria.setMarca(request.getMarca());
        maquinaria.setModelo(request.getModelo());
        maquinaria.setEstado(Maquinaria.EstadoMaquinaria.ACTIVA);
        maquinaria.setDescripcion(request.getObservaciones());
        maquinaria.setUser(egreso.getUser());
        maquinaria.setFechaCreacion(LocalDateTime.now());
        maquinaria.setFechaActualizacion(LocalDateTime.now());

        // Guardar maquinaria
        maquinaria = maquinariaRepository.save(maquinaria);

        // Establecer referencia a la maquinaria
        egreso.setReferenciaId(maquinaria.getId());
        egreso.setCostoTotal(request.getMonto());

        // Asociar con lote si se especifica
        if (request.getLoteId() != null) {
            Plot lote = plotRepository.findById(request.getLoteId())
                .orElseThrow(() -> new RuntimeException("Lote no encontrado"));
            egreso.setLote(lote);
        }

        return egresoRepository.save(egreso);
    }

    /**
     * Crea egreso de tipo MAQUINARIA_ALQUILER y registra el alquiler
     */
    private Egreso crearEgresoMaquinariaAlquiler(Egreso egreso, CrearEgresoRequest request) {
        // Buscar la maquinaria
        Maquinaria maquinaria = maquinariaRepository.findById(request.getMaquinariaId())
            .orElseThrow(() -> new RuntimeException("Maquinaria no encontrada"));

        // Crear registro de alquiler
        AlquilerMaquinaria alquiler = new AlquilerMaquinaria();
        alquiler.setMaquinaria(maquinaria);
        alquiler.setFechaInicio(request.getFechaInicio());
        alquiler.setFechaFin(request.getFechaFin());
        alquiler.setCostoDia(request.getCostoDia());
        alquiler.setCostoTotal(request.getMonto());
        alquiler.setObservaciones(request.getObservaciones());
        alquiler.setUser(egreso.getUser());
        alquiler.setFechaCreacion(LocalDateTime.now());
        alquiler.setFechaActualizacion(LocalDateTime.now());

        // Asociar con lote si se especifica
        if (request.getLoteId() != null) {
            Plot lote = plotRepository.findById(request.getLoteId())
                .orElseThrow(() -> new RuntimeException("Lote no encontrado"));
            alquiler.setLote(lote);
            egreso.setLote(lote);
        }

        // Guardar alquiler
        alquiler = alquilerRepository.save(alquiler);

        // Establecer referencia al alquiler
        egreso.setReferenciaId(alquiler.getId());
        egreso.setCostoTotal(request.getMonto());

        return egresoRepository.save(egreso);
    }

    /**
     * Crea egreso general (SERVICIO, OTROS)
     */
    private Egreso crearEgresoGeneral(Egreso egreso, CrearEgresoRequest request) {
        egreso.setCostoTotal(request.getMonto());

        // Asociar con lote si se especifica
        if (request.getLoteId() != null) {
            Plot lote = plotRepository.findById(request.getLoteId())
                .orElseThrow(() -> new RuntimeException("Lote no encontrado"));
            egreso.setLote(lote);
        }

        return egresoRepository.save(egreso);
    }

    /**
     * Obtiene todos los egresos de un usuario
     */
    public List<Egreso> obtenerEgresosPorUsuario(Long userId) {
        return egresoRepository.findByUserIdAndFechaBetweenOrderByFechaDesc(
            userId, LocalDate.now().minusYears(1), LocalDate.now());
    }

    /**
     * Obtiene egresos por lote
     */
    public List<Egreso> obtenerEgresosPorLote(Long loteId) {
        return egresoRepository.findByLoteIdAndFechaBetweenOrderByFechaDesc(
            loteId, LocalDate.now().minusYears(1), LocalDate.now());
    }

    /**
     * Obtiene egresos por tipo
     */
    public List<Egreso> obtenerEgresosPorTipo(Egreso.TipoEgreso tipo, Long userId) {
        return egresoRepository.findByTipoAndUserIdOrderByFechaDesc(tipo, userId);
    }

    /**
     * Calcula el total de egresos en un rango de fechas
     */
    public BigDecimal calcularTotalEgresos(Long userId, LocalDate fechaInicio, LocalDate fechaFin) {
        return egresoRepository.calcularTotalEgresosPorUsuarioYFecha(userId, fechaInicio, fechaFin);
    }

    /**
     * Calcula el total de egresos por lote en un rango de fechas
     */
    public BigDecimal calcularTotalEgresosPorLote(Long loteId, LocalDate fechaInicio, LocalDate fechaFin) {
        return egresoRepository.calcularTotalEgresosPorLoteYFecha(loteId, fechaInicio, fechaFin);
    }
}
