package com.agrocloud.service;

import com.agrocloud.model.entity.Empresa;
import com.agrocloud.model.entity.Field;
import com.agrocloud.model.entity.User;
import com.agrocloud.repository.FieldRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class FieldService {

    @Autowired
    private FieldRepository fieldRepository;


    @Autowired
    private UserService userService;

    // Obtener todos los campos activos (público)
    public List<Field> getAllFields() {
        return fieldRepository.findAll().stream()
                .filter(Field::getActivo)
                .toList();
    }

    // Obtener campo activo por ID (público)
    public Field getFieldById(Long id) {
        Optional<Field> field = fieldRepository.findById(id);
        return field.isPresent() && field.get().getActivo() ? field.get() : null;
    }

    // Obtener todos los campos activos accesibles por un usuario
    public List<Field> getFieldsByUser(User user) {
        try {
            System.out.println("[FIELD_SERVICE] getFieldsByUser iniciado para usuario: " + (user != null ? user.getEmail() : "null"));
            
            if (user == null) {
                System.err.println("[FIELD_SERVICE] ERROR: Usuario es null");
                return new ArrayList<>();
            }
            
            if (user.isSuperAdmin()) {
                // Solo SuperAdmin ve todos los campos activos
                System.out.println("[FIELD_SERVICE] Usuario es SuperAdmin, mostrando todos los campos");
                return fieldRepository.findAll().stream()
                        .filter(field -> {
                            Boolean activo = field.getActivo();
                            return activo != null && activo;
                        })
                        .toList();
            } else if (user.esAdministradorEmpresa(user.getEmpresa() != null ? user.getEmpresa().getId() : null)) {
                // Admin de empresa ve TODOS los campos de su empresa
                System.out.println("[FIELD_SERVICE] Usuario es Admin de empresa, mostrando TODOS los campos de la empresa");
                
                Empresa empresa = user.getEmpresa();
                if (empresa == null) {
                    System.out.println("[FIELD_SERVICE] Usuario ADMIN no tiene empresa asignada");
                    return new ArrayList<>();
                }
                
                // Obtener todos los usuarios de la empresa
                List<User> todosUsuarios = userService.findAll();
                List<User> usuariosEmpresa = todosUsuarios.stream()
                        .filter(u -> u.perteneceAEmpresa(empresa.getId()))
                        .collect(Collectors.toList());
                
                System.out.println("[FIELD_SERVICE] Empresa ID: " + empresa.getId() + ", Usuarios: " + usuariosEmpresa.size());
                
                // Obtener campos de TODOS los usuarios de la empresa
                List<Field> todosLosCampos = new ArrayList<>();
                for (User userEmpresa : usuariosEmpresa) {
                    List<Field> camposUsuario = fieldRepository.findByUserIdAndActivoTrue(userEmpresa.getId());
                    if (camposUsuario != null) {
                        todosLosCampos.addAll(camposUsuario);
                        System.out.println("[FIELD_SERVICE] Campos del usuario " + userEmpresa.getUsername() + ": " + camposUsuario.size());
                    }
                }
                
                System.out.println("[FIELD_SERVICE] Total campos de la empresa: " + todosLosCampos.size());
                return todosLosCampos;
            } else {
                // Otros usuarios ven solo sus campos y los de sus sub-usuarios
                System.out.println("[FIELD_SERVICE] Usuario normal, mostrando campos accesibles");
                
                // Obtener campos del usuario actual
                List<Field> userFields = fieldRepository.findByUserIdAndActivoTrue(user.getId());
                System.out.println("[FIELD_SERVICE] Campos del usuario encontrados: " + (userFields != null ? userFields.size() : "null"));
                
                if (userFields == null) {
                    userFields = new ArrayList<>();
                }
                
                // Obtener campos de usuarios dependientes
                List<User> usuariosDependientes = userService.findByParentUserId(user.getId());
                if (usuariosDependientes != null && !usuariosDependientes.isEmpty()) {
                    System.out.println("[FIELD_SERVICE] Usuarios dependientes encontrados: " + usuariosDependientes.size());
                    for (User dependiente : usuariosDependientes) {
                        List<Field> camposDependiente = fieldRepository.findByUserIdAndActivoTrue(dependiente.getId());
                        if (camposDependiente != null) {
                            userFields.addAll(camposDependiente);
                            System.out.println("[FIELD_SERVICE] Campos del dependiente " + dependiente.getUsername() + ": " + camposDependiente.size());
                        }
                    }
                }
                
                System.out.println("[FIELD_SERVICE] Total campos accesibles: " + userFields.size());
                return userFields;
            }
        } catch (Exception e) {
            System.err.println("[FIELD_SERVICE] ERROR en getFieldsByUser: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Obtener campo activo por ID (con validación de acceso)
    public Optional<Field> getFieldById(Long id, User user) {
        Optional<Field> field = fieldRepository.findById(id);
        
        if (field.isPresent()) {
            Field f = field.get();
            if (f.getActivo() && (user.isSuperAdmin() || user.canAccessUser(f.getUser()))) {
                return field;
            }
        }
        
        return Optional.empty();
    }

    // Crear nuevo campo
    public Field createField(Field field, User user) {
        System.out.println("[FIELD_SERVICE] Iniciando creación de campo para usuario: " + user.getEmail());
        
        field.setUser(user);
        
        // Establecer la empresa del usuario
        Empresa empresa = user.getEmpresa();
        if (empresa != null) {
            System.out.println("[FIELD_SERVICE] Asignando empresa: " + empresa.getNombre() + " (ID: " + empresa.getId() + ")");
            field.setEmpresa(empresa);
        } else {
            System.err.println("[FIELD_SERVICE] ERROR: El usuario " + user.getEmail() + " no tiene empresa asignada");
            throw new RuntimeException("El usuario debe tener una empresa asignada para crear campos");
        }
        
        // Establecer valores por defecto si no están presentes
        if (field.getEstado() == null || field.getEstado().trim().isEmpty()) {
            field.setEstado("ACTIVO");
        }
        if (field.getActivo() == null) {
            field.setActivo(true);
        }
        
        System.out.println("[FIELD_SERVICE] Guardando campo: " + field.getNombre());
        Field savedField = fieldRepository.save(field);
        System.out.println("[FIELD_SERVICE] Campo creado exitosamente con ID: " + savedField.getId());
        
        return savedField;
    }

    // Actualizar campo (con validación de acceso)
    public Optional<Field> updateField(Long id, Field fieldData, User user) {
        Optional<Field> existingField = fieldRepository.findById(id);
        
        if (existingField.isPresent()) {
            Field field = existingField.get();
            
            // Verificar acceso
            if (user.isSuperAdmin() || user.canAccessUser(field.getUser())) {
                // Actualizar campos
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

    // Eliminar campo (con validación de acceso)
    public boolean deleteField(Long id, User user) {
        try {
            System.out.println("🔍 [FIELD_SERVICE] Buscando campo con ID: " + id);
            Optional<Field> field = fieldRepository.findById(id);
            
            if (field.isPresent()) {
                Field f = field.get();
                System.out.println("🔍 [FIELD_SERVICE] Campo encontrado: " + f.getNombre());
                System.out.println("🔍 [FIELD_SERVICE] Usuario del campo: " + (f.getUser() != null ? f.getUser().getEmail() : "null"));
                System.out.println("🔍 [FIELD_SERVICE] Usuario actual: " + (user != null ? user.getEmail() : "null"));
                System.out.println("🔍 [FIELD_SERVICE] Usuario es admin: " + (user != null ? user.isAdmin() : "false"));
                
                if (user != null && (user.isSuperAdmin() || user.canAccessUser(f.getUser()))) {
                    System.out.println("✅ [FIELD_SERVICE] Usuario tiene permisos para eliminar");
                    // Eliminación lógica: marcar como inactivo
                    f.setActivo(false);
                    f.setEstado("ELIMINADO");
                    fieldRepository.save(f);
                    System.out.println("✅ [FIELD_SERVICE] Campo marcado como eliminado (eliminación lógica)");
                    return true;
                } else {
                    System.out.println("❌ [FIELD_SERVICE] Usuario no tiene permisos para eliminar este campo");
                }
            } else {
                System.out.println("❌ [FIELD_SERVICE] Campo no encontrado con ID: " + id);
            }
        } catch (Exception e) {
            System.err.println("❌ [FIELD_SERVICE] Error eliminando campo: " + e.getMessage());
            e.printStackTrace();
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
