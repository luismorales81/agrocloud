package com.agrocloud.repository;

import com.agrocloud.model.entity.Field;
import com.agrocloud.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FieldRepository extends JpaRepository<Field, Long> {

    // Buscar campos por usuario propietario
    List<Field> findByUserId(Long userId);

    // Buscar campos por usuario y estado activo
    List<Field> findByUserIdAndActivoTrue(Long userId);

    // Buscar campos por usuario y estado
    List<Field> findByUserIdAndEstado(Long userId, String estado);

    // Query personalizada para buscar campos accesibles por un usuario
    @Query("SELECT f FROM Field f WHERE " +
           "f.user = :user OR " +
           "f.user IN (SELECT u FROM User u WHERE u.parentUser = :user) OR " +
           "f.user IN (SELECT u FROM User u WHERE u.parentUser IN (SELECT c FROM User c WHERE c.parentUser = :user))")
    List<Field> findAccessibleByUser(@Param("user") User user);

    // Query para buscar campos por nombre (filtrado por usuario)
    @Query("SELECT f FROM Field f WHERE f.user.id = :userId AND f.nombre LIKE %:nombre%")
    List<Field> findByUserIdAndNombreContaining(@Param("userId") Long userId, @Param("nombre") String nombre);

    // Contar campos por usuario
    long countByUserId(Long userId);

    // Contar campos activos por usuario
    long countByUserIdAndActivoTrue(Long userId);

    // Métodos para eliminación lógica
    List<Field> findByActivoTrue();
    List<Field> findByActivoFalse();
    List<Field> findByUserIdAndActivoFalse(Long userId);
    
    // Métodos faltantes para los tests
    List<Field> findByNombreContainingIgnoreCase(String nombre);
    List<Field> findByTipoSuelo(String tipoSuelo);
}
