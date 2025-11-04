package com.agrocloud.service;

import com.agrocloud.dto.CrearInsumoConDosisRequest;
import com.agrocloud.dto.InsumoConDosisDTO;
import com.agrocloud.dto.InsumoDTO;
import com.agrocloud.model.entity.DosisAplicacion;
import com.agrocloud.model.entity.Insumo;
import com.agrocloud.model.entity.User;
import com.agrocloud.model.enums.RolEmpresa;
import com.agrocloud.repository.DosisAplicacionRepository;
import com.agrocloud.repository.InsumoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class InsumoService {

    @Autowired
    private InsumoRepository insumoRepository;
    
    @Autowired
    private DosisAplicacionRepository dosisAplicacionRepository;


    // Obtener todos los insumos (público)
    public List<Insumo> getAllInsumos() {
        return insumoRepository.findAll();
    }

    // Obtener insumo por ID (público)
    public Insumo getInsumoById(Long id) {
        return insumoRepository.findById(id).orElse(null);
    }

    // Obtener insumos con stock bajo (público)
    public List<Insumo> getInsumosStockBajo() {
        return insumoRepository.findAll().stream()
                .filter(insumo -> insumo.getStockActual().compareTo(insumo.getStockMinimo()) <= 0)
                .toList();
    }

    // Obtener todos los insumos accesibles por un usuario
    public List<Insumo> getInsumosByUser(User user) {
        List<Insumo> insumos;
        
        if (user.isAdmin() || 
            user.tieneRolEnEmpresa(RolEmpresa.JEFE_CAMPO) || 
            user.tieneRolEnEmpresa(RolEmpresa.OPERARIO) ||
            user.tieneRolEnEmpresa(RolEmpresa.CONSULTOR_EXTERNO)) {
            // Admin, JEFE_CAMPO, OPERARIO y CONSULTOR_EXTERNO ven todos los insumos de la empresa (solo lectura para OPERARIO y CONSULTOR_EXTERNO)
            insumos = insumoRepository.findAll();
        } else {
            // Usuario ve sus insumos y los de sus sub-usuarios
            insumos = insumoRepository.findAccessibleByUser(user);
        }
        
        // Filtrar solo activos y inicializar relaciones lazy
        if (insumos == null) {
            return java.util.Collections.emptyList();
        }

        List<Insumo> activos = insumos.stream()
                .filter(i -> Boolean.TRUE.equals(i.getActivo()))
                .collect(java.util.stream.Collectors.toList());

        activos.forEach(insumo -> {
            if (insumo.getEmpresa() != null) {
                insumo.getEmpresa().getId();
            }
            if (insumo.getUser() != null) {
                insumo.getUser().getId();
            }
        });

        return activos;
    }

    // Obtener insumo por ID (con validación de acceso)
    public Optional<Insumo> getInsumoById(Long id, User user) {
        Optional<Insumo> insumo = insumoRepository.findById(id);
        
        if (insumo.isPresent()) {
            Insumo i = insumo.get();
            if ((user.isAdmin() || user.tieneRolEnEmpresa(RolEmpresa.JEFE_CAMPO) || user.canAccessUser(i.getUser())) && i.getActivo()) {
                return insumo;
            }
        }
        
        return Optional.empty();
    }

    // Crear nuevo insumo
    public Insumo createInsumo(Insumo insumo, User user) {
        insumo.setUser(user);
        // Asignar la empresa del usuario al insumo
        if (user.getEmpresa() != null) {
            insumo.setEmpresa(user.getEmpresa());
        }
        return insumoRepository.save(insumo);
    }

    // Actualizar insumo (con validación de acceso)
    public Optional<Insumo> updateInsumo(Long id, Insumo insumoData, User user) {
        Optional<Insumo> existingInsumo = insumoRepository.findById(id);
        
        if (existingInsumo.isPresent()) {
            Insumo insumo = existingInsumo.get();
            
            // Verificar acceso
            if (user.isAdmin() || user.tieneRolEnEmpresa(RolEmpresa.JEFE_CAMPO) || user.canAccessUser(insumo.getUser())) {
                // Actualizar campos
                insumo.setNombre(insumoData.getNombre());
                insumo.setDescripcion(insumoData.getDescripcion());
                insumo.setTipo(insumoData.getTipo());
                insumo.setUnidadMedida(insumoData.getUnidadMedida());
                insumo.setPrecioUnitario(insumoData.getPrecioUnitario());
                insumo.setStockMinimo(insumoData.getStockMinimo());
                insumo.setStockActual(insumoData.getStockActual());
                insumo.setProveedor(insumoData.getProveedor());
                insumo.setFechaVencimiento(insumoData.getFechaVencimiento());
                insumo.setActivo(insumoData.getActivo());

                // Actualizar campos específicos de agroquímicos
                insumo.setPrincipioActivo(insumoData.getPrincipioActivo());
                insumo.setConcentracion(insumoData.getConcentracion());
                insumo.setClaseQuimica(insumoData.getClaseQuimica());
                insumo.setCategoriaToxicologica(insumoData.getCategoriaToxicologica());
                insumo.setPeriodoCarenciaDias(insumoData.getPeriodoCarenciaDias());
                insumo.setDosisMinimaPorHa(insumoData.getDosisMinimaPorHa());
                insumo.setDosisMaximaPorHa(insumoData.getDosisMaximaPorHa());
                insumo.setUnidadDosis(insumoData.getUnidadDosis());
                
                return Optional.of(insumoRepository.save(insumo));
            }
        }
        
        return Optional.empty();
    }

    // Eliminar insumo lógicamente (con validación de acceso)
    public boolean deleteInsumo(Long id, User user) {
        Optional<Insumo> insumo = insumoRepository.findById(id);
        
        if (insumo.isPresent()) {
            Insumo i = insumo.get();
            if (user.isAdmin() || user.canAccessUser(i.getUser())) {
                i.setActivo(false);
                insumoRepository.save(i);
                return true;
            }
        }
        
        return false;
    }

    // Eliminar insumo físicamente (solo para administradores)
    public boolean deleteInsumoFisicamente(Long id, User user) {
        if (!user.isAdmin() && !user.tieneRolEnEmpresa(RolEmpresa.JEFE_CAMPO)) {
            return false;
        }
        
        Optional<Insumo> insumo = insumoRepository.findById(id);
        if (insumo.isPresent()) {
            insumoRepository.delete(insumo.get());
            return true;
        }
        
        return false;
    }

    // Buscar insumo por nombre
    public List<Insumo> searchInsumoByName(String nombre, User user) {
        if (user.isAdmin() || user.tieneRolEnEmpresa(RolEmpresa.JEFE_CAMPO)) {
            // Admin busca en todos los insumos
            return insumoRepository.findAll().stream()
                    .filter(i -> i.getNombre().toLowerCase().contains(nombre.toLowerCase()))
                    .toList();
        } else {
            // Usuario busca solo en sus insumos accesibles
            return insumoRepository.findAccessibleByUser(user).stream()
                    .filter(i -> i.getNombre().toLowerCase().contains(nombre.toLowerCase()))
                    .toList();
        }
    }

    // Obtener insumos con stock bajo por usuario
    public List<Insumo> getInsumosStockBajo(User user) {
        if (user.isAdmin() || user.tieneRolEnEmpresa(RolEmpresa.JEFE_CAMPO)) {
            return insumoRepository.findAll().stream()
                    .filter(Insumo::isStockBajo)
                    .toList();
        } else {
            return insumoRepository.findStockBajoByUserId(user.getId());
        }
    }

    // Obtener insumos próximos a vencer por usuario
    public List<Insumo> getInsumosProximosAVencer(User user) {
        LocalDate proximoMes = LocalDate.now().plusMonths(1);
        if (user.isAdmin() || user.tieneRolEnEmpresa(RolEmpresa.JEFE_CAMPO)) {
            return insumoRepository.findAll().stream()
                    .filter(i -> i.estaPorVencer())
                    .toList();
        } else {
            return insumoRepository.findProximosAVencerByUserId(user.getId(), proximoMes);
        }
    }

    // Obtener estadísticas de insumos por usuario
    public InsumoStats getInsumoStats(User user) {
        List<Insumo> insumos = getInsumosByUser(user);
        
        long total = insumos.size();
        long activos = insumos.stream().filter(Insumo::getActivo).count();
        long stockBajo = insumos.stream().filter(Insumo::isStockBajo).count();
        long proximosAVencer = insumos.stream().filter(Insumo::estaPorVencer).count();
        
        return new InsumoStats(total, activos, stockBajo, proximosAVencer);
    }

    /**
     * Valida si hay stock disponible de un insumo para una cantidad específica
     * Migrado desde el frontend para centralizar validaciones de negocio
     * 
     * @param insumoId ID del insumo
     * @param cantidadRequerida Cantidad que se quiere usar
     * @return Resultado de la validación con mensaje descriptivo
     */
    public StockValidationResult validarStockDisponible(Long insumoId, java.math.BigDecimal cantidadRequerida) {
        Optional<Insumo> insumoOpt = insumoRepository.findById(insumoId);
        
        if (!insumoOpt.isPresent()) {
            return new StockValidationResult(false, "Insumo no encontrado", java.math.BigDecimal.ZERO);
        }
        
        Insumo insumo = insumoOpt.get();
        
        if (!insumo.getActivo()) {
            return new StockValidationResult(false, "El insumo no está activo", java.math.BigDecimal.ZERO);
        }
        
        java.math.BigDecimal stockDisponible = insumo.getStockActual();
        
        if (cantidadRequerida.compareTo(stockDisponible) > 0) {
            String mensaje = String.format(
                "Stock insuficiente. Disponible: %s %s, Requerido: %s %s",
                stockDisponible,
                insumo.getUnidadMedida(),
                cantidadRequerida,
                insumo.getUnidadMedida()
            );
            return new StockValidationResult(false, mensaje, stockDisponible);
        }
        
        return new StockValidationResult(true, "Stock disponible", stockDisponible);
    }
    
    /**
     * Calcula el stock disponible real considerando las cantidades ya asignadas a otras labores
     * pendientes o en progreso
     * 
     * @param insumoId ID del insumo
     * @param cantidadesYaAsignadas Cantidades ya asignadas en el contexto actual (ej: formulario)
     * @return Stock disponible real
     */
    public java.math.BigDecimal calcularStockRealDisponible(
            Long insumoId, 
            java.math.BigDecimal cantidadesYaAsignadas) {
        
        Optional<Insumo> insumoOpt = insumoRepository.findById(insumoId);
        
        if (!insumoOpt.isPresent()) {
            return java.math.BigDecimal.ZERO;
        }
        
        Insumo insumo = insumoOpt.get();
        java.math.BigDecimal stockActual = insumo.getStockActual();
        
        // Restar las cantidades ya asignadas en el contexto actual
        if (cantidadesYaAsignadas != null) {
            stockActual = stockActual.subtract(cantidadesYaAsignadas);
        }
        
        return stockActual.max(java.math.BigDecimal.ZERO);
    }
    
    /**
     * Descuenta stock de un insumo
     * 
     * @param insumoId ID del insumo
     * @param cantidad Cantidad a descontar
     * @param user Usuario que realiza la operación
     * @return true si se descontó correctamente, false si no hay stock suficiente
     */
    @Transactional
    public boolean descontarStock(Long insumoId, java.math.BigDecimal cantidad, User user) {
        Optional<Insumo> insumoOpt = insumoRepository.findById(insumoId);
        
        if (!insumoOpt.isPresent()) {
            throw new RuntimeException("Insumo no encontrado con ID: " + insumoId);
        }
        
        Insumo insumo = insumoOpt.get();
        
        // Validar stock disponible
        StockValidationResult validacion = validarStockDisponible(insumoId, cantidad);
        if (!validacion.isValido()) {
            throw new RuntimeException(validacion.getMensaje());
        }
        
        // Descontar stock
        java.math.BigDecimal nuevoStock = insumo.getStockActual().subtract(cantidad);
        insumo.setStockActual(nuevoStock);
        
        insumoRepository.save(insumo);
        
        return true;
    }
    
    /**
     * Devuelve stock de un insumo (al cancelar una labor, por ejemplo)
     * 
     * @param insumoId ID del insumo
     * @param cantidad Cantidad a devolver
     * @param user Usuario que realiza la operación
     * @return true si se devolvió correctamente
     */
    @Transactional
    public boolean devolverStock(Long insumoId, java.math.BigDecimal cantidad, User user) {
        Optional<Insumo> insumoOpt = insumoRepository.findById(insumoId);
        
        if (!insumoOpt.isPresent()) {
            throw new RuntimeException("Insumo no encontrado con ID: " + insumoId);
        }
        
        Insumo insumo = insumoOpt.get();
        
        // Aumentar stock
        java.math.BigDecimal nuevoStock = insumo.getStockActual().add(cantidad);
        insumo.setStockActual(nuevoStock);
        
        insumoRepository.save(insumo);
        
        return true;
    }

    // Clase interna para estadísticas
    public static class InsumoStats {
        private final long total;
        private final long activos;
        private final long stockBajo;
        private final long proximosAVencer;

        public InsumoStats(long total, long activos, long stockBajo, long proximosAVencer) {
            this.total = total;
            this.activos = activos;
            this.stockBajo = stockBajo;
            this.proximosAVencer = proximosAVencer;
        }

        public long getTotal() { return total; }
        public long getActivos() { return activos; }
        public long getStockBajo() { return stockBajo; }
        public long getProximosAVencer() { return proximosAVencer; }
    }
    
    // Clase para resultado de validación de stock
    public static class StockValidationResult {
        private final boolean valido;
        private final String mensaje;
        private final java.math.BigDecimal stockDisponible;
        
        public StockValidationResult(boolean valido, String mensaje, java.math.BigDecimal stockDisponible) {
            this.valido = valido;
            this.mensaje = mensaje;
            this.stockDisponible = stockDisponible;
        }
        
        public boolean isValido() { return valido; }
        public String getMensaje() { return mensaje; }
        public java.math.BigDecimal getStockDisponible() { return stockDisponible; }
    }
    
    // ========== NUEVOS MÉTODOS PARA GESTIÓN DE DOSIS ==========
    
    /**
     * Crear un insumo con sus dosis de aplicación
     */
    public InsumoConDosisDTO createInsumoConDosis(CrearInsumoConDosisRequest request, User user) {
        System.out.println("[INSUMO_SERVICE] Creando insumo con dosis: " + request.getNombre());
        
        // 1. Crear el insumo
        Insumo insumo = new Insumo();
        insumo.setNombre(request.getNombre());
        insumo.setDescripcion(request.getDescripcion());
        insumo.setTipo(request.getTipo());
        insumo.setUnidadMedida(request.getUnidadMedida());
        insumo.setPrecioUnitario(request.getPrecioUnitario());
        insumo.setStockMinimo(request.getStockMinimo() != null ? request.getStockMinimo() : BigDecimal.ZERO);
        insumo.setStockActual(request.getStockActual() != null ? request.getStockActual() : BigDecimal.ZERO);
        insumo.setProveedor(request.getProveedor());
        insumo.setFechaVencimiento(request.getFechaVencimiento());
        insumo.setUser(user);
        insumo.setEmpresa(user.getEmpresa());
        insumo.setActivo(true);
        
        Insumo savedInsumo = insumoRepository.save(insumo);
        System.out.println("[INSUMO_SERVICE] Insumo creado: " + savedInsumo.getId());
        
        // 2. Crear las dosis si se proporcionaron
        if (request.getDosisAplicaciones() != null && !request.getDosisAplicaciones().isEmpty()) {
            for (CrearInsumoConDosisRequest.DosisInsumoRequest dosisRequest : request.getDosisAplicaciones()) {
                DosisAplicacion dosis = new DosisAplicacion();
                dosis.setInsumo(savedInsumo);
                dosis.setTipoAplicacion(dosisRequest.getTipoAplicacion());
                dosis.setDosisPorHa(dosisRequest.getDosisPorHa());
                dosis.setUnidadMedida(dosisRequest.getUnidadMedida() != null ? dosisRequest.getUnidadMedida() : savedInsumo.getUnidadMedida());
                dosis.setDescripcion(dosisRequest.getDescripcion());
                dosis.setActivo(true);
                
                dosisAplicacionRepository.save(dosis);
                System.out.println("[INSUMO_SERVICE] Dosis creada: " + dosisRequest.getTipoAplicacion());
            }
        }
        
        // 3. Retornar el insumo con sus dosis
        return convertToInsumoConDosisDTO(savedInsumo);
    }
    
    /**
     * Actualizar un insumo con sus dosis de aplicación
     */
    public InsumoConDosisDTO updateInsumoConDosis(Long id, CrearInsumoConDosisRequest request, User user) {
        System.out.println("[INSUMO_SERVICE] Actualizando insumo con dosis: " + id);
        
        // 1. Obtener el insumo existente
        Insumo insumo = insumoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado con ID: " + id));
        
        // 2. Actualizar los campos del insumo
        insumo.setNombre(request.getNombre());
        insumo.setDescripcion(request.getDescripcion());
        insumo.setTipo(request.getTipo());
        insumo.setUnidadMedida(request.getUnidadMedida());
        insumo.setPrecioUnitario(request.getPrecioUnitario());
        insumo.setStockMinimo(request.getStockMinimo() != null ? request.getStockMinimo() : BigDecimal.ZERO);
        insumo.setStockActual(request.getStockActual() != null ? request.getStockActual() : BigDecimal.ZERO);
        insumo.setProveedor(request.getProveedor());
        insumo.setFechaVencimiento(request.getFechaVencimiento());
        
        Insumo savedInsumo = insumoRepository.save(insumo);
        
        // 3. Eliminar dosis existentes
        List<DosisAplicacion> dosisExistentes = dosisAplicacionRepository.findByInsumo(insumo);
        for (DosisAplicacion dosis : dosisExistentes) {
            dosis.setActivo(false);
            dosisAplicacionRepository.save(dosis);
        }
        
        // 4. Crear las nuevas dosis
        if (request.getDosisAplicaciones() != null && !request.getDosisAplicaciones().isEmpty()) {
            for (CrearInsumoConDosisRequest.DosisInsumoRequest dosisRequest : request.getDosisAplicaciones()) {
                DosisAplicacion dosis = new DosisAplicacion();
                dosis.setInsumo(savedInsumo);
                dosis.setTipoAplicacion(dosisRequest.getTipoAplicacion());
                dosis.setDosisPorHa(dosisRequest.getDosisPorHa());
                dosis.setUnidadMedida(dosisRequest.getUnidadMedida() != null ? dosisRequest.getUnidadMedida() : savedInsumo.getUnidadMedida());
                dosis.setDescripcion(dosisRequest.getDescripcion());
                dosis.setActivo(true);
                
                dosisAplicacionRepository.save(dosis);
            }
        }
        
        return convertToInsumoConDosisDTO(savedInsumo);
    }
    
    /**
     * Obtener un insumo con sus dosis de aplicación
     */
    public InsumoConDosisDTO getInsumoConDosis(Long id) {
        Insumo insumo = insumoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado con ID: " + id));
        
        return convertToInsumoConDosisDTO(insumo);
    }
    
    /**
     * Verificar si un insumo tiene dosis configuradas
     */
    public boolean tieneDosisConfiguradas(Long insumoId) {
        Insumo insumo = insumoRepository.findById(insumoId)
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado con ID: " + insumoId));
        
        long count = dosisAplicacionRepository.countByInsumoAndActivoTrue(insumo);
        return count > 0;
    }
    
    /**
     * Convertir Insumo a InsumoConDosisDTO
     */
    private InsumoConDosisDTO convertToInsumoConDosisDTO(Insumo insumo) {
        InsumoConDosisDTO dto = new InsumoConDosisDTO();
        dto.setId(insumo.getId());
        dto.setNombre(insumo.getNombre());
        dto.setDescripcion(insumo.getDescripcion());
        dto.setTipo(insumo.getTipo());
        dto.setUnidadMedida(insumo.getUnidadMedida());
        dto.setPrecioUnitario(insumo.getPrecioUnitario());
        dto.setStockMinimo(insumo.getStockMinimo());
        dto.setStockActual(insumo.getStockActual());
        dto.setProveedor(insumo.getProveedor());
        dto.setFechaVencimiento(insumo.getFechaVencimiento());
        dto.setActivo(insumo.getActivo());
        dto.setFechaCreacion(insumo.getFechaCreacion());
        dto.setFechaActualizacion(insumo.getFechaActualizacion());
        
        // Obtener las dosis activas
        List<DosisAplicacion> dosis = dosisAplicacionRepository.findByInsumoAndActivoTrue(insumo);
        dto.setTieneDosisConfiguradas(!dosis.isEmpty());
        
        if (dosis.isEmpty()) {
            dto.setMensajeSugerencia("Este insumo no tiene dosis configuradas. Se recomienda configurar dosis sugeridas para facilitar las aplicaciones.");
        } else {
            dto.setMensajeSugerencia("Insumo con " + dosis.size() + " dosis configuradas.");
        }
        
        // Convertir las dosis a DTOs
        List<com.agrocloud.dto.DosisAplicacionDTO> dosisDTOs = dosis.stream()
                .map(this::convertDosisToDTO)
                .toList();
        dto.setDosisAplicaciones(dosisDTOs);
        
        return dto;
    }
    
    /**
     * Convertir DosisAplicacion a DTO
     */
    private com.agrocloud.dto.DosisAplicacionDTO convertDosisToDTO(DosisAplicacion dosis) {
        com.agrocloud.dto.DosisAplicacionDTO dto = new com.agrocloud.dto.DosisAplicacionDTO();
        dto.setId(dosis.getId());
        dto.setInsumoId(dosis.getInsumo().getId());
        dto.setInsumoNombre(dosis.getInsumo().getNombre());
        dto.setTipoAplicacion(dosis.getTipoAplicacion());
        dto.setDosisPorHa(dosis.getDosisPorHa());
        dto.setUnidadMedida(dosis.getUnidadMedida());
        dto.setDescripcion(dosis.getDescripcion());
        dto.setActivo(dosis.getActivo());
        return dto;
    }

    // ========================================
    // MÉTODOS PARA EL WIZARD DE INSUMOS
    // ========================================

    /**
     * Crear insumo desde DTO
     */
    public Insumo crearInsumo(InsumoDTO insumoDTO) {
        Insumo insumo = new Insumo();
        insumo.setNombre(insumoDTO.getNombre());
        insumo.setDescripcion(insumoDTO.getDescripcion());
        insumo.setTipo(Insumo.TipoInsumo.valueOf(insumoDTO.getTipo()));
        insumo.setUnidadMedida(insumoDTO.getUnidadMedida());
        insumo.setPrecioUnitario(insumoDTO.getPrecioUnitario());
        insumo.setStockActual(insumoDTO.getStockActual());
        insumo.setStockMinimo(insumoDTO.getStockMinimo());
        insumo.setProveedor(insumoDTO.getProveedor());
        if (insumoDTO.getFechaVencimiento() != null && !insumoDTO.getFechaVencimiento().isEmpty()) {
            insumo.setFechaVencimiento(LocalDate.parse(insumoDTO.getFechaVencimiento()));
        }
        insumo.setActivo(true);
        
        // Campos específicos de agroquímicos
        insumo.setPrincipioActivo(insumoDTO.getPrincipioActivo());
        insumo.setConcentracion(insumoDTO.getConcentracion());
        insumo.setClaseQuimica(insumoDTO.getClaseQuimica());
        insumo.setCategoriaToxicologica(insumoDTO.getCategoriaToxicologica());
        insumo.setPeriodoCarenciaDias(insumoDTO.getPeriodoCarenciaDias());
        insumo.setDosisMinimaPorHa(insumoDTO.getDosisMinimaPorHa());
        insumo.setDosisMaximaPorHa(insumoDTO.getDosisMaximaPorHa());
        insumo.setUnidadDosis(insumoDTO.getUnidadDosis());
        
        return insumoRepository.save(insumo);
    }

    /**
     * Actualizar insumo desde DTO
     */
    public Insumo actualizarInsumo(Long id, InsumoDTO insumoDTO) {
        Insumo insumo = insumoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Insumo no encontrado"));
        
        insumo.setNombre(insumoDTO.getNombre());
        insumo.setDescripcion(insumoDTO.getDescripcion());
        insumo.setTipo(Insumo.TipoInsumo.valueOf(insumoDTO.getTipo()));
        insumo.setUnidadMedida(insumoDTO.getUnidadMedida());
        insumo.setPrecioUnitario(insumoDTO.getPrecioUnitario());
        insumo.setStockActual(insumoDTO.getStockActual());
        insumo.setStockMinimo(insumoDTO.getStockMinimo());
        insumo.setProveedor(insumoDTO.getProveedor());
        if (insumoDTO.getFechaVencimiento() != null && !insumoDTO.getFechaVencimiento().isEmpty()) {
            insumo.setFechaVencimiento(LocalDate.parse(insumoDTO.getFechaVencimiento()));
        }
        
        // Campos específicos de agroquímicos
        insumo.setPrincipioActivo(insumoDTO.getPrincipioActivo());
        insumo.setConcentracion(insumoDTO.getConcentracion());
        insumo.setClaseQuimica(insumoDTO.getClaseQuimica());
        insumo.setCategoriaToxicologica(insumoDTO.getCategoriaToxicologica());
        insumo.setPeriodoCarenciaDias(insumoDTO.getPeriodoCarenciaDias());
        insumo.setDosisMinimaPorHa(insumoDTO.getDosisMinimaPorHa());
        insumo.setDosisMaximaPorHa(insumoDTO.getDosisMaximaPorHa());
        insumo.setUnidadDosis(insumoDTO.getUnidadDosis());
        
        return insumoRepository.save(insumo);
    }

    /**
     * Eliminar insumo (lógica)
     */
    public void eliminarInsumo(Long id) {
        Insumo insumo = insumoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Insumo no encontrado"));
        
        insumo.setActivo(false);
        insumoRepository.save(insumo);
    }

    /**
     * Obtener todos los insumos como DTO
     */
    public List<InsumoDTO> obtenerTodosLosInsumos() {
        return insumoRepository.findAll().stream()
            .map(this::convertirInsumoADTO)
            .collect(Collectors.toList());
    }

    /**
     * Obtener insumo por ID como DTO
     */
    public InsumoDTO obtenerInsumoPorId(Long id) {
        Insumo insumo = insumoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Insumo no encontrado"));
        
        return convertirInsumoADTO(insumo);
    }

    /**
     * Convertir entidad Insumo a DTO
     */
    private InsumoDTO convertirInsumoADTO(Insumo insumo) {
        InsumoDTO dto = new InsumoDTO();
        dto.setId(insumo.getId());
        dto.setNombre(insumo.getNombre());
        dto.setDescripcion(insumo.getDescripcion());
        dto.setTipo(insumo.getTipo().toString());
        dto.setUnidadMedida(insumo.getUnidadMedida());
        dto.setPrecioUnitario(insumo.getPrecioUnitario());
        dto.setStockActual(insumo.getStockActual());
        dto.setStockMinimo(insumo.getStockMinimo());
        dto.setProveedor(insumo.getProveedor());
        dto.setFechaVencimiento(insumo.getFechaVencimiento() != null ? insumo.getFechaVencimiento().toString() : null);
        dto.setActivo(insumo.getActivo());
        
        // Campos específicos de agroquímicos
        dto.setPrincipioActivo(insumo.getPrincipioActivo());
        dto.setConcentracion(insumo.getConcentracion());
        dto.setClaseQuimica(insumo.getClaseQuimica());
        dto.setCategoriaToxicologica(insumo.getCategoriaToxicologica());
        dto.setPeriodoCarenciaDias(insumo.getPeriodoCarenciaDias());
        dto.setDosisMinimaPorHa(insumo.getDosisMinimaPorHa());
        dto.setDosisMaximaPorHa(insumo.getDosisMaximaPorHa());
        dto.setUnidadDosis(insumo.getUnidadDosis());
        
        return dto;
    }
}
