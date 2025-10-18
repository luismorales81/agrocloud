package com.agrocloud.service;

import com.agrocloud.model.entity.Insumo;
import com.agrocloud.model.entity.User;
import com.agrocloud.model.enums.RolEmpresa;
import com.agrocloud.repository.InsumoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InsumoService {

    @Autowired
    private InsumoRepository insumoRepository;


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
        
        // Inicializar relaciones lazy para evitar LazyInitializationException en serialización JSON
        if (insumos != null) {
            insumos.forEach(insumo -> {
                if (insumo.getEmpresa() != null) {
                    insumo.getEmpresa().getId(); // Inicializar empresa
                }
                if (insumo.getUser() != null) {
                    insumo.getUser().getId(); // Inicializar usuario
                }
            });
        }
        
        return insumos;
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
}
