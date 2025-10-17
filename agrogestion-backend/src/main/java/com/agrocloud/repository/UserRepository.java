package com.agrocloud.repository;

import com.agrocloud.model.entity.EstadoUsuario;
import com.agrocloud.model.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    // Método para cargar el usuario con sus relaciones (usuarioEmpresas y userCompanyRoles)
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.usuarioEmpresas ue " +
           "LEFT JOIN FETCH ue.empresa " +
           "LEFT JOIN FETCH ue.rol " +
           "LEFT JOIN FETCH u.userCompanyRoles ucr " +
           "LEFT JOIN FETCH ucr.empresa " +
           "LEFT JOIN FETCH ucr.rol " +
           "WHERE u.email = :email")
    Optional<User> findByEmailWithRelations(@Param("email") String email);
    
    // Método para cargar todos los usuarios con sus roles
    @EntityGraph(attributePaths = {"roles"})
    @Query("SELECT u FROM User u")
    List<User> findAllWithRoles();
    
    List<User> findByActivoTrue();
    
    List<User> findByActivoFalse();
    
    @Query("SELECT u FROM User u WHERE u.username LIKE %:username%")
    List<User> findByUsernameContaining(@Param("username") String username);
    
    @Query("SELECT u FROM User u WHERE u.email LIKE %:email%")
    List<User> findByEmailContaining(@Param("email") String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    // Métodos adicionales requeridos por el código existente
    List<User> findByEstado(EstadoUsuario estado);
    
    long countByEstado(EstadoUsuario estado);
    
    long countByActivoTrue();
    
    Optional<User> findByVerificationToken(String verificationToken);
    
    Optional<User> findByResetPasswordToken(String resetPasswordToken);
    
    List<User> findByParentUserId(Long parentUserId);
    
    List<User> findByCreadoPorOrderByFechaCreacionDesc(User creadoPor);
    
    @Query("SELECT u FROM User u WHERE " +
           "(:firstName IS NULL OR u.firstName LIKE %:firstName%) AND " +
           "(:lastName IS NULL OR u.lastName LIKE %:lastName%) AND " +
           "(:activo IS NULL OR u.activo = :activo)")
    List<User> findUsersWithFilters(@Param("firstName") String firstName, 
                                   @Param("lastName") String lastName, 
                                   @Param("activo") Boolean activo);
    
    @Query("SELECT u FROM User u WHERE " +
           "(:estado IS NULL OR u.estado = :estado) AND " +
           "(:searchTerm IS NULL OR u.firstName LIKE %:searchTerm% OR u.lastName LIKE %:searchTerm% OR u.email LIKE %:searchTerm%) AND " +
           "(:creadoPor IS NULL OR u.creadoPor = :creadoPor) AND " +
           "(:activo IS NULL OR u.activo = :activo) AND " +
           "(:roleName IS NULL OR EXISTS (SELECT 1 FROM u.userCompanyRoles ucr WHERE ucr.rol.nombre = :roleName))")
    List<User> findUsersWithAdvancedFilters(@Param("estado") EstadoUsuario estado,
                                           @Param("searchTerm") String searchTerm,
                                           @Param("creadoPor") User creadoPor,
                                           @Param("activo") Boolean activo,
                                           @Param("roleName") String roleName);
    
    @Query("SELECT u FROM User u JOIN u.userCompanyRoles ucr JOIN ucr.rol r WHERE r.nombre = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);
    
    // Métodos para estadísticas de uso del sistema
    @Query("SELECT COUNT(DISTINCT u) FROM User u JOIN u.userCompanyRoles ucr JOIN ucr.rol r WHERE r.nombre = :roleName")
    long countByRoleName(@Param("roleName") String roleName);
    
    // Método alternativo más robusto para contar usuarios por rol
    @Query("SELECT COUNT(DISTINCT u) FROM User u WHERE EXISTS (SELECT 1 FROM u.userCompanyRoles ucr WHERE ucr.rol.nombre = :roleName)")
    long countByRoleNameRobust(@Param("roleName") String roleName);
    
    long countByFechaCreacionAfter(java.time.LocalDateTime fecha);
    
    @Query("SELECT u, COUNT(*) as actividad FROM User u " +
           "WHERE u.fechaActualizacion >= :fechaInicio " +
           "GROUP BY u " +
           "ORDER BY actividad DESC")
    List<Object[]> findUsuariosMasActivos(@Param("fechaInicio") java.time.LocalDateTime fechaInicio);
    
    @Query(value = "SELECT u.first_name, u.last_name, u.email, COUNT(c.id) as campos_creados " +
           "FROM usuarios u LEFT JOIN campos c ON u.id = c.user_id " +
           "GROUP BY u.id, u.first_name, u.last_name, u.email " +
           "ORDER BY campos_creados DESC", nativeQuery = true)
    List<Object[]> findUsuariosConMasCampos();
}