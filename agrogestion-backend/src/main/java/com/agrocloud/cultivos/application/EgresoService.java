package com.agrocloud.cultivos.application;
import com.agrocloud.core.domain.Egreso;
import com.agrocloud.core.domain.User;
import com.agrocloud.cultivos.domain.Maquinaria;
import com.agrocloud.cultivos.domain.Plot;
import com.agrocloud.core.inventory.domain.Insumo;
import com.agrocloud.cultivos.infrastructure.PlotRepository;
import com.agrocloud.core.infrastructure.UserRepository;
import com.agrocloud.core.infrastructure.EgresoRepository;
import com.agrocloud.core.inventory.infrastructure.InsumoRepository;
import com.agrocloud.cultivos.infrastructure.MaquinariaRepository;
import com.agrocloud.dto.CrearEgresoRequest;
import com.agrocloud.core.application.CampanaContextService;
import com.agrocloud.core.security.ServicioSeguridadContexto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para la gestión de egresos con integración automática
 */
@Service("egresoServiceCultivos")
public class EgresoService {

    @Autowired
    @Qualifier("egresoRepositoryCore")
    private EgresoRepository egresoRepository;

    @Autowired
    @Qualifier("insumoRepositoryInventario")
    private InsumoRepository insumoRepository;

    @Autowired
    @Qualifier("maquinariaRepositoryCultivos")
    private MaquinariaRepository maquinariaRepository;

    // @Autowired
    // private AlquilerMaquinariaRepository alquilerRepository;  // DEPRECADO - No se usa

    @Autowired
    @Qualifier("plotRepositoryCultivos")
    private PlotRepository plotRepository;

    @Autowired
    @Qualifier("userRepositoryCore")
    private UserRepository userRepository;

    @Autowired
    private ServicioSeguridadContexto servicioSeguridadContexto;

    @Autowired
    @Qualifier("campanaContextServiceCore")
    private CampanaContextService campanaContextService;

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
        try {
            egreso.setCampanaId(campanaContextService.resolverCampanaIdActiva(
                    servicioSeguridadContexto.obtenerEmpresaIdActual()));
        } catch (Exception ignored) {
        }

        // Lógica específica según el tipo de egreso
        switch (request.getTipoEgreso()) {
            case INSUMO:
            case INSUMOS:
                return crearEgresoInsumo(egreso, request);
            case MAQUINARIA_COMPRA:
                return crearEgresoMaquinariaCompra(egreso, request);
            case MAQUINARIA_ALQUILER:
                throw new UnsupportedOperationException(
                    "El tipo MAQUINARIA_ALQUILER está deprecado. " +
                    "Por favor, registre la maquinaria alquilada como parte de una labor " +
                    "usando tipoMaquinaria=ALQUILADA en el formulario de la labor correspondiente."
                );
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

    // MÉTODO ELIMINADO - Funcionalidad deprecada
    // La tabla alquiler_maquinaria fue eliminada en V1_13
    // Alternativa: Registrar maquinaria alquilada en labor_maquinaria con tipoMaquinaria=ALQUILADA

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
