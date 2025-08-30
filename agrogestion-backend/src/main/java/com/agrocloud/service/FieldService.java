package com.agrocloud.service;

import com.agrocloud.model.entity.Field;
import com.agrocloud.model.entity.User;
import com.agrocloud.repository.FieldRepository;
import com.agrocloud.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FieldService {

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private UserRepository userRepository;

    // Obtener todos los campos (público)
    public List<Field> getAllFields() {
        return fieldRepository.findAll();
    }

    // Obtener campo por ID (público)
    public Field getFieldById(Long id) {
        return fieldRepository.findById(id).orElse(null);
    }

    // Obtener todos los campos accesibles por un usuario
    public List<Field> getFieldsByUser(User user) {
        if (user.isAdmin()) {
            // Admin ve todos los campos
            return fieldRepository.findAll();
        } else {
            // Usuario ve sus campos y los de sus sub-usuarios
            return fieldRepository.findAccessibleByUser(user);
        }
    }

    // Obtener campo por ID (con validación de acceso)
    public Optional<Field> getFieldById(Long id, User user) {
        Optional<Field> field = fieldRepository.findById(id);
        
        if (field.isPresent()) {
            Field f = field.get();
            if (user.isAdmin() || user.canAccessUser(f.getUser())) {
                return field;
            }
        }
        
        return Optional.empty();
    }

    // Crear nuevo campo
    public Field createField(Field field, User user) {
        field.setUser(user);
        return fieldRepository.save(field);
    }

    // Actualizar campo (con validación de acceso)
    public Optional<Field> updateField(Long id, Field fieldData, User user) {
        Optional<Field> existingField = fieldRepository.findById(id);
        
        if (existingField.isPresent()) {
            Field field = existingField.get();
            
            // Verificar acceso
            if (user.isAdmin() || user.canAccessUser(field.getUser())) {
                // Actualizar campos
                field.setNombre(fieldData.getNombre());
                field.setDescripcion(fieldData.getDescripcion());
                field.setUbicacion(fieldData.getUbicacion());
                field.setAreaHectareas(fieldData.getAreaHectareas());
                field.setTipoSuelo(fieldData.getTipoSuelo());
                field.setEstado(fieldData.getEstado());
                field.setActivo(fieldData.getActivo());
                field.setPoligono(fieldData.getPoligono());
                field.setCoordenadas(fieldData.getCoordenadas());
                
                return Optional.of(fieldRepository.save(field));
            }
        }
        
        return Optional.empty();
    }

    // Eliminar campo (con validación de acceso)
    public boolean deleteField(Long id, User user) {
        Optional<Field> field = fieldRepository.findById(id);
        
        if (field.isPresent()) {
            Field f = field.get();
            if (user.isAdmin() || user.canAccessUser(f.getUser())) {
                fieldRepository.delete(f);
                return true;
            }
        }
        
        return false;
    }

    // Buscar campo por nombre
    public List<Field> searchFieldByName(String nombre, User user) {
        if (user.isAdmin()) {
            // Admin busca en todos los campos
            return fieldRepository.findAll().stream()
                    .filter(f -> f.getNombre().toLowerCase().contains(nombre.toLowerCase()))
                    .toList();
        } else {
            // Usuario busca solo en sus campos accesibles
            return fieldRepository.findAccessibleByUser(user).stream()
                    .filter(f -> f.getNombre().toLowerCase().contains(nombre.toLowerCase()))
                    .toList();
        }
    }

    // Obtener estadísticas de campos por usuario
    public FieldStats getFieldStats(User user) {
        List<Field> fields = getFieldsByUser(user);
        
        long total = fields.size();
        long activos = fields.stream().filter(Field::getActivo).count();
        long activosEstado = fields.stream()
                .filter(f -> "ACTIVO".equals(f.getEstado()))
                .count();
        
        return new FieldStats(total, activos, activosEstado);
    }

    // Clase interna para estadísticas
    public static class FieldStats {
        private final long total;
        private final long activos;
        private final long activosEstado;

        public FieldStats(long total, long activos, long activosEstado) {
            this.total = total;
            this.activos = activos;
            this.activosEstado = activosEstado;
        }

        public long getTotal() { return total; }
        public long getActivos() { return activos; }
        public long getActivosEstado() { return activosEstado; }
    }
}
