package com.agrocloud.repository;

import com.agrocloud.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Buscar usuario por email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Buscar usuario por username
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Verificar si existe un usuario con el email dado
     */
    boolean existsByEmail(String email);
    
    /**
     * Verificar si existe un usuario con el username dado
     */
    boolean existsByUsername(String username);
    
    /**
     * Buscar usuarios activos
     */
    List<User> findByActivoTrue();
    
    /**
     * Buscar usuarios con paginación y filtros
     */
    @Query("SELECT u FROM User u WHERE " +
           "(:firstName IS NULL OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))) AND " +
           "(:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:activo IS NULL OR u.activo = :activo)")
    List<User> findUsersWithFilters(@Param("firstName") String firstName, 
                                   @Param("email") String email, 
                                   @Param("activo") Boolean activo);
    
    /**
     * Buscar usuario por token de reset de contraseña
     */
    Optional<User> findByResetPasswordToken(String resetPasswordToken);
    
    /**
     * Buscar usuario por token de verificación
     */
    Optional<User> findByVerificationToken(String verificationToken);
}
