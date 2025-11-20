package com.agrocloud.repository;

import com.agrocloud.model.entity.UsuarioEmpresaRol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad UsuarioEmpresaRol que permite múltiples roles por usuario-empresa.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@Repository
public interface UsuarioEmpresaRolRepository extends JpaRepository<UsuarioEmpresaRol, Long> {

    /**
     * Busca todos los roles de un usuario en una empresa
     */
    @Query("SELECT uer FROM UsuarioEmpresaRol uer WHERE uer.usuario.id = :usuarioId AND uer.empresa.id = :empresaId")
    List<UsuarioEmpresaRol> findByUsuarioIdAndEmpresaId(@Param("usuarioId") Long usuarioId, @Param("empresaId") Long empresaId);

    /**
     * Busca todos los roles activos de un usuario en una empresa
     */
    @Query("SELECT uer FROM UsuarioEmpresaRol uer WHERE uer.usuario.id = :usuarioId AND uer.empresa.id = :empresaId AND uer.activo = true")
    List<UsuarioEmpresaRol> findRolesActivosByUsuarioIdAndEmpresaId(@Param("usuarioId") Long usuarioId, @Param("empresaId") Long empresaId);

    /**
     * Busca un rol específico de un usuario en una empresa por rol_id
     */
    @Query("SELECT uer FROM UsuarioEmpresaRol uer WHERE uer.usuario.id = :usuarioId AND uer.empresa.id = :empresaId AND uer.rol.id = :rolId")
    Optional<UsuarioEmpresaRol> findByUsuarioIdAndEmpresaIdAndRolId(@Param("usuarioId") Long usuarioId, @Param("empresaId") Long empresaId, @Param("rolId") Long rolId);

    /**
     * Busca todos los roles de un usuario en todas las empresas
     */
    @Query("SELECT uer FROM UsuarioEmpresaRol uer WHERE uer.usuario.id = :usuarioId")
    List<UsuarioEmpresaRol> findByUsuarioId(@Param("usuarioId") Long usuarioId);

    /**
     * Busca todos los usuarios con un rol específico en una empresa por rol_id
     */
    @Query("SELECT uer FROM UsuarioEmpresaRol uer WHERE uer.empresa.id = :empresaId AND uer.rol.id = :rolId AND uer.activo = true")
    List<UsuarioEmpresaRol> findByEmpresaIdAndRolId(@Param("empresaId") Long empresaId, @Param("rolId") Long rolId);

    /**
     * Elimina todos los roles de un usuario en una empresa
     */
    @Modifying
    @Query("DELETE FROM UsuarioEmpresaRol uer WHERE uer.usuario.id = :usuarioId AND uer.empresa.id = :empresaId")
    void deleteByUsuarioIdAndEmpresaId(@Param("usuarioId") Long usuarioId, @Param("empresaId") Long empresaId);

    /**
     * Elimina un rol específico de un usuario en una empresa por rol_id
     */
    @Modifying
    @Query("DELETE FROM UsuarioEmpresaRol uer WHERE uer.usuario.id = :usuarioId AND uer.empresa.id = :empresaId AND uer.rol.id = :rolId")
    void deleteByUsuarioIdAndEmpresaIdAndRolId(@Param("usuarioId") Long usuarioId, @Param("empresaId") Long empresaId, @Param("rolId") Long rolId);

    /**
     * Verifica si un usuario tiene un rol específico en una empresa por rol_id
     */
    @Query("SELECT COUNT(uer) > 0 FROM UsuarioEmpresaRol uer WHERE uer.usuario.id = :usuarioId AND uer.empresa.id = :empresaId AND uer.rol.id = :rolId AND uer.activo = true")
    boolean existsByUsuarioIdAndEmpresaIdAndRolId(@Param("usuarioId") Long usuarioId, @Param("empresaId") Long empresaId, @Param("rolId") Long rolId);
    
    /**
     * Cuenta usuarios únicos por rol (solo roles activos)
     * Retorna una lista de Object[] donde [0] es el nombre del rol y [1] es el conteo
     */
    @Query("SELECT uer.rol.nombre, COUNT(DISTINCT uer.usuario.id) " +
           "FROM UsuarioEmpresaRol uer " +
           "WHERE uer.activo = true " +
           "GROUP BY uer.rol.nombre")
    List<Object[]> countUsuariosUnicosPorRol();
}

