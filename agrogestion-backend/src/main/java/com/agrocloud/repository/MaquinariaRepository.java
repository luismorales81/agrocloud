package com.agrocloud.repository;

import com.agrocloud.model.entity.Maquinaria;
import com.agrocloud.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaquinariaRepository extends JpaRepository<Maquinaria, Long> {

    // Buscar maquinaria por usuario propietario
    List<Maquinaria> findByUserId(Long userId);

    // Buscar maquinaria por usuario y estado activo
    List<Maquinaria> findByUserIdAndEstado(Long userId, Maquinaria.EstadoMaquinaria estado);



    // Query personalizada para buscar maquinaria accesible por un usuario
    @Query("SELECT m FROM Maquinaria m WHERE " +
           "m.user = :user OR " +
           "m.user IN (SELECT u FROM User u WHERE u.parentUser = :user) OR " +
           "m.user IN (SELECT u FROM User u WHERE u.parentUser IN (SELECT c FROM User c WHERE c.parentUser = :user))")
    List<Maquinaria> findAccessibleByUser(@Param("user") User user);

    // Query para buscar maquinaria por nombre (filtrado por usuario)
    @Query("SELECT m FROM Maquinaria m WHERE m.user.id = :userId AND m.nombre LIKE %:nombre%")
    List<Maquinaria> findByUserIdAndNombreContaining(@Param("userId") Long userId, @Param("nombre") String nombre);

    // Contar maquinaria por usuario
    long countByUserId(Long userId);

    // Contar maquinaria activa por usuario
    long countByUserIdAndEstado(Long userId, Maquinaria.EstadoMaquinaria estado);

    // Métodos para eliminación lógica
    List<Maquinaria> findByUserIdAndActivoTrue(Long userId);
    long countByUserIdAndActivoTrue(Long userId);
    List<Maquinaria> findByActivoTrue();
    List<Maquinaria> findByActivoFalse();
    List<Maquinaria> findByUserIdAndActivoFalse(Long userId);
}
