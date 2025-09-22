package com.agrocloud.repository;

import com.agrocloud.model.entity.UserCompanyRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCompanyRoleRepository extends JpaRepository<UserCompanyRole, Long> {
    
    List<UserCompanyRole> findByUsuarioId(Long usuarioId);
    
    List<UserCompanyRole> findByEmpresaId(Long empresaId);
    
    List<UserCompanyRole> findByRolId(Long rolId);
    
    List<UserCompanyRole> findByUsuarioIdAndEmpresaId(Long usuarioId, Long empresaId);
    
    List<UserCompanyRole> findByUsuarioIdAndActivoTrue(Long usuarioId);
    
    List<UserCompanyRole> findByEmpresaIdAndActivoTrue(Long empresaId);
    
    List<UserCompanyRole> findByRolIdAndActivoTrue(Long rolId);
    
    @Query("SELECT ucr FROM UserCompanyRole ucr WHERE ucr.usuario.id = :usuarioId AND ucr.empresa.id = :empresaId AND ucr.activo = true")
    List<UserCompanyRole> findActiveByUsuarioIdAndEmpresaId(@Param("usuarioId") Long usuarioId, @Param("empresaId") Long empresaId);
    
    @Query("SELECT ucr FROM UserCompanyRole ucr WHERE ucr.usuario.id = :usuarioId AND ucr.rol.nombre = :rolNombre")
    List<UserCompanyRole> findByUsuarioIdAndRolNombre(@Param("usuarioId") Long usuarioId, @Param("rolNombre") String rolNombre);
    
    @Query("SELECT ucr FROM UserCompanyRole ucr WHERE ucr.empresa.id = :empresaId AND ucr.rol.nombre = :rolNombre")
    List<UserCompanyRole> findByEmpresaIdAndRolNombre(@Param("empresaId") Long empresaId, @Param("rolNombre") String rolNombre);
    
    Optional<UserCompanyRole> findByUsuarioIdAndEmpresaIdAndRolId(Long usuarioId, Long empresaId, Long rolId);
    
    void deleteByUsuarioIdAndEmpresaIdAndRolId(Long usuarioId, Long empresaId, Long rolId);
    
    @Query("SELECT COUNT(ucr) FROM UserCompanyRole ucr WHERE ucr.empresa.id = :empresaId AND ucr.activo = true")
    long countActiveByEmpresaId(@Param("empresaId") Long empresaId);
    
    @Query("SELECT COUNT(ucr) FROM UserCompanyRole ucr WHERE ucr.usuario.id = :usuarioId AND ucr.activo = true")
    long countActiveByUsuarioId(@Param("usuarioId") Long usuarioId);
}
