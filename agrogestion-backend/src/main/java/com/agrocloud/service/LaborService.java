package com.agrocloud.service;

import com.agrocloud.dto.LaborDetalladoDTO;
import com.agrocloud.dto.LaborMaquinariaDTO;
import com.agrocloud.dto.LaborManoObraDTO;
import com.agrocloud.dto.LaborInsumoDTO;
import com.agrocloud.dto.RespuestaCambioEstado;
import com.agrocloud.dto.ConfirmacionCambioEstado;
import com.agrocloud.dto.ReporteCosechaDTO;
import com.agrocloud.dto.CrearLaborRequest;
import com.agrocloud.model.entity.Labor;
import com.agrocloud.model.entity.LaborMaquinaria;
import com.agrocloud.model.entity.LaborManoObra;
import com.agrocloud.model.entity.LaborInsumo;
import com.agrocloud.model.entity.Plot;
import com.agrocloud.model.entity.User;
import com.agrocloud.model.entity.Empresa;
import com.agrocloud.model.entity.Role;
import com.agrocloud.model.entity.Insumo;
import com.agrocloud.model.entity.DosisAgroquimico;
import com.agrocloud.model.enums.TipoMaquinaria;
import com.agrocloud.model.enums.EstadoLote;
import com.agrocloud.model.enums.Rol;
import com.agrocloud.model.enums.RolEmpresa;
import com.agrocloud.model.enums.TipoAplicacion;
import com.agrocloud.model.enums.FormaAplicacion;
import com.agrocloud.repository.LaborRepository;
import com.agrocloud.repository.LaborMaquinariaRepository;
import com.agrocloud.repository.LaborManoObraRepository;
import com.agrocloud.repository.LaborInsumoRepository;
import com.agrocloud.repository.PlotRepository;
import com.agrocloud.repository.InsumoRepository;
import com.agrocloud.repository.DosisAgroquimicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
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
    private InsumoRepository insumoRepository;
    
    @Autowired
    private DosisAgroquimicoRepository dosisAgroquimicoRepository;
    
    @Autowired
    private InventarioService inventarioService;
    
    @Autowired
    private TransicionEstadoService transicionEstadoService;

    @Autowired
    private UserService userService;
    
    
    @Autowired
    private EstadoLoteService estadoLoteService;

    /**
     * Obtener todas las labores accesibles por el usuario
     * Incluye labores del usuario y de sus dependientes
     */
    public List<Labor> getLaboresByUser(User user) {
        try {
            System.out.println("[LABOR_SERVICE] getLaboresByUser iniciado para usuario: " + (user != null ? user.getEmail() : "null"));
            
            if (user == null) {
                System.err.println("[LABOR_SERVICE] ERROR: Usuario es null");
                return new ArrayList<>();
            }
            
            List<Plot> lotesUsuario;
            
            if (user.isSuperAdmin()) {
                // Solo SuperAdmin ve todos los lotes activos
                System.out.println("[LABOR_SERVICE] Usuario es SuperAdmin, mostrando todos los lotes");
                lotesUsuario = plotRepository.findAll().stream()
                        .filter(plot -> {
                            Boolean activo = plot.getActivo();
                            return activo != null && activo;
                        })
                        .toList();
            } else if (user.esAdministradorEmpresa(user.getEmpresa() != null ? user.getEmpresa().getId() : null) ||
                       user.tieneRolEnEmpresa(RolEmpresa.JEFE_CAMPO) ||
                       user.tieneRolEnEmpresa(RolEmpresa.OPERARIO) ||
                       user.tieneRolEnEmpresa(RolEmpresa.CONSULTOR_EXTERNO)) {
                // Admin, JEFE_CAMPO, OPERARIO y CONSULTOR_EXTERNO ven TODAS las labores de los lotes de su empresa (solo lectura para OPERARIO y CONSULTOR_EXTERNO)
                System.out.println("[LABOR_SERVICE] Usuario es Admin/JEFE_CAMPO/OPERARIO/CONSULTOR_EXTERNO de empresa, mostrando TODAS las labores de la empresa");
                
                Empresa empresa = user.getEmpresa();
                if (empresa == null) {
                    System.out.println("[LABOR_SERVICE] Usuario ADMIN no tiene empresa asignada");
                    return new ArrayList<>();
                }
                
                // Obtener todos los usuarios de la empresa
                List<User> todosUsuarios = userService.findAll();
                List<User> usuariosEmpresa = todosUsuarios.stream()
                        .filter(u -> u.perteneceAEmpresa(empresa.getId()))
                        .collect(Collectors.toList());
                
                System.out.println("[LABOR_SERVICE] Empresa ID: " + empresa.getId() + ", Usuarios: " + usuariosEmpresa.size());
                
                // Obtener lotes de TODOS los usuarios de la empresa
                lotesUsuario = new ArrayList<>();
                for (User userEmpresa : usuariosEmpresa) {
                    List<Plot> lotesUsuarioEmpresa = plotRepository.findByUserIdAndActivoTrue(userEmpresa.getId());
                    if (lotesUsuarioEmpresa != null) {
                        lotesUsuario.addAll(lotesUsuarioEmpresa);
                        System.out.println("[LABOR_SERVICE] Lotes del usuario " + userEmpresa.getUsername() + ": " + lotesUsuarioEmpresa.size());
                    }
                }
                
                System.out.println("[LABOR_SERVICE] Total lotes de la empresa: " + lotesUsuario.size());
            } else {
                // Otros usuarios ven solo sus lotes y los de sus sub-usuarios
                System.out.println("[LABOR_SERVICE] Usuario normal, mostrando lotes accesibles");
                
                // Obtener lotes del usuario actual
                lotesUsuario = plotRepository.findByUserIdAndActivoTrue(user.getId());
                System.out.println("[LABOR_SERVICE] Lotes del usuario encontrados: " + (lotesUsuario != null ? lotesUsuario.size() : "null"));
                
                if (lotesUsuario == null) {
                    lotesUsuario = new ArrayList<>();
                }
                
                // Obtener lotes de usuarios dependientes
                List<User> usuariosDependientes = userService.findByParentUserId(user.getId());
                if (usuariosDependientes != null && !usuariosDependientes.isEmpty()) {
                    System.out.println("[LABOR_SERVICE] Usuarios dependientes encontrados: " + usuariosDependientes.size());
                    for (User dependiente : usuariosDependientes) {
                        List<Plot> lotesDependiente = plotRepository.findByUserIdAndActivoTrue(dependiente.getId());
                        if (lotesDependiente != null) {
                            lotesUsuario.addAll(lotesDependiente);
                            System.out.println("[LABOR_SERVICE] Lotes del dependiente " + dependiente.getUsername() + ": " + lotesDependiente.size());
                        }
                    }
                }
                
                System.out.println("[LABOR_SERVICE] Total lotes accesibles: " + lotesUsuario.size());
            }
            
            // Extraer los IDs de los lotes
            List<Long> loteIds = lotesUsuario.stream()
                .map(Plot::getId)
                .toList();
            
            if (loteIds.isEmpty()) {
                System.out.println("[LABOR_SERVICE] No hay lotes accesibles, retornando lista vacía");
                return new ArrayList<>();
            }
            
            // Buscar labores por los IDs de lotes
            List<Labor> labores = laborRepository.findByLoteIdInOrderByFechaInicioDesc(loteIds);
            System.out.println("[LABOR_SERVICE] Labores encontradas: " + labores.size());
            
            // Inicializar relaciones lazy para evitar LazyInitializationException en serialización JSON
            if (labores != null) {
                labores.forEach(labor -> {
                    if (labor.getLote() != null) {
                        labor.getLote().getId(); // Inicializar lote
                    }
                    if (labor.getUsuario() != null) {
                        labor.getUsuario().getId(); // Inicializar usuario
                    }
                    if (labor.getUsuarioAnulacion() != null) {
                        labor.getUsuarioAnulacion().getId(); // Inicializar usuarioAnulacion
                    }
                });
            }
            
            return labores;
            
        } catch (Exception e) {
            System.err.println("[LABOR_SERVICE] ERROR en getLaboresByUser: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Obtener labor por ID verificando permisos
     */
    public Optional<Labor> getLaborById(Long id, User user) {
        Optional<Labor> labor = laborRepository.findById(id);
        
        if (labor.isPresent()) {
            Labor laborEntity = labor.get();
            Plot lote = laborEntity.getLote();
            
            // Verificar si el usuario tiene acceso al lote
            if (lote != null && tieneAccesoAlLote(lote, user)) {
                return labor;
            }
        }
        
        return Optional.empty();
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
            if (lote == null || !tieneAccesoAlLote(lote, usuario)) {
                throw new RuntimeException("No tiene permisos para crear labores en este lote");
            }
        }
        
        labor.setUsuario(usuario);
        labor.setActivo(true);
        
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
            if (!tieneAccesoAlLote(lote, usuario)) {
                throw new RuntimeException("No tiene permisos para crear labores en este lote");
            }
            labor.setLote(lote);
        }
        
        labor.setUsuario(usuario);
        labor.setActivo(true);
        
        // Guardar la labor primero para obtener el ID
        Labor laborGuardada = laborRepository.save(labor);
        
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
                    // Si es error de validación (variación > 20%), rechazar todo el request
                    if (e.getMessage() != null && e.getMessage().contains("excede el límite permitido")) {
                        throw e;
                    }
                    // Otros errores: continuar con los demás insumos pero registrar el error
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
        
        Labor laborGuardada = laborRepository.save(labor);
        
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
            // Confirmar el cambio de estado
            estadoLoteService.confirmarCambioEstado(confirmacion, usuario);
            
            // Actualizar información del cultivo
            actualizarInformacionCultivo(labor.getLote(), labor);
            
            // Marcar labor como completada
            labor.setEstado(Labor.EstadoLabor.COMPLETADA);
            laborRepository.save(labor);
            
        } else {
            // Cancelar la labor si no se confirma el cambio de estado
            labor.setEstado(Labor.EstadoLabor.CANCELADA);
            labor.setObservaciones("Cancelada por el usuario - cambio de estado no confirmado");
            laborRepository.save(labor);
        }
    }
    
    /**
     * Confirmar labor de cosecha y actualizar estado del lote
     */
    public void confirmarLaborCosecha(Long laborId, ConfirmacionCambioEstado confirmacion, User usuario) {
        Labor labor = laborRepository.findById(laborId)
            .orElseThrow(() -> new RuntimeException("Labor no encontrada"));
            
        if (confirmacion.isConfirmado()) {
            // Confirmar el cambio de estado
            estadoLoteService.confirmarCambioEstado(confirmacion, usuario);
            
            // Crear registro de cosecha
            crearRegistroCosecha(labor.getLote(), labor);
            
            // Marcar labor como completada
            labor.setEstado(Labor.EstadoLabor.COMPLETADA);
            laborRepository.save(labor);
            
        } else {
            // Cancelar la labor si no se confirma el cambio de estado
            labor.setEstado(Labor.EstadoLabor.CANCELADA);
            labor.setObservaciones("Cancelada por el usuario - cambio de estado no confirmado");
            laborRepository.save(labor);
        }
    }
    
    /**
     * Actualizar información del cultivo cuando se confirma siembra
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
            if (descripcion.contains("maíz") || descripcion.contains("maiz")) {
                lote.setCultivoActual("Maíz");
            } else if (descripcion.contains("soja")) {
                lote.setCultivoActual("Soja");
            } else if (descripcion.contains("trigo")) {
                lote.setCultivoActual("Trigo");
            } else {
                lote.setCultivoActual("Cultivo");
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
     * Actualizar labor existente
     */
    public Labor actualizarLabor(Long id, Labor laborData, User usuario) {
        Labor labor = laborRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Labor no encontrada"));
        
        // Verificar permisos
        if (labor.getLote() != null && !tieneAccesoAlLote(labor.getLote(), usuario)) {
            throw new RuntimeException("No tiene permisos para modificar esta labor");
        }
        
        // Actualizar campos
        labor.setTipoLabor(laborData.getTipoLabor());
        labor.setDescripcion(laborData.getDescripcion());
        labor.setFechaInicio(laborData.getFechaInicio());
        labor.setFechaFin(laborData.getFechaFin());
        labor.setCostoTotal(laborData.getCostoTotal());
        labor.setEstado(laborData.getEstado());
        labor.setObservaciones(laborData.getObservaciones());
        labor.setLote(laborData.getLote());
        
        return laborRepository.save(labor);
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
        if (labor.getLote() != null && !tieneAccesoAlLote(labor.getLote(), usuario)) {
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
        if (labor.getLote() != null && !tieneAccesoAlLote(labor.getLote(), usuario)) {
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
        
        System.out.println("Labor anulada exitosamente. Insumos restaurados: " + restaurarInsumos);
    }

    /**
     * Obtener labores por lote
     */
    public List<Labor> getLaboresByLote(Long loteId, User usuario) {
        Optional<Plot> lote = plotRepository.findById(loteId);
        
        if (lote.isPresent() && tieneAccesoAlLote(lote.get(), usuario)) {
            return laborRepository.findByLoteIdOrderByFechaInicioDesc(loteId);
        }
        
        return List.of();
    }

    /**
     * Verificar si el usuario tiene acceso al lote usando el sistema completo de roles
     */
    private boolean tieneAccesoAlLote(Plot lote, User usuario) {
        if (usuario == null || lote == null) {
            return false;
        }
        
        // 1. El usuario es propietario directo del lote
        if (lote.getUser() != null && lote.getUser().getId().equals(usuario.getId())) {
            return true;
        }
        
        // 2. El usuario es líder (parent_user_id) del propietario del lote
        if (lote.getUser() != null && lote.getUser().getParentUser() != null && 
            lote.getUser().getParentUser().getId().equals(usuario.getId())) {
            return true;
        }
        
        // 3. Verificar acceso por empresa usando UserCompanyRoles
        if (perteneceAMismaEmpresa(usuario, lote)) {
            // Verificar si el rol del usuario permite acceso a labores
            Rol rolUsuario = obtenerRolUsuario(usuario);
            return tienePermisoParaLabores(rolUsuario);
        }
        
        return false;
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
     * Obtener todas las labores accesibles por el usuario con costos detallados
     */
    public List<LaborDetalladoDTO> getLaboresDetalladasByUser(User user) {
        List<Labor> labores = getLaboresByUser(user);
        
        return labores.stream()
            .map(this::convertirADetalladoDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Convertir entidad Labor a LaborDetalladoDTO
     */
    private LaborDetalladoDTO convertirADetalladoDTO(Labor labor) {
        // Obtener maquinarias, mano de obra e insumos
        List<LaborMaquinaria> maquinarias = laborMaquinariaRepository.findByLabor(labor);
        List<LaborManoObra> manoObra = laborManoObraRepository.findByLabor(labor);
        List<LaborInsumo> insumos = laborInsumoRepository.findByLaborIdWithInsumo(labor.getId());
        
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
        
        return new LaborDetalladoDTO(
            labor.getId(),
            labor.getTipoLabor() != null ? labor.getTipoLabor().toString() : null,
            labor.getDescripcion(), // Usar descripcion en lugar de nombre
            labor.getDescripcion(),
            labor.getFechaInicio(),
            labor.getFechaFin(),
            labor.getEstado() != null ? labor.getEstado().toString() : null,
            labor.getCostoTotal(),
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
            
            // ADMIN y JEFE_CAMPO pueden eliminar cualquier labor
            labor.setActivo(false);
            laborRepository.save(labor);
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
            laborRepository.delete(laborOpt.get());
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
        if (!tieneAccesoAlLote(lote, usuario)) {
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
     * Obtiene las tareas disponibles según el estado del lote
     * Implementa la lógica de filtrado inteligente por estado
     */
    public List<String> getTareasDisponiblesPorEstado(EstadoLote estado) {
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
}
