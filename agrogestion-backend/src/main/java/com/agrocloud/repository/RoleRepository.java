package com.agrocloud.repository;

import com.agrocloud.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    Optional<Role> findByNombre(String nombre);
    
    List<Role> findByActivoTrue();
    
    List<Role> findByActivoFalse();
    
    @Query("SELECT r FROM Role r WHERE r.nombre LIKE %:nombre%")
    List<Role> findByNombreContaining(@Param("nombre") String nombre);
    
    boolean existsByNombre(String nombre);
    
}