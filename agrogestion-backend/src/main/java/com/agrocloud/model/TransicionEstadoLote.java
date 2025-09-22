package com.agrocloud.model;

import com.agrocloud.model.enums.EstadoLote;
import java.util.*;

/**
 * Clase que define las transiciones válidas entre estados de lotes.
 * Contiene la lógica de negocio para validar cambios de estado.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
public class TransicionEstadoLote {
    
    // Mapa de transiciones válidas desde cada estado
    private static final Map<EstadoLote, List<EstadoLote>> TRANSICIONES_VALIDAS = new HashMap<>();
    
    static {
        // Estados iniciales
        TRANSICIONES_VALIDAS.put(EstadoLote.DISPONIBLE, Arrays.asList(
            EstadoLote.PREPARADO, 
            EstadoLote.SEMBRADO
        ));
        
        TRANSICIONES_VALIDAS.put(EstadoLote.PREPARADO, Arrays.asList(
            EstadoLote.SEMBRADO, 
            EstadoLote.DISPONIBLE
        ));
        
        // Estados de cultivo
        TRANSICIONES_VALIDAS.put(EstadoLote.SEMBRADO, Arrays.asList(
            EstadoLote.EN_CRECIMIENTO, 
            EstadoLote.ENFERMO
        ));
        
        TRANSICIONES_VALIDAS.put(EstadoLote.EN_CRECIMIENTO, Arrays.asList(
            EstadoLote.EN_FLORACION, 
            EstadoLote.ENFERMO
        ));
        
        TRANSICIONES_VALIDAS.put(EstadoLote.EN_FLORACION, Arrays.asList(
            EstadoLote.EN_FRUTIFICACION, 
            EstadoLote.ENFERMO
        ));
        
        TRANSICIONES_VALIDAS.put(EstadoLote.EN_FRUTIFICACION, Arrays.asList(
            EstadoLote.LISTO_PARA_COSECHA, 
            EstadoLote.ENFERMO
        ));
        
        // Estados de cosecha
        TRANSICIONES_VALIDAS.put(EstadoLote.LISTO_PARA_COSECHA, Arrays.asList(
            EstadoLote.EN_COSECHA, 
            EstadoLote.ENFERMO
        ));
        
        TRANSICIONES_VALIDAS.put(EstadoLote.EN_COSECHA, Arrays.asList(
            EstadoLote.COSECHADO, 
            EstadoLote.ENFERMO
        ));
        
        TRANSICIONES_VALIDAS.put(EstadoLote.COSECHADO, Arrays.asList(
            EstadoLote.EN_DESCANSO, 
            EstadoLote.EN_PREPARACION
        ));
        
        // Estados de post-cosecha
        TRANSICIONES_VALIDAS.put(EstadoLote.EN_DESCANSO, Arrays.asList(
            EstadoLote.EN_PREPARACION, 
            EstadoLote.DISPONIBLE
        ));
        
        TRANSICIONES_VALIDAS.put(EstadoLote.EN_PREPARACION, Arrays.asList(
            EstadoLote.DISPONIBLE, 
            EstadoLote.PREPARADO
        ));
        
        // Estados especiales
        TRANSICIONES_VALIDAS.put(EstadoLote.ENFERMO, Arrays.asList(
            EstadoLote.ABANDONADO, 
            EstadoLote.EN_CRECIMIENTO, // Recuperación
            EstadoLote.EN_FLORACION,   // Recuperación
            EstadoLote.EN_FRUTIFICACION // Recuperación
        ));
        
        TRANSICIONES_VALIDAS.put(EstadoLote.ABANDONADO, Arrays.asList(
            EstadoLote.EN_PREPARACION // Rehabilitación
        ));
    }
    
    /**
     * Verifica si una transición entre dos estados es válida
     * 
     * @param estadoActual Estado actual del lote
     * @param estadoNuevo Estado al que se quiere cambiar
     * @return true si la transición es válida, false en caso contrario
     */
    public static boolean esTransicionValida(EstadoLote estadoActual, EstadoLote estadoNuevo) {
        if (estadoActual == null || estadoNuevo == null) {
            return false;
        }
        
        List<EstadoLote> estadosValidos = TRANSICIONES_VALIDAS.get(estadoActual);
        return estadosValidos != null && estadosValidos.contains(estadoNuevo);
    }
    
    /**
     * Obtiene la lista de estados válidos desde un estado dado
     * 
     * @param estadoActual Estado actual del lote
     * @return Lista de estados válidos para transición
     */
    public static List<EstadoLote> getEstadosValidos(EstadoLote estadoActual) {
        if (estadoActual == null) {
            return Collections.emptyList();
        }
        
        return TRANSICIONES_VALIDAS.getOrDefault(estadoActual, Collections.emptyList());
    }
    
    /**
     * Obtiene el mensaje de error para una transición inválida
     * 
     * @param estadoActual Estado actual del lote
     * @param estadoNuevo Estado al que se quiere cambiar
     * @return Mensaje de error descriptivo
     */
    public static String getMensajeErrorTransicion(EstadoLote estadoActual, EstadoLote estadoNuevo) {
        if (estadoActual == null || estadoNuevo == null) {
            return "Estado inválido";
        }
        
        List<EstadoLote> estadosValidos = getEstadosValidos(estadoActual);
        
        StringBuilder mensaje = new StringBuilder();
        mensaje.append("No se puede cambiar de '").append(estadoActual.getDescripcion())
               .append("' a '").append(estadoNuevo.getDescripcion()).append("'.\n\n");
        
        if (estadosValidos.isEmpty()) {
            mensaje.append("No hay transiciones válidas desde este estado.");
        } else {
            mensaje.append("Estados válidos desde '").append(estadoActual.getDescripcion()).append("':\n");
            for (EstadoLote estado : estadosValidos) {
                mensaje.append("• ").append(estado.getDescripcion()).append("\n");
            }
        }
        
        return mensaje.toString();
    }
    
    /**
     * Obtiene todas las transiciones válidas del sistema
     * 
     * @return Mapa con todas las transiciones válidas
     */
    public static Map<EstadoLote, List<EstadoLote>> getTodasLasTransiciones() {
        return new HashMap<>(TRANSICIONES_VALIDAS);
    }
    
    /**
     * Verifica si un estado es terminal (no permite más transiciones)
     * 
     * @param estado Estado a verificar
     * @return true si es terminal, false en caso contrario
     */
    public static boolean esEstadoTerminal(EstadoLote estado) {
        List<EstadoLote> estadosValidos = TRANSICIONES_VALIDAS.get(estado);
        return estadosValidos == null || estadosValidos.isEmpty();
    }
}
