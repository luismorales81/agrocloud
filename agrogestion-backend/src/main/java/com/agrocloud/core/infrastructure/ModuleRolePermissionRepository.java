package com.agrocloud.core.infrastructure;

import com.agrocloud.core.domain.Module;
import com.agrocloud.core.domain.ModuleRolePermission;
import com.agrocloud.core.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuleRolePermissionRepository extends JpaRepository<ModuleRolePermission, Long> {

    Optional<ModuleRolePermission> findByRoleAndModule(Role role, Module module);
    Optional<ModuleRolePermission> findByRoleIdAndModuleId(Long roleId, Long moduleId);
    List<ModuleRolePermission> findByRoleId(Long roleId);
    List<ModuleRolePermission> findByModuleId(Long moduleId);
    boolean existsByRoleIdAndModuleId(Long roleId, Long moduleId);

    @Query("SELECT mrp FROM ModuleRolePermission mrp WHERE mrp.role.id = :roleId AND mrp.module.code = :moduleCode")
    Optional<ModuleRolePermission> findByRoleIdAndModuleCode(@Param("roleId") Long roleId, @Param("moduleCode") String moduleCode);

    @Query("SELECT mrp FROM ModuleRolePermission mrp WHERE mrp.role.id = :roleId AND mrp.module.code = :moduleCode AND (mrp.read = true OR mrp.write = true OR mrp.manage = true)")
    Optional<ModuleRolePermission> findActivePermissionByRoleIdAndModuleCode(@Param("roleId") Long roleId, @Param("moduleCode") String moduleCode);
}
