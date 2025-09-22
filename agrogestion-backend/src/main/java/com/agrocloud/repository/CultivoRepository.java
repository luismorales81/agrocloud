package com.agrocloud.repository;

import com.agrocloud.model.entity.Cultivo;
import com.agrocloud.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CultivoRepository extends JpaRepository<Cultivo, Long> {

    // Buscar cultivos por usuario propietario
    List<Cultivo> findByUsuario(User usuario);

    // Buscar cultivos por usuario ID
    List<Cultivo> findByUsuarioId(Long usuarioId);

    // Buscar cultivos por usuario y estado activo
    List<Cultivo> findByUsuarioIdAndEstado(Long usuarioId, Cultivo.EstadoCultivo estado);

    // Buscar cultivos por nombre (filtrado por usuario)
    @Query("SELECT c FROM Cultivo c WHERE c.usuario.id = :usuarioId AND c.nombre LIKE %:nombre%")
    List<Cultivo> findByUsuarioIdAndNombreContaining(@Param("usuarioId") Long usuarioId, @Param("nombre") String nombre);

    // Buscar cultivos por variedad (filtrado por usuario)
    @Query("SELECT c FROM Cultivo c WHERE c.usuario.id = :usuarioId AND c.variedad LIKE %:variedad%")
    List<Cultivo> findByUsuarioIdAndVariedadContaining(@Param("usuarioId") Long usuarioId, @Param("variedad") String variedad);

    // Contar cultivos por usuario
    long countByUsuarioId(Long usuarioId);

    // Contar cultivos activos por usuario
    long countByUsuarioIdAndEstado(Long usuarioId, Cultivo.EstadoCultivo estado);

    // Buscar cultivos por estado
    List<Cultivo> findByEstado(Cultivo.EstadoCultivo estado);

    // Buscar cultivos por nombre
    List<Cultivo> findByNombreContaining(String nombre);

    // Buscar cultivos por variedad
    List<Cultivo> findByVariedadContaining(String variedad);

    // Query personalizada para buscar cultivos accesibles por un usuario (incluye jerarquía)
    @Query("SELECT c FROM Cultivo c WHERE " +
           "c.usuario = :user OR " +
           "c.usuario IN (SELECT u FROM User u WHERE u.parentUser = :user) OR " +
           "c.usuario IN (SELECT u FROM User u WHERE u.parentUser IN (SELECT c FROM User c WHERE c.parentUser = :user))")
    List<Cultivo> findAccessibleByUser(@Param("user") User user);

    // Query para buscar cultivos accesibles por usuario y estado activo
    @Query("SELECT c FROM Cultivo c WHERE c.estado = :estado AND (" +
           "c.usuario = :user OR " +
           "c.usuario IN (SELECT u FROM User u WHERE u.parentUser = :user) OR " +
           "c.usuario IN (SELECT u FROM User u WHERE u.parentUser IN (SELECT c FROM User c WHERE c.parentUser = :user)))")
    List<Cultivo> findAccessibleByUserAndEstado(@Param("user") User user, @Param("estado") Cultivo.EstadoCultivo estado);

    // Contar cultivos accesibles por usuario
    @Query("SELECT COUNT(c) FROM Cultivo c WHERE " +
           "c.usuario = :user OR " +
           "c.usuario IN (SELECT u FROM User u WHERE u.parentUser = :user) OR " +
           "c.usuario IN (SELECT u FROM User u WHERE u.parentUser IN (SELECT c FROM User c WHERE c.parentUser = :user))")
    long countAccessibleByUser(@Param("user") User user);

    // Métodos para eliminación lógica
    List<Cultivo> findByUsuarioIdAndActivoTrue(Long usuarioId);
    long countByUsuarioIdAndActivoTrue(Long usuarioId);
    List<Cultivo> findByActivoTrue();
    List<Cultivo> findByActivoFalse();
    List<Cultivo> findByUsuarioIdAndActivoFalse(Long usuarioId);
}
