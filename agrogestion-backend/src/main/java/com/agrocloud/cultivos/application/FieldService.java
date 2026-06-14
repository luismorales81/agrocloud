package com.agrocloud.cultivos.application;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.cultivos.domain.Field;
import com.agrocloud.core.domain.User;
import com.agrocloud.model.enums.RolEmpresa;
import com.agrocloud.cultivos.infrastructure.FieldRepository;
import com.agrocloud.core.application.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("fieldServiceCultivos")
@Transactional
public class FieldService {

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private UserService userService;

    public List<Field> getAllFields() {
        return fieldRepository.findAll().stream()
                .filter(Field::getActivo)
                .toList();
    }

    public Field getFieldById(Long id) {
        Optional<Field> field = fieldRepository.findById(id);
        return field.isPresent() && field.get().getActivo() ? field.get() : null;
    }

    public List<Field> getFieldsByUser(User user) {
        try {
            if (user == null) return new ArrayList<>();
            if (user.isSuperAdmin()) {
                List<Field> campos = fieldRepository.findAll().stream()
                        .filter(field -> field.getActivo() != null && field.getActivo())
                        .toList();
                campos.forEach(field -> { if (field.getEmpresa() != null) field.getEmpresa().getId(); });
                return campos;
            } else if (user.esAdministradorEmpresa(user.getEmpresa() != null ? user.getEmpresa().getId() : null) ||
                       user.tieneRolEnEmpresa(RolEmpresa.JEFE_CAMPO) ||
                       user.tieneRolEnEmpresa(RolEmpresa.OPERARIO) ||
                       user.tieneRolEnEmpresa(RolEmpresa.CONSULTOR_EXTERNO)) {
                Empresa empresa = user.getEmpresa();
                if (empresa == null) return new ArrayList<>();
                List<User> usuariosEmpresa = userService.findAll().stream()
                        .filter(u -> u.perteneceAEmpresa(empresa.getId()))
                        .collect(Collectors.toList());
                List<Field> todosLosCampos = new ArrayList<>();
                for (User userEmpresa : usuariosEmpresa) {
                    List<Field> camposUsuario = fieldRepository.findByUserIdAndActivoTrue(userEmpresa.getId());
                    if (camposUsuario != null) todosLosCampos.addAll(camposUsuario);
                }
                todosLosCampos.forEach(field -> { if (field.getEmpresa() != null) field.getEmpresa().getId(); });
                return todosLosCampos;
            } else {
                List<Field> userFields = fieldRepository.findByUserIdAndActivoTrue(user.getId());
                if (userFields == null) userFields = new ArrayList<>();
                List<User> usuariosDependientes = userService.findByParentUserId(user.getId());
                if (usuariosDependientes != null) {
                    for (User dependiente : usuariosDependientes) {
                        List<Field> camposDependiente = fieldRepository.findByUserIdAndActivoTrue(dependiente.getId());
                        if (camposDependiente != null) userFields.addAll(camposDependiente);
                    }
                }
                userFields.forEach(field -> { if (field.getEmpresa() != null) field.getEmpresa().getId(); });
                return userFields;
            }
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public Optional<Field> getFieldById(Long id, User user) {
        Optional<Field> field = fieldRepository.findById(id);
        if (field.isPresent()) {
            Field f = field.get();
            if (f.getActivo() && (user.isSuperAdmin() || user.canAccessUser(f.getUser())))
                return field;
        }
        return Optional.empty();
    }

    public Field createField(Field field, User user) {
        field.setUser(user);
        Empresa empresa = user.getEmpresa();
        if (empresa == null) throw new RuntimeException("El usuario debe tener una empresa asignada para crear campos");
        field.setEmpresa(empresa);
        if (field.getEstado() == null || field.getEstado().trim().isEmpty()) field.setEstado("ACTIVO");
        if (field.getActivo() == null) field.setActivo(true);
        return fieldRepository.save(field);
    }

    public Optional<Field> updateField(Long id, Field fieldData, User user) {
        Optional<Field> existingField = fieldRepository.findById(id);
        if (existingField.isPresent()) {
            Field field = existingField.get();
            if (user.isSuperAdmin() || user.canAccessUser(field.getUser())) {
                field.setNombre(fieldData.getNombre());
                field.setDescripcion(fieldData.getDescripcion());
                field.setUbicacion(fieldData.getUbicacion());
                field.setAreaHectareas(fieldData.getAreaHectareas());
                field.setEstado(fieldData.getEstado());
                field.setActivo(fieldData.getActivo());
                field.setPoligono(fieldData.getPoligono());
                field.setCoordenadas(fieldData.getCoordenadas());
                return Optional.of(fieldRepository.save(field));
            }
        }
        return Optional.empty();
    }

    public boolean deleteField(Long id, User user) {
        Optional<Field> field = fieldRepository.findById(id);
        if (field.isPresent()) {
            Field f = field.get();
            if (user != null && (user.isSuperAdmin() || user.canAccessUser(f.getUser()))) {
                f.setActivo(false);
                f.setEstado("ELIMINADO");
                fieldRepository.save(f);
                return true;
            }
        }
        return false;
    }

    public List<Field> searchFieldByName(String nombre, User user) {
        if (user.isAdmin())
            return fieldRepository.findAll().stream()
                    .filter(f -> f.getNombre().toLowerCase().contains(nombre.toLowerCase()))
                    .toList();
        return fieldRepository.findAccessibleByUser(user).stream()
                .filter(f -> f.getNombre().toLowerCase().contains(nombre.toLowerCase()))
                .toList();
    }

    public FieldStats getFieldStats(User user) {
        List<Field> fields = getFieldsByUser(user);
        long total = fields.size();
        long activos = fields.stream().filter(Field::getActivo).count();
        long activosEstado = fields.stream().filter(f -> "ACTIVO".equals(f.getEstado())).count();
        return new FieldStats(total, activos, activosEstado);
    }

    public static class FieldStats {
        private final long total, activos, activosEstado;
        public FieldStats(long total, long activos, long activosEstado) {
            this.total = total; this.activos = activos; this.activosEstado = activosEstado;
        }
        public long getTotal() { return total; }
        public long getActivos() { return activos; }
        public long getActivosEstado() { return activosEstado; }
    }
}
