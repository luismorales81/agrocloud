package com.agrocloud.service;

import com.agrocloud.model.entity.Plot;
import com.agrocloud.model.entity.User;
import com.agrocloud.repository.PlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PlotService {

    @Autowired
    private PlotRepository plotRepository;

    // Obtener todos los lotes activos accesibles por un usuario
    public List<Plot> getLotesByUser(User user) {
        if (user.isAdmin()) {
            // Admin ve todos los lotes activos
            return plotRepository.findByActivoTrue();
        } else {
            // Usuario ve sus lotes activos y los de sus sub-usuarios
            return plotRepository.findAccessibleByUserAndActivoTrue(user);
        }
    }
    
    // Obtener todos los lotes (incluyendo inactivos) - solo para administradores
    public List<Plot> getAllLotesIncludingInactive(User user) {
        if (!user.isAdmin()) {
            return getLotesByUser(user); // Usuarios normales solo ven activos
        }
        
        return plotRepository.findAll();
    }

    // Obtener lote por ID (con validación de acceso)
    public Optional<Plot> getLoteById(Long id, User user) {
        Optional<Plot> lote = plotRepository.findById(id);
        
        if (lote.isPresent()) {
            Plot p = lote.get();
            if (user.isAdmin() || p.getUser().getId().equals(user.getId())) {
                return lote;
            }
        }
        
        return Optional.empty();
    }

    // Crear nuevo lote
    public Plot saveLote(Plot lote) {
        return plotRepository.save(lote);
    }

    // Actualizar lote (con validación de acceso)
    public Optional<Plot> updateLote(Long id, Plot loteData, User user) {
        Optional<Plot> existingLote = getLoteById(id, user);
        
        if (existingLote.isPresent()) {
            Plot lote = existingLote.get();
            lote.setNombre(loteData.getNombre());
            lote.setDescripcion(loteData.getDescripcion());
            lote.setAreaHectareas(loteData.getAreaHectareas());
            lote.setEstado(loteData.getEstado());
            lote.setTipoSuelo(loteData.getTipoSuelo());
            lote.setActivo(loteData.getActivo());
            lote.setCampo(loteData.getCampo());
            
            return Optional.of(plotRepository.save(lote));
        }
        
        return Optional.empty();
    }

    // Eliminar lote lógicamente (con validación de acceso)
    public boolean deleteLote(Long id, User user) {
        Optional<Plot> existingLote = getLoteById(id, user);
        
        if (existingLote.isPresent()) {
            Plot lote = existingLote.get();
            lote.setActivo(false); // Eliminación lógica
            plotRepository.save(lote);
            return true;
        }
        return false;
    }
    
    // Eliminar lote físicamente (solo para administradores)
    public boolean deleteLoteFisicamente(Long id, User user) {
        if (!user.isAdmin()) {
            return false; // Solo administradores pueden eliminar físicamente
        }
        
        if (plotRepository.existsById(id)) {
            plotRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Buscar lotes por nombre
    public List<Plot> searchLotesByNombre(String nombre, User user) {
        if (user.isAdmin()) {
            return plotRepository.findAll().stream()
                    .filter(lote -> lote.getNombre().toLowerCase().contains(nombre.toLowerCase()))
                    .toList();
        } else {
            return plotRepository.findByUserIdAndNombreContaining(user.getId(), nombre);
        }
    }

    // Contar lotes por usuario
    public long countLotesByUser(User user) {
        if (user.isAdmin()) {
            return plotRepository.count();
        } else {
            return plotRepository.countByUserId(user.getId());
        }
    }

    // Contar lotes activos por usuario
    public long countLotesActivosByUser(User user) {
        if (user.isAdmin()) {
            return plotRepository.findByActivoTrue().size();
        } else {
            return plotRepository.countByUserIdAndActivoTrue(user.getId());
        }
    }

    // Obtener lotes por campo
    public List<Plot> getLotesByCampo(Long campoId, User user) {
        if (user.isAdmin()) {
            return plotRepository.findAll().stream()
                    .filter(lote -> lote.getCampo() != null && lote.getCampo().getId().equals(campoId))
                    .toList();
        } else {
            return plotRepository.findByCampoIdAndUserId(campoId, user.getId());
        }
    }
}
