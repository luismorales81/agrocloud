package com.agrocloud.service;

import com.agrocloud.model.entity.Plot;
import com.agrocloud.model.entity.User;
import com.agrocloud.model.entity.Empresa;
import com.agrocloud.repository.PlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PlotService {

    @Autowired
    private PlotRepository plotRepository;

    @Autowired
    private UserService userService;

    // Obtener todos los lotes activos accesibles por un usuario
    public List<Plot> getLotesByUser(User user) {
        try {
            System.out.println("[PLOT_SERVICE] getLotesByUser iniciado para usuario: " + (user != null ? user.getEmail() : "null"));
            
            if (user == null) {
                System.err.println("[PLOT_SERVICE] ERROR: Usuario es null");
                return new ArrayList<>();
            }
            
            if (user.isSuperAdmin()) {
                // Solo SuperAdmin ve todos los lotes activos
                System.out.println("[PLOT_SERVICE] Usuario es SuperAdmin, mostrando todos los lotes");
                return plotRepository.findAll().stream()
                        .filter(plot -> {
                            Boolean activo = plot.getActivo();
                            return activo != null && activo;
                        })
                        .toList();
            } else if (user.getUserCompanyRoles() != null && !user.getUserCompanyRoles().isEmpty() && 
                       user.esAdministradorEmpresa(user.getEmpresa() != null ? user.getEmpresa().getId() : null)) {
                // Admin de empresa ve TODOS los lotes de su empresa
                System.out.println("[PLOT_SERVICE] Usuario es Admin de empresa, mostrando TODOS los lotes de la empresa");
                
                Empresa empresa = user.getEmpresa();
                if (empresa == null) {
                    System.out.println("[PLOT_SERVICE] Usuario ADMIN no tiene empresa asignada, mostrando solo sus lotes");
                    // Si no tiene empresa, mostrar solo sus lotes
                    List<Plot> userPlots = plotRepository.findByUserIdAndActivoTrue(user.getId());
                    return userPlots != null ? userPlots : new ArrayList<>();
                }
                
                // Obtener todos los usuarios de la empresa
                List<User> todosUsuarios = userService.findAll();
                List<User> usuariosEmpresa = todosUsuarios.stream()
                        .filter(u -> u.perteneceAEmpresa(empresa.getId()))
                        .collect(Collectors.toList());
                
                System.out.println("[PLOT_SERVICE] Empresa ID: " + empresa.getId() + ", Usuarios: " + usuariosEmpresa.size());
                
                // Obtener lotes de TODOS los usuarios de la empresa
                List<Plot> todosLosLotes = new ArrayList<>();
                for (User userEmpresa : usuariosEmpresa) {
                    List<Plot> lotesUsuario = plotRepository.findByUserIdAndActivoTrue(userEmpresa.getId());
                    if (lotesUsuario != null) {
                        todosLosLotes.addAll(lotesUsuario);
                        System.out.println("[PLOT_SERVICE] Lotes del usuario " + userEmpresa.getUsername() + ": " + lotesUsuario.size());
                    }
                }
                
                System.out.println("[PLOT_SERVICE] Total lotes de la empresa: " + todosLosLotes.size());
                return todosLosLotes;
            } else {
                // Otros usuarios ven solo sus lotes y los de sus sub-usuarios
                System.out.println("[PLOT_SERVICE] Usuario normal, mostrando lotes accesibles");
                
                // Obtener lotes del usuario actual
                List<Plot> userPlots = plotRepository.findByUserIdAndActivoTrue(user.getId());
                System.out.println("[PLOT_SERVICE] Lotes del usuario encontrados: " + (userPlots != null ? userPlots.size() : "null"));
                
                if (userPlots == null) {
                    userPlots = new ArrayList<>();
                }
                
                // Obtener lotes de usuarios dependientes
                List<User> usuariosDependientes = userService.findByParentUserId(user.getId());
                if (usuariosDependientes != null && !usuariosDependientes.isEmpty()) {
                    System.out.println("[PLOT_SERVICE] Usuarios dependientes encontrados: " + usuariosDependientes.size());
                    for (User dependiente : usuariosDependientes) {
                        List<Plot> lotesDependiente = plotRepository.findByUserIdAndActivoTrue(dependiente.getId());
                        if (lotesDependiente != null) {
                            userPlots.addAll(lotesDependiente);
                            System.out.println("[PLOT_SERVICE] Lotes del dependiente " + dependiente.getUsername() + ": " + lotesDependiente.size());
                        }
                    }
                }
                
                System.out.println("[PLOT_SERVICE] Total lotes accesibles: " + userPlots.size());
                return userPlots;
            }
        } catch (Exception e) {
            System.err.println("[PLOT_SERVICE] ERROR en getLotesByUser: " + e.getMessage());
            System.err.println("[PLOT_SERVICE] Stack trace completo:");
            e.printStackTrace();
            throw new RuntimeException("Error al obtener lotes del usuario: " + e.getMessage(), e);
        }
    }
    
    // Obtener todos los lotes (incluyendo inactivos) - solo para administradores
    public List<Plot> getAllLotesIncludingInactive(User user) {
        if (!user.isSuperAdmin()) {
            return getLotesByUser(user); // Usuarios normales solo ven activos
        }
        
        return plotRepository.findAll();
    }

    // Obtener lote por ID (con validación de acceso)
    public Optional<Plot> getLoteById(Long id, User user) {
        Optional<Plot> lote = plotRepository.findById(id);
        
        if (lote.isPresent()) {
            Plot p = lote.get();
            if (user.isSuperAdmin() || p.getUser().getId().equals(user.getId())) {
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
        if (!user.isSuperAdmin()) {
            return false; // Solo SuperAdmin puede eliminar físicamente
        }
        
        if (plotRepository.existsById(id)) {
            plotRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Buscar lotes por nombre
    public List<Plot> searchLotesByNombre(String nombre, User user) {
        if (user.isSuperAdmin()) {
            return plotRepository.findAll().stream()
                    .filter(lote -> lote.getNombre().toLowerCase().contains(nombre.toLowerCase()))
                    .toList();
        } else {
            return plotRepository.findByUserIdAndNombreContaining(user.getId(), nombre);
        }
    }

    // Contar lotes por usuario
    public long countLotesByUser(User user) {
        if (user.isSuperAdmin()) {
            return plotRepository.count();
        } else {
            return plotRepository.countByUserId(user.getId());
        }
    }

    // Contar lotes activos por usuario
    public long countLotesActivosByUser(User user) {
        if (user.isSuperAdmin()) {
            return plotRepository.findByActivoTrue().size();
        } else {
            return plotRepository.countByUserIdAndActivoTrue(user.getId());
        }
    }

    // Obtener lotes por campo
    public List<Plot> getLotesByCampo(Long campoId, User user) {
        if (user.isSuperAdmin()) {
            return plotRepository.findAll().stream()
                    .filter(lote -> lote.getCampo() != null && lote.getCampo().getId().equals(campoId))
                    .toList();
        } else {
            return plotRepository.findByCampoIdAndUserId(campoId, user.getId());
        }
    }
}
