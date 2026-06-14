package com.agrocloud.core.infrastructure;

import com.agrocloud.core.domain.Role;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    Optional<UserRole> findByUserAndRole(User user, Role role);
    Optional<UserRole> findByUserIdAndRoleId(Long userId, Long roleId);
    List<UserRole> findByUserId(Long userId);
    List<UserRole> findByUserIdAndActiveTrue(Long userId);
    List<UserRole> findByRoleId(Long roleId);
    boolean existsByUserIdAndRoleId(Long userId, Long roleId);
    boolean existsByUserIdAndRoleIdAndActiveTrue(Long userId, Long roleId);

    @Query("SELECT ur FROM UserRole ur WHERE ur.user.id = :userId AND ur.active = true")
    List<UserRole> findActiveRolesByUserId(@Param("userId") Long userId);

    @Query("SELECT ur.role FROM UserRole ur WHERE ur.user.id = :userId AND ur.active = true")
    List<Role> findActiveRolesForUser(@Param("userId") Long userId);
}
