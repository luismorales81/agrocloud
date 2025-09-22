package com.agrocloud.service;

import com.agrocloud.model.entity.Cultivo;
import com.agrocloud.model.entity.User;
import com.agrocloud.repository.CultivoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CultivoService {

    @Autowired
    private CultivoRepository cultivoRepository;

    // Obtener todos los cultivos accesibles por un usuario (solo activos)
    public List<Cultivo> getCultivosByUser(User user) {
        if (user.isAdmin()) {
            // Admin ve todos los cultivos activos
            return cultivoRepository.findByActivoTrue();
        } else {
            // Usuario ve sus cultivos activos
            return cultivoRepository.findByUsuarioIdAndActivoTrue(user.getId());
        }
    }

    // Obtener cultivo por ID (con validación de acceso y solo activos)
    public Optional<Cultivo> getCultivoById(Long id, User user) {
        Optional<Cultivo> cultivo = cultivoRepository.findById(id);
        
        if (cultivo.isPresent()) {
            Cultivo c = cultivo.get();
            // Verificar que esté activo y que el usuario tenga acceso
            if (c.getActivo() && (user.isAdmin() || c.getUsuario().getId().equals(user.getId()))) {
                return cultivo;
            }
        }
        
        return Optional.empty();
    }

    // Crear nuevo cultivo
    public Cultivo saveCultivo(Cultivo cultivo) {
        return cultivoRepository.save(cultivo);
    }

    // Actualizar cultivo (con validación de acceso)
    public Optional<Cultivo> updateCultivo(Long id, Cultivo cultivoData, User user) {
        Optional<Cultivo> existingCultivo = getCultivoById(id, user);
        
        if (existingCultivo.isPresent()) {
            Cultivo cultivo = existingCultivo.get();
            cultivo.setNombre(cultivoData.getNombre());
            cultivo.setTipo(cultivoData.getTipo());
            cultivo.setVariedad(cultivoData.getVariedad());
            cultivo.setCicloDias(cultivoData.getCicloDias());
            cultivo.setRendimientoEsperado(cultivoData.getRendimientoEsperado());
            cultivo.setUnidadRendimiento(cultivoData.getUnidadRendimiento());
            cultivo.setPrecioPorTonelada(cultivoData.getPrecioPorTonelada());
            cultivo.setDescripcion(cultivoData.getDescripcion());
            cultivo.setEstado(cultivoData.getEstado());
            
            return Optional.of(cultivoRepository.save(cultivo));
        }
        
        return Optional.empty();
    }

    // Eliminar cultivo lógicamente (con validación de acceso)
    public boolean deleteCultivo(Long id, User user) {
        Optional<Cultivo> cultivoOpt = getCultivoById(id, user);
        
        if (cultivoOpt.isPresent()) {
            Cultivo cultivo = cultivoOpt.get();
            cultivo.setActivo(false);
            cultivo.setEstado(Cultivo.EstadoCultivo.INACTIVO);
            cultivoRepository.save(cultivo);
            return true;
        }
        return false;
    }

    // Eliminar cultivo físicamente (solo para administradores)
    public boolean deleteCultivoFisicamente(Long id, User user) {
        if (user.isAdmin()) {
            Optional<Cultivo> cultivoOpt = cultivoRepository.findById(id);
            if (cultivoOpt.isPresent()) {
                cultivoRepository.deleteById(id);
                return true;
            }
        }
        return false;
    }

    // Buscar cultivos por nombre
    public List<Cultivo> searchCultivosByNombre(String nombre, User user) {
        if (user.isAdmin()) {
            return cultivoRepository.findByNombreContaining(nombre);
        } else {
            return cultivoRepository.findByUsuarioIdAndNombreContaining(user.getId(), nombre);
        }
    }

    // Buscar cultivos por variedad
    public List<Cultivo> searchCultivosByVariedad(String variedad, User user) {
        if (user.isAdmin()) {
            return cultivoRepository.findByVariedadContaining(variedad);
        } else {
            return cultivoRepository.findByUsuarioIdAndVariedadContaining(user.getId(), variedad);
        }
    }

    // Contar cultivos por usuario
    public long countCultivosByUser(User user) {
        if (user.isAdmin()) {
            return cultivoRepository.count();
        } else {
            return cultivoRepository.countByUsuarioId(user.getId());
        }
    }

    // Contar cultivos activos por usuario
    public long countCultivosActivosByUser(User user) {
        if (user.isAdmin()) {
            return cultivoRepository.findByActivoTrue().size();
        } else {
            return cultivoRepository.countByUsuarioIdAndActivoTrue(user.getId());
        }
    }

    // Obtener cultivos eliminados lógicamente (para administradores)
    public List<Cultivo> getCultivosEliminados(User user) {
        if (user.isAdmin()) {
            return cultivoRepository.findByActivoFalse();
        } else {
            return cultivoRepository.findByUsuarioIdAndActivoFalse(user.getId());
        }
    }

    // Restaurar cultivo eliminado lógicamente
    public boolean restaurarCultivo(Long id, User user) {
        if (user.isAdmin()) {
            Optional<Cultivo> cultivoOpt = cultivoRepository.findById(id);
            if (cultivoOpt.isPresent()) {
                Cultivo cultivo = cultivoOpt.get();
                cultivo.setActivo(true);
                cultivo.setEstado(Cultivo.EstadoCultivo.ACTIVO);
                cultivoRepository.save(cultivo);
                return true;
            }
        }
        return false;
    }
}
