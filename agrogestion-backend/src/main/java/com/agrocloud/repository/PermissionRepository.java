package com.agrocloud.repository;

import com.agrocloud.model.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    
    Optional<Permission> findByNombre(String nombre);
    
    List<Permission> findByActivoTrue();
    
    List<Permission> findByActivoFalse();
    
    List<Permission> findByModulo(String modulo);
    
    List<Permission> findByAccion(String accion);
    
    @Query("SELECT p FROM Permission p WHERE p.nombre LIKE %:nombre%")
    List<Permission> findByNombreContaining(@Param("nombre") String nombre);
    
    @Query("SELECT p FROM Permission p WHERE p.modulo = :modulo AND p.accion = :accion")
    List<Permission> findByModuloAndAccion(@Param("modulo") String modulo, @Param("accion") String accion);
    
    boolean existsByNombre(String nombre);
}
