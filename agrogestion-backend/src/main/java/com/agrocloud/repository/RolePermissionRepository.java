package com.agrocloud.repository;

import com.agrocloud.model.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    
    List<RolePermission> findByRolId(Long rolId);
    
    List<RolePermission> findByPermisoId(Long permisoId);
    
    List<RolePermission> findByRolIdAndActivoTrue(Long rolId);
    
    List<RolePermission> findByPermisoIdAndActivoTrue(Long permisoId);
    
    @Query("SELECT rp FROM RolePermission rp WHERE rp.rol.id = :rolId AND rp.permiso.nombre = :permisoNombre")
    List<RolePermission> findByRolIdAndPermisoNombre(@Param("rolId") Long rolId, @Param("permisoNombre") String permisoNombre);
    
    @Query("SELECT rp FROM RolePermission rp WHERE rp.rol.nombre = :rolNombre AND rp.permiso.id = :permisoId")
    List<RolePermission> findByRolNombreAndPermisoId(@Param("rolNombre") String rolNombre, @Param("permisoId") Long permisoId);
    
    @Query("SELECT rp FROM RolePermission rp WHERE rp.rol.nombre = :rolNombre AND rp.permiso.nombre = :permisoNombre")
    List<RolePermission> findByRolNombreAndPermisoNombre(@Param("rolNombre") String rolNombre, @Param("permisoNombre") String permisoNombre);
    
    Optional<RolePermission> findByRolIdAndPermisoId(Long rolId, Long permisoId);
    
    void deleteByRolIdAndPermisoId(Long rolId, Long permisoId);
    
    @Query("SELECT COUNT(rp) FROM RolePermission rp WHERE rp.rol.id = :rolId AND rp.activo = true")
    long countActiveByRolId(@Param("rolId") Long rolId);
    
    @Query("SELECT COUNT(rp) FROM RolePermission rp WHERE rp.permiso.id = :permisoId AND rp.activo = true")
    long countActiveByPermisoId(@Param("permisoId") Long permisoId);
}
