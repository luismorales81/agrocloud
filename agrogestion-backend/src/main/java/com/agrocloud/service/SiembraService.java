package com.agrocloud.service;

import com.agrocloud.dto.*;
import com.agrocloud.exception.ResourceNotFoundException;
import com.agrocloud.exception.BadRequestException;
import com.agrocloud.model.entity.*;
import com.agrocloud.model.entity.Labor.TipoLabor;
import com.agrocloud.model.entity.Labor.EstadoLabor;
import com.agrocloud.model.enums.EstadoLote;
import com.agrocloud.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class SiembraService {
    
    @Autowired
    private PlotRepository plotRepository;
    
    @Autowired
    private CultivoRepository cultivoRepository;
    
    @Autowired
    private LaborRepository laborRepository;
    
    @Autowired
    private LaborInsumoRepository laborInsumoRepository;
    
    @Autowired
    private LaborMaquinariaRepository laborMaquinariaRepository;
    
    @Autowired
    private LaborManoObraRepository laborManoObraRepository;
    
    @Autowired
    private InventarioService inventarioService;
    
    @Autowired
    private InsumoRepository insumoRepository;
    
    @Autowired
    private HistorialCosechaRepository historialCosechaRepository;
    
    /**
     * Realiza la siembra de un lote
     */
    public Plot sembrarLote(Long loteId, SiembraRequest request, User usuario, Empresa empresa) {
        // El usuario y empresa son pasados como parámetros
        
        // Validar que el lote existe
        Plot lote = plotRepository.findById(loteId)
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado con ID: " + loteId));
        
        // Validar que el lote pertenece a la empresa
        if (!lote.getCampo().getEmpresa().getId().equals(empresa.getId())) {
            throw new BadRequestException("El lote no pertenece a la empresa actual");
        }
        
        // Validar que el lote esté en estado apropiado para sembrar
        if (lote.getEstado() != EstadoLote.DISPONIBLE && 
            lote.getEstado() != EstadoLote.PREPARADO &&
            lote.getEstado() != EstadoLote.EN_PREPARACION) {
            throw new BadRequestException(
                "El lote debe estar en estado DISPONIBLE, PREPARADO o EN_PREPARACION para sembrar. " +
                "Estado actual: " + lote.getEstado()
            );
        }
        
        // Validar que el cultivo existe
        Cultivo cultivo = cultivoRepository.findById(request.getCultivoId())
                .orElseThrow(() -> new ResourceNotFoundException("Cultivo no encontrado con ID: " + request.getCultivoId()));
        
        // Crear la labor de siembra
        Labor laborSiembra = new Labor();
        laborSiembra.setTipoLabor(TipoLabor.SIEMBRA);
        laborSiembra.setDescripcion("Siembra de " + cultivo.getNombre() + " en lote " + lote.getNombre());
        laborSiembra.setFechaInicio(request.getFechaSiembra());
        laborSiembra.setFechaFin(request.getFechaSiembra());
        laborSiembra.setEstado(EstadoLabor.COMPLETADA);
        laborSiembra.setResponsable(usuario.getEmail());
        laborSiembra.setLote(lote);
        laborSiembra.setUsuario(usuario);
        laborSiembra.setActivo(true);
        laborSiembra.setObservaciones(request.getObservaciones());
        
        // Calcular costo total
        BigDecimal costoTotal = BigDecimal.ZERO;
        
        // Guardar la labor primero para obtener el ID
        laborSiembra = laborRepository.save(laborSiembra);
        
        // Procesar insumos usados y descontar del inventario
        java.util.List<LaborInsumo> insumosGuardados = new java.util.ArrayList<>();
        if (request.getInsumos() != null && !request.getInsumos().isEmpty()) {
            for (InsumoUsadoDTO insumoDTO : request.getInsumos()) {
                Insumo insumo = insumoRepository.findById(insumoDTO.getInsumoId())
                        .orElseThrow(() -> new ResourceNotFoundException("Insumo no encontrado con ID: " + insumoDTO.getInsumoId()));
                
                LaborInsumo laborInsumo = new LaborInsumo();
                laborInsumo.setLabor(laborSiembra);
                laborInsumo.setInsumo(insumo);
                laborInsumo.setCantidadUsada(insumoDTO.getCantidadUsada());
                laborInsumo.setCantidadPlanificada(insumoDTO.getCantidadUsada());
                laborInsumo.setCostoUnitario(insumo.getPrecioUnitario());
                laborInsumo.setCostoTotal(insumoDTO.getCantidadUsada().multiply(insumo.getPrecioUnitario()));
                
                laborInsumoRepository.save(laborInsumo);
                insumosGuardados.add(laborInsumo);
                costoTotal = costoTotal.add(laborInsumo.getCostoTotal());
            }
            
            // Actualizar inventario (descontar stock)
            inventarioService.actualizarInventarioLabor(laborSiembra.getId(), insumosGuardados, null, usuario);
        }
        
        // Procesar maquinaria
        if (request.getMaquinaria() != null && !request.getMaquinaria().isEmpty()) {
            for (MaquinariaAsignadaDTO maqDTO : request.getMaquinaria()) {
                LaborMaquinaria laborMaquinaria = new LaborMaquinaria();
                laborMaquinaria.setLabor(laborSiembra);
                laborMaquinaria.setDescripcion(maqDTO.getDescripcion());
                laborMaquinaria.setProveedor(maqDTO.getProveedor());
                laborMaquinaria.setTipoMaquinaria(maqDTO.getTipoMaquinaria());
                laborMaquinaria.setCosto(maqDTO.getCostoTotal());
                laborMaquinaria.setObservaciones(maqDTO.getObservaciones());
                
                laborMaquinariaRepository.save(laborMaquinaria);
                costoTotal = costoTotal.add(maqDTO.getCostoTotal());
            }
        }
        
        // Procesar mano de obra
        if (request.getManoObra() != null && !request.getManoObra().isEmpty()) {
            for (ManoObraDTO moDTO : request.getManoObra()) {
                LaborManoObra laborManoObra = new LaborManoObra();
                laborManoObra.setLabor(laborSiembra);
                laborManoObra.setDescripcion(moDTO.getDescripcion());
                laborManoObra.setCantidadPersonas(moDTO.getCantidadPersonas());
                laborManoObra.setProveedor(moDTO.getProveedor());
                laborManoObra.setCostoTotal(moDTO.getCostoTotal());
                laborManoObra.setHorasTrabajo(moDTO.getHorasTrabajo());
                laborManoObra.setObservaciones(moDTO.getObservaciones());
                
                laborManoObraRepository.save(laborManoObra);
                costoTotal = costoTotal.add(moDTO.getCostoTotal());
            }
        }
        
        // Actualizar costo total de la labor
        laborSiembra.setCostoTotal(costoTotal);
        laborRepository.save(laborSiembra);
        
        // Actualizar el lote
        lote.setEstado(EstadoLote.SEMBRADO);
        lote.setCultivoActual(cultivo.getNombre());
        lote.setFechaSiembra(request.getFechaSiembra());
        lote.setFechaCosechaEsperada(request.getFechaSiembra().plusDays(120)); // Estimado 120 días
        
        // Guardar densidad de siembra en descripción si se proporcionó
        if (request.getDensidadSiembra() != null) {
            String descripcionActual = lote.getDescripcion() != null ? lote.getDescripcion() + " | " : "";
            lote.setDescripcion(descripcionActual + "Densidad: " + request.getDensidadSiembra() + " plantas/ha");
        }
        
        return plotRepository.save(lote);
    }
    
    /**
     * Realiza la cosecha de un lote
     */
    public Plot cosecharLote(Long loteId, CosechaRequest request, User usuario, Empresa empresa) {
        // El usuario y empresa son pasados como parámetros
        
        // Validar que el lote existe
        Plot lote = plotRepository.findById(loteId)
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado con ID: " + loteId));
        
        // Validar que el lote pertenece a la empresa
        if (!lote.getCampo().getEmpresa().getId().equals(empresa.getId())) {
            throw new BadRequestException("El lote no pertenece a la empresa actual");
        }
        
        // Validar que el lote esté en estado apropiado para cosechar
        if (!lote.puedeCosechar()) {
            throw new BadRequestException(
                "El lote no puede ser cosechado en su estado actual. " +
                "Estado actual: " + lote.getEstado() + ". " +
                "Estados válidos: SEMBRADO, EN_CRECIMIENTO, EN_FLORACION, EN_FRUTIFICACION, LISTO_PARA_COSECHA"
            );
        }
        
        // Crear la labor de cosecha
        Labor laborCosecha = new Labor();
        laborCosecha.setTipoLabor(TipoLabor.COSECHA);
        laborCosecha.setDescripcion("Cosecha de " + lote.getCultivoActual() + " en lote " + lote.getNombre());
        laborCosecha.setFechaInicio(request.getFechaCosecha());
        laborCosecha.setFechaFin(request.getFechaCosecha());
        laborCosecha.setEstado(EstadoLabor.COMPLETADA);
        laborCosecha.setResponsable(usuario.getEmail());
        laborCosecha.setLote(lote);
        laborCosecha.setUsuario(usuario);
        laborCosecha.setActivo(true);
        laborCosecha.setObservaciones(
            String.format("Cantidad cosechada: %s %s | Estado suelo: %s%s",
                request.getCantidadCosechada(),
                request.getUnidadMedida(),
                request.getEstadoSuelo() != null ? request.getEstadoSuelo() : "BUENO",
                request.getObservaciones() != null ? " | " + request.getObservaciones() : "")
        );
        
        // Calcular costo total inicial
        BigDecimal costoTotal = BigDecimal.ZERO;
        
        // Procesar maquinaria para calcular costo total
        if (request.getMaquinaria() != null && !request.getMaquinaria().isEmpty()) {
            for (MaquinariaAsignadaDTO maqDTO : request.getMaquinaria()) {
                if (maqDTO.getCostoTotal() != null) {
                    costoTotal = costoTotal.add(maqDTO.getCostoTotal());
                }
            }
        }
        
        // Procesar mano de obra para calcular costo total
        if (request.getManoObra() != null && !request.getManoObra().isEmpty()) {
            for (ManoObraDTO moDTO : request.getManoObra()) {
                if (moDTO.getCostoTotal() != null) {
                    costoTotal = costoTotal.add(moDTO.getCostoTotal());
                }
            }
        }
        
        // Si no hay costos, establecer un costo mínimo para cumplir la validación
        if (costoTotal.compareTo(BigDecimal.ZERO) <= 0) {
            costoTotal = new BigDecimal("0.01"); // Costo mínimo simbólico
        }
        
        // Establecer el costo total en la labor
        laborCosecha.setCostoTotal(costoTotal);
        
        // Guardar la labor primero para obtener el ID
        laborCosecha = laborRepository.save(laborCosecha);
        
        // Procesar maquinaria (guardar registros)
        if (request.getMaquinaria() != null && !request.getMaquinaria().isEmpty()) {
            for (MaquinariaAsignadaDTO maqDTO : request.getMaquinaria()) {
                LaborMaquinaria laborMaquinaria = new LaborMaquinaria();
                laborMaquinaria.setLabor(laborCosecha);
                laborMaquinaria.setDescripcion(maqDTO.getDescripcion());
                laborMaquinaria.setProveedor(maqDTO.getProveedor());
                laborMaquinaria.setTipoMaquinaria(maqDTO.getTipoMaquinaria());
                laborMaquinaria.setCosto(maqDTO.getCostoTotal());
                laborMaquinaria.setObservaciones(maqDTO.getObservaciones());
                
                laborMaquinariaRepository.save(laborMaquinaria);
            }
        }
        
        // Procesar mano de obra (guardar registros)
        if (request.getManoObra() != null && !request.getManoObra().isEmpty()) {
            for (ManoObraDTO moDTO : request.getManoObra()) {
                LaborManoObra laborManoObra = new LaborManoObra();
                laborManoObra.setLabor(laborCosecha);
                laborManoObra.setDescripcion(moDTO.getDescripcion());
                laborManoObra.setCantidadPersonas(moDTO.getCantidadPersonas());
                laborManoObra.setProveedor(moDTO.getProveedor());
                laborManoObra.setCostoTotal(moDTO.getCostoTotal());
                laborManoObra.setHorasTrabajo(moDTO.getHorasTrabajo());
                laborManoObra.setObservaciones(moDTO.getObservaciones());
                
                laborManoObraRepository.save(laborManoObra);
            }
        }
        
        // Guardar en historial_cosechas para reportes y análisis
        HistorialCosecha historialCosecha = new HistorialCosecha();
        historialCosecha.setLote(lote);
        
        // Obtener cultivo desde el lote
        Cultivo cultivo = null;
        if (lote.getCultivoActual() != null) {
            List<Cultivo> cultivosEncontrados = cultivoRepository.findByNombreContaining(lote.getCultivoActual());
            // Buscar coincidencia exacta primero
            cultivo = cultivosEncontrados.stream()
                .filter(c -> c.getNombre().equalsIgnoreCase(lote.getCultivoActual()))
                .findFirst()
                .orElse(cultivosEncontrados.isEmpty() ? null : cultivosEncontrados.get(0));
        }
        if (cultivo == null) {
            throw new ResourceNotFoundException("No se encontró el cultivo: " + lote.getCultivoActual() + ". Asegúrese de que el lote tenga un cultivo registrado.");
        }
        
        System.out.println("🌱 [SiembraService] Cultivo encontrado para cosecha:");
        System.out.println("   - ID: " + cultivo.getId());
        System.out.println("   - Nombre: " + cultivo.getNombre());
        System.out.println("   - Variedad: " + cultivo.getVariedad());
        System.out.println("   - Empresa ID: " + (cultivo.getEmpresa() != null ? cultivo.getEmpresa().getId() : "null"));
        
        historialCosecha.setCultivo(cultivo);
        
        historialCosecha.setFechaSiembra(lote.getFechaSiembra() != null ? lote.getFechaSiembra() : request.getFechaCosecha().minusDays(120));
        historialCosecha.setFechaCosecha(request.getFechaCosecha());
        historialCosecha.setSuperficieHectareas(lote.getAreaHectareas());
        historialCosecha.setCantidadCosechada(request.getCantidadCosechada());
        historialCosecha.setUnidadCosecha(request.getUnidadMedida());
        
        // Calcular rendimiento real
        if (lote.getAreaHectareas() != null && lote.getAreaHectareas().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal rendimientoReal = request.getCantidadCosechada().divide(lote.getAreaHectareas(), 2, java.math.RoundingMode.HALF_UP);
            historialCosecha.setRendimientoReal(rendimientoReal);
            lote.setRendimientoReal(rendimientoReal);
        }
        
        historialCosecha.setRendimientoEsperado(cultivo.getRendimientoEsperado());
        historialCosecha.setVariedadSemilla(request.getVariedadSemilla());
        historialCosecha.setEstadoSuelo(request.getEstadoSuelo() != null ? request.getEstadoSuelo() : "BUENO");
        historialCosecha.setRequiereDescanso(request.getRequiereDescanso() != null ? request.getRequiereDescanso() : false);
        historialCosecha.setDiasDescansoRecomendados(request.getDiasDescansoRecomendados() != null ? request.getDiasDescansoRecomendados() : 0);
        historialCosecha.setObservaciones(request.getObservaciones());
        historialCosecha.setUsuario(usuario);
        
        // Guardar datos de rentabilidad
        if (request.getPrecioVenta() != null && request.getPrecioVenta().compareTo(BigDecimal.ZERO) > 0) {
            historialCosecha.setPrecioVentaUnitario(request.getPrecioVenta());
            // Calcular ingreso total: cantidad * precio
            BigDecimal ingresoTotal = request.getCantidadCosechada().multiply(request.getPrecioVenta());
            historialCosecha.setIngresoTotal(ingresoTotal);
        }
        
        // Guardar costo total de producción (calculado de maquinaria y mano de obra)
        if (costoTotal != null && costoTotal.compareTo(BigDecimal.ZERO) > 0) {
            historialCosecha.setCostoTotalProduccion(costoTotal);
        }
        
        System.out.println("📝 [SiembraService] Guardando historial de cosecha:");
        System.out.println("   - Lote ID: " + historialCosecha.getLote().getId());
        System.out.println("   - Cultivo ID: " + historialCosecha.getCultivo().getId());
        System.out.println("   - Usuario ID: " + historialCosecha.getUsuario().getId());
        System.out.println("   - Cantidad: " + historialCosecha.getCantidadCosechada());
        System.out.println("   - Superficie: " + historialCosecha.getSuperficieHectareas());
        
        historialCosechaRepository.save(historialCosecha);
        System.out.println("✅ Registro guardado en historial_cosechas ID: " + historialCosecha.getId());
        
        // Actualizar el lote
        lote.setEstado(EstadoLote.COSECHADO);
        lote.setFechaCosechaReal(request.getFechaCosecha());
        
        return plotRepository.save(lote);
    }
    
    /**
     * Abandona un cultivo por problemas (plagas, sequía, etc.)
     */
    public Plot abandonarCultivo(Long loteId, String motivo, User usuario, Empresa empresa) {
        // Validar que el lote existe
        Plot lote = plotRepository.findById(loteId)
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado con ID: " + loteId));
        
        // Validar que el lote pertenece a la empresa
        if (!lote.getCampo().getEmpresa().getId().equals(empresa.getId())) {
            throw new BadRequestException("El lote no pertenece a la empresa actual");
        }
        
        // Validar que tiene cultivo activo
        if (lote.getEstado() == EstadoLote.DISPONIBLE || 
            lote.getEstado() == EstadoLote.COSECHADO ||
            lote.getEstado() == EstadoLote.ABANDONADO) {
            throw new BadRequestException("El lote no tiene un cultivo activo para abandonar");
        }
        
        // Crear labor de abandono
        Labor laborAbandono = new Labor();
        laborAbandono.setTipoLabor(TipoLabor.OTROS);
        laborAbandono.setDescripcion("Abandono de cultivo " + lote.getCultivoActual() + " - Motivo: " + motivo);
        laborAbandono.setFechaInicio(java.time.LocalDate.now());
        laborAbandono.setFechaFin(java.time.LocalDate.now());
        laborAbandono.setEstado(EstadoLabor.COMPLETADA);
        laborAbandono.setResponsable(usuario.getEmail());
        laborAbandono.setLote(lote);
        laborAbandono.setUsuario(usuario);
        laborAbandono.setObservaciones("CULTIVO ABANDONADO | Motivo: " + motivo);
        laborAbandono.setCostoTotal(BigDecimal.ZERO);
        
        laborRepository.save(laborAbandono);
        
        // Cambiar estado del lote
        lote.setEstado(EstadoLote.ABANDONADO);
        lote.setMotivoCambioEstado(motivo);
        lote.setFechaUltimoCambioEstado(java.time.LocalDateTime.now());
        
        return plotRepository.save(lote);
    }
    
    /**
     * Limpia un cultivo sin cosechar (eliminar y volver a DISPONIBLE)
     */
    public Plot limpiarCultivo(Long loteId, String motivo, User usuario, Empresa empresa) {
        // Validar que el lote existe
        Plot lote = plotRepository.findById(loteId)
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado con ID: " + loteId));
        
        // Validar que el lote pertenece a la empresa
        if (!lote.getCampo().getEmpresa().getId().equals(empresa.getId())) {
            throw new BadRequestException("El lote no pertenece a la empresa actual");
        }
        
        // Validar que tiene cultivo para limpiar
        if (lote.getEstado() == EstadoLote.DISPONIBLE) {
            throw new BadRequestException("El lote ya está disponible, no hay cultivo para limpiar");
        }
        
        // Crear labor de limpieza
        Labor laborLimpieza = new Labor();
        laborLimpieza.setTipoLabor(TipoLabor.OTROS);
        laborLimpieza.setDescripcion("Limpieza de cultivo " + (lote.getCultivoActual() != null ? lote.getCultivoActual() : "sin nombre"));
        laborLimpieza.setFechaInicio(java.time.LocalDate.now());
        laborLimpieza.setFechaFin(java.time.LocalDate.now());
        laborLimpieza.setEstado(EstadoLabor.COMPLETADA);
        laborLimpieza.setResponsable(usuario.getEmail());
        laborLimpieza.setLote(lote);
        laborLimpieza.setUsuario(usuario);
        laborLimpieza.setObservaciones("LIMPIEZA DE CULTIVO | Motivo: " + motivo);
        laborLimpieza.setCostoTotal(BigDecimal.ZERO);
        
        laborRepository.save(laborLimpieza);
        
        // Resetear lote a estado DISPONIBLE
        lote.setEstado(EstadoLote.DISPONIBLE);
        lote.setCultivoActual(null);
        lote.setFechaSiembra(null);
        lote.setFechaCosechaEsperada(null);
        lote.setFechaCosechaReal(null);
        lote.setRendimientoReal(null);
        lote.setMotivoCambioEstado("Limpieza de cultivo: " + motivo);
        lote.setFechaUltimoCambioEstado(java.time.LocalDateTime.now());
        
        return plotRepository.save(lote);
    }
    
    /**
     * Convierte cultivo a forraje (cosecha anticipada para alimentación animal)
     */
    public Plot convertirAForraje(Long loteId, CosechaRequest request, User usuario, Empresa empresa) {
        // Validar que el lote existe
        Plot lote = plotRepository.findById(loteId)
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado con ID: " + loteId));
        
        // Validar que el lote pertenece a la empresa
        if (!lote.getCampo().getEmpresa().getId().equals(empresa.getId())) {
            throw new BadRequestException("El lote no pertenece a la empresa actual");
        }
        
        // Validar que tiene cultivo para convertir
        if (lote.getEstado() == EstadoLote.DISPONIBLE || 
            lote.getEstado() == EstadoLote.COSECHADO) {
            throw new BadRequestException("El lote no tiene un cultivo activo para convertir a forraje");
        }
        
        // Modificar request para indicar que es forraje
        String observacionesOriginal = request.getObservaciones() != null ? request.getObservaciones() : "";
        request.setObservaciones("CONVERSIÓN A FORRAJE | Cosecha anticipada para alimentación animal | " + observacionesOriginal);
        request.setEstadoSuelo("BUENO"); // Forraje normalmente no agota el suelo
        
        // Usar el método de cosecha existente
        return cosecharLote(loteId, request, usuario, empresa);
    }
}
