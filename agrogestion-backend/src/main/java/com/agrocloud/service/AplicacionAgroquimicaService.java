package com.agrocloud.service;

import com.agrocloud.dto.AplicacionAgroquimicaDTO;
import com.agrocloud.dto.CrearAplicacionAgroquimicaRequest;
import com.agrocloud.model.entity.AplicacionAgroquimica;
import com.agrocloud.model.entity.Insumo;
import com.agrocloud.model.entity.Labor;
import com.agrocloud.model.entity.Plot;
import com.agrocloud.repository.AplicacionAgroquimicaRepository;
import com.agrocloud.repository.InsumoRepository;
import com.agrocloud.repository.LaborRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AplicacionAgroquimicaService {

    @Autowired
    private AplicacionAgroquimicaRepository aplicacionRepository;

    @Autowired
    private LaborRepository laborRepository;

    @Autowired
    private InsumoRepository insumoRepository;

    @Autowired
    private DosisAplicacionService dosisAplicacionService;

    /**
     * Obtener todas las aplicaciones activas
     */
    public List<AplicacionAgroquimicaDTO> getAllAplicaciones() {
        System.out.println("🔍 [AplicacionAgroquimicaService] getAllAplicaciones - INICIO");
        try {
            System.out.println("🔍 [AplicacionAgroquimicaService] Buscando aplicaciones activas...");
            List<AplicacionAgroquimica> aplicaciones = aplicacionRepository.findByActivoTrue();
            System.out.println("🔍 [AplicacionAgroquimicaService] Aplicaciones encontradas: " + aplicaciones.size());
            System.out.println("🔍 [AplicacionAgroquimicaService] Convirtiendo a DTO...");
            List<AplicacionAgroquimicaDTO> resultado = aplicaciones.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            System.out.println("🔍 [AplicacionAgroquimicaService] Conversión completada. DTOs: " + resultado.size());
            return resultado;
        } catch (Exception e) {
            System.err.println("❌ [AplicacionAgroquimicaService] ERROR en getAllAplicaciones: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Obtener aplicaciones por labor (tarea)
     */
    public List<AplicacionAgroquimicaDTO> getAplicacionesByLabor(Long laborId) {
        Labor labor = laborRepository.findById(laborId)
                .orElseThrow(() -> new RuntimeException("Labor no encontrada con ID: " + laborId));
        
        List<AplicacionAgroquimica> aplicaciones = aplicacionRepository.findByLaborAndActivoTrue(labor);
        return aplicaciones.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener aplicaciones por insumo
     */
    public List<AplicacionAgroquimicaDTO> getAplicacionesByInsumo(Long insumoId) {
        Insumo insumo = insumoRepository.findById(insumoId)
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado con ID: " + insumoId));
        
        List<AplicacionAgroquimica> aplicaciones = aplicacionRepository.findByInsumoAndActivoTrue(insumo);
        return aplicaciones.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener una aplicación por ID
     */
    public Optional<AplicacionAgroquimicaDTO> getAplicacionById(Long id) {
        return aplicacionRepository.findById(id)
                .map(this::convertToDTO);
    }

    /**
     * Crear una nueva aplicación de agroquímico
     * Con validación de stock y cálculo automático de cantidad
     */
    public AplicacionAgroquimicaDTO createAplicacion(CrearAplicacionAgroquimicaRequest request) {
        System.out.println("[APLICACION_AGROQUIMICA_SERVICE] Creando nueva aplicación");
        
        // 1. Obtener la labor (tarea)
        Labor labor = laborRepository.findById(request.getLaborId())
                .orElseThrow(() -> new RuntimeException("Labor no encontrada con ID: " + request.getLaborId()));
        
        System.out.println("[APLICACION_AGROQUIMICA_SERVICE] Labor encontrada: " + labor.getId());
        
        // 2. Obtener el insumo
        Insumo insumo = insumoRepository.findById(request.getInsumoId())
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado con ID: " + request.getInsumoId()));
        
        System.out.println("[APLICACION_AGROQUIMICA_SERVICE] Insumo encontrado: " + insumo.getNombre());
        
        // 3. Obtener el lote asociado a la labor para calcular la superficie
        Plot lote = labor.getLote();
        if (lote == null) {
            throw new RuntimeException("La labor no tiene un lote asociado");
        }
        
        BigDecimal superficieHa = lote.getAreaHectareas();
        System.out.println("[APLICACION_AGROQUIMICA_SERVICE] Superficie del lote: " + superficieHa + " ha");
        
        // 4. Calcular la cantidad total a aplicar si no se proporciona
        BigDecimal cantidadTotalAplicar = request.getCantidadTotalAplicar();
        BigDecimal dosisAplicadaPorHa = request.getDosisAplicadaPorHa();
        
        if (cantidadTotalAplicar == null && dosisAplicadaPorHa != null) {
            // Calcular cantidad total = superficie * dosis por hectárea
            cantidadTotalAplicar = superficieHa.multiply(dosisAplicadaPorHa);
            System.out.println("[APLICACION_AGROQUIMICA_SERVICE] Cantidad calculada: " + cantidadTotalAplicar);
        } else if (cantidadTotalAplicar == null) {
            // Intentar obtener dosis sugerida del insumo
            var dosisSugerida = dosisAplicacionService.sugerirDosis(insumo.getId(), request.getTipoAplicacion());
            dosisAplicadaPorHa = dosisSugerida.getDosisPorHa();
            cantidadTotalAplicar = superficieHa.multiply(dosisAplicadaPorHa);
            System.out.println("[APLICACION_AGROQUIMICA_SERVICE] Usando dosis sugerida: " + dosisAplicadaPorHa);
        }
        
        if (cantidadTotalAplicar == null) {
            throw new RuntimeException("No se pudo calcular la cantidad total a aplicar");
        }
        
        // 5. Validar que el stock sea suficiente
        if (insumo.getStockActual().compareTo(cantidadTotalAplicar) < 0) {
            throw new RuntimeException(
                String.format("Stock insuficiente. Stock actual: %s, Cantidad requerida: %s",
                    insumo.getStockActual(), cantidadTotalAplicar)
            );
        }
        
        System.out.println("[APLICACION_AGROQUIMICA_SERVICE] Stock validado correctamente");
        
        // 6. Crear la aplicación
        AplicacionAgroquimica aplicacion = new AplicacionAgroquimica();
        aplicacion.setLabor(labor);
        aplicacion.setInsumo(insumo);
        aplicacion.setTipoAplicacion(request.getTipoAplicacion());
        aplicacion.setCantidadTotalAplicar(cantidadTotalAplicar);
        aplicacion.setDosisAplicadaPorHa(dosisAplicadaPorHa);
        aplicacion.setSuperficieAplicadaHa(superficieHa);
        aplicacion.setUnidadMedida(request.getUnidadMedida() != null ? request.getUnidadMedida() : insumo.getUnidadMedida());
        aplicacion.setObservaciones(request.getObservaciones());
        aplicacion.setFechaAplicacion(request.getFechaAplicacion() != null ? request.getFechaAplicacion() : LocalDateTime.now());
        aplicacion.setActivo(true);
        
        AplicacionAgroquimica saved = aplicacionRepository.save(aplicacion);
        System.out.println("[APLICACION_AGROQUIMICA_SERVICE] Aplicación guardada: " + saved.getId());
        
        // 7. Descontar del stock del insumo
        BigDecimal nuevoStock = insumo.getStockActual().subtract(cantidadTotalAplicar);
        insumo.setStockActual(nuevoStock);
        insumoRepository.save(insumo);
        
        System.out.println("[APLICACION_AGROQUIMICA_SERVICE] Stock actualizado: " + nuevoStock);
        
        return convertToDTO(saved);
    }

    /**
     * Eliminar una aplicación (eliminación lógica)
     */
    public boolean deleteAplicacion(Long id) {
        AplicacionAgroquimica aplicacion = aplicacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aplicación no encontrada con ID: " + id));
        
        // Restaurar el stock del insumo
        Insumo insumo = aplicacion.getInsumo();
        BigDecimal nuevoStock = insumo.getStockActual().add(aplicacion.getCantidadTotalAplicar());
        insumo.setStockActual(nuevoStock);
        insumoRepository.save(insumo);
        
        // Eliminar lógicamente la aplicación
        aplicacion.setActivo(false);
        aplicacionRepository.save(aplicacion);
        
        return true;
    }

    /**
     * Obtener estadísticas de aplicaciones por insumo
     */
    public java.util.Map<String, Object> getEstadisticasByInsumo(Long insumoId) {
        Insumo insumo = insumoRepository.findById(insumoId)
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado con ID: " + insumoId));
        
        List<AplicacionAgroquimica> aplicaciones = aplicacionRepository.findByInsumoAndActivoTrue(insumo);
        
        Double totalAplicado = aplicacionRepository.sumCantidadTotalByInsumo(insumo);
        long vecesUtilizado = aplicaciones.size();
        
        java.util.Map<String, Object> estadisticas = new java.util.HashMap<>();
        estadisticas.put("insumoId", insumoId);
        estadisticas.put("insumoNombre", insumo.getNombre());
        estadisticas.put("vecesUtilizado", vecesUtilizado);
        estadisticas.put("totalAplicado", totalAplicado != null ? totalAplicado : 0);
        estadisticas.put("stockActual", insumo.getStockActual());
        estadisticas.put("aplicaciones", aplicaciones.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));
        
        return estadisticas;
    }

    /**
     * Convertir entidad a DTO
     */
    private AplicacionAgroquimicaDTO convertToDTO(AplicacionAgroquimica aplicacion) {
        System.out.println("🔍 [AplicacionAgroquimicaService] convertToDTO - INICIO para ID: " + aplicacion.getId());
        try {
            AplicacionAgroquimicaDTO dto = new AplicacionAgroquimicaDTO();
            System.out.println("🔍 [AplicacionAgroquimicaService] DTO creado");
            
            dto.setId(aplicacion.getId());
            System.out.println("🔍 [AplicacionAgroquimicaService] ID establecido: " + aplicacion.getId());
            
            System.out.println("🔍 [AplicacionAgroquimicaService] Accediendo a labor...");
            dto.setLaborId(aplicacion.getLabor().getId());
            dto.setLaborNombre(aplicacion.getLabor().getTipoLabor().toString());
            System.out.println("🔍 [AplicacionAgroquimicaService] Labor procesada: " + aplicacion.getLabor().getId());
            
            System.out.println("🔍 [AplicacionAgroquimicaService] Accediendo a insumo...");
            dto.setInsumoId(aplicacion.getInsumo().getId());
            dto.setInsumoNombre(aplicacion.getInsumo().getNombre());
            System.out.println("🔍 [AplicacionAgroquimicaService] Insumo procesado: " + aplicacion.getInsumo().getId());
            
            dto.setTipoAplicacion(aplicacion.getTipoAplicacion());
            dto.setCantidadTotalAplicar(aplicacion.getCantidadTotalAplicar());
            dto.setDosisAplicadaPorHa(aplicacion.getDosisAplicadaPorHa());
            dto.setSuperficieAplicadaHa(aplicacion.getSuperficieAplicadaHa());
            dto.setUnidadMedida(aplicacion.getUnidadMedida());
            dto.setObservaciones(aplicacion.getObservaciones());
            dto.setFechaAplicacion(aplicacion.getFechaAplicacion());
            dto.setFechaRegistro(aplicacion.getFechaRegistro());
            dto.setActivo(aplicacion.getActivo());
            
            System.out.println("🔍 [AplicacionAgroquimicaService] convertToDTO - COMPLETADO para ID: " + aplicacion.getId());
            return dto;
        } catch (Exception e) {
            System.err.println("❌ [AplicacionAgroquimicaService] ERROR en convertToDTO para ID " + aplicacion.getId() + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}

