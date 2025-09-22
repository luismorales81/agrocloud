package com.agrocloud.service;

import com.agrocloud.model.entity.Insumo;
import com.agrocloud.model.entity.User;
import com.agrocloud.repository.InsumoRepository;
import com.agrocloud.repository.UserRepository;
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

    @Autowired
    private UserRepository userRepository;

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
        if (user.isAdmin()) {
            // Admin ve todos los insumos
            return insumoRepository.findAll();
        } else {
            // Usuario ve sus insumos y los de sus sub-usuarios
            return insumoRepository.findAccessibleByUser(user);
        }
    }

    // Obtener insumo por ID (con validación de acceso)
    public Optional<Insumo> getInsumoById(Long id, User user) {
        Optional<Insumo> insumo = insumoRepository.findById(id);
        
        if (insumo.isPresent()) {
            Insumo i = insumo.get();
            if (user.isAdmin() || user.canAccessUser(i.getUser())) {
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
            if (user.isAdmin() || user.canAccessUser(insumo.getUser())) {
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
        if (!user.isAdmin()) {
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
        if (user.isAdmin()) {
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
        if (user.isAdmin()) {
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
        if (user.isAdmin()) {
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
}
