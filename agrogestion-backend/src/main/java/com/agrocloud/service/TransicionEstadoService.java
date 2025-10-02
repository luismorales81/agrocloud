package com.agrocloud.service;

import com.agrocloud.model.entity.Labor;
import com.agrocloud.model.entity.Plot;
import com.agrocloud.model.enums.EstadoLote;
import com.agrocloud.repository.LaborRepository;
import com.agrocloud.repository.PlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio para gestionar las transiciones automáticas de estado de los lotes
 * basadas en las labores realizadas.
 */
@Service
@Transactional
public class TransicionEstadoService {

    @Autowired
    private PlotRepository plotRepository;
    
    @Autowired
    private LaborRepository laborRepository;
    
    /**
     * Evalúa y aplica transiciones automáticas de estado después de registrar una labor
     */
    public boolean evaluarYAplicarTransicion(Plot lote, Labor labor) {
        if (lote == null || labor == null) {
            return false;
        }
        
        EstadoLote estadoAnterior = lote.getEstado();
        EstadoLote nuevoEstado = evaluarTransicion(lote, labor);
        
        if (nuevoEstado != null && nuevoEstado != estadoAnterior) {
            lote.setEstado(nuevoEstado);
            lote.setFechaUltimoCambioEstado(java.time.LocalDateTime.now());
            lote.setMotivoCambioEstado(
                String.format("Cambio automático por labor: %s", 
                    labor.getTipoLabor())
            );
            plotRepository.save(lote);
            
            System.out.println(String.format(
                "🔄 [TransicionEstado] Lote '%s' cambió de %s → %s por labor %s",
                lote.getNombre(), estadoAnterior, nuevoEstado, labor.getTipoLabor()
            ));
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Evalúa qué transición de estado debería ocurrir basándose en la labor realizada
     * y el historial de labores del lote
     */
    private EstadoLote evaluarTransicion(Plot lote, Labor laborActual) {
        EstadoLote estadoActual = lote.getEstado();
        Labor.TipoLabor tipoLabor = laborActual.getTipoLabor();
        
        // SIEMBRA y COSECHA ya cambian automáticamente el estado en sus servicios respectivos
        // Aquí manejamos las transiciones para otras labores
        
        switch (estadoActual) {
            case DISPONIBLE:
                return evaluarDesdeDisponible(lote, tipoLabor);
                
            case EN_PREPARACION:
                return evaluarDesdeEnPreparacion(lote, tipoLabor);
                
            case SEMBRADO:
                return evaluarDesdeSembrado(lote, tipoLabor);
                
            case EN_CRECIMIENTO:
                return evaluarDesdeEnCrecimiento(lote, tipoLabor);
                
            case EN_FLORACION:
                return evaluarDesdeEnFloracion(lote, tipoLabor);
                
            case EN_FRUTIFICACION:
                return evaluarDesdeEnFrutificacion(lote, tipoLabor);
                
            case COSECHADO:
                return evaluarDesdeCosechado(lote, tipoLabor);
                
            case EN_DESCANSO:
                return evaluarDesdeEnDescanso(lote, tipoLabor);
                
            case ENFERMO:
                return evaluarDesdeEnfermo(lote, tipoLabor);
                
            case ABANDONADO:
                return evaluarDesdeAbandonado(lote, tipoLabor);
                
            default:
                return null; // Sin cambio
        }
    }
    
    /**
     * Evalúa transición desde DISPONIBLE
     * Regla: Primera labor de preparación (arado/rastra) → EN_PREPARACION
     */
    private EstadoLote evaluarDesdeDisponible(Plot lote, Labor.TipoLabor tipoLabor) {
        if (tipoLabor == Labor.TipoLabor.MANTENIMIENTO) {
            // Consideramos que MANTENIMIENTO incluye arado/rastra
            return EstadoLote.EN_PREPARACION;
        }
        
        if (tipoLabor == Labor.TipoLabor.FERTILIZACION) {
            // Fertilización inicial también inicia la preparación
            return EstadoLote.EN_PREPARACION;
        }
        
        return null; // Sin cambio
    }
    
    /**
     * Evalúa transición desde EN_PREPARACION
     * Regla: Cuando se completan labores de preparación (arado + rastra) → PREPARADO
     */
    private EstadoLote evaluarDesdeEnPreparacion(Plot lote, Labor.TipoLabor tipoLabor) {
        // Obtener labores de preparación del lote
        List<Labor> laboresPreparacion = laborRepository.findByLoteAndTipoLaborAndEstado(
            lote, 
            Labor.TipoLabor.MANTENIMIENTO,
            Labor.EstadoLabor.COMPLETADA
        );
        
        // Si tiene al menos 2 labores de preparación completadas → PREPARADO
        if (laboresPreparacion.size() >= 2) {
            return EstadoLote.PREPARADO;
        }
        
        // Si tiene 1 labor de mantenimiento + 1 fertilización → PREPARADO
        List<Labor> laboresFertilizacion = laborRepository.findByLoteAndTipoLaborAndEstado(
            lote,
            Labor.TipoLabor.FERTILIZACION,
            Labor.EstadoLabor.COMPLETADA
        );
        
        if (laboresPreparacion.size() >= 1 && laboresFertilizacion.size() >= 1) {
            return EstadoLote.PREPARADO;
        }
        
        return null; // Aún no está listo
    }
    
    /**
     * Evalúa transición desde SEMBRADO
     * Regla: Basado en días desde siembra o labores de desarrollo
     */
    private EstadoLote evaluarDesdeSembrado(Plot lote, Labor.TipoLabor tipoLabor) {
        // Verificar días desde siembra
        if (lote.getFechaSiembra() != null) {
            long diasDesdeSiembra = java.time.temporal.ChronoUnit.DAYS.between(
                lote.getFechaSiembra(), 
                LocalDate.now()
            );
            
            // Después de 15-20 días típicamente pasa a EN_CRECIMIENTO
            if (diasDesdeSiembra >= 15) {
                return EstadoLote.EN_CRECIMIENTO;
            }
        }
        
        // Si se realizan labores de desarrollo (riego, fertilización) y han pasado al menos 7 días
        if (lote.getFechaSiembra() != null) {
            long diasDesdeSiembra = java.time.temporal.ChronoUnit.DAYS.between(
                lote.getFechaSiembra(), 
                LocalDate.now()
            );
            
            if (diasDesdeSiembra >= 7 && 
                (tipoLabor == Labor.TipoLabor.RIEGO || 
                 tipoLabor == Labor.TipoLabor.FERTILIZACION)) {
                return EstadoLote.EN_CRECIMIENTO;
            }
        }
        
        return null;
    }
    
    /**
     * Evalúa transición desde EN_CRECIMIENTO
     * Regla: Basado en días desde siembra
     */
    private EstadoLote evaluarDesdeEnCrecimiento(Plot lote, Labor.TipoLabor tipoLabor) {
        if (lote.getFechaSiembra() != null) {
            long diasDesdeSiembra = java.time.temporal.ChronoUnit.DAYS.between(
                lote.getFechaSiembra(), 
                LocalDate.now()
            );
            
            // Después de 40-50 días típicamente pasa a EN_FLORACION
            // (varía según el cultivo, esto es un promedio)
            if (diasDesdeSiembra >= 45) {
                return EstadoLote.EN_FLORACION;
            }
        }
        
        return null;
    }
    
    /**
     * Evalúa transición desde EN_FLORACION
     * Regla: Basado en días en floración
     */
    private EstadoLote evaluarDesdeEnFloracion(Plot lote, Labor.TipoLabor tipoLabor) {
        if (lote.getFechaSiembra() != null) {
            long diasDesdeSiembra = java.time.temporal.ChronoUnit.DAYS.between(
                lote.getFechaSiembra(), 
                LocalDate.now()
            );
            
            // Después de 60-70 días pasa a EN_FRUTIFICACION
            if (diasDesdeSiembra >= 65) {
                return EstadoLote.EN_FRUTIFICACION;
            }
        }
        
        return null;
    }
    
    /**
     * Evalúa transición desde EN_FRUTIFICACION
     * Regla: Basado en días desde siembra (cerca de la cosecha)
     */
    private EstadoLote evaluarDesdeEnFrutificacion(Plot lote, Labor.TipoLabor tipoLabor) {
        if (lote.getFechaSiembra() != null) {
            long diasDesdeSiembra = java.time.temporal.ChronoUnit.DAYS.between(
                lote.getFechaSiembra(), 
                LocalDate.now()
            );
            
            // Después de 100-110 días típicamente está LISTO_PARA_COSECHA
            if (diasDesdeSiembra >= 100) {
                return EstadoLote.LISTO_PARA_COSECHA;
            }
        }
        
        return null;
    }
    
    /**
     * Evalúa transición desde COSECHADO
     * Regla: Primera labor de preparación → EN_PREPARACION
     */
    private EstadoLote evaluarDesdeCosechado(Plot lote, Labor.TipoLabor tipoLabor) {
        if (tipoLabor == Labor.TipoLabor.MANTENIMIENTO) {
            // Arado/rastra después de cosecha → EN_PREPARACION
            return EstadoLote.EN_PREPARACION;
        }
        
        return null;
    }
    
    /**
     * Evalúa transición desde EN_DESCANSO
     * Regla: Después del período de descanso + labor de preparación → EN_PREPARACION
     */
    private EstadoLote evaluarDesdeEnDescanso(Plot lote, Labor.TipoLabor tipoLabor) {
        if (lote.getFechaCosechaReal() != null) {
            long diasDesdeDescanso = java.time.temporal.ChronoUnit.DAYS.between(
                lote.getFechaCosechaReal(), 
                LocalDate.now()
            );
            
            // Después de 30 días de descanso + labor de preparación
            if (diasDesdeDescanso >= 30 && tipoLabor == Labor.TipoLabor.MANTENIMIENTO) {
                return EstadoLote.EN_PREPARACION;
            }
        }
        
        return null;
    }
    
    /**
     * Evalúa transición desde ENFERMO
     * Regla: Después de tratamientos, vuelve al estado de cultivo correspondiente
     */
    private EstadoLote evaluarDesdeEnfermo(Plot lote, Labor.TipoLabor tipoLabor) {
        // Contar labores de tratamiento
        List<Labor> laborTratamiento = laborRepository.findByLoteAndTipoLaborInAndEstado(
            lote,
            List.of(Labor.TipoLabor.CONTROL_PLAGAS, Labor.TipoLabor.CONTROL_MALEZAS),
            Labor.EstadoLabor.COMPLETADA
        );
        
        // Si tiene al menos 2 tratamientos, asumimos recuperación
        if (laborTratamiento.size() >= 2) {
            // Determinar estado basado en días desde siembra
            if (lote.getFechaSiembra() != null) {
                long diasDesdeSiembra = java.time.temporal.ChronoUnit.DAYS.between(
                    lote.getFechaSiembra(), 
                    LocalDate.now()
                );
                
                if (diasDesdeSiembra < 30) return EstadoLote.SEMBRADO;
                if (diasDesdeSiembra < 60) return EstadoLote.EN_CRECIMIENTO;
                if (diasDesdeSiembra < 80) return EstadoLote.EN_FLORACION;
                if (diasDesdeSiembra < 110) return EstadoLote.EN_FRUTIFICACION;
                return EstadoLote.LISTO_PARA_COSECHA;
            }
        }
        
        return null;
    }
    
    /**
     * Evalúa transición desde ABANDONADO
     * Regla: Primera labor significativa → EN_PREPARACION o estado correspondiente
     */
    private EstadoLote evaluarDesdeAbandonado(Plot lote, Labor.TipoLabor tipoLabor) {
        if (tipoLabor == Labor.TipoLabor.MANTENIMIENTO) {
            // Reactivación con preparación
            return EstadoLote.EN_PREPARACION;
        }
        
        return null;
    }
    
    /**
     * Verifica si un lote requiere atención basado en su estado y tiempo
     */
    public boolean requiereAtencion(Plot lote) {
        if (lote.getEstado() == EstadoLote.LISTO_PARA_COSECHA) {
            // Alerta si está listo para cosechar por más de 15 días
            long diasEnEstado = lote.calcularDiasDesdeUltimoCambioEstado();
            return diasEnEstado > 15;
        }
        
        // Estados que requieren atención especial
        return lote.getEstado() == EstadoLote.ENFERMO || 
               lote.getEstado() == EstadoLote.ABANDONADO;
    }
}


