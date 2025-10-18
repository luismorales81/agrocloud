package com.agrocloud.service;

import com.agrocloud.model.entity.Field;
import com.agrocloud.model.entity.Plot;
import com.agrocloud.model.entity.User;
import com.agrocloud.model.entity.Empresa;
import com.agrocloud.model.enums.RolEmpresa;
import com.agrocloud.repository.FieldRepository;
import com.agrocloud.repository.PlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    
    @Autowired
    private FieldRepository fieldRepository;

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
                List<Plot> lotes = plotRepository.findAll().stream()
                        .filter(plot -> {
                            Boolean activo = plot.getActivo();
                            return activo != null && activo;
                        })
                        .toList();
                
                // Inicializar relaciones lazy para evitar LazyInitializationException en serialización JSON
                lotes.forEach(plot -> {
                    if (plot.getCampo() != null) {
                        plot.getCampo().getNombre(); // Inicializar campo
                    }
                });
                
                return lotes;
            } else if (user.esAdministradorEmpresa(user.getEmpresa() != null ? user.getEmpresa().getId() : null) ||
                        user.tieneRolEnEmpresa(RolEmpresa.JEFE_CAMPO) ||
                        user.tieneRolEnEmpresa(RolEmpresa.OPERARIO) ||
                        user.tieneRolEnEmpresa(RolEmpresa.CONSULTOR_EXTERNO)) {
                // Admin, JEFE_CAMPO, OPERARIO y CONSULTOR_EXTERNO ven TODOS los lotes de su empresa (solo lectura para OPERARIO y CONSULTOR_EXTERNO)
                System.out.println("[PLOT_SERVICE] Usuario es Admin/JEFE_CAMPO/OPERARIO/CONSULTOR_EXTERNO de empresa, mostrando TODOS los lotes de la empresa");
                
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
                
                // Inicializar relaciones lazy para evitar LazyInitializationException en serialización JSON
                todosLosLotes.forEach(plot -> {
                    if (plot.getCampo() != null) {
                        plot.getCampo().getNombre(); // Inicializar campo
                    }
                });
                
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
                
                // Inicializar relaciones lazy para evitar LazyInitializationException en serialización JSON
                userPlots.forEach(plot -> {
                    if (plot.getCampo() != null) {
                        plot.getCampo().getNombre(); // Inicializar campo
                    }
                });
                
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
        // Validar superficie disponible si tiene campo asignado
        if (lote.getCampo() != null) {
            validarSuperficieDisponible(lote.getCampo().getId(), lote.getAreaHectareas(), lote.getId());
        }
        return plotRepository.save(lote);
    }

    // Actualizar lote (con validación de acceso)
    public Optional<Plot> updateLote(Long id, Plot loteData, User user) {
        Optional<Plot> existingLote = getLoteById(id, user);
        
        if (existingLote.isPresent()) {
            Plot lote = existingLote.get();
            
            // Validar superficie disponible si se cambia el campo o la superficie
            if (loteData.getCampo() != null) {
                validarSuperficieDisponible(loteData.getCampo().getId(), loteData.getAreaHectareas(), id);
            }
            
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
    
    /**
     * Calcula la superficie disponible de un campo
     * @param campoId ID del campo
     * @return Superficie disponible en hectáreas
     */
    public BigDecimal calcularSuperficieDisponible(Long campoId) {
        Field campo = fieldRepository.findById(campoId)
                .orElseThrow(() -> new RuntimeException("Campo no encontrado con ID: " + campoId));
        
        BigDecimal superficieTotal = campo.getAreaHectareas();
        BigDecimal superficieOcupada = plotRepository.calcularSuperficieOcupadaPorCampo(campoId);
        
        return superficieTotal.subtract(superficieOcupada);
    }
    
    /**
     * Valida que la superficie del lote no exceda la superficie disponible del campo
     * @param campoId ID del campo
     * @param superficieLote Superficie del lote a validar
     * @param loteIdActual ID del lote actual (null si es un lote nuevo)
     * @throws RuntimeException si la superficie excede la disponible
     */
    private void validarSuperficieDisponible(Long campoId, BigDecimal superficieLote, Long loteIdActual) {
        System.out.println("[PLOT_SERVICE] Validando superficie disponible para campo ID: " + campoId);
        
        // Obtener campo
        Field campo = fieldRepository.findById(campoId)
                .orElseThrow(() -> new RuntimeException("Campo no encontrado con ID: " + campoId));
        
        BigDecimal superficieTotal = campo.getAreaHectareas();
        BigDecimal superficieOcupada = plotRepository.calcularSuperficieOcupadaPorCampo(campoId);
        
        // Si estamos actualizando un lote existente, restar su superficie actual de la ocupada
        if (loteIdActual != null) {
            Optional<Plot> loteActual = plotRepository.findById(loteIdActual);
            if (loteActual.isPresent() && loteActual.get().getActivo()) {
                superficieOcupada = superficieOcupada.subtract(loteActual.get().getAreaHectareas());
            }
        }
        
        BigDecimal superficieDisponible = superficieTotal.subtract(superficieOcupada);
        
        System.out.println("[PLOT_SERVICE] Superficie total del campo: " + superficieTotal + " ha");
        System.out.println("[PLOT_SERVICE] Superficie ocupada: " + superficieOcupada + " ha");
        System.out.println("[PLOT_SERVICE] Superficie disponible: " + superficieDisponible + " ha");
        System.out.println("[PLOT_SERVICE] Superficie del lote a crear/actualizar: " + superficieLote + " ha");
        
        // Validar que la superficie del lote no exceda la disponible
        if (superficieLote.compareTo(superficieDisponible) > 0) {
            throw new RuntimeException(
                String.format("La superficie del lote (%.2f ha) excede la superficie disponible del campo (%.2f ha). " +
                             "Superficie total del campo: %.2f ha. Superficie ocupada por otros lotes: %.2f ha.",
                             superficieLote, superficieDisponible, superficieTotal, superficieOcupada)
            );
        }
        
        System.out.println("[PLOT_SERVICE] Validación de superficie exitosa");
    }
}
