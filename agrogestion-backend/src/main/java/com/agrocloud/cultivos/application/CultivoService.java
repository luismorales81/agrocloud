package com.agrocloud.cultivos.application;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.cultivos.domain.Cultivo;
import com.agrocloud.core.domain.User;
import com.agrocloud.model.enums.RolEmpresa;
import com.agrocloud.cultivos.infrastructure.CultivoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service("cultivoServiceCultivos")
@Transactional
public class CultivoService {

    @Autowired
    private CultivoRepository cultivoRepository;

    public List<Cultivo> getCultivosByUser(User user) {
        List<Cultivo> cultivos;
        if (user.isAdmin() || user.tieneRolEnEmpresa(RolEmpresa.JEFE_CAMPO) || user.tieneRolEnEmpresa(RolEmpresa.OPERARIO) || user.tieneRolEnEmpresa(RolEmpresa.CONSULTOR_EXTERNO))
            cultivos = cultivoRepository.findByActivoTrue();
        else
            cultivos = cultivoRepository.findByUsuarioIdAndActivoTrue(user.getId());
        if (cultivos != null) {
            cultivos.forEach(cultivo -> {
                if (cultivo.getEmpresa() != null) cultivo.getEmpresa().getId();
                if (cultivo.getUsuario() != null) cultivo.getUsuario().getId();
            });
        }
        return cultivos;
    }

    public Optional<Cultivo> getCultivoById(Long id, User user) {
        Optional<Cultivo> cultivo = cultivoRepository.findById(id);
        if (cultivo.isPresent()) {
            Cultivo c = cultivo.get();
            if (c.getActivo() && (user.isAdmin() || user.tieneRolEnEmpresa(RolEmpresa.JEFE_CAMPO) || c.getUsuario().getId().equals(user.getId())))
                return cultivo;
        }
        return Optional.empty();
    }

    public Cultivo saveCultivo(Cultivo cultivo) {
        return cultivoRepository.save(cultivo);
    }

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

    public boolean deleteCultivoFisicamente(Long id, User user) {
        if (user.isAdmin()) {
            if (cultivoRepository.findById(id).isPresent()) {
                cultivoRepository.deleteById(id);
                return true;
            }
        }
        return false;
    }

    public List<Cultivo> searchCultivosByNombre(String nombre, User user) {
        if (user.isAdmin()) return cultivoRepository.findByNombreContaining(nombre);
        return cultivoRepository.findByUsuarioIdAndNombreContaining(user.getId(), nombre);
    }

    public List<Cultivo> searchCultivosByVariedad(String variedad, User user) {
        if (user.isAdmin()) return cultivoRepository.findByVariedadContaining(variedad);
        return cultivoRepository.findByUsuarioIdAndVariedadContaining(user.getId(), variedad);
    }

    public long countCultivosByUser(User user) {
        return user.isAdmin() ? cultivoRepository.count() : cultivoRepository.countByUsuarioId(user.getId());
    }

    public long countCultivosActivosByUser(User user) {
        if (user.isAdmin()) return cultivoRepository.findByActivoTrue().size();
        return cultivoRepository.countByUsuarioIdAndActivoTrue(user.getId());
    }

    public List<Cultivo> getCultivosEliminados(User user) {
        if (user.isAdmin()) return cultivoRepository.findByActivoFalse();
        return cultivoRepository.findByUsuarioIdAndActivoFalse(user.getId());
    }

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
