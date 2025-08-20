package com.agrocloud.repository;

import com.agrocloud.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    /**
     * Buscar rol por nombre
     */
    Optional<Role> findByName(String name);
    
    /**
     * Verificar si existe un rol con el nombre dado
     */
    boolean existsByName(String name);
    
    /**
     * Buscar rol por nombre ignorando mayúsculas/minúsculas
     */
    Optional<Role> findByNameIgnoreCase(String name);
}
