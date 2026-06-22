package com.agrocloud.cultivos.application;

import com.agrocloud.dto.LaborDetalladoDTO;
import com.agrocloud.dto.FiltrosLaboresDTO;
import com.agrocloud.dto.LaborMaquinariaDTO;
import com.agrocloud.dto.LaborManoObraDTO;
import com.agrocloud.dto.LaborInsumoDTO;
import com.agrocloud.dto.RespuestaCambioEstado;
import com.agrocloud.dto.ConfirmacionCambioEstado;
import com.agrocloud.dto.ReporteCosechaDTO;
import com.agrocloud.dto.ActualizarLaborParcialRequest;
import com.agrocloud.dto.CrearLaborRequest;
import com.agrocloud.cultivos.domain.Labor;
import com.agrocloud.cultivos.domain.LaborMaquinaria;
import com.agrocloud.cultivos.domain.LaborManoObra;
import com.agrocloud.cultivos.domain.LaborInsumo;
import com.agrocloud.cultivos.domain.Plot;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.domain.Role;
import com.agrocloud.core.inventory.domain.Insumo;
import com.agrocloud.cultivos.domain.DosisAgroquimico;
import com.agrocloud.cultivos.domain.Cultivo;
import com.agrocloud.cultivos.domain.TipoCultivo;
import com.agrocloud.cultivos.domain.EstadoLoteConfig;
import com.agrocloud.cultivos.domain.TareaPorEstadoConfig;
import com.agrocloud.model.enums.TipoMaquinaria;
import com.agrocloud.model.enums.EstadoLote;
import com.agrocloud.model.enums.Rol;
import com.agrocloud.model.enums.RolEmpresa;
import com.agrocloud.model.enums.TipoAplicacion;
import com.agrocloud.model.enums.FormaAplicacion;
import com.agrocloud.cultivos.infrastructure.LaborRepository;
import com.agrocloud.cultivos.infrastructure.LaborMaquinariaRepository;
import com.agrocloud.cultivos.infrastructure.LaborManoObraRepository;
import com.agrocloud.cultivos.infrastructure.LaborInsumoRepository;
import com.agrocloud.cultivos.infrastructure.PlotRepository;
import com.agrocloud.core.inventory.infrastructure.InsumoRepository;
import com.agrocloud.cultivos.infrastructure.DosisAgroquimicoRepository;
import com.agrocloud.cultivos.infrastructure.CultivoRepository;
import com.agrocloud.cultivos.infrastructure.EstadoLoteConfigRepository;
import com.agrocloud.cultivos.infrastructure.TareaPorEstadoConfigRepository;
import com.agrocloud.core.application.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.List;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service("laborServicioCultivos")
@Transactional
public class LaborService {

    @Autowired
    private LaborRepository laborRepository;

    @Autowired
    private PlotRepository plotRepository;
    
    @Autowired
    private LaborMaquinariaRepository laborMaquinariaRepository;
    
    @Autowired
    private LaborManoObraRepository laborManoObraRepository;
    
    @Autowired
    private LaborInsumoRepository laborInsumoRepository;
    
    @Autowired
    @Qualifier("insumoRepositoryInventario")
        private InsumoRepository insumoRepository;
    
    @Autowired
    private DosisAgroquimicoRepository dosisAgroquimicoRepository;
    
    @Autowired
    @Qualifier("inventarioServiceCultivos")
    private InventarioService inventarioService;
    
    @Autowired
    @Qualifier("transicionEstadoServiceCultivos")
    private TransicionEstadoService transicionEstadoService;

    @Autowired
    private UserService userService;
    
    
    @Autowired
    private EstadoLoteService estadoLoteService;

    @Autowired
    private EstadoLoteUpdater estadoLoteUpdater;

    @Autowired
    private ConfiguracionEstadosService configuracionEstadosService;
    
    @Autowired
    @SuppressWarnings("unused") // Reservado para futuras implementaciones
    private EstadoLoteConfigRepository estadoLoteConfigRepository;
    
    @Autowired
    private TareaPorEstadoConfigRepository tareaPorEstadoConfigRepository;
    
    @Autowired
    private CultivoRepository cultivoRepository;

    @Autowired
    private LaborQueryService laborQueryService;

    @Autowired
    @Qualifier("cicloCultivoServiceCultivos")
    private CicloCultivoService cicloCultivoService;

    @Autowired
    @Qualifier("campanaContextServiceCore")
    private com.agrocloud.core.application.CampanaContextService campanaContextService;

    /**
     * Obtener todas las labores accesibles por el usuario.
     * Delega en LaborQueryService.
     */
    @Transactional(readOnly = true)
    public List<Labor> getLaboresByUser(User user) {
        return laborQueryService.getLaboresByUser(user);
    }

    /**
     * Labores activas del calendario filtradas por rango de fechas en BD.
     */
    @Transactional(readOnly = true)
    public List<Labor> getLaboresCalendarioPorRango(User user, LocalDate desde, LocalDate hasta) {
        List<Long> loteIds = laborQueryService.getLoteIdsByUser(user);
        if (loteIds.isEmpty()) {
            return List.of();
        }
        return laborRepository.findActivasCalendarioByLoteIdInAndFechaInicioBetween(loteIds, desde, hasta);
    }

    /**
     * Obtener labor por ID verificando permisos.
     * Delega en LaborQueryService.
     */
    public Optional<Labor> getLaborById(Long id, User user) {
        return laborQueryService.getLaborById(id, user);
    }

    /**
     * Validación T1: no permitir dos labores SIEMBRA ni dos COSECHA solapadas en el mismo lote.
     * Solapamiento: [fechaInicio, fechaFin] ∩ [existente] ≠ ∅.
     * Expuesto para SiembraService al crear labores internamente.
     */
    public void validarSolapamientoT1(Long loteId, Labor.TipoLabor tipo, LocalDate fechaInicio, LocalDate fechaFin, Long laborIdExcluir) {
        if (tipo != Labor.TipoLabor.SIEMBRA && tipo != Labor.TipoLabor.COSECHA) return;
        LocalDate fechaFinMax = fechaFin != null ? fechaFin : fechaInicio;
        LocalDate fechaInicioMin = fechaInicio;
        Long excluirId = laborIdExcluir != null ? laborIdExcluir : -1L;
        List<Labor> solapadas = laborRepository.findLaboresActivasSolapadas(loteId, tipo, fechaInicioMin, fechaFinMax, excluirId);
        if (!solapadas.isEmpty()) {
            throw new IllegalArgumentException(
                "Ya existe una labor de tipo " + tipo + " en este lote con fechas solapadas. " +
                "No se permiten dos siembras ni dos cosechas solapadas en el mismo lote."
            );
        }
    }

    /**
     * Crear nueva labor
     */
    public Labor crearLabor(Labor labor, User usuario) {
        // Log para debug
        System.out.println("=== CREAR LABOR DEBUG ===");
        System.out.println("Tipo Labor: " + labor.getTipoLabor());
        System.out.println("Descripcion: " + labor.getDescripcion());
        System.out.println("Maquinaria asignada: " + labor.getMaquinariaAsignada());
        System.out.println("Mano de obra: " + labor.getManoObra());
        System.out.println("Insumos usados: " + labor.getInsumosUsados());
        
        // Verificar que el usuario tenga acceso al lote
        if (labor.getLote() != null) {
            Plot lote = plotRepository.findById(labor.getLote().getId()).orElse(null);
            if (lote == null || !laborQueryService.tieneAccesoAlLote(lote, usuario)) {
                throw new RuntimeException("No tiene permisos para crear labores en este lote");
            }
            asignarCicloCultivoSiCorresponde(labor, lote);
        }
        
        labor.setUsuario(usuario);
        labor.setActivo(true);

        if (labor.getLote() != null && (labor.getTipoLabor() == Labor.TipoLabor.SIEMBRA || labor.getTipoLabor() == Labor.TipoLabor.COSECHA)) {
            validarSolapamientoT1(labor.getLote().getId(), labor.getTipoLabor(), labor.getFechaInicio(), labor.getFechaFin(), null);
        }
        
        // Guardar la labor primero para obtener el ID
        Labor laborGuardada = laborRepository.save(labor);
        
        // Evaluar y aplicar transiciones automáticas de estado
        if (labor.getLote() != null && laborGuardada.getEstado() == Labor.EstadoLabor.COMPLETADA) {
            transicionEstadoService.evaluarYAplicarTransicion(labor.getLote(), laborGuardada);
        }
        
        // Procesar insumos usados si existen
        if (labor.getInsumosUsados() != null && !labor.getInsumosUsados().isEmpty()) {
            // TODO: Implementar procesamiento de insumos usados
            // Por ahora solo guardamos la labor básica
        }
        
        // Procesar maquinaria asignada si existe
        if (labor.getMaquinariaAsignada() != null && !labor.getMaquinariaAsignada().isEmpty()) {
            System.out.println("Procesando " + labor.getMaquinariaAsignada().size() + " elementos de maquinaria");
            for (Object maqData : labor.getMaquinariaAsignada()) {
                System.out.println("Tipo de maqData: " + maqData.getClass().getName());
                System.out.println("Contenido de maqData: " + maqData);
                if (maqData instanceof Map<?, ?>) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> maq = (Map<String, Object>) maqData;
                    System.out.println("Mapeando maquinaria: " + maq);
                    LaborMaquinaria laborMaq = new LaborMaquinaria();
                    laborMaq.setLabor(laborGuardada);
                    laborMaq.setDescripcion(maq.get("descripcion") != null ? maq.get("descripcion").toString() : "");
                    
                    // Determinar el tipo de maquinaria basado en el proveedor
                    String proveedor = maq.get("proveedor") != null ? maq.get("proveedor").toString() : null;
                    if (proveedor != null && !proveedor.trim().isEmpty()) {
                        laborMaq.setTipoMaquinaria(TipoMaquinaria.ALQUILADA);
                    } else {
                        laborMaq.setTipoMaquinaria(TipoMaquinaria.PROPIA);
                    }
                    
                    laborMaq.setProveedor(proveedor);
                    laborMaq.setCosto(maq.get("costo") != null ? 
                        BigDecimal.valueOf(Double.parseDouble(maq.get("costo").toString())) : 
                        BigDecimal.ZERO);
                    laborMaq.setObservaciones(maq.get("observaciones") != null ? maq.get("observaciones").toString() : null);
                    laborMaquinariaRepository.save(laborMaq);
                }
            }
        }
        
        // Procesar mano de obra si existe
        if (labor.getManoObra() != null && !labor.getManoObra().isEmpty()) {
            System.out.println("Procesando " + labor.getManoObra().size() + " elementos de mano de obra");
            for (Object moData : labor.getManoObra()) {
                System.out.println("Tipo de moData: " + moData.getClass().getName());
                System.out.println("Contenido de moData: " + moData);
                if (moData instanceof Map<?, ?>) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> mo = (Map<String, Object>) moData;
                    System.out.println("Mapeando mano de obra: " + mo);
                    LaborManoObra laborMo = new LaborManoObra();
                    laborMo.setLabor(laborGuardada);
                    laborMo.setDescripcion(mo.get("descripcion") != null ? mo.get("descripcion").toString() : "");
                    laborMo.setCantidadPersonas(mo.get("cantidad_personas") != null ? Integer.parseInt(mo.get("cantidad_personas").toString()) : 1);
                    laborMo.setProveedor(mo.get("proveedor") != null ? mo.get("proveedor").toString() : null);
                    laborMo.setCostoTotal(mo.get("costo_total") != null ? 
                        BigDecimal.valueOf(Double.parseDouble(mo.get("costo_total").toString())) : 
                        BigDecimal.ZERO);
                    laborMo.setHorasTrabajo(mo.get("horas_trabajo") != null ? 
                        BigDecimal.valueOf(Double.parseDouble(mo.get("horas_trabajo").toString())) : 
                        null);
                    laborMo.setObservaciones(mo.get("observaciones") != null ? mo.get("observaciones").toString() : null);
                    laborManoObraRepository.save(laborMo);
                }
            }
        }
        if (laborGuardada.getLote() != null) {
            estadoLoteUpdater.recalcularEstado(laborGuardada.getLote().getId());
        }
        return laborGuardada;
    }

    /**
     * Crear nueva labor desde request del frontend
     */
    public Labor crearLaborDesdeRequest(CrearLaborRequest request, User usuario) {
        System.out.println("=== CREAR LABOR DESDE REQUEST ===");
        System.out.println("Request: " + request);
        
        // Crear objeto Labor básico
        Labor labor = new Labor();
        
        // Mapear campos básicos
        if (request.getTipoLabor() != null) {
            try {
                labor.setTipoLabor(Labor.TipoLabor.valueOf(request.getTipoLabor()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Tipo de labor inválido: " + request.getTipoLabor());
            }
        }
        
        labor.setDescripcion(request.getDescripcion());
        
        // Mapear fechas
        if (request.getFechaInicio() != null) {
            labor.setFechaInicio(LocalDate.parse(request.getFechaInicio()));
        }
        if (request.getFechaFin() != null) {
            labor.setFechaFin(LocalDate.parse(request.getFechaFin()));
        }
        
        // Mapear estado
        if (request.getEstado() != null) {
            try {
                labor.setEstado(Labor.EstadoLabor.valueOf(request.getEstado()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Estado de labor inválido: " + request.getEstado());
            }
        }
        
        labor.setResponsable(request.getResponsable());
        
        // Establecer costo total, si es null usar BigDecimal.ZERO
        if (request.getCostoTotal() != null) {
            labor.setCostoTotal(request.getCostoTotal());
        } else {
            labor.setCostoTotal(BigDecimal.ZERO);
        }
        
        // Mapear lote
        if (request.getLote() != null && request.getLote().get("id") != null) {
            Long loteId = Long.valueOf(request.getLote().get("id").toString());
            Plot lote = plotRepository.findById(loteId).orElse(null);
            if (lote == null) {
                throw new RuntimeException("Lote no encontrado con ID: " + loteId);
            }
            if (!laborQueryService.tieneAccesoAlLote(lote, usuario)) {
                throw new RuntimeException("No tiene permisos para crear labores en este lote");
            }
            labor.setLote(lote);
            asignarCicloCultivoSiCorresponde(labor, lote);
        }
        if (request.getCultivoId() != null) {
            labor.setCultivo(cultivoRepository.findById(request.getCultivoId()).orElse(null));
        }
        labor.setUsuario(usuario);
        labor.setActivo(true);

        if (labor.getLote() != null && (labor.getTipoLabor() == Labor.TipoLabor.SIEMBRA || labor.getTipoLabor() == Labor.TipoLabor.COSECHA)) {
            validarSolapamientoT1(labor.getLote().getId(), labor.getTipoLabor(), labor.getFechaInicio(), labor.getFechaFin(), null);
        }
        
        // Guardar la labor primero para obtener el ID
        Labor laborGuardada = laborRepository.save(labor);
        
        // Si es labor de SIEMBRA y se envió cultivoId, asignar cultivo y tipo de cultivo al lote
        if (labor.getLote() != null && labor.getTipoLabor() == Labor.TipoLabor.SIEMBRA && request.getCultivoId() != null) {
            asignarCultivoAlLotePorSiembra(laborGuardada.getLote().getId(), request.getCultivoId(), laborGuardada);
        }
        
        // Spec SDD: Calendario deriva solo de labores; no se crean recordatorios por labor.
        
        // Evaluar y aplicar transiciones automáticas de estado
        if (labor.getLote() != null && laborGuardada.getEstado() == Labor.EstadoLabor.COMPLETADA) {
            transicionEstadoService.evaluarYAplicarTransicion(labor.getLote(), laborGuardada);
        }
        
        // Procesar insumos usados
        if (request.getInsumosUsados() != null && !request.getInsumosUsados().isEmpty()) {
            System.out.println("Procesando " + request.getInsumosUsados().size() + " insumos");
            
            // Obtener hectáreas del lote
            BigDecimal hectareas = BigDecimal.ZERO;
            if (laborGuardada.getLote() != null) {
                Plot lote = plotRepository.findById(laborGuardada.getLote().getId())
                    .orElse(null);
                if (lote != null && lote.getAreaHectareas() != null) {
                    hectareas = lote.getAreaHectareas();
                    System.out.println("Hectáreas del lote: " + hectareas);
                }
            }
            
            List<LaborInsumo> insumosGuardados = new ArrayList<>();
            List<String> advertenciasStock = new ArrayList<>();
            
            for (Map<String, Object> insumoData : request.getInsumosUsados()) {
                System.out.println("Procesando insumo: " + insumoData);
                
                try {
                    // Obtener ID del insumo
                    final Long insumoId;
                    if (insumoData.get("insumo_id") != null) {
                        insumoId = Long.parseLong(insumoData.get("insumo_id").toString());
                    } else if (insumoData.get("id") != null) {
                        insumoId = Long.parseLong(insumoData.get("id").toString());
                    } else {
                        System.err.println("⚠️ Insumo sin ID, omitiendo");
                        continue;
                    }
                    
                    // Buscar el insumo
                    Insumo insumo = insumoRepository.findById(insumoId)
                        .orElseThrow(() -> new RuntimeException("Insumo no encontrado con ID: " + insumoId));
                    
                    // Obtener cantidad usada del request
                    BigDecimal cantidadUsada = BigDecimal.ZERO;
                    if (insumoData.get("cantidad_usada") != null) {
                        cantidadUsada = BigDecimal.valueOf(Double.parseDouble(insumoData.get("cantidad_usada").toString()));
                    } else if (insumoData.get("cantidad") != null) {
                        cantidadUsada = BigDecimal.valueOf(Double.parseDouble(insumoData.get("cantidad").toString()));
                    }
                    
                    BigDecimal cantidadPlanificada = cantidadUsada;
                    if (insumoData.get("cantidad_planificada") != null) {
                        cantidadPlanificada = BigDecimal.valueOf(Double.parseDouble(insumoData.get("cantidad_planificada").toString()));
                    }
                    
                    // Verificar si es agroquímico
                    if (insumo.esAgroquimico() && hectareas.compareTo(BigDecimal.ZERO) > 0) {
                        System.out.println("🦠 Detectado agroquímico: " + insumo.getNombre());
                        
                        // Buscar tipoAplicacion y formaAplicacion del request (si están presentes)
                        TipoAplicacion tipoAplicacion = null;
                        FormaAplicacion formaAplicacion = null;
                        
                        if (insumoData.get("tipo_aplicacion") != null) {
                            try {
                                tipoAplicacion = TipoAplicacion.valueOf(insumoData.get("tipo_aplicacion").toString());
                            } catch (IllegalArgumentException e) {
                                System.err.println("⚠️ Tipo de aplicación inválido: " + insumoData.get("tipo_aplicacion"));
                            }
                        }
                        
                        if (insumoData.get("forma_aplicacion") != null) {
                            try {
                                formaAplicacion = FormaAplicacion.valueOf(insumoData.get("forma_aplicacion").toString());
                            } catch (IllegalArgumentException e) {
                                System.err.println("⚠️ Forma de aplicación inválida: " + insumoData.get("forma_aplicacion"));
                            }
                        }
                        
                        // Buscar configuración de dosis
                        DosisAgroquimico dosis = null;
                        if (tipoAplicacion != null && formaAplicacion != null) {
                            dosis = dosisAgroquimicoRepository
                                .findByInsumoIdAndTipoAplicacionAndFormaAplicacionAndActivoTrue(
                                    insumoId, tipoAplicacion, formaAplicacion)
                                .orElse(null);
                        }
                        
                        // Si no se encontró dosis específica, buscar la primera disponible
                        if (dosis == null) {
                            List<DosisAgroquimico> dosisDisponibles = dosisAgroquimicoRepository
                                .findByInsumoIdAndActivoTrue(insumoId);
                            if (!dosisDisponibles.isEmpty()) {
                                dosis = dosisDisponibles.get(0);
                                System.out.println("📋 Usando primera dosis disponible: " + 
                                    dosis.getTipoAplicacion() + " - " + dosis.getFormaAplicacion());
                            }
                        }
                        
                        // Calcular cantidad necesaria basada en dosis recomendada
                        if (dosis != null && dosis.getDosisRecomendadaPorHa() != null) {
                            BigDecimal dosisRecomendada = BigDecimal.valueOf(dosis.getDosisRecomendadaPorHa());
                            BigDecimal cantidadNecesaria = hectareas.multiply(dosisRecomendada);
                            
                            System.out.println("📊 Cálculo de dosis:");
                            System.out.println("  - Hectáreas: " + hectareas);
                            System.out.println("  - Dosis recomendada por ha: " + dosisRecomendada);
                            System.out.println("  - Cantidad necesaria: " + cantidadNecesaria);
                            System.out.println("  - Cantidad especificada por usuario: " + cantidadUsada);
                            
                            // Validar variación ±20%
                            BigDecimal variacionPorcentual = cantidadUsada
                                .subtract(cantidadNecesaria)
                                .divide(cantidadNecesaria, 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100));
                            
                            BigDecimal variacionAbsoluta = variacionPorcentual.abs();
                            BigDecimal limiteVariacion = BigDecimal.valueOf(20);
                            
                            System.out.println("  - Variación: " + variacionPorcentual + "%");
                            
                            if (variacionAbsoluta.compareTo(limiteVariacion) > 0) {
                                throw new RuntimeException(
                                    "La dosis especificada (" + cantidadUsada + ") excede el límite permitido (±20%) de la dosis recomendada (" + 
                                    cantidadNecesaria + "). Variación: " + variacionPorcentual.abs() + "%"
                                );
                            }
                            
                            // Si el usuario no especificó cantidad, usar la calculada
                            if (cantidadUsada.compareTo(BigDecimal.ZERO) == 0) {
                                cantidadUsada = cantidadNecesaria;
                                cantidadPlanificada = cantidadNecesaria;
                                System.out.println("✅ Usando cantidad calculada automáticamente: " + cantidadUsada);
                            }
                        }
                        
                        // Verificar stock suficiente
                        if (insumo.getStockActual().compareTo(cantidadUsada) < 0) {
                            BigDecimal faltante = cantidadUsada.subtract(insumo.getStockActual());
                            String advertencia = String.format(
                                "⚠️ Stock insuficiente para %s. Disponible: %s %s, Requerido: %s %s, Faltante: %s %s",
                                insumo.getNombre(),
                                insumo.getStockActual(),
                                insumo.getUnidadMedida(),
                                cantidadUsada,
                                insumo.getUnidadMedida(),
                                faltante,
                                insumo.getUnidadMedida()
                            );
                            advertenciasStock.add(advertencia);
                            System.err.println(advertencia);
                            // No rechazamos, solo advertimos para que el usuario decida
                        }
                    }
                    
                    // Crear LaborInsumo
                    LaborInsumo laborInsumo = new LaborInsumo();
                    laborInsumo.setLabor(laborGuardada);
                    laborInsumo.setInsumo(insumo);
                    laborInsumo.setCantidadUsada(cantidadUsada);
                    laborInsumo.setCantidadPlanificada(cantidadPlanificada);
                    
                    // Obtener costo unitario
                    BigDecimal costoUnitario = insumo.getPrecioUnitario() != null ? insumo.getPrecioUnitario() : BigDecimal.ZERO;
                    if (insumoData.get("costo_unitario") != null) {
                        costoUnitario = BigDecimal.valueOf(Double.parseDouble(insumoData.get("costo_unitario").toString()));
                    } else if (insumoData.get("precio_unitario") != null) {
                        costoUnitario = BigDecimal.valueOf(Double.parseDouble(insumoData.get("precio_unitario").toString()));
                    }
                    
                    laborInsumo.setCostoUnitario(costoUnitario);
                    laborInsumo.setCostoTotal(cantidadUsada.multiply(costoUnitario));
                    laborInsumo.setObservaciones(insumoData.get("observaciones") != null ? 
                        insumoData.get("observaciones").toString() : null);
                    
                    // Guardar LaborInsumo
                    laborInsumo = laborInsumoRepository.save(laborInsumo);
                    insumosGuardados.add(laborInsumo);
                    
                    // Actualizar inventario (descontar stock) - solo si hay suficiente stock
                    if (insumo.getStockActual().compareTo(cantidadUsada) >= 0) {
                        inventarioService.actualizarInventarioLabor(
                            laborGuardada.getId(), 
                            List.of(laborInsumo), 
                            null, 
                            usuario
                        );
                    }
                    
                    System.out.println("✅ Insumo procesado exitosamente: " + insumo.getNombre());
                    
                } catch (Exception e) {
                    System.err.println("❌ Error al procesar insumo: " + e.getMessage());
                    e.printStackTrace();
                    // Cualquier excepción implica rollback de la transacción; no continuar
                    // o el flush posterior (ej. al guardar mano de obra) provoca
                    // AssertionFailure por entidades inconsistentes (ej. MovimientoInventario con id nulo)
                    throw new RuntimeException("Error al procesar insumo: " + e.getMessage(), e);
                }
            }
            
            // Si hay advertencias de stock, registrarlas pero no rechazar la creación
            // El frontend puede decidir si continuar o no
            if (!advertenciasStock.isEmpty()) {
                String mensajeAdvertencias = "Problemas de stock detectados:\n" + 
                    String.join("\n", advertenciasStock);
                System.err.println("⚠️ " + mensajeAdvertencias);
                // Registrar en observaciones de la labor o log
                // Las advertencias se pueden recuperar del frontend si es necesario
            }
        }
        
        // Procesar maquinaria asignada
        if (request.getMaquinariaAsignada() != null && !request.getMaquinariaAsignada().isEmpty()) {
            System.out.println("Procesando " + request.getMaquinariaAsignada().size() + " elementos de maquinaria");
            for (Map<String, Object> maqData : request.getMaquinariaAsignada()) {
                System.out.println("Procesando maquinaria: " + maqData);
                LaborMaquinaria laborMaq = new LaborMaquinaria();
                laborMaq.setLabor(laborGuardada);
                laborMaq.setDescripcion(maqData.get("descripcion") != null ? maqData.get("descripcion").toString() : "");
                
                // Determinar el tipo de maquinaria basado en el proveedor
                String proveedor = maqData.get("proveedor") != null ? maqData.get("proveedor").toString() : null;
                if (proveedor != null && !proveedor.trim().isEmpty()) {
                    laborMaq.setTipoMaquinaria(TipoMaquinaria.ALQUILADA);
                } else {
                    laborMaq.setTipoMaquinaria(TipoMaquinaria.PROPIA);
                }
                
                laborMaq.setProveedor(proveedor);
                laborMaq.setCosto(maqData.get("costo") != null ? 
                    BigDecimal.valueOf(Double.parseDouble(maqData.get("costo").toString())) : 
                    BigDecimal.ZERO);
                laborMaq.setObservaciones(maqData.get("observaciones") != null ? maqData.get("observaciones").toString() : null);
                laborMaquinariaRepository.save(laborMaq);
            }
        }
        
        // Procesar mano de obra
        if (request.getManoObra() != null && !request.getManoObra().isEmpty()) {
            System.out.println("Procesando " + request.getManoObra().size() + " elementos de mano de obra");
            for (Map<String, Object> moData : request.getManoObra()) {
                System.out.println("Procesando mano de obra: " + moData);
                LaborManoObra laborMo = new LaborManoObra();
                laborMo.setLabor(laborGuardada);
                laborMo.setDescripcion(moData.get("descripcion") != null ? moData.get("descripcion").toString() : "");
                laborMo.setCantidadPersonas(moData.get("cantidad_personas") != null ? 
                    Integer.valueOf(moData.get("cantidad_personas").toString()) : 1);
                laborMo.setProveedor(moData.get("proveedor") != null ? moData.get("proveedor").toString() : null);
                laborMo.setCostoTotal(moData.get("costo_total") != null ? 
                    BigDecimal.valueOf(Double.parseDouble(moData.get("costo_total").toString())) : 
                    BigDecimal.ZERO);
                laborMo.setHorasTrabajo(moData.get("horas_trabajo") != null ? 
                    BigDecimal.valueOf(Double.parseDouble(moData.get("horas_trabajo").toString())) : 
                    null);
                laborMo.setObservaciones(moData.get("observaciones") != null ? moData.get("observaciones").toString() : null);
                laborManoObraRepository.save(laborMo);
            }
        }
        
        // Recalcular costo total sumando todos los componentes
        BigDecimal costoTotal = BigDecimal.ZERO;
        
        // Sumar costo de insumos
        List<LaborInsumo> insumosLabor = laborInsumoRepository.findByLaborId(laborGuardada.getId());
        for (LaborInsumo li : insumosLabor) {
            if (li.getCostoTotal() != null) {
                costoTotal = costoTotal.add(li.getCostoTotal());
            }
        }
        
        // Sumar costo de maquinaria
        List<LaborMaquinaria> maquinariasLabor = laborMaquinariaRepository.findByLaborId(laborGuardada.getId());
        for (LaborMaquinaria lm : maquinariasLabor) {
            if (lm.getCosto() != null) {
                costoTotal = costoTotal.add(lm.getCosto());
            }
        }
        
        // Sumar costo de mano de obra
        List<LaborManoObra> manoObraLabor = laborManoObraRepository.findByLaborId(laborGuardada.getId());
        for (LaborManoObra lmo : manoObraLabor) {
            if (lmo.getCostoTotal() != null) {
                costoTotal = costoTotal.add(lmo.getCostoTotal());
            }
        }
        
        // Establecer costo total en la labor y actualizar
        laborGuardada.setCostoTotal(costoTotal);
        laborGuardada = laborRepository.save(laborGuardada);
        if (laborGuardada.getLote() != null) {
            estadoLoteUpdater.recalcularEstado(laborGuardada.getLote().getId());
        }
        System.out.println("💰 Costo total calculado para labor ID " + laborGuardada.getId() + ": $" + costoTotal);
        return laborGuardada;
    }

    /**
     * Crear labor de siembra con confirmación de cambio de estado
     */
    public RespuestaCambioEstado crearLaborSiembraConConfirmacion(Labor labor, User usuario) {
        // Verificar que el lote esté en estado válido para siembra
        if (labor.getLote() == null) {
            throw new RuntimeException("La labor debe estar asociada a un lote");
        }
        
        Plot lote = plotRepository.findById(labor.getLote().getId())
            .orElseThrow(() -> new RuntimeException("Lote no encontrado"));
            
        if (!lote.puedeSembrar()) {
            throw new IllegalStateException(
                "El lote " + lote.getNombre() + " no está disponible para siembra. " +
                "Estado actual: " + lote.getEstado().getDescripcion()
            );
        }
        
        // Crear la labor primero
        labor.setUsuario(usuario);
        labor.setActivo(true);
        labor.setTipoLabor(Labor.TipoLabor.SIEMBRA);

        validarSolapamientoT1(labor.getLote().getId(), Labor.TipoLabor.SIEMBRA, labor.getFechaInicio(), labor.getFechaFin(), null);
        
        Labor laborGuardada = laborRepository.save(labor);
        
        // Spec SDD: no recordatorios; calendario deriva de labores.
        
        // Proponer cambio de estado
        return estadoLoteService.proponerCambioEstado(
            lote.getId(), 
            EstadoLote.SEMBRADO, 
            "Siembra realizada - Labor ID: " + laborGuardada.getId(),
            usuario
        );
    }
    
    /**
     * Crear labor de cosecha con confirmación de cambio de estado
     */
    public RespuestaCambioEstado crearLaborCosechaConConfirmacion(Labor labor, User usuario) {
        // Verificar que el lote esté en estado válido para cosecha
        if (labor.getLote() == null) {
            throw new RuntimeException("La labor debe estar asociada a un lote");
        }
        
        Plot lote = plotRepository.findById(labor.getLote().getId())
            .orElseThrow(() -> new RuntimeException("Lote no encontrado"));
            
        if (!lote.puedeCosechar()) {
            throw new IllegalStateException(
                "El lote " + lote.getNombre() + " no está listo para cosecha. " +
                "Estado actual: " + lote.getEstado().getDescripcion()
            );
        }
        
        // Crear la labor primero
        labor.setUsuario(usuario);
        labor.setActivo(true);
        labor.setTipoLabor(Labor.TipoLabor.COSECHA);

        validarSolapamientoT1(labor.getLote().getId(), Labor.TipoLabor.COSECHA, labor.getFechaInicio(), labor.getFechaFin(), null);
        
        Labor laborGuardada = laborRepository.save(labor);
        
        // Proponer cambio de estado
        return estadoLoteService.proponerCambioEstado(
            lote.getId(), 
            EstadoLote.COSECHADO, 
            "Cosecha realizada - Labor ID: " + laborGuardada.getId(),
            usuario
        );
    }
    
    /**
     * Confirmar labor de siembra y actualizar estado del lote
     */
    public void confirmarLaborSiembra(Long laborId, ConfirmacionCambioEstado confirmacion, User usuario) {
        Labor labor = laborRepository.findById(laborId)
            .orElseThrow(() -> new RuntimeException("Labor no encontrada"));
        if (confirmacion.isConfirmado()) {
            actualizarInformacionCultivo(labor.getLote(), labor);
            labor.setEstado(Labor.EstadoLabor.COMPLETADA);
            labor.setFechaRealizacion(labor.getFechaInicio() != null ? labor.getFechaInicio() : LocalDate.now());
            laborRepository.save(labor);
        } else {
            labor.setEstado(Labor.EstadoLabor.CANCELADA);
            labor.setObservaciones("Cancelada por el usuario - cambio de estado no confirmado");
            laborRepository.save(labor);
        }
        if (labor.getLote() != null) {
            estadoLoteUpdater.recalcularEstado(labor.getLote().getId());
        }
    }
    
    /**
     * Finaliza una labor de cosecha existente (completada o cancelada) y recalcula el estado del lote.
     * Para el flujo completo con historial e inventario, usar {@link SiembraService#ejecutarCosecha}.
     */
    public void finalizarLaborCosecha(Long laborId, ConfirmacionCambioEstado confirmacion, User usuario) {
        Labor labor = laborRepository.findById(laborId)
            .orElseThrow(() -> new RuntimeException("Labor no encontrada"));
        if (confirmacion.isConfirmado()) {
            crearRegistroCosecha(labor.getLote(), labor);
            labor.setEstado(Labor.EstadoLabor.COMPLETADA);
            labor.setFechaRealizacion(labor.getFechaInicio() != null ? labor.getFechaInicio() : LocalDate.now());
            laborRepository.save(labor);
        } else {
            labor.setEstado(Labor.EstadoLabor.CANCELADA);
            labor.setObservaciones("Cancelada por el usuario - cambio de estado no confirmado");
            laborRepository.save(labor);
        }
        if (labor.getLote() != null) {
            estadoLoteUpdater.recalcularEstado(labor.getLote().getId());
        }
    }
    
    /**
     * Asigna el cultivo y tipo de cultivo al lote cuando se crea una labor de SIEMBRA con cultivoId.
     * Determina el tipo de cultivo desde Cultivo.tipo (ej. "Soja", "Maíz") y aplica estado inicial configurado.
     */
    private void asignarCultivoAlLotePorSiembra(Long loteId, Long cultivoId, Labor laborGuardada) {
        Plot lote = plotRepository.findById(loteId).orElseThrow(() -> new RuntimeException("Lote no encontrado"));
        Cultivo cultivo = cultivoRepository.findById(cultivoId).orElseThrow(() -> new RuntimeException("Cultivo no encontrado"));
        lote.setCultivo(cultivo);
        lote.setCultivoActual(cultivo.getNombre());
        lote.setLiberadoParaSiembra(false);
        try {
            resolverTipoCultivoParaCultivo(cultivo).ifPresent(lote::setTipoCultivo);
            if (laborGuardada.getFechaInicio() != null) {
                lote.setFechaSiembra(laborGuardada.getFechaInicio());
            }
        } catch (Exception e) {
            System.err.println("Error al asignar cultivo al lote por siembra: " + e.getMessage());
        }
        plotRepository.save(lote);
        estadoLoteUpdater.recalcularEstado(loteId);
    }

    /**
     * Actualizar información del cultivo cuando se confirma siembra
     * También actualiza las relaciones directas con Cultivo y TipoCultivo si es posible
     */
    private void actualizarInformacionCultivo(Plot lote, Labor labor) {
        // Actualizar fecha de siembra
        lote.setFechaSiembra(labor.getFechaInicio());
        
        // Calcular fecha de cosecha esperada (ejemplo: 120 días después)
        if (lote.getFechaSiembra() != null) {
            lote.setFechaCosechaEsperada(lote.getFechaSiembra().plusDays(120));
        }
        
        // Actualizar cultivo actual si se proporciona en la labor
        if (labor.getDescripcion() != null && !labor.getDescripcion().isEmpty()) {
            // Extraer nombre del cultivo de la descripción (lógica simple)
            String descripcion = labor.getDescripcion().toLowerCase();
            String nombreCultivo = null;
            if (descripcion.contains("maíz") || descripcion.contains("maiz")) {
                nombreCultivo = "Maíz";
            } else if (descripcion.contains("soja")) {
                nombreCultivo = "Soja";
            } else if (descripcion.contains("trigo")) {
                nombreCultivo = "Trigo";
            } else {
                nombreCultivo = "Cultivo";
            }
            
            lote.setCultivoActual(nombreCultivo);
            
            // Intentar asociar el cultivo y tipo de cultivo directamente
            try {
                if (lote.getCampo() != null && lote.getCampo().getEmpresa() != null) {
                    // Buscar cultivo por nombre
                    List<Cultivo> cultivosEncontrados = cultivoRepository.findByNombreContaining(nombreCultivo);
                    Cultivo cultivo = cultivosEncontrados.stream()
                        .filter(c -> c.getEmpresa() != null && 
                                c.getEmpresa().getId().equals(lote.getCampo().getEmpresa().getId()))
                        .findFirst()
                        .orElse(null);
                    
                    if (cultivo != null) {
                        lote.setCultivo(cultivo);
                        resolverTipoCultivoParaCultivo(cultivo).ifPresent(lote::setTipoCultivo);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error al asociar cultivo y tipo de cultivo en actualizarInformacionCultivo: " + e.getMessage());
                // Continuar sin fallar si no se puede asociar
            }
        }
        
        plotRepository.save(lote);
    }
    
    /**
     * Crear registro de cosecha cuando se confirma cosecha
     */
    private void crearRegistroCosecha(Plot lote, Labor labor) {
        // Actualizar fecha de cosecha real
        lote.setFechaCosechaReal(labor.getFechaFin() != null ? labor.getFechaFin() : labor.getFechaInicio());
        
        // Calcular rendimiento real basado en datos de la labor
        calcularRendimientoCosecha(lote, labor);
        
        plotRepository.save(lote);
    }
    
    /**
     * Calcular rendimiento de cosecha basado en cantidad esperada vs obtenida
     */
    private void calcularRendimientoCosecha(Plot lote, Labor labor) {
        // Obtener información del cultivo del lote
        String cultivoActual = lote.getCultivoActual();
        BigDecimal areaHectareas = lote.getAreaHectareas();
        
        if (areaHectareas == null || areaHectareas.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("⚠️ [LaborService] No se puede calcular rendimiento: área del lote no válida");
            return;
        }
        
        // Extraer cantidad cosechada de la descripción de la labor
        BigDecimal cantidadObtenida = extraerCantidadCosechada(labor.getDescripcion());
        
        if (cantidadObtenida == null) {
            System.out.println("⚠️ [LaborService] No se pudo extraer cantidad cosechada de la descripción");
            return;
        }
        
        // Obtener rendimiento esperado del lote o del cultivo
        BigDecimal rendimientoEsperado = lote.getRendimientoEsperado();
        if (rendimientoEsperado == null) {
            // Si no hay rendimiento esperado en el lote, usar valores por defecto según el cultivo
            rendimientoEsperado = obtenerRendimientoEsperadoPorCultivo(cultivoActual);
        }
        
        // Calcular rendimiento real (cantidad obtenida / área en hectáreas)
        BigDecimal rendimientoReal = cantidadObtenida.divide(areaHectareas, 2, RoundingMode.HALF_UP);
        
        // Actualizar valores en el lote
        lote.setRendimientoEsperado(rendimientoEsperado);
        lote.setRendimientoReal(rendimientoReal);
        
        // Calcular porcentaje de cumplimiento
        BigDecimal porcentajeCumplimiento = BigDecimal.ZERO;
        if (rendimientoEsperado != null && rendimientoEsperado.compareTo(BigDecimal.ZERO) > 0) {
            porcentajeCumplimiento = rendimientoReal.divide(rendimientoEsperado, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }
        
        System.out.println("📊 [LaborService] Cálculo de rendimiento:");
        System.out.println("   Cultivo: " + cultivoActual);
        System.out.println("   Área: " + areaHectareas + " ha");
        System.out.println("   Cantidad obtenida: " + cantidadObtenida + " kg");
        System.out.println("   Rendimiento real: " + rendimientoReal + " kg/ha");
        System.out.println("   Rendimiento esperado: " + rendimientoEsperado + " kg/ha");
        System.out.println("   Cumplimiento: " + porcentajeCumplimiento + "%");
    }
    
    /**
     * Extraer cantidad cosechada de la descripción de la labor
     */
    private BigDecimal extraerCantidadCosechada(String descripcion) {
        if (descripcion == null || descripcion.isEmpty()) {
            return null;
        }
        
        // Buscar patrones como "cosechado 1500 kg", "obtenido 2.5 toneladas", etc.
        String descripcionLower = descripcion.toLowerCase();
        
        // Patrones para extraer números seguidos de unidades
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "(?:cosechado|obtenido|recolectado|producido)\\s+(\\d+(?:\\.\\d+)?)\\s*(kg|toneladas?|t|quintales?|q)"
        );
        
        java.util.regex.Matcher matcher = pattern.matcher(descripcionLower);
        if (matcher.find()) {
            BigDecimal cantidad = new BigDecimal(matcher.group(1));
            String unidad = matcher.group(2).toLowerCase();
            
            // Convertir a kg para estandarizar
            switch (unidad) {
                case "toneladas":
                case "tonelada":
                case "t":
                    cantidad = cantidad.multiply(new BigDecimal("1000")); // 1 tonelada = 1000 kg
                    break;
                case "quintales":
                case "quintal":
                case "q":
                    cantidad = cantidad.multiply(new BigDecimal("100")); // 1 quintal = 100 kg
                    break;
                case "kg":
                default:
                    // Ya está en kg
                    break;
            }
            
            return cantidad;
        }
        
        // Si no se encuentra patrón, buscar solo números
        pattern = java.util.regex.Pattern.compile("(\\d+(?:\\.\\d+)?)");
        matcher = pattern.matcher(descripcion);
        if (matcher.find()) {
            return new BigDecimal(matcher.group(1));
        }
        
        return null;
    }
    
    /**
     * Obtener rendimiento esperado por defecto según el cultivo
     */
    private BigDecimal obtenerRendimientoEsperadoPorCultivo(String cultivo) {
        if (cultivo == null) {
            return new BigDecimal("3000"); // Valor por defecto
        }
        
        String cultivoLower = cultivo.toLowerCase();
        
        // Rendimientos esperados típicos en kg/ha
        if (cultivoLower.contains("maíz") || cultivoLower.contains("maiz")) {
            return new BigDecimal("8000"); // 8 ton/ha
        } else if (cultivoLower.contains("soja")) {
            return new BigDecimal("3500"); // 3.5 ton/ha
        } else if (cultivoLower.contains("trigo")) {
            return new BigDecimal("4000"); // 4 ton/ha
        } else if (cultivoLower.contains("girasol")) {
            return new BigDecimal("2500"); // 2.5 ton/ha
        } else if (cultivoLower.contains("sorgo")) {
            return new BigDecimal("6000"); // 6 ton/ha
        } else {
            return new BigDecimal("3000"); // Valor por defecto
        }
    }

    /**
     * Única autoridad para transiciones de estado (spec SDD).
     * Valida matriz de transiciones, aplica invariantes I1–I4 y efectos laterales.
     * Lanza IllegalStateException en transición inválida.
     */
    private void aplicarTransicionDeEstado(Labor labor, Labor.EstadoLabor nuevoEstado, LocalDate fechaRealizacion) {
        if (labor == null || nuevoEstado == null) {
            return;
        }
        if (Boolean.FALSE.equals(labor.getActivo())) {
            throw new IllegalStateException("No se puede cambiar el estado de una labor eliminada");
        }
        Labor.EstadoLabor actual = labor.getEstado();
        if (actual == nuevoEstado) {
            return;
        }
        // Transiciones permitidas (spec)
        boolean permitida = false;
        if (actual == Labor.EstadoLabor.PLANIFICADA) {
            permitida = nuevoEstado == Labor.EstadoLabor.EN_PROGRESO
                || nuevoEstado == Labor.EstadoLabor.COMPLETADA
                || nuevoEstado == Labor.EstadoLabor.CANCELADA;
        } else if (actual == Labor.EstadoLabor.EN_PROGRESO) {
            permitida = nuevoEstado == Labor.EstadoLabor.COMPLETADA
                || nuevoEstado == Labor.EstadoLabor.CANCELADA;
        } else if (actual == Labor.EstadoLabor.CANCELADA) {
            permitida = nuevoEstado == Labor.EstadoLabor.PLANIFICADA;
        }
        if (!permitida) {
            throw new IllegalStateException("Transición no permitida: " + actual + " → " + nuevoEstado);
        }
        // Efectos: COMPLETADA exige fechaRealizacion (I1) y no anterior a fechaInicio (I4)
        if (nuevoEstado == Labor.EstadoLabor.COMPLETADA) {
            LocalDate fecha = fechaRealizacion != null ? fechaRealizacion : LocalDate.now();
            if (labor.getFechaInicio() != null && fecha.isBefore(labor.getFechaInicio())) {
                throw new IllegalStateException("fechaRealizacion no puede ser anterior a fechaInicio");
            }
            labor.setFechaRealizacion(fecha);
        }
        // I2: al pasar a PLANIFICADA, fechaRealizacion debe ser null
        if (nuevoEstado == Labor.EstadoLabor.PLANIFICADA) {
            labor.setFechaRealizacion(null);
        }
        labor.setEstado(nuevoEstado);
        // Efecto lateral: transición de estado del lote al completar
        if (nuevoEstado == Labor.EstadoLabor.COMPLETADA && labor.getLote() != null) {
            Plot lote = plotRepository.findById(labor.getLote().getId()).orElse(null);
            if (lote != null) {
                transicionEstadoService.evaluarYAplicarTransicion(lote, labor);
            }
        }
    }

    /**
     * Actualizar labor existente.
     * Cualquier cambio de estado se delega en aplicarTransicionDeEstado (spec SDD).
     */
    public Labor actualizarLabor(Long id, Labor laborData, User usuario) {
        Labor labor = laborRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Labor no encontrada"));
        
        if (labor.getLote() != null && !laborQueryService.tieneAccesoAlLote(labor.getLote(), usuario)) {
            throw new RuntimeException("No tiene permisos para modificar esta labor");
        }

        Labor.EstadoLabor estadoDeseado = laborData.getEstado();
        boolean cambioEstado = estadoDeseado != null && !estadoDeseado.equals(labor.getEstado());

        if (cambioEstado) {
            aplicarTransicionDeEstado(labor, estadoDeseado, laborData.getFechaRealizacion());
        } else {
            // Sin cambio de estado: actualizar solo campos no-estado; I5 para fechaInicio
            if (laborData.getFechaInicio() != null && !laborData.getFechaInicio().equals(labor.getFechaInicio())) {
                if (labor.getEstado() != Labor.EstadoLabor.PLANIFICADA) {
                    throw new IllegalStateException("Solo se puede reprogramar una labor en estado PLANIFICADA");
                }
                labor.setFechaInicio(laborData.getFechaInicio());
            }
            if (labor.getEstado() == Labor.EstadoLabor.COMPLETADA && laborData.getFechaRealizacion() != null) {
                if (labor.getFechaInicio() != null && laborData.getFechaRealizacion().isBefore(labor.getFechaInicio())) {
                    throw new IllegalStateException("fechaRealizacion no puede ser anterior a fechaInicio");
                }
                labor.setFechaRealizacion(laborData.getFechaRealizacion());
            }
        }

        labor.setTipoLabor(laborData.getTipoLabor());
        labor.setDescripcion(laborData.getDescripcion());
        if (laborData.getFechaInicio() != null) {
            if (!laborData.getFechaInicio().equals(labor.getFechaInicio()) && labor.getEstado() != Labor.EstadoLabor.PLANIFICADA) {
                throw new IllegalStateException("Solo se puede reprogramar una labor en estado PLANIFICADA");
            }
            labor.setFechaInicio(laborData.getFechaInicio());
        }
        labor.setFechaFin(laborData.getFechaFin());
        labor.setCostoTotal(laborData.getCostoTotal());
        labor.setObservaciones(laborData.getObservaciones());
        labor.setLote(laborData.getLote());

        if (labor.getLote() != null && (labor.getTipoLabor() == Labor.TipoLabor.SIEMBRA || labor.getTipoLabor() == Labor.TipoLabor.COSECHA)) {
            validarSolapamientoT1(labor.getLote().getId(), labor.getTipoLabor(), labor.getFechaInicio(), labor.getFechaFin(), labor.getId());
        }
        Labor guardada = laborRepository.save(labor);
        if (guardada.getLote() != null) {
            estadoLoteUpdater.recalcularEstado(guardada.getLote().getId());
        }
        return guardada;
    }

    /**
     * Actualiza una labor existente usando el mismo payload que el create (insumos, maquinaria, mano de obra).
     * Restaura inventario de insumos previos, elimina hijos, actualiza cabecera y vuelve a crear hijos desde el request.
     */
    @Transactional
    public Labor actualizarLaborDesdeRequest(Long id, CrearLaborRequest request, User usuario) {
        Labor labor = laborRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Labor no encontrada"));
        if (labor.getLote() != null && !laborQueryService.tieneAccesoAlLote(labor.getLote(), usuario)) {
            throw new RuntimeException("No tiene permisos para modificar esta labor");
        }
        if (!Boolean.TRUE.equals(labor.getActivo())) {
            throw new RuntimeException("No se puede actualizar una labor eliminada");
        }

        // Restaurar inventario de insumos actuales antes de borrarlos
        List<LaborInsumo> insumosExistentes = laborInsumoRepository.findByLaborId(id);
        if (!insumosExistentes.isEmpty()) {
            inventarioService.restaurarInventarioLabor(insumosExistentes, usuario, "Edición de labor");
        }
        laborInsumoRepository.deleteByLaborId(id);
        laborMaquinariaRepository.deleteByLabor(labor);
        laborManoObraRepository.deleteByLabor(labor);

        // Actualizar campos principales desde el request
        if (request.getTipoLabor() != null) {
            try {
                labor.setTipoLabor(Labor.TipoLabor.valueOf(request.getTipoLabor()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Tipo de labor inválido: " + request.getTipoLabor());
            }
        }
        labor.setDescripcion(request.getDescripcion());
        if (request.getFechaInicio() != null) labor.setFechaInicio(LocalDate.parse(request.getFechaInicio()));
        if (request.getFechaFin() != null) labor.setFechaFin(LocalDate.parse(request.getFechaFin()));
        if (request.getEstado() != null) {
            try {
                Labor.EstadoLabor nuevoEstado = Labor.EstadoLabor.valueOf(request.getEstado());
                if (!nuevoEstado.equals(labor.getEstado())) {
                    aplicarTransicionDeEstado(labor, nuevoEstado, null);
                }
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Estado de labor inválido: " + request.getEstado());
            }
        }
        labor.setResponsable(request.getResponsable());
        labor.setCostoTotal(request.getCostoTotal() != null ? request.getCostoTotal() : BigDecimal.ZERO);
        if (request.getLote() != null && request.getLote().get("id") != null) {
            Long loteId = Long.valueOf(request.getLote().get("id").toString());
            Plot lote = plotRepository.findById(loteId).orElse(null);
            if (lote != null && laborQueryService.tieneAccesoAlLote(lote, usuario)) labor.setLote(lote);
        }
        if (request.getCultivoId() != null) {
            labor.setCultivo(cultivoRepository.findById(request.getCultivoId()).orElse(null));
        }
        if (labor.getLote() != null && (labor.getTipoLabor() == Labor.TipoLabor.SIEMBRA || labor.getTipoLabor() == Labor.TipoLabor.COSECHA)) {
            validarSolapamientoT1(labor.getLote().getId(), labor.getTipoLabor(), labor.getFechaInicio(), labor.getFechaFin(), labor.getId());
        }
        Labor laborGuardada = laborRepository.save(labor);

        // Procesar insumos, maquinaria y mano de obra (misma lógica que crear)
        if (request.getInsumosUsados() != null && !request.getInsumosUsados().isEmpty()) {
            BigDecimal hectareas = BigDecimal.ZERO;
            if (laborGuardada.getLote() != null) {
                Plot lote = plotRepository.findById(laborGuardada.getLote().getId()).orElse(null);
                if (lote != null && lote.getAreaHectareas() != null) hectareas = lote.getAreaHectareas();
            }
            for (Map<String, Object> insumoData : request.getInsumosUsados()) {
                try {
                    Long insumoId = insumoData.get("insumo_id") != null ? Long.parseLong(insumoData.get("insumo_id").toString())
                        : insumoData.get("id") != null ? Long.parseLong(insumoData.get("id").toString()) : null;
                    if (insumoId == null) continue;
                    Insumo insumo = insumoRepository.findById(insumoId).orElseThrow(() -> new RuntimeException("Insumo no encontrado: " + insumoId));
                    BigDecimal cantidadUsada = insumoData.get("cantidad_usada") != null ? BigDecimal.valueOf(Double.parseDouble(insumoData.get("cantidad_usada").toString()))
                        : insumoData.get("cantidad") != null ? BigDecimal.valueOf(Double.parseDouble(insumoData.get("cantidad").toString())) : BigDecimal.ZERO;
                    BigDecimal cantidadPlanificada = insumoData.get("cantidad_planificada") != null ? BigDecimal.valueOf(Double.parseDouble(insumoData.get("cantidad_planificada").toString())) : cantidadUsada;
                    LaborInsumo li = new LaborInsumo();
                    li.setLabor(laborGuardada);
                    li.setInsumo(insumo);
                    li.setCantidadUsada(cantidadUsada);
                    li.setCantidadPlanificada(cantidadPlanificada);
                    BigDecimal costoUnitario = insumo.getPrecioUnitario() != null ? insumo.getPrecioUnitario() : BigDecimal.ZERO;
                    if (insumoData.get("costo_unitario") != null) costoUnitario = BigDecimal.valueOf(Double.parseDouble(insumoData.get("costo_unitario").toString()));
                    else if (insumoData.get("precio_unitario") != null) costoUnitario = BigDecimal.valueOf(Double.parseDouble(insumoData.get("precio_unitario").toString()));
                    li.setCostoUnitario(costoUnitario);
                    li.setCostoTotal(cantidadUsada.multiply(costoUnitario));
                    li.setObservaciones(insumoData.get("observaciones") != null ? insumoData.get("observaciones").toString() : null);
                    li = laborInsumoRepository.save(li);
                    if (insumo.getStockActual() != null && insumo.getStockActual().compareTo(cantidadUsada) >= 0) {
                        inventarioService.actualizarInventarioLabor(laborGuardada.getId(), List.of(li), null, usuario);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Error al procesar insumo: " + e.getMessage(), e);
                }
            }
        }
        if (request.getMaquinariaAsignada() != null && !request.getMaquinariaAsignada().isEmpty()) {
            for (Map<String, Object> maqData : request.getMaquinariaAsignada()) {
                LaborMaquinaria laborMaq = new LaborMaquinaria();
                laborMaq.setLabor(laborGuardada);
                laborMaq.setDescripcion(maqData.get("descripcion") != null ? maqData.get("descripcion").toString() : "");
                String proveedor = maqData.get("proveedor") != null ? maqData.get("proveedor").toString() : null;
                laborMaq.setTipoMaquinaria(proveedor != null && !proveedor.trim().isEmpty() ? TipoMaquinaria.ALQUILADA : TipoMaquinaria.PROPIA);
                laborMaq.setProveedor(proveedor);
                laborMaq.setCosto(maqData.get("costo") != null ? BigDecimal.valueOf(Double.parseDouble(maqData.get("costo").toString())) : BigDecimal.ZERO);
                laborMaq.setObservaciones(maqData.get("observaciones") != null ? maqData.get("observaciones").toString() : null);
                laborMaquinariaRepository.save(laborMaq);
            }
        }
        if (request.getManoObra() != null && !request.getManoObra().isEmpty()) {
            for (Map<String, Object> moData : request.getManoObra()) {
                LaborManoObra laborMo = new LaborManoObra();
                laborMo.setLabor(laborGuardada);
                laborMo.setDescripcion(moData.get("descripcion") != null ? moData.get("descripcion").toString() : "");
                laborMo.setCantidadPersonas(moData.get("cantidad_personas") != null ? Integer.valueOf(moData.get("cantidad_personas").toString()) : 1);
                laborMo.setProveedor(moData.get("proveedor") != null ? moData.get("proveedor").toString() : null);
                laborMo.setCostoTotal(moData.get("costo_total") != null ? BigDecimal.valueOf(Double.parseDouble(moData.get("costo_total").toString())) : BigDecimal.ZERO);
                laborMo.setHorasTrabajo(moData.get("horas_trabajo") != null ? BigDecimal.valueOf(Double.parseDouble(moData.get("horas_trabajo").toString())) : null);
                laborMo.setObservaciones(moData.get("observaciones") != null ? moData.get("observaciones").toString() : null);
                laborManoObraRepository.save(laborMo);
            }
        }
        BigDecimal costoTotal = laborInsumoRepository.findByLaborId(laborGuardada.getId()).stream().map(LaborInsumo::getCostoTotal).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        costoTotal = laborMaquinariaRepository.findByLaborId(laborGuardada.getId()).stream().map(LaborMaquinaria::getCosto).filter(Objects::nonNull).reduce(costoTotal, BigDecimal::add);
        costoTotal = laborManoObraRepository.findByLaborId(laborGuardada.getId()).stream().map(LaborManoObra::getCostoTotal).filter(Objects::nonNull).reduce(costoTotal, BigDecimal::add);
        laborGuardada.setCostoTotal(costoTotal);
        laborGuardada = laborRepository.save(laborGuardada);
        if (laborGuardada.getLote() != null) estadoLoteUpdater.recalcularEstado(laborGuardada.getLote().getId());
        return laborGuardada;
    }

    /**
     * Actualización parcial (PATCH) con máquina de estados (spec SDD).
     * Todas las transiciones de estado se delegan en aplicarTransicionDeEstado.
     * I5: solo se puede cambiar fecha_planificada si estado = PLANIFICADA.
     */
    public Labor actualizarParcialLabor(Long id, ActualizarLaborParcialRequest request, User usuario) {
        Labor labor = laborRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Labor no encontrada"));
        if (labor.getLote() != null && !laborQueryService.tieneAccesoAlLote(labor.getLote(), usuario)) {
            throw new RuntimeException("No tiene permisos para modificar esta labor");
        }
        if (!labor.getActivo()) {
            throw new RuntimeException("No se puede actualizar una labor eliminada");
        }

        if (request.getEstado() != null && !request.getEstado().isBlank()) {
            Labor.EstadoLabor nuevoEstado;
            String estadoReq = request.getEstado().trim().toUpperCase();
            if ("REALIZADA".equals(estadoReq)) {
                nuevoEstado = Labor.EstadoLabor.COMPLETADA;
            } else {
                try {
                    nuevoEstado = Labor.EstadoLabor.valueOf(estadoReq);
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Estado inválido: " + request.getEstado());
                }
            }
            aplicarTransicionDeEstado(labor, nuevoEstado, request.getFechaRealizacion());
        }

        if (request.getFechaPlanificada() != null) {
            if (labor.getEstado() != Labor.EstadoLabor.PLANIFICADA) {
                throw new IllegalStateException("Solo se puede reprogramar una labor en estado PLANIFICADA");
            }
            labor.setFechaInicio(request.getFechaPlanificada());
        }

        if (request.getFechaRealizacion() != null && labor.getEstado() == Labor.EstadoLabor.COMPLETADA) {
            if (labor.getFechaInicio() != null && request.getFechaRealizacion().isBefore(labor.getFechaInicio())) {
                throw new IllegalStateException("fechaRealizacion no puede ser anterior a fechaInicio");
            }
            labor.setFechaRealizacion(request.getFechaRealizacion());
        }

        if (request.getObservaciones() != null) {
            labor.setObservaciones(request.getObservaciones());
        }

        if (labor.getLote() != null && (labor.getTipoLabor() == Labor.TipoLabor.SIEMBRA || labor.getTipoLabor() == Labor.TipoLabor.COSECHA)) {
            validarSolapamientoT1(labor.getLote().getId(), labor.getTipoLabor(), labor.getFechaInicio(), labor.getFechaFin(), labor.getId());
        }
        Labor guardada = laborRepository.save(labor);
        if (guardada.getLote() != null) {
            estadoLoteUpdater.recalcularEstado(guardada.getLote().getId());
        }
        return guardada;
    }

    /**
     * Listar labores del usuario con filtros (spec SDD): fecha_desde, fecha_hasta, lote_id, estado, overdue.
     */
    @Transactional(readOnly = true)
    public List<Labor> getLaboresConFiltros(User user, LocalDate fechaDesde, LocalDate fechaHasta,
                                            Long loteId, String estado, Boolean soloVencidas) {
        List<Long> loteIds = laborQueryService.getLoteIdsByUser(user);
        if (loteIds.isEmpty()) {
            return List.of();
        }
        Labor.EstadoLabor estadoEnum = mapearEstadoFiltro(estado);
        boolean vencidas = Boolean.TRUE.equals(soloVencidas);
        if (loteId == null && estadoEnum == null && !vencidas && fechaDesde == null && fechaHasta == null) {
            return laborRepository.findByLoteIdInWithFetch(loteIds);
        }
        return laborRepository.findActivasFiltradasByLoteIdIn(
                loteIds, loteId, estadoEnum, vencidas, fechaDesde, fechaHasta, null);
    }

    private Labor.EstadoLabor mapearEstadoFiltro(String estado) {
        if (estado == null || estado.isBlank() || "vencidas".equalsIgnoreCase(estado.trim())) {
            return null;
        }
        if ("REALIZADA".equalsIgnoreCase(estado.trim())) {
            return Labor.EstadoLabor.COMPLETADA;
        }
        try {
            return Labor.EstadoLabor.valueOf(estado.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return switch (estado.trim().toLowerCase()) {
                case "planificada" -> Labor.EstadoLabor.PLANIFICADA;
                case "en_progreso" -> Labor.EstadoLabor.EN_PROGRESO;
                case "completada", "realizada" -> Labor.EstadoLabor.COMPLETADA;
                case "cancelada" -> Labor.EstadoLabor.CANCELADA;
                case "anulada" -> Labor.EstadoLabor.ANULADA;
                default -> null;
            };
        }
    }

    /**
     * Listar labores del usuario con filtros, devolviendo siempre DTO (contrato unificado).
     * GET /api/labores devuelve siempre List&lt;LaborDetalladoDTO&gt; con o sin filtros.
     * Usa carga batch para evitar N+1.
     */
    @Transactional(readOnly = true)
    public List<LaborDetalladoDTO> getLaboresDetalladasConFiltros(User user, LocalDate fechaDesde, LocalDate fechaHasta,
                                                                  Long loteId, String estado, Boolean soloVencidas) {
        List<Labor> labores = getLaboresConFiltros(user, fechaDesde, fechaHasta, loteId, estado, soloVencidas);
        return convertirLaboresADetalladoDTOBatch(labores);
    }

    /**
     * Eliminar labor según su estado.
     * 
     * PLANIFICADA: Se cancela y restauran los insumos automáticamente
     * EN_PROGRESO/COMPLETADA: Requiere anulación formal con justificación
     */
    public void eliminarLabor(Long id, User usuario) {
        Labor labor = laborRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Labor no encontrada con ID: " + id));
        
        // Verificar si la labor ya está inactiva
        if (!labor.getActivo()) {
            throw new RuntimeException("La labor ya está eliminada");
        }
        
        // Verificar permisos sobre el lote
        if (labor.getLote() != null && !laborQueryService.tieneAccesoAlLote(labor.getLote(), usuario)) {
            throw new RuntimeException("No tiene permisos para eliminar esta labor. Usuario: " + usuario.getEmail() + ", Lote: " + labor.getLote().getId());
        }
        
        // Si es OPERARIO, solo puede eliminar sus propias labores
        if (usuario.tieneRolEnEmpresa(RolEmpresa.OPERARIO)) {
            String nombreUsuario = usuario.getFirstName() + " " + usuario.getLastName();
            String responsableLabor = labor.getResponsable();
            
            if (responsableLabor == null || !responsableLabor.equalsIgnoreCase(nombreUsuario)) {
                throw new RuntimeException("Los operarios solo pueden eliminar sus propias labores");
            }
        }
        
        // Caso 1: Labor PLANIFICADA → Cancelar y restaurar insumos automáticamente
        if (labor.isPlanificada()) {
            System.out.println("Cancelando labor planificada ID: " + id);
            
            // Obtener insumos de la labor
            List<LaborInsumo> insumosLabor = laborInsumoRepository.findByLaborId(id);
            
            // Restaurar insumos al inventario
            if (!insumosLabor.isEmpty()) {
                inventarioService.restaurarInventarioLabor(
                    insumosLabor, 
                    usuario, 
                    "Cancelación de labor planificada"
                );
                System.out.println("Restaurados " + insumosLabor.size() + " insumos al inventario");
            }
            
            // Marcar como cancelada
            labor.setEstado(Labor.EstadoLabor.CANCELADA);
            labor.setActivo(false);
            laborRepository.save(labor);
            if (labor.getLote() != null) {
                estadoLoteUpdater.recalcularEstado(labor.getLote().getId());
            }
            System.out.println("Labor cancelada exitosamente");
        }
        // Caso 2: Labor EN_PROGRESO o COMPLETADA → Requiere anulación formal
        else if (labor.requiereAnulacionFormal()) {
            throw new RuntimeException(
                "Esta labor está " + labor.getEstado() + " y requiere anulación formal. " +
                "Use el proceso de anulación con justificación (solo ADMINISTRADOR)."
            );
        }
        // Caso 3: Ya está CANCELADA o ANULADA
        else {
            labor.setActivo(false);
            laborRepository.save(labor);
            if (labor.getLote() != null) {
                estadoLoteUpdater.recalcularEstado(labor.getLote().getId());
            }
        }
    }

    /**
     * Anular una labor ejecutada (EN_PROGRESO o COMPLETADA).
     * Requiere permisos de ADMINISTRADOR y justificación obligatoria.
     * 
     * @param id ID de la labor
     * @param justificacion Motivo de la anulación (obligatorio)
     * @param restaurarInsumos Si true, restaura los insumos al inventario
     * @param usuario Usuario que realiza la anulación (debe ser ADMINISTRADOR)
     */
    public void anularLabor(Long id, String justificacion, boolean restaurarInsumos, User usuario) {
        Labor labor = laborRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Labor no encontrada con ID: " + id));
        
        // Verificar si la labor ya está inactiva
        if (!labor.getActivo()) {
            throw new RuntimeException("La labor ya está eliminada o anulada");
        }
        
        // Verificar permisos: debe ser ADMINISTRADOR o JEFE_CAMPO
        if (!usuario.isSuperAdmin() && 
            !usuario.esAdministradorEmpresa(usuario.getEmpresa() != null ? usuario.getEmpresa().getId() : null) &&
            !usuario.tieneRolEnEmpresa(RolEmpresa.JEFE_CAMPO)) {
            throw new RuntimeException("Solo los ADMINISTRADORES y JEFE_CAMPO pueden anular labores ejecutadas");
        }
        
        // Verificar permisos sobre el lote
        if (labor.getLote() != null && !laborQueryService.tieneAccesoAlLote(labor.getLote(), usuario)) {
            throw new RuntimeException("No tiene permisos para anular esta labor");
        }
        
        // Validar justificación
        if (justificacion == null || justificacion.trim().isEmpty()) {
            throw new RuntimeException("La justificación es obligatoria para anular una labor");
        }
        
        if (justificacion.length() > 1000) {
            throw new RuntimeException("La justificación no puede exceder 1000 caracteres");
        }
        
        System.out.println("Anulando labor ID: " + id + " por: " + usuario.getEmail());
        
        // Obtener insumos de la labor
        List<LaborInsumo> insumosLabor = laborInsumoRepository.findByLaborId(id);
        
        // Restaurar insumos si se solicita
        if (restaurarInsumos && !insumosLabor.isEmpty()) {
            inventarioService.restaurarInventarioLabor(
                insumosLabor, 
                usuario, 
                "Anulación de labor: " + justificacion
            );
            System.out.println("Restaurados " + insumosLabor.size() + " insumos al inventario por anulación");
        }
        
        // Marcar como anulada con auditoría completa
        labor.setEstado(Labor.EstadoLabor.ANULADA);
        labor.setActivo(false);
        labor.setMotivoAnulacion(justificacion);
        labor.setFechaAnulacion(java.time.LocalDateTime.now());
        labor.setUsuarioAnulacion(usuario);
        
        laborRepository.save(labor);
        if (labor.getLote() != null) {
            estadoLoteUpdater.recalcularEstado(labor.getLote().getId());
        }
        System.out.println("Labor anulada exitosamente. Insumos restaurados: " + restaurarInsumos);
    }

    /**
     * Obtener labores por lote
     */
    public List<Labor> getLaboresByLote(Long loteId, User usuario) {
        Optional<Plot> lote = plotRepository.findById(loteId);
        
        if (lote.isPresent() && laborQueryService.tieneAccesoAlLote(lote.get(), usuario)) {
            return laborRepository.findByLoteIdOrderByFechaInicioDesc(loteId);
        }
        
        return List.of();
    }

    /**
     * Verificar si el usuario pertenece a la misma empresa que el lote
     */
    private boolean perteneceAMismaEmpresa(User usuario, Plot lote) {
        // Obtener empresa del usuario usando el método getEmpresa() que tiene manejo de excepciones
        Empresa empresaUsuario = usuario.getEmpresa();
        if (empresaUsuario != null) {
            Long empresaUsuarioId = empresaUsuario.getId();
            
            // Obtener empresa del lote
            Long empresaLote = obtenerEmpresaDelLote(lote);
            
            return empresaUsuarioId.equals(empresaLote);
        }
        
        // Si no tiene empresa, asumir que puede acceder (compatibilidad legacy)
        return true;
    }
    
    /**
     * Obtener la empresa asociada al lote
     */
    private Long obtenerEmpresaDelLote(Plot lote) {
        // Como la tabla lotes no tiene empresa_id, usar la empresa del usuario propietario
        if (lote.getUser() != null) {
            Empresa empresa = lote.getUser().getEmpresa();
            if (empresa != null) {
                return empresa.getId();
            }
        }
        
        // Default: empresa ID 1 (para compatibilidad)
        return 1L;
    }
    
    /**
     * Obtener rol del usuario usando el sistema completo de roles multitenant
     */
    private Rol obtenerRolUsuario(User usuario) {
        if (usuario == null) {
            return Rol.PRODUCTOR; // Default
        }
        
        // 1. Verificar en UserCompanyRoles (sistema multitenant) primero
        // Usar el método getRoles() que tiene manejo de excepciones
        try {
            Set<Role> roles = usuario.getRoles();
            if (roles != null && !roles.isEmpty()) {
                Role role = roles.iterator().next();
                if (role != null && role.getNombre() != null) {
                    try {
                        return Rol.valueOf(role.getNombre());
                    } catch (IllegalArgumentException e) {
                        System.err.println("Rol no válido en Role: " + role.getNombre());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error al obtener roles del usuario: " + e.getMessage());
        }
        
        // 2. Fallback: verificar en el sistema legacy (Roles directos)
        if (!usuario.getRoles().isEmpty()) {
            Role role = usuario.getRoles().iterator().next();
            if (role.getNombre() != null) {
                try {
                    return Rol.valueOf(role.getNombre());
                } catch (IllegalArgumentException e) {
                    System.err.println("Rol no válido en Roles legacy: " + role.getNombre());
                }
            }
        }
        
        // 3. Default: PRODUCTOR
        return Rol.PRODUCTOR;
    }
    
    /**
     * Verificar si el rol del usuario permite acceso a labores
     */
    private boolean tienePermisoParaLabores(Rol rol) {
        switch (rol) {
            case SUPERADMIN:
            case ADMINISTRADOR:
            case PRODUCTOR:
            case TECNICO:
            case ASESOR:
            case OPERARIO:  // ← CORREGIDO: OPERARIO SÍ puede crear labores
                return true;
            case INVITADO:
                return false; // Solo INVITADO no puede gestionar labores
            default:
                return false;
        }
    }

    /**
     * Contar labores por usuario
     */
    public long contarLaboresPorUsuario(User usuario) {
        List<Plot> lotesUsuario = plotRepository.findAccessibleByUser(usuario);
        
        List<Long> loteIds = lotesUsuario.stream()
            .map(Plot::getId)
            .toList();
        
        if (loteIds.isEmpty()) {
            return 0;
        }
        
        return laborRepository.countByLoteIdIn(loteIds);
    }
    
    /**
     * Obtener todas las labores accesibles por el usuario con costos detallados.
     * Usa carga batch para evitar N+1.
     */
    @Transactional(readOnly = true)
    public List<LaborDetalladoDTO> getLaboresDetalladasByUser(User user) {
        List<Labor> labores = getLaboresByUser(user);
        return convertirLaboresADetalladoDTOBatch(labores);
    }

    /**
     * Obtener labores detalladas paginadas. Paginación a nivel JPA (IDs primero, luego fetch).
     * Evita N+1 y explosión cartesiana. countQuery sin JOIN FETCH.
     */
    @Transactional(readOnly = true)
    public Page<LaborDetalladoDTO> getLaboresDetalladasPaginadas(User user, Pageable pageable) {
        return getLaboresDetalladasPaginadas(user, pageable, null);
    }

    /**
     * Labores detalladas paginadas con filtros opcionales en BD.
     */
    @Transactional(readOnly = true)
    public Page<LaborDetalladoDTO> getLaboresDetalladasPaginadas(User user, Pageable pageable, FiltrosLaboresDTO filtros) {
        List<Long> loteIds = laborQueryService.getLoteIdsByUser(user);
        if (loteIds.isEmpty()) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
        Page<Long> paginaIds;
        if (filtros != null && filtros.tieneFiltros()) {
            String busqueda = filtros.getBusqueda() != null && !filtros.getBusqueda().isBlank()
                    ? filtros.getBusqueda().trim() : null;
            paginaIds = laborRepository.findLaborIdsFiltradasByLoteIdIn(
                    loteIds,
                    filtros.getLoteId(),
                    filtros.getEstado(),
                    Boolean.TRUE.equals(filtros.getSoloVencidas()),
                    busqueda,
                    pageable);
        } else {
            paginaIds = laborRepository.findLaborIdsByLoteIdIn(loteIds, pageable);
        }
        List<Long> ids = paginaIds.getContent();
        if (ids.isEmpty()) {
            return new PageImpl<>(new ArrayList<>(), pageable, paginaIds.getTotalElements());
        }
        List<Labor> labores = laborRepository.findByIdInWithFetch(ids);
        Map<Long, Integer> ordenPorId = new java.util.HashMap<>();
        for (int i = 0; i < ids.size(); i++) {
            ordenPorId.put(ids.get(i), i);
        }
        labores.sort((a, b) -> Integer.compare(
                ordenPorId.getOrDefault(a.getId(), Integer.MAX_VALUE),
                ordenPorId.getOrDefault(b.getId(), Integer.MAX_VALUE)));
        List<LaborDetalladoDTO> dtos = convertirLaboresADetalladoDTOBatch(labores);
        return new PageImpl<>(dtos, pageable, paginaIds.getTotalElements());
    }

    /**
     * Obtener labor detallada por ID verificando permisos
     */
    public Optional<LaborDetalladoDTO> getLaborDetalladaById(Long id, User user) {
        Optional<Labor> laborOpt = getLaborById(id, user);
        
        if (laborOpt.isPresent()) {
            Labor labor = laborOpt.get();
            return Optional.of(convertirADetalladoDTO(labor));
        }
        
        return Optional.empty();
    }
    
    /**
     * Conversión batch: evita N+1 cargando maquinarias, mano de obra e insumos en 3 queries.
     */
    private List<LaborDetalladoDTO> convertirLaboresADetalladoDTOBatch(List<Labor> labores) {
        if (labores == null || labores.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> laborIds = labores.stream().map(Labor::getId).collect(Collectors.toList());
        
        List<LaborMaquinaria> todasMaquinarias = laborMaquinariaRepository.findByLaborIdIn(laborIds);
        List<LaborManoObra> todaManoObra = laborManoObraRepository.findByLaborIdIn(laborIds);
        List<LaborInsumo> todosInsumos = laborInsumoRepository.findByLaborIdInWithInsumo(laborIds);
        
        Map<Long, List<LaborMaquinaria>> maqPorLabor = todasMaquinarias.stream()
                .collect(Collectors.groupingBy(lm -> lm.getLabor().getId()));
        Map<Long, List<LaborManoObra>> moPorLabor = todaManoObra.stream()
                .collect(Collectors.groupingBy(lmo -> lmo.getLabor().getId()));
        Map<Long, List<LaborInsumo>> insPorLabor = todosInsumos.stream()
                .collect(Collectors.groupingBy(li -> li.getLabor().getId()));
        
        return labores.stream()
                .map(l -> convertirADetalladoDTOConMapas(
                        l,
                        maqPorLabor.getOrDefault(l.getId(), new ArrayList<>()),
                        moPorLabor.getOrDefault(l.getId(), new ArrayList<>()),
                        insPorLabor.getOrDefault(l.getId(), new ArrayList<>())))
                .collect(Collectors.toList());
    }

    /**
     * Convertir entidad Labor a LaborDetalladoDTO usando colecciones pre-cargadas (batch).
     */
    private LaborDetalladoDTO convertirADetalladoDTOConMapas(Labor labor,
            List<LaborMaquinaria> maquinarias, List<LaborManoObra> manoObra, List<LaborInsumo> insumos) {
        
        // Calcular costos totales
        BigDecimal costoMaquinaria = maquinarias.stream()
            .map(LaborMaquinaria::getCosto)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        BigDecimal costoManoObra = manoObra.stream()
            .map(LaborManoObra::getCostoTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        BigDecimal costoInsumos = insumos.stream()
            .map(LaborInsumo::getCostoTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calcular horas totales (solo de mano de obra, ya que maquinaria no tiene horas)
        BigDecimal horasTotales = manoObra.stream()
            .map(LaborManoObra::getHorasTrabajo)
            .filter(horas -> horas != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Convertir a DTOs
        List<LaborMaquinariaDTO> maquinariasDTO = maquinarias.stream()
            .map(this::convertirMaquinariaADTO)
            .collect(Collectors.toList());
            
        List<LaborManoObraDTO> manoObraDTO = manoObra.stream()
            .map(this::convertirManoObraADTO)
            .collect(Collectors.toList());
            
        List<LaborInsumoDTO> insumosDTO = insumos.stream()
            .map(this::convertirInsumoADTO)
            .collect(Collectors.toList());
        
        LaborDetalladoDTO dto = new LaborDetalladoDTO(
            labor.getId(),
            labor.getTipoLabor() != null ? labor.getTipoLabor().toString() : null,
            labor.getDescripcion(), // Usar descripcion en lugar de nombre
            labor.getDescripcion(),
            labor.getFechaInicio(),
            labor.getFechaFin(),
            labor.getEstado() != null ? labor.getEstado().toString() : null,
            costoInsumos, // costoBase = insumos (no el total, para no duplicar en tabla)
            labor.getObservaciones(),
            labor.getLote() != null ? labor.getLote().getId() : null,
            labor.getLote() != null ? labor.getLote().getNombre() : null,
            labor.getResponsable(), // responsable - obtener de la entidad
            horasTotales, // horasTrabajo - calculadas de mano de obra
            labor.getCostoTotal(),
            null, // progreso - no existe en la entidad
            labor.getFechaCreacion(),
            labor.getFechaActualizacion(),
            labor.getActivo(),
            null, // empresaId - no existe en la entidad
            labor.getUsuario() != null ? labor.getUsuario().getId() : null,
            costoMaquinaria,
            costoManoObra,
            costoInsumos,
            maquinariasDTO,
            manoObraDTO,
            insumosDTO
        );
        
        // Spec SDD: campos derivados
        dto.setFechaRealizacion(labor.getFechaRealizacion());
        dto.setCultivoId(labor.getCultivo() != null ? labor.getCultivo().getId() : null);
        dto.setOverdue(labor.isVencida());

        // Agregar información adicional del lote
        if (labor.getLote() != null) {
            Plot lote = labor.getLote();
            dto.setLoteSuperficie(lote.getAreaHectareas());
            dto.setLoteCultivo(lote.getCultivoActual());
            if (lote.getCampo() != null) {
                dto.setLoteCampo(lote.getCampo().getNombre());
            }
        }
        
        return dto;
    }

    /**
     * Convertir una sola Labor a DTO (para getLaborDetalladaById).
     * Reutiliza la lógica batch con lista de un elemento.
     */
    private LaborDetalladoDTO convertirADetalladoDTO(Labor labor) {
        List<LaborDetalladoDTO> dtos = convertirLaboresADetalladoDTOBatch(List.of(labor));
        return dtos.isEmpty() ? null : dtos.get(0);
    }
    
    /**
     * Convertir LaborMaquinaria a DTO
     */
    private LaborMaquinariaDTO convertirMaquinariaADTO(LaborMaquinaria maquinaria) {
        return new LaborMaquinariaDTO(
            maquinaria.getIdLaborMaquinaria(),
            maquinaria.getLabor().getId(),
            maquinaria.getTipoMaquinaria() != null ? maquinaria.getTipoMaquinaria().toString() : "PROPIA",
            maquinaria.getDescripcion(),
            maquinaria.getProveedor(),
            maquinaria.getCosto(),
            null, // horas_uso eliminado
            null, // kilometros_recorridos eliminado
            maquinaria.getObservaciones(),
            maquinaria.getCreatedAt(),
            maquinaria.getUpdatedAt()
        );
    }
    
    /**
     * Convertir LaborManoObra a DTO
     */
    private LaborManoObraDTO convertirManoObraADTO(LaborManoObra manoObra) {
        return new LaborManoObraDTO(
            manoObra.getIdLaborManoObra(),
            manoObra.getLabor().getId(),
            manoObra.getDescripcion(),
            manoObra.getCantidadPersonas(),
            manoObra.getProveedor(),
            manoObra.getCostoTotal(),
            manoObra.getHorasTrabajo(),
            null, // costo_por_hora eliminado
            manoObra.getObservaciones(),
            manoObra.getCreatedAt(),
            manoObra.getUpdatedAt()
        );
    }

    /**
     * Agregar maquinaria a una labor
     */
    public LaborMaquinaria agregarMaquinaria(Long laborId, LaborMaquinaria maquinaria, User user) {
        // Verificar que la labor existe y el usuario tiene acceso
        Labor labor = laborRepository.findById(laborId)
            .orElseThrow(() -> new RuntimeException("Labor no encontrada"));
        
        // Verificar acceso del usuario a la labor
        if (!tieneAccesoALabor(labor, user)) {
            throw new RuntimeException("No tiene acceso a esta labor");
        }
        
        // Asignar la labor a la maquinaria
        maquinaria.setLabor(labor);
        
        // Guardar la maquinaria
        return laborMaquinariaRepository.save(maquinaria);
    }

    /**
     * Agregar mano de obra a una labor
     */
    public LaborManoObra agregarManoObra(Long laborId, LaborManoObra manoObra, User user) {
        // Verificar que la labor existe y el usuario tiene acceso
        Labor labor = laborRepository.findById(laborId)
            .orElseThrow(() -> new RuntimeException("Labor no encontrada"));
        
        // Verificar acceso del usuario a la labor
        if (!tieneAccesoALabor(labor, user)) {
            throw new RuntimeException("No tiene acceso a esta labor");
        }
        
        // Asignar la labor a la mano de obra
        manoObra.setLabor(labor);
        
        // Guardar la mano de obra
        return laborManoObraRepository.save(manoObra);
    }

    /**
     * Verificar si el usuario tiene acceso a una labor
     */
    private boolean tieneAccesoALabor(Labor labor, User user) {
        // Obtener todos los lotes accesibles por el usuario
        List<Plot> lotesUsuario = plotRepository.findAccessibleByUser(user);
        
        // Verificar si el lote de la labor está en la lista de lotes accesibles
        return lotesUsuario.stream()
            .anyMatch(lote -> lote.getId().equals(labor.getLote().getId()));
    }

    /**
     * Eliminar labor lógicamente (con validación de acceso)
     */
    public boolean deleteLabor(Long id, User user) {
        Optional<Labor> laborOpt = laborRepository.findById(id);
        
        if (laborOpt.isPresent()) {
            Labor labor = laborOpt.get();
            
            // Verificar acceso al lote de la labor
            if (!tieneAccesoALabor(labor, user)) {
                System.err.println("[LABOR_SERVICE] Usuario no tiene acceso al lote de la labor");
                return false;
            }
            
            // Si es OPERARIO, solo puede eliminar sus propias labores
            if (user.tieneRolEnEmpresa(RolEmpresa.OPERARIO)) {
                String nombreUsuario = user.getFirstName() + " " + user.getLastName();
                String responsableLabor = labor.getResponsable();
                
                System.out.println("[LABOR_SERVICE] Validando permiso para OPERARIO:");
                System.out.println("[LABOR_SERVICE] - Usuario: " + nombreUsuario);
                System.out.println("[LABOR_SERVICE] - Responsable labor: " + responsableLabor);
                
                if (responsableLabor == null || !responsableLabor.equalsIgnoreCase(nombreUsuario)) {
                    System.err.println("[LABOR_SERVICE] OPERARIO no puede eliminar labores de otros");
                    throw new RuntimeException("Los operarios solo pueden eliminar sus propias labores");
                }
            }
            
            // I3: solo desactivar si estado es CANCELADA o ANULADA
            if (labor.getEstado() != Labor.EstadoLabor.CANCELADA && labor.getEstado() != Labor.EstadoLabor.ANULADA) {
                throw new IllegalStateException("Cannot deactivate labor unless it is CANCELADA or ANULADA");
            }
            labor.setActivo(false);
            laborRepository.save(labor);
            if (labor.getLote() != null) {
                estadoLoteUpdater.recalcularEstado(labor.getLote().getId());
            }
            System.out.println("[LABOR_SERVICE] Labor eliminada exitosamente por: " + user.getEmail());
            return true;
        }
        return false;
    }

    /**
     * Eliminar labor físicamente (solo para administradores)
     */
    public boolean deleteLaborFisicamente(Long id, User user) {
        if (!user.isAdmin()) {
            return false;
        }
        Optional<Labor> laborOpt = laborRepository.findById(id);
        if (laborOpt.isPresent()) {
            Long loteId = laborOpt.get().getLote() != null ? laborOpt.get().getLote().getId() : null;
            laborRepository.delete(laborOpt.get());
            if (loteId != null) {
                estadoLoteUpdater.recalcularEstado(loteId);
            }
            return true;
        }
        return false;
    }
    
    /**
     * Convertir LaborInsumo a DTO
     */
    private LaborInsumoDTO convertirInsumoADTO(LaborInsumo laborInsumo) {
        return new LaborInsumoDTO(
            laborInsumo.getIdLaborInsumo(),
            laborInsumo.getLabor().getId(),
            laborInsumo.getInsumo().getId(),
            laborInsumo.getInsumo().getNombre(),
            laborInsumo.getInsumo().getTipo().name(),
            laborInsumo.getCantidadUsada(),
            laborInsumo.getCantidadPlanificada(),
            laborInsumo.getCostoUnitario(),
            laborInsumo.getCostoTotal(),
            laborInsumo.getObservaciones(),
            laborInsumo.getCreatedAt(),
            laborInsumo.getUpdatedAt()
        );
    }
    
    /**
     * Generar reporte de cosecha para un lote específico
     */
    public ReporteCosechaDTO generarReporteCosecha(Long loteId, User usuario) {
        Plot lote = plotRepository.findById(loteId)
            .orElseThrow(() -> new RuntimeException("Lote no encontrado"));
            
        // Verificar permisos
        if (!laborQueryService.tieneAccesoAlLote(lote, usuario)) {
            throw new RuntimeException("No tiene permisos para acceder a este lote");
        }
        
        // Buscar labor de cosecha más reciente
        List<Labor> laboresCosecha = laborRepository.findByLoteAndTipoLaborOrderByFechaInicioDesc(
            lote, Labor.TipoLabor.COSECHA
        );
        
        if (laboresCosecha.isEmpty()) {
            throw new RuntimeException("No se encontraron labores de cosecha para este lote");
        }
        
        Labor laborCosecha = laboresCosecha.get(0);
        
        // Calcular cantidades
        BigDecimal cantidadObtenida = extraerCantidadCosechada(laborCosecha.getDescripcion());
        BigDecimal cantidadEsperada = null;
        
        if (lote.getRendimientoEsperado() != null && lote.getAreaHectareas() != null) {
            cantidadEsperada = lote.getRendimientoEsperado().multiply(lote.getAreaHectareas());
        }
        
        // Crear reporte
        ReporteCosechaDTO reporte = new ReporteCosechaDTO(
            lote.getId(),
            lote.getNombre(),
            lote.getCultivoActual(),
            lote.getFechaSiembra(),
            lote.getFechaCosechaReal(),
            lote.getAreaHectareas(),
            cantidadEsperada,
            cantidadObtenida,
            "kg", // Unidad estandarizada
            lote.getRendimientoEsperado(),
            lote.getRendimientoReal(),
            lote.getRendimientoReal() != null && lote.getRendimientoEsperado() != null && 
            lote.getRendimientoEsperado().compareTo(BigDecimal.ZERO) > 0 ?
                lote.getRendimientoReal().divide(lote.getRendimientoEsperado(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")) : null,
            lote.getEstado() != null ? lote.getEstado().getDescripcion() : "Sin estado",
            laborCosecha.getObservaciones()
        );
        
        return reporte;
    }
    
    /**
     * Generar reporte de cosecha para todos los lotes del usuario
     */
    public List<ReporteCosechaDTO> generarReportesCosechaPorUsuario(User usuario) {
        List<Plot> lotes = plotRepository.findByUserIdOrParentUserId(usuario.getId());
        
        return lotes.stream()
            .filter(lote -> (lote.getTipoUso() == null || lote.getTipoUso() == Plot.TipoUsoLote.CULTIVO)) // Excluir lotes porcinos
            .filter(lote -> lote.getFechaCosechaReal() != null) // Solo lotes cosechados
            .map(lote -> {
                try {
                    return generarReporteCosecha(lote.getId(), usuario);
                } catch (Exception e) {
                    System.out.println("⚠️ [LaborService] Error generando reporte para lote " + lote.getId() + ": " + e.getMessage());
                    return null;
                }
            })
            .filter(reporte -> reporte != null)
            .collect(Collectors.toList());
    }
    
    /**
     * Obtener estadísticas de cosecha por cultivo
     */
    public Map<String, Object> obtenerEstadisticasCosechaPorCultivo(User usuario) {
        List<ReporteCosechaDTO> reportes = generarReportesCosechaPorUsuario(usuario);
        
        Map<String, Object> estadisticas = new java.util.HashMap<>();
        Map<String, List<ReporteCosechaDTO>> reportesPorCultivo = reportes.stream()
            .collect(Collectors.groupingBy(ReporteCosechaDTO::getCultivoActual));
        
        for (Map.Entry<String, List<ReporteCosechaDTO>> entry : reportesPorCultivo.entrySet()) {
            String cultivo = entry.getKey();
            List<ReporteCosechaDTO> reportesCultivo = entry.getValue();
            
            Map<String, Object> statsCultivo = new java.util.HashMap<>();
            statsCultivo.put("totalCosechas", reportesCultivo.size());
            
            // Calcular promedios
            BigDecimal rendimientoPromedio = reportesCultivo.stream()
                .map(ReporteCosechaDTO::getRendimientoReal)
                .filter(r -> r != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(reportesCultivo.size()), 2, RoundingMode.HALF_UP);
            
            BigDecimal cumplimientoPromedio = reportesCultivo.stream()
                .map(ReporteCosechaDTO::getPorcentajeCumplimiento)
                .filter(c -> c != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(reportesCultivo.size()), 2, RoundingMode.HALF_UP);
            
            statsCultivo.put("rendimientoPromedio", rendimientoPromedio);
            statsCultivo.put("cumplimientoPromedio", cumplimientoPromedio);
            
            // Contar por categorías de cumplimiento
            long excelentes = reportesCultivo.stream().mapToLong(r -> r.isSobreCumplimiento() ? 1 : 0).sum();
            long buenos = reportesCultivo.stream().mapToLong(r -> r.isCumplimientoSatisfactorio() && !r.isSobreCumplimiento() ? 1 : 0).sum();
            long regulares = reportesCultivo.stream().mapToLong(r -> r.isSubCumplimiento() ? 1 : 0).sum();
            
            statsCultivo.put("excelentes", excelentes);
            statsCultivo.put("buenos", buenos);
            statsCultivo.put("regulares", regulares);
            
            estadisticas.put(cultivo, statsCultivo);
        }
        
        return estadisticas;
    }
    
    /**
     * Obtiene las tareas disponibles según el estado del lote usando configuración
     * Si hay configuración personalizada, la usa; si no, devuelve lista vacía (no hay configuración)
     * Solo usa el método tradicional si explícitamente no hay cultivo asignado
     */
    public List<String> getTareasDisponiblesPorEstado(EstadoLote estadoEnum, Long tipoCultivoId, Long empresaId) {
        // Si hay tipo de cultivo y empresa, intentar usar configuración
        if (tipoCultivoId != null && empresaId != null) {
            try {
                // Buscar estado configurado
                List<EstadoLoteConfig> estadosConfig = configuracionEstadosService.obtenerEstadosPorTipoCultivo(tipoCultivoId, empresaId);
                
                // Si no hay estados configurados para este tipo de cultivo, devolver lista vacía
                if (estadosConfig == null || estadosConfig.isEmpty()) {
                    System.out.println("⚠️ [LaborService] No hay configuración de estados para tipoCultivoId: " + tipoCultivoId);
                    return new ArrayList<>(); // No hay configuración, no mostrar tareas
                }
                
                // Buscar el estado que corresponde al enum (nombres config: "Disponible", "Listo para Cosecha", "Establecimiento", etc.)
                String enumNombreNorm = (estadoEnum.name().replace("_", " ")).toLowerCase().replace(" ", "");
                Optional<EstadoLoteConfig> estadoConfigOpt = estadosConfig.stream()
                    .filter(e -> {
                        if (e.getNombre() == null) return false;
                        String configNorm = e.getNombre().toLowerCase().replace(" ", "");
                        if (e.getNombre().equalsIgnoreCase(estadoEnum.name())
                            || e.getNombre().equalsIgnoreCase(estadoEnum.getDescripcion())
                            || configNorm.equals(enumNombreNorm)) {
                            return true;
                        }
                        // Equivalencias: EN_CRECIMIENTO puede coincidir con "Establecimiento", "Rebrote", "En Crecimiento" (ej. tipo Alfalfa)
                        if (estadoEnum == EstadoLote.EN_CRECIMIENTO) {
                            return configNorm.contains("crecimiento") || configNorm.contains("establecimiento") || configNorm.contains("rebrote");
                        }
                        // COSECHADO: en Alfalfa no hay estado "Cosechado"; tras el corte sigue "Rebrote". Otros tipos pueden tener "Cosechado" o "Levantado".
                        if (estadoEnum == EstadoLote.COSECHADO) {
                            return configNorm.contains("rebrote") || configNorm.contains("cosechado") || configNorm.contains("levantado") || configNorm.contains("dormancia");
                        }
                        return false;
                    })
                    .findFirst();
                
                if (estadoConfigOpt.isPresent()) {
                    EstadoLoteConfig estadoConfig = estadoConfigOpt.get();
                    // Obtener tareas configuradas para ese estado
                    List<TareaPorEstadoConfig> tareasConfig = tareaPorEstadoConfigRepository
                        .findTareasDisponiblesPorEstado(estadoConfig.getId(), empresaId);
                    
                    if (tareasConfig != null && !tareasConfig.isEmpty()) {
                        return tareasConfig.stream()
                            .map(TareaPorEstadoConfig::getTipoLabor)
                            .distinct()
                            .map(String::toLowerCase)
                            .collect(Collectors.toList());
                    } else {
                        System.out.println("⚠️ [LaborService] No hay tareas configuradas para el estado: " + estadoConfig.getNombre());
                        return new ArrayList<>(); // No hay tareas configuradas para este estado
                    }
                } else {
                    System.out.println("⚠️ [LaborService] No se encontró estado configurado que coincida con: " + estadoEnum.name());
                    return new ArrayList<>(); // No hay estado configurado que coincida
                }
            } catch (Exception e) {
                System.out.println("❌ [LaborService] Error al obtener tareas desde configuración: " + e.getMessage());
                e.printStackTrace();
                // Si hay error, no devolver tareas tradicionales si hay tipoCultivoId
                // (indica que debería haber configuración pero falló)
                return new ArrayList<>();
            }
        }
        
        // Fallback: lote sin tipo de cultivo (ej. Disponible o En preparación sin cultivo asignado)
        // Buscar en plantillas un estado que coincida con el enum y devolver sus tareas.
        // Si el enum no existe en plantillas (ej. EN_PREPARACION), se mapea a un estado equivalente.
        if (empresaId != null) {
            try {
                String nombreEstadoPlantillaABuscar = mapEstadoEnumANombrePlantilla(estadoEnum);
                String enumNombreNorm = (estadoEnum.name().replace("_", " ")).toLowerCase().replace(" ", "");
                List<TipoCultivo> plantillas = configuracionEstadosService.obtenerPlantillas();
                for (TipoCultivo tipo : plantillas) {
                    List<EstadoLoteConfig> estadosConfig = configuracionEstadosService.obtenerEstadosPorTipoCultivo(tipo.getId(), null);
                    if (estadosConfig == null || estadosConfig.isEmpty()) continue;
                    Optional<EstadoLoteConfig> estadoConfigOpt = estadosConfig.stream()
                        .filter(e -> e.getNombre() != null && (
                            (nombreEstadoPlantillaABuscar != null && e.getNombre().equalsIgnoreCase(nombreEstadoPlantillaABuscar))
                            || e.getNombre().equalsIgnoreCase(estadoEnum.name())
                            || e.getNombre().equalsIgnoreCase(estadoEnum.getDescripcion())
                            || e.getNombre().toLowerCase().replace(" ", "").equals(enumNombreNorm)))
                        .findFirst();
                    if (estadoConfigOpt.isPresent()) {
                        List<TareaPorEstadoConfig> tareasConfig = tareaPorEstadoConfigRepository
                            .findTareasDisponiblesPorEstado(estadoConfigOpt.get().getId(), empresaId);
                        if (tareasConfig != null && !tareasConfig.isEmpty()) {
                            return tareasConfig.stream()
                                .map(TareaPorEstadoConfig::getTipoLabor)
                                .distinct()
                                .map(String::toLowerCase)
                                .collect(Collectors.toList());
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("❌ [LaborService] Fallback plantilla por estado: " + e.getMessage());
            }
        }
        return new ArrayList<>();
    }
    
    /**
     * Mapea estados del enum que no existen en las plantillas a un nombre de estado de plantilla
     * para poder devolver tareas cuando el lote no tiene tipo de cultivo.
     */
    private String mapEstadoEnumANombrePlantilla(EstadoLote estadoEnum) {
        if (estadoEnum == EstadoLote.EN_PREPARACION) {
            return "Disponible"; // Las plantillas no tienen "En Preparación"; usamos tareas de Disponible
        }
        return null;
    }
    
    /**
     * Resuelve un TipoCultivo por nombre del cultivo (ej. "Forrajera").
     * Prueba: 1) exacta; 2) contiene; 3) normalizando plural/singular (quitar o agregar 's').
     */
    private Optional<TipoCultivo> resolverTipoCultivoPorNombre(String nombreDelCultivo) {
        if (nombreDelCultivo == null || nombreDelCultivo.isBlank()) {
            return Optional.empty();
        }
        String busqueda = nombreDelCultivo.trim().toLowerCase();
        java.util.List<TipoCultivo> todos = configuracionEstadosService.obtenerTodosLosTiposCultivo();
        // 1) Coincidencia exacta (ignore case, trim)
        Optional<TipoCultivo> exacto = todos.stream()
            .filter(tc -> tc.getNombre() != null && tc.getNombre().trim().equalsIgnoreCase(busqueda))
            .findFirst();
        if (exacto.isPresent()) return exacto;
        // 2) Uno contiene al otro (ej. "Forrajera" vs "Forrajeras")
        Optional<TipoCultivo> porContiene = todos.stream()
            .filter(tc -> {
                if (tc.getNombre() == null) return false;
                String n = tc.getNombre().trim().toLowerCase();
                return n.contains(busqueda) || busqueda.contains(n);
            })
            .findFirst();
        if (porContiene.isPresent()) return porContiene;
        // 3) Normalizar plural/singular: "forrajeras" <-> "forrajera", "pastura" <-> "pasturas"
        String busquedaSinS = busqueda.endsWith("s") ? busqueda.substring(0, busqueda.length() - 1) : busqueda + "s";
        return todos.stream()
            .filter(tc -> {
                if (tc.getNombre() == null) return false;
                String n = tc.getNombre().trim().toLowerCase();
                String nSinS = n.endsWith("s") ? n.substring(0, n.length() - 1) : n + "s";
                return n.equals(busquedaSinS) || nSinS.equals(busqueda);
            })
            .findFirst();
    }

    /**
     * Resuelve el TipoCultivo que corresponde a un Cultivo (para asignar al lote).
     * Prueba primero por cultivo.getTipo(), luego por cultivo.getNombre() (mismo criterio que en tareas disponibles).
     * Uso: SiembraService y cualquier flujo que asigne cultivo al lote para setear también tipo_cultivo_id.
     */
    public Optional<TipoCultivo> resolverTipoCultivoParaCultivo(Cultivo cultivo) {
        if (cultivo == null) return Optional.empty();
        if (cultivo.getTipo() != null && !cultivo.getTipo().isBlank()) {
            Optional<TipoCultivo> porTipo = resolverTipoCultivoPorNombre(cultivo.getTipo());
            if (porTipo.isPresent()) return porTipo;
        }
        if (cultivo.getNombre() != null && !cultivo.getNombre().isBlank()) {
            return resolverTipoCultivoPorNombre(cultivo.getNombre());
        }
        return Optional.empty();
    }

    /**
     * Obtiene las tareas disponibles para un lote específico
     * Usa la configuración de estados si está disponible
     */
    public Map<String, Object> getTareasDisponiblesPorLote(Long loteId, Long empresaId) {
        Map<String, Object> resultado = new java.util.HashMap<>();
        
        Plot lote = plotRepository.findById(loteId)
            .orElseThrow(() -> new RuntimeException("Lote no encontrado"));

        // Si el lote tiene tipo de cultivo con config pero estadoConfigurado es null, recalcular para usar la config
        boolean tieneTipoCultivo = lote.getTipoCultivo() != null
                || (lote.getCultivo() != null && (lote.getCultivo().getTipo() != null || lote.getCultivo().getNombre() != null));
        if (tieneTipoCultivo && lote.getEstadoConfigurado() == null && lote.getCampo() != null && lote.getCampo().getEmpresa() != null) {
            estadoLoteUpdater.recalcularEstado(loteId);
            lote = plotRepository.findById(loteId).orElse(lote);
        }

        EstadoLote estadoEnum = lote.getEstado();
        resultado.put("estadoEnum", estadoEnum.name());
        // Priorizar nombre de la configuración cuando exista (ej. "Rebrote" en lugar de "COSECHADO")
        resultado.put("estadoDescripcion", lote.getEstadoConfigurado() != null
                ? lote.getEstadoConfigurado().getNombre() : estadoEnum.getDescripcion());
        
        // Verificar si el lote tiene cultivo asignado
        boolean tieneCultivo = lote.getCultivo() != null || lote.getCultivoActual() != null;
        resultado.put("tieneCultivo", tieneCultivo);
        
        // Intentar obtener tipo de cultivo del lote (usar relación directa si está disponible)
        Long tipoCultivoId = null;
        String nombreTipoCultivo = null;
        
        if (lote.getTipoCultivo() != null) {
            // Usar relación directa (más eficiente)
            tipoCultivoId = lote.getTipoCultivo().getId();
            nombreTipoCultivo = lote.getTipoCultivo().getNombre();
        } else if (lote.getCultivo() != null) {
            // Fallback: resolver por cultivo.tipo y, si no hay match, por cultivo.nombre (ej. tipo "Forrajera" vs TipoCultivo "Alfalfa")
            Cultivo cultivo = lote.getCultivo();
            String tipoCultivoNombre = cultivo.getTipo();
            Optional<TipoCultivo> tipoCultivoOpt = tipoCultivoNombre != null && !tipoCultivoNombre.isBlank()
                ? resolverTipoCultivoPorNombre(tipoCultivoNombre)
                : Optional.empty();
            if (tipoCultivoOpt.isEmpty() && cultivo.getNombre() != null && !cultivo.getNombre().isBlank()) {
                tipoCultivoOpt = resolverTipoCultivoPorNombre(cultivo.getNombre());
            }
            if (tipoCultivoOpt.isPresent()) {
                tipoCultivoId = tipoCultivoOpt.get().getId();
                nombreTipoCultivo = tipoCultivoOpt.get().getNombre();
            } else {
                nombreTipoCultivo = tipoCultivoNombre != null ? tipoCultivoNombre : cultivo.getNombre();
            }
        } else if (lote.getCultivoActual() != null) {
            // Último fallback: buscar por nombre (compatibilidad con lotes antiguos)
            List<Cultivo> cultivosEncontrados = cultivoRepository.findByNombreContaining(lote.getCultivoActual());
            if (!cultivosEncontrados.isEmpty()) {
                Cultivo cultivo = cultivosEncontrados.get(0);
                if (cultivo.getTipo() != null) {
                    Optional<TipoCultivo> tipoCultivoOpt = resolverTipoCultivoPorNombre(cultivo.getTipo());
                    if (tipoCultivoOpt.isPresent()) {
                        tipoCultivoId = tipoCultivoOpt.get().getId();
                        nombreTipoCultivo = tipoCultivoOpt.get().getNombre();
                    } else {
                        nombreTipoCultivo = cultivo.getTipo();
                    }
                }
            }
        }
        
        resultado.put("tipoCultivoId", tipoCultivoId);
        resultado.put("nombreTipoCultivo", nombreTipoCultivo);
        resultado.put("tieneConfiguracion", tipoCultivoId != null);
        
        // Prioridad 1: si el lote tiene estado configurado (esquema de estados), usar sus tareas directamente
        if (lote.getEstadoConfigurado() != null) {
            EstadoLoteConfig estadoConfig = lote.getEstadoConfigurado();
            Map<String, Object> estadoConfigMap = new java.util.HashMap<>();
            estadoConfigMap.put("id", estadoConfig.getId());
            estadoConfigMap.put("nombre", estadoConfig.getNombre());
            estadoConfigMap.put("color", estadoConfig.getColor());
            estadoConfigMap.put("icono", estadoConfig.getIcono() != null ? estadoConfig.getIcono() : "");
            resultado.put("estadoConfigurado", estadoConfigMap);
            
            List<TareaPorEstadoConfig> tareasConfig = tareaPorEstadoConfigRepository
                .findTareasDisponiblesPorEstado(estadoConfig.getId(), empresaId);
            Set<String> tipoLaborVistos = new HashSet<>();
            List<Map<String, Object>> listaTareas = tareasConfig.stream()
                .filter(t -> tipoLaborVistos.add(t.getTipoLabor()))
                .map(t -> {
                    Map<String, Object> tareaMap = new java.util.HashMap<>();
                    tareaMap.put("tipoLabor", t.getTipoLabor());
                    tareaMap.put("nombreTarea", t.getNombreTarea());
                    tareaMap.put("descripcion", t.getDescripcion() != null ? t.getDescripcion() : "");
                    tareaMap.put("esObligatoria", t.getEsObligatoria());
                    return tareaMap;
                })
                .collect(Collectors.toList());
            
            resultado.put("tareasDetalladas", listaTareas);
            List<String> tareasDesdeConfig = listaTareas.stream()
                .map(t -> (String) t.get("tipoLabor"))
                .map(String::toLowerCase)
                .collect(Collectors.toList());
            resultado.put("tareas", tareasDesdeConfig);
            // Con estado configurado y tareas encontradas, no requiere configuración
            if (!listaTareas.isEmpty()) {
                resultado.put("requiereConfiguracion", false);
                resultado.remove("mensaje");
            }
        }
        
        // Prioridad 2: si no hay tareas desde estadoConfigurado, usar configuración por tipo de cultivo + enum de estado
        Object tareasObj = resultado.get("tareas");
        boolean sinTareas = tareasObj == null || !(tareasObj instanceof List) || ((List<?>) tareasObj).isEmpty();
        if (sinTareas) {
            List<String> tareas = getTareasDisponiblesPorEstado(estadoEnum, tipoCultivoId, empresaId);
            // Fallback: tipo no resuelto por nombre pero hay cultivo → buscar estado de la empresa que coincida con el enum del lote
            if (tieneCultivo && tipoCultivoId == null && (tareas == null || tareas.isEmpty()) && empresaId != null) {
                java.util.List<EstadoLoteConfig> estadosEmpresa = estadoLoteConfigRepository.findByEmpresaIdAndActivoTrueOrderByOrdenAsc(empresaId);
                String enumNombre = estadoEnum.name();
                String enumDesc = estadoEnum.getDescripcion();
                String enumNorm = enumNombre.replace("_", " ").toLowerCase().replace(" ", "");
                Optional<EstadoLoteConfig> estadoCoincidente = estadosEmpresa.stream()
                    .filter(e -> {
                        if (e.getNombre() == null) return false;
                        String n = e.getNombre().trim();
                        String nNorm = n.toLowerCase().replace(" ", "");
                        return n.equalsIgnoreCase(enumNombre) || n.equalsIgnoreCase(enumDesc)
                            || nNorm.equals(enumNorm) || nNorm.contains(enumNorm) || enumNorm.contains(nNorm);
                    })
                    .findFirst();
                if (estadoCoincidente.isPresent()) {
                    List<TareaPorEstadoConfig> tareasCfg = tareaPorEstadoConfigRepository
                        .findTareasDisponiblesPorEstado(estadoCoincidente.get().getId(), empresaId);
                    if (tareasCfg != null && !tareasCfg.isEmpty()) {
                        tareas = tareasCfg.stream()
                            .map(TareaPorEstadoConfig::getTipoLabor)
                            .distinct()
                            .map(String::toLowerCase)
                            .collect(Collectors.toList());
                        resultado.put("tareas", tareas);
                        resultado.put("requiereConfiguracion", false);
                        resultado.remove("mensaje");
                        // Exponer estado usado para que el frontend sepa
                        Map<String, Object> ec = new java.util.HashMap<>();
                        ec.put("id", estadoCoincidente.get().getId());
                        ec.put("nombre", estadoCoincidente.get().getNombre());
                        resultado.put("estadoConfigurado", ec);
                    }
                }
            }
            if (resultado.get("tareas") == null) resultado.put("tareas", tareas != null ? tareas : new ArrayList<>());
            @SuppressWarnings("unchecked")
            List<String> tareasFinal = (List<String>) resultado.get("tareas");
            boolean yaResueltoPorFallback = Boolean.FALSE.equals(resultado.get("requiereConfiguracion"));
            if (!yaResueltoPorFallback) {
                if (tieneCultivo && tipoCultivoId == null && (tareasFinal == null || tareasFinal.isEmpty())) {
                    resultado.put("mensaje", "El cultivo asignado no tiene un esquema de estados, transiciones y tareas configurado. Configure el esquema para este tipo de cultivo para poder crear labores.");
                    resultado.put("requiereConfiguracion", true);
                } else if (tieneCultivo && tipoCultivoId != null && (tareasFinal == null || tareasFinal.isEmpty())) {
                    // Hay tipo de cultivo configurado pero este estado no tiene tareas (ej. COSECHADO): mensaje informativo, no bloqueante
                    resultado.put("mensaje", "No hay tareas configuradas para el estado actual del lote. Puede configurar tareas para este estado en Configuración de Estados.");
                    resultado.put("requiereConfiguracion", false);
                }
            }
        }
        
        return resultado;
    }
    
    /**
     * Obtiene las tareas disponibles según el estado del lote (método tradicional - fallback)
     * Implementa la lógica de filtrado inteligente por estado
     * Este método se mantiene para compatibilidad con código existente
     */
    public List<String> getTareasDisponiblesPorEstado(EstadoLote estado) {
        return getTareasDisponiblesPorEstadoTradicional(estado);
    }
    
    /**
     * Implementación tradicional de tareas por estado
     */
    private List<String> getTareasDisponiblesPorEstadoTradicional(EstadoLote estado) {
        switch (estado) {
            case DISPONIBLE:
                return Arrays.asList("arado", "rastra", "fertilizacion", "monitoreo");
            
            case PREPARADO:
                return Arrays.asList("siembra", "fertilizacion", "monitoreo");
            
            case EN_PREPARACION:
                return Arrays.asList("arado", "rastra", "fertilizacion", "monitoreo");
            
            case SEMBRADO:
                return Arrays.asList("riego", "fertilizacion", "pulverizacion", "monitoreo");
            
            case EN_CRECIMIENTO:
                return Arrays.asList("riego", "fertilizacion", "pulverizacion", "desmalezado", 
                                   "aplicacion_herbicida", "aplicacion_insecticida", "monitoreo");
            
            case EN_FLORACION:
                return Arrays.asList("riego", "pulverizacion", "aplicacion_insecticida", "monitoreo");
            
            case EN_FRUTIFICACION:
                return Arrays.asList("riego", "pulverizacion", "aplicacion_insecticida", "monitoreo");
            
            case LISTO_PARA_COSECHA:
                return Arrays.asList("cosecha", "monitoreo");
            
            case EN_COSECHA:
                return Arrays.asList("cosecha", "monitoreo");
            
            case COSECHADO:
                return Arrays.asList("arado", "rastra", "monitoreo");
            
            case EN_DESCANSO:
                return Arrays.asList("monitoreo");
            
            case ENFERMO:
                return Arrays.asList("pulverizacion", "aplicacion_herbicida", "aplicacion_insecticida", 
                                   "monitoreo", "otro");
            
            case ABANDONADO:
                return Arrays.asList("monitoreo", "otro");
            
            default:
                return Arrays.asList("monitoreo", "otro");
        }
    }
    
    /**
     * Valida si una tarea es apropiada para el estado actual del lote
     */
    public boolean validarTareaParaEstado(EstadoLote estado, String tipoTarea) {
        List<String> tareasDisponibles = getTareasDisponiblesPorEstado(estado);
        return tareasDisponibles.contains(tipoTarea);
    }
    
    /**
     * Obtiene información detallada sobre las tareas disponibles
     */
    public Map<String, Object> getInfoTareasDisponibles(EstadoLote estado) {
        List<String> tareas = getTareasDisponiblesPorEstado(estado);
        
        Map<String, String> tareasConDescripcion = new java.util.HashMap<>();
        tareasConDescripcion.put("arado", "🚜 Preparación profunda del suelo");
        tareasConDescripcion.put("rastra", "🔧 Nivelación y refinamiento del suelo");
        tareasConDescripcion.put("siembra", "🌱 Plantación del cultivo");
        tareasConDescripcion.put("fertilizacion", "🌿 Aplicación de nutrientes");
        tareasConDescripcion.put("riego", "💧 Aplicación de agua");
        tareasConDescripcion.put("pulverizacion", "💨 Aplicación de productos fitosanitarios");
        tareasConDescripcion.put("desmalezado", "🌾 Control manual de malezas");
        tareasConDescripcion.put("aplicacion_herbicida", "🧪 Control químico de malezas");
        tareasConDescripcion.put("aplicacion_insecticida", "🐛 Control de plagas");
        tareasConDescripcion.put("cosecha", "🌾 Recolección del cultivo");
        tareasConDescripcion.put("monitoreo", "👁️ Inspección y seguimiento");
        tareasConDescripcion.put("otro", "📝 Otra actividad");
        
        Map<String, Object> resultado = new java.util.HashMap<>();
        resultado.put("estado", estado.name());
        resultado.put("tareas", tareas);
        resultado.put("tareasConInfo", tareas.stream()
                .collect(Collectors.toMap(
                    tarea -> tarea,
                    tarea -> tareasConDescripcion.getOrDefault(tarea, "📝 " + tarea)
                )));
        
        return resultado;
    }
    
    /**
     * Calcula el costo total de una labor sumando todos sus componentes
     * Migrado desde el frontend para centralizar la lógica de negocio
     * 
     * @param laborId ID de la labor
     * @return Costo total calculado
     */
    public BigDecimal calcularCostoTotalLabor(Long laborId) {
        Optional<Labor> laborOpt = laborRepository.findById(laborId);
        
        if (!laborOpt.isPresent()) {
            throw new RuntimeException("Labor no encontrada con ID: " + laborId);
        }
        
        // Calcular costo de insumos
        BigDecimal costoInsumos = laborInsumoRepository.findByLaborId(laborId).stream()
                .map(LaborInsumo::getCostoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calcular costo de maquinaria
        BigDecimal costoMaquinaria = laborMaquinariaRepository.findByLaborId(laborId).stream()
                .map(LaborMaquinaria::getCosto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calcular costo de mano de obra
        BigDecimal costoManoObra = laborManoObraRepository.findByLaborId(laborId).stream()
                .map(LaborManoObra::getCostoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Sumar todos los costos
        BigDecimal costoTotal = costoInsumos
                .add(costoMaquinaria)
                .add(costoManoObra);
        
        return costoTotal;
    }
    
    /**
     * Calcula el desglose detallado de costos de una labor
     * 
     * @param laborId ID de la labor
     * @return Map con el desglose de costos
     */
    public Map<String, BigDecimal> calcularDesgloseCostosLabor(Long laborId) {
        Optional<Labor> laborOpt = laborRepository.findById(laborId);
        
        if (!laborOpt.isPresent()) {
            throw new RuntimeException("Labor no encontrada con ID: " + laborId);
        }
        
        // Calcular costo de insumos
        BigDecimal costoInsumos = laborInsumoRepository.findByLaborId(laborId).stream()
                .map(LaborInsumo::getCostoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calcular costo de maquinaria
        BigDecimal costoMaquinaria = laborMaquinariaRepository.findByLaborId(laborId).stream()
                .map(LaborMaquinaria::getCosto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calcular costo de mano de obra
        BigDecimal costoManoObra = laborManoObraRepository.findByLaborId(laborId).stream()
                .map(LaborManoObra::getCostoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Costo total
        BigDecimal costoTotal = costoInsumos
                .add(costoMaquinaria)
                .add(costoManoObra);
        
        Map<String, BigDecimal> desglose = new java.util.HashMap<>();
        desglose.put("costoInsumos", costoInsumos);
        desglose.put("costoMaquinaria", costoMaquinaria);
        desglose.put("costoManoObra", costoManoObra);
        desglose.put("costoTotal", costoTotal);
        
        return desglose;
    }
    
    /**
     * Actualiza el costo total de una labor recalculándolo desde sus componentes
     * 
     * @param laborId ID de la labor
     * @return Labor actualizada
     */
    @Transactional
    public Labor actualizarCostoTotalLabor(Long laborId) {
        Optional<Labor> laborOpt = laborRepository.findById(laborId);
        
        if (!laborOpt.isPresent()) {
            throw new RuntimeException("Labor no encontrada con ID: " + laborId);
        }
        
        Labor labor = laborOpt.get();
        BigDecimal costoTotal = calcularCostoTotalLabor(laborId);
        labor.setCostoTotal(costoTotal);
        
        return laborRepository.save(labor);
    }

    private void asignarCicloCultivoSiCorresponde(Labor labor, Plot lote) {
        if (lote == null || labor.getCicloCultivoId() != null) {
            return;
        }
        if (lote.getCicloActivoId() != null) {
            labor.setCicloCultivoId(lote.getCicloActivoId());
            return;
        }
        cicloCultivoService.obtenerCicloActivo(lote.getId())
                .ifPresent(c -> labor.setCicloCultivoId(c.getId()));
    }
}
