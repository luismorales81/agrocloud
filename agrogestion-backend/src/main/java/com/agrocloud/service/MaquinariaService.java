package com.agrocloud.service;

import com.agrocloud.model.entity.Maquinaria;
import com.agrocloud.model.entity.User;
import com.agrocloud.repository.MaquinariaRepository;
import com.agrocloud.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MaquinariaService {

    @Autowired
    private MaquinariaRepository maquinariaRepository;

    @Autowired
    private UserRepository userRepository;

    // Obtener todas las maquinarias (para administración global)
    public List<Maquinaria> getAllMaquinaria() {
        return maquinariaRepository.findAll();
    }

    // Obtener todas las maquinarias accesibles por un usuario
    public List<Maquinaria> getMaquinariasByUser(User user) {
        if (user.isAdmin()) {
            // Admin ve todas las maquinarias
            return maquinariaRepository.findAll();
        } else {
            // Usuario ve sus maquinarias y las de sus sub-usuarios
            return maquinariaRepository.findAccessibleByUser(user);
        }
    }

    // Obtener maquinaria por ID (con validación de acceso)
    public Optional<Maquinaria> getMaquinariaById(Long id, User user) {
        Optional<Maquinaria> maquinaria = maquinariaRepository.findById(id);
        
        if (maquinaria.isPresent()) {
            Maquinaria m = maquinaria.get();
            if (user.isAdmin() || user.canAccessUser(m.getUser())) {
                return maquinaria;
            }
        }
        
        return Optional.empty();
    }

    // Crear nueva maquinaria
    public Maquinaria createMaquinaria(Maquinaria maquinaria, User user) {
        maquinaria.setUser(user);
        maquinaria.setEmpresa(user.getEmpresa());
        return maquinariaRepository.save(maquinaria);
    }

    // Actualizar maquinaria (con validación de acceso)
    public Optional<Maquinaria> updateMaquinaria(Long id, Maquinaria maquinariaData, User user) {
        Optional<Maquinaria> existingMaquinaria = maquinariaRepository.findById(id);
        
        if (existingMaquinaria.isPresent()) {
            Maquinaria maquinaria = existingMaquinaria.get();
            
            // Verificar acceso
            if (user.isAdmin() || user.canAccessUser(maquinaria.getUser())) {
                // Actualizar campos básicos
                maquinaria.setNombre(maquinariaData.getNombre());
                maquinaria.setDescripcion(maquinariaData.getDescripcion());
                maquinaria.setMarca(maquinariaData.getMarca());
                maquinaria.setModelo(maquinariaData.getModelo());
                maquinaria.setAnioFabricacion(maquinariaData.getAnioFabricacion());
                maquinaria.setNumeroSerie(maquinariaData.getNumeroSerie());
                maquinaria.setEstado(maquinariaData.getEstado());
                
                // Actualizar campos de costos
                maquinaria.setKilometrosUso(maquinariaData.getKilometrosUso());
                maquinaria.setCostoPorHora(maquinariaData.getCostoPorHora());
                maquinaria.setKilometrosMantenimientoIntervalo(maquinariaData.getKilometrosMantenimientoIntervalo());
                maquinaria.setUltimoMantenimientoKilometros(maquinariaData.getUltimoMantenimientoKilometros());
                maquinaria.setRendimientoCombustible(maquinariaData.getRendimientoCombustible());
                maquinaria.setUnidadRendimiento(maquinariaData.getUnidadRendimiento());
                maquinaria.setCostoCombustiblePorLitro(maquinariaData.getCostoCombustiblePorLitro());
                maquinaria.setValorActual(maquinariaData.getValorActual());
                maquinaria.setFechaCompra(maquinariaData.getFechaCompra());
                maquinaria.setTipo(maquinariaData.getTipo());
                
                return Optional.of(maquinariaRepository.save(maquinaria));
            }
        }
        
        return Optional.empty();
    }

    // Eliminar maquinaria lógicamente (con validación de acceso)
    public boolean deleteMaquinaria(Long id, User user) {
        Optional<Maquinaria> maquinaria = maquinariaRepository.findById(id);
        
        if (maquinaria.isPresent()) {
            Maquinaria m = maquinaria.get();
            if (user.isAdmin() || user.canAccessUser(m.getUser())) {
                m.setActivo(false);
                maquinariaRepository.save(m);
                return true;
            }
        }
        
        return false;
    }

    // Eliminar maquinaria físicamente (solo para administradores)
    public boolean deleteMaquinariaFisicamente(Long id, User user) {
        if (!user.isAdmin()) {
            return false;
        }
        
        Optional<Maquinaria> maquinaria = maquinariaRepository.findById(id);
        if (maquinaria.isPresent()) {
            maquinariaRepository.delete(maquinaria.get());
            return true;
        }
        
        return false;
    }

    // Buscar maquinaria por nombre
    public List<Maquinaria> searchMaquinariaByName(String nombre, User user) {
        if (user.isAdmin()) {
            // Admin busca en todas las maquinarias
            return maquinariaRepository.findAll().stream()
                    .filter(m -> m.getNombre().toLowerCase().contains(nombre.toLowerCase()))
                    .toList();
        } else {
            // Usuario busca solo en sus maquinarias accesibles
            return maquinariaRepository.findAccessibleByUser(user).stream()
                    .filter(m -> m.getNombre().toLowerCase().contains(nombre.toLowerCase()))
                    .toList();
        }
    }

    // Obtener estadísticas de maquinaria por usuario
    public MaquinariaStats getMaquinariaStats(User user) {
        List<Maquinaria> maquinarias = getMaquinariasByUser(user);
        
        long total = maquinarias.size();
        long activas = maquinarias.stream().filter(m -> Maquinaria.EstadoMaquinaria.ACTIVA.equals(m.getEstado())).count();
        long operativas = maquinarias.stream()
                .filter(m -> Maquinaria.EstadoMaquinaria.ACTIVA.equals(m.getEstado()))
                .count();
        
        return new MaquinariaStats(total, activas, operativas);
    }

    // Clase interna para estadísticas
    public static class MaquinariaStats {
        private final long total;
        private final long activas;
        private final long operativas;

        public MaquinariaStats(long total, long activas, long operativas) {
            this.total = total;
            this.activas = activas;
            this.operativas = operativas;
        }

        public long getTotal() { return total; }
        public long getActivas() { return activas; }
        public long getOperativas() { return operativas; }
    }
}
