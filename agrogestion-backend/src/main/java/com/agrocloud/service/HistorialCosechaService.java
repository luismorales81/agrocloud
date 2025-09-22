package com.agrocloud.service;

import com.agrocloud.model.entity.*;
import com.agrocloud.model.enums.EstadoLote;
import com.agrocloud.repository.HistorialCosechaRepository;
import com.agrocloud.repository.PlotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar el historial de cosechas
 */
@Service
@Transactional
public class HistorialCosechaService {

    private static final Logger logger = LoggerFactory.getLogger(HistorialCosechaService.class);

    @Autowired
    private HistorialCosechaRepository historialCosechaRepository;

    @Autowired
    private PlotRepository plotRepository;

    @Autowired
    private UserService userService;

    /**
     * Crear un nuevo registro en el historial de cosechas
     */
    public HistorialCosecha crearHistorialCosecha(Cosecha cosecha, User usuario) {
        HistorialCosecha historial = new HistorialCosecha();
        
        // Mapear datos de la cosecha al historial
        historial.setLote(cosecha.getLote());
        historial.setCultivo(cosecha.getCultivo());
        
        // Obtener datos del lote para fechas y superficie
        Plot lote = cosecha.getLote();
        if (lote != null) {
            historial.setFechaSiembra(lote.getFechaSiembra());
            historial.setSuperficieHectareas(lote.getAreaHectareas());
        }
        
        // Usar la fecha de la cosecha
        historial.setFechaCosecha(cosecha.getFecha());
        
        // Mapear cantidad cosechada (convertir de toneladas a kg)
        if (cosecha.getCantidadToneladas() != null) {
            historial.setCantidadCosechada(cosecha.getCantidadToneladas().multiply(new BigDecimal("1000")));
            historial.setUnidadCosecha("kg");
        }
        
        // Calcular rendimiento real si tenemos superficie
        if (lote != null && lote.getAreaHectareas() != null && lote.getAreaHectareas().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal rendimientoReal = cosecha.getCantidadToneladas() != null ? 
                cosecha.getCantidadToneladas().multiply(new BigDecimal("1000")).divide(lote.getAreaHectareas(), 2, java.math.RoundingMode.HALF_UP) : 
                BigDecimal.ZERO;
            historial.setRendimientoReal(rendimientoReal);
        }
        
        // Obtener rendimiento esperado del cultivo
        if (cosecha.getCultivo() != null && cosecha.getCultivo().getRendimientoEsperado() != null) {
            historial.setRendimientoEsperado(cosecha.getCultivo().getRendimientoEsperado());
        }
        
        // Valores por defecto para campos no disponibles en la entidad Cosecha actual
        historial.setVariedadSemilla(cosecha.getCultivo() != null ? cosecha.getCultivo().getVariedad() : "");
        historial.setObservaciones(cosecha.getObservaciones());
        historial.setUsuario(usuario);

        // Determinar si requiere descanso basado en el cultivo y rendimiento
        determinarRequerimientoDescanso(historial);

        return historialCosechaRepository.save(historial);
    }

    /**
     * Obtener historial de cosechas por lote
     */
    public List<HistorialCosecha> getHistorialPorLote(Long loteId, User usuario) {
        if (usuario.isAdmin()) {
            return historialCosechaRepository.findByLoteIdOrderByFechaCosechaDesc(loteId);
        } else {
            return historialCosechaRepository.findAccessibleByUserAndLote(usuario, loteId);
        }
    }

    /**
     * Obtener historial de cosechas por usuario
     */
    public List<HistorialCosecha> getHistorialPorUsuario(User usuario) {
        if (usuario.isAdmin()) {
            return historialCosechaRepository.findAll();
        } else {
            return historialCosechaRepository.findAccessibleByUser(usuario);
        }
    }

    /**
     * Obtener última cosecha de un lote
     */
    public Optional<HistorialCosecha> getUltimaCosechaPorLote(Long loteId) {
        return historialCosechaRepository.findFirstByLoteIdOrderByFechaCosechaDesc(loteId);
    }

    /**
     * Liberar lote para nueva siembra
     */
    public boolean liberarLoteParaNuevaSiembra(Long loteId, User usuario) {
        Optional<Plot> loteOpt = plotRepository.findById(loteId);
        if (loteOpt.isEmpty()) {
            return false;
        }

        Plot lote = loteOpt.get();
        
        // Verificar que el usuario tenga acceso al lote
        if (!usuario.isAdmin() && !lote.getUser().equals(usuario)) {
            return false;
        }

        // Verificar que no haya cosechas muy recientes (mínimo 7 días)
        LocalDate fechaMinima = LocalDate.now().minusDays(7);
        long cosechasRecientes = historialCosechaRepository.countCosechasRecientesPorLote(loteId, fechaMinima);
        
        if (cosechasRecientes > 0) {
            throw new IllegalStateException("No se puede liberar el lote. Tiene cosechas muy recientes (menos de 7 días).");
        }

        // Liberar el lote
        lote.setEstado(EstadoLote.DISPONIBLE);
        lote.setCultivoActual(null);
        lote.setFechaSiembra(null);
        lote.setFechaCosechaEsperada(null);
        
        plotRepository.save(lote);
        return true;
    }

    /**
     * Liberar lote forzadamente (ignorando período de descanso)
     */
    public boolean liberarLoteForzadamente(Long loteId, User usuario, String justificacion) {
        Optional<Plot> loteOpt = plotRepository.findById(loteId);
        if (loteOpt.isEmpty()) {
            return false;
        }

        Plot lote = loteOpt.get();
        
        // Verificar que el usuario tenga acceso al lote
        if (!usuario.isAdmin() && !lote.getUser().equals(usuario)) {
            return false;
        }

        // Registrar la liberación forzada en el log
        logger.warn("Liberación forzada del lote {} por usuario {}. Justificación: {}", 
                   loteId, usuario.getEmail(), justificacion);

        // Liberar el lote (ignorando período de descanso)
        lote.setEstado(EstadoLote.DISPONIBLE);
        lote.setCultivoActual(null);
        lote.setFechaSiembra(null);
        lote.setFechaCosechaEsperada(null);
        
        plotRepository.save(lote);
        return true;
    }

    /**
     * Verificar si un lote puede ser liberado
     */
    public boolean puedeLiberarLote(Long loteId) {
        LocalDate fechaMinima = LocalDate.now().minusDays(7);
        long cosechasRecientes = historialCosechaRepository.countCosechasRecientesPorLote(loteId, fechaMinima);
        return cosechasRecientes == 0;
    }

    /**
     * Obtener días de descanso recomendados para un lote
     */
    public int getDiasDescansoRecomendados(Long loteId) {
        Optional<HistorialCosecha> ultimaCosecha = getUltimaCosechaPorLote(loteId);
        if (ultimaCosecha.isPresent()) {
            return ultimaCosecha.get().getDiasDescansoRecomendados();
        }
        return 0;
    }

    /**
     * Obtener estadísticas de rendimiento por cultivo
     */
    public List<Object[]> getEstadisticasRendimientoPorCultivo(User usuario) {
        return historialCosechaRepository.getEstadisticasRendimientoPorCultivo(usuario);
    }

    /**
     * Obtener cosechas recientes (últimos 30 días)
     */
    public List<HistorialCosecha> getCosechasRecientes(User usuario) {
        LocalDate fechaInicio = LocalDate.now().minusDays(30);
        return historialCosechaRepository.findCosechasRecientes(usuario, fechaInicio);
    }

    /**
     * Determinar si el lote requiere descanso basado en el cultivo y rendimiento
     */
    private void determinarRequerimientoDescanso(HistorialCosecha historial) {
        String nombreCultivo = historial.getCultivo().getNombre().toLowerCase();
        BigDecimal rendimientoReal = historial.getRendimientoReal();
        BigDecimal rendimientoEsperado = historial.getRendimientoEsperado();

        // Cultivos que generalmente requieren descanso
        boolean cultivoRequiereDescanso = nombreCultivo.contains("soja") || 
                                        nombreCultivo.contains("maíz") || 
                                        nombreCultivo.contains("trigo");

        // Si el rendimiento fue muy bajo, recomendar más descanso
        boolean rendimientoBajo = false;
        if (rendimientoEsperado != null && rendimientoReal != null && 
            rendimientoEsperado.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal porcentajeCumplimiento = rendimientoReal.divide(rendimientoEsperado, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            rendimientoBajo = porcentajeCumplimiento.compareTo(new BigDecimal("70")) < 0;
        }

        if (cultivoRequiereDescanso || rendimientoBajo) {
            historial.setRequiereDescanso(true);
            
            // Calcular días de descanso recomendados
            int diasBase = cultivoRequiereDescanso ? 30 : 15;
            int diasAdicionales = rendimientoBajo ? 15 : 0;
            
            historial.setDiasDescansoRecomendados(diasBase + diasAdicionales);
            
            // Determinar estado del suelo
            if (rendimientoBajo) {
                historial.setEstadoSuelo("AGOTADO");
            } else {
                historial.setEstadoSuelo("DESCANSANDO");
            }
        } else {
            historial.setRequiereDescanso(false);
            historial.setDiasDescansoRecomendados(0);
            historial.setEstadoSuelo("BUENO");
        }
    }

    /**
     * Obtener historial por ID
     */
    public Optional<HistorialCosecha> getHistorialById(Long id, User usuario) {
        Optional<HistorialCosecha> historial = historialCosechaRepository.findById(id);
        
        if (historial.isPresent()) {
            HistorialCosecha h = historial.get();
            if (usuario.isAdmin() || h.getUsuario().equals(usuario)) {
                return historial;
            }
        }
        
        return Optional.empty();
    }

    /**
     * Eliminar historial (solo para administradores)
     */
    public boolean eliminarHistorial(Long id, User usuario) {
        if (!usuario.isAdmin()) {
            return false;
        }
        
        if (historialCosechaRepository.existsById(id)) {
            historialCosechaRepository.deleteById(id);
            return true;
        }
        
        return false;
    }
}
