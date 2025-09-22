package com.agrocloud.repository;

import com.agrocloud.model.entity.Empresa;
import com.agrocloud.model.entity.User;
import com.agrocloud.model.entity.UsuarioEmpresa;
import com.agrocloud.model.enums.EstadoUsuarioEmpresa;
import com.agrocloud.model.enums.RolEmpresa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad UsuarioEmpresa en el sistema multiempresa.
 * 
 * @author AgroGestion Team
 * @version 2.0.0
 */
@Repository
public interface UsuarioEmpresaRepository extends JpaRepository<UsuarioEmpresa, Long> {

    /**
     * Busca la relación usuario-empresa por usuario y empresa
     */
    Optional<UsuarioEmpresa> findByUsuarioAndEmpresa(User usuario, Empresa empresa);

    /**
     * Busca la relación usuario-empresa por ID de usuario y ID de empresa
     */
    @Query("SELECT ue FROM UsuarioEmpresa ue WHERE ue.usuario.id = :usuarioId AND ue.empresa.id = :empresaId")
    Optional<UsuarioEmpresa> findByUsuarioIdAndEmpresaId(@Param("usuarioId") Long usuarioId, @Param("empresaId") Long empresaId);

    /**
     * Busca todas las empresas de un usuario
     */
    List<UsuarioEmpresa> findByUsuario(User usuario);

    /**
     * Busca todas las empresas de un usuario por ID
     */
    @Query("SELECT ue FROM UsuarioEmpresa ue WHERE ue.usuario.id = :usuarioId")
    List<UsuarioEmpresa> findByUsuarioId(@Param("usuarioId") Long usuarioId);

    /**
     * Busca todas las empresas activas de un usuario
     */
    @Query("SELECT ue FROM UsuarioEmpresa ue WHERE ue.usuario.id = :usuarioId AND ue.estado = 'ACTIVO'")
    List<UsuarioEmpresa> findEmpresasActivasByUsuarioId(@Param("usuarioId") Long usuarioId);

    /**
     * Busca todos los usuarios de una empresa
     */
    List<UsuarioEmpresa> findByEmpresa(Empresa empresa);

    /**
     * Busca todos los usuarios de una empresa por ID
     */
    @Query("SELECT ue FROM UsuarioEmpresa ue WHERE ue.empresa.id = :empresaId")
    List<UsuarioEmpresa> findByEmpresaId(@Param("empresaId") Long empresaId);

    /**
     * Busca todos los usuarios activos de una empresa
     */
    @Query("SELECT ue FROM UsuarioEmpresa ue WHERE ue.empresa.id = :empresaId AND ue.estado = 'ACTIVO'")
    List<UsuarioEmpresa> findUsuariosActivosByEmpresaId(@Param("empresaId") Long empresaId);

    /**
     * Busca usuarios por rol en una empresa
     */
    @Query("SELECT ue FROM UsuarioEmpresa ue WHERE ue.empresa.id = :empresaId AND ue.rol = :rol")
    List<UsuarioEmpresa> findByEmpresaIdAndRol(@Param("empresaId") Long empresaId, @Param("rol") RolEmpresa rol);

    /**
     * Busca usuarios activos por rol en una empresa
     */
    @Query("SELECT ue FROM UsuarioEmpresa ue WHERE ue.empresa.id = :empresaId AND ue.rol = :rol AND ue.estado = 'ACTIVO'")
    List<UsuarioEmpresa> findUsuariosActivosByEmpresaIdAndRol(@Param("empresaId") Long empresaId, @Param("rol") RolEmpresa rol);

    /**
     * Busca relaciones por estado
     */
    List<UsuarioEmpresa> findByEstado(EstadoUsuarioEmpresa estado);

    /**
     * Busca relaciones por rol
     */
    List<UsuarioEmpresa> findByRol(RolEmpresa rol);

    /**
     * Cuenta usuarios por empresa
     */
    long countByEmpresa(Empresa empresa);

    /**
     * Cuenta usuarios activos por empresa
     */
    @Query("SELECT COUNT(ue) FROM UsuarioEmpresa ue WHERE ue.empresa.id = :empresaId AND ue.estado = 'ACTIVO'")
    long countUsuariosActivosByEmpresaId(@Param("empresaId") Long empresaId);

    /**
     * Cuenta usuarios por rol en una empresa
     */
    @Query("SELECT COUNT(ue) FROM UsuarioEmpresa ue WHERE ue.empresa.id = :empresaId AND ue.rol = :rol")
    long countByEmpresaIdAndRol(@Param("empresaId") Long empresaId, @Param("rol") RolEmpresa rol);

    /**
     * Cuenta usuarios activos por rol en una empresa
     */
    @Query("SELECT COUNT(ue) FROM UsuarioEmpresa ue WHERE ue.empresa.id = :empresaId AND ue.rol = :rol AND ue.estado = 'ACTIVO'")
    long countUsuariosActivosByEmpresaIdAndRol(@Param("empresaId") Long empresaId, @Param("rol") RolEmpresa rol);

    /**
     * Verifica si un usuario pertenece a una empresa
     */
    @Query("SELECT COUNT(ue) > 0 FROM UsuarioEmpresa ue WHERE ue.usuario.id = :usuarioId AND ue.empresa.id = :empresaId")
    boolean existsByUsuarioIdAndEmpresaId(@Param("usuarioId") Long usuarioId, @Param("empresaId") Long empresaId);

    /**
     * Verifica si existe una relación usuario-empresa
     */
    boolean existsByUsuarioAndEmpresa(User usuario, Empresa empresa);

    /**
     * Verifica si un usuario tiene alguna empresa asignada
     */
    boolean existsByUsuario(User usuario);

    /**
     * Verifica si un usuario está activo en una empresa
     */
    @Query("SELECT COUNT(ue) > 0 FROM UsuarioEmpresa ue WHERE ue.usuario.id = :usuarioId AND ue.empresa.id = :empresaId AND ue.estado = 'ACTIVO'")
    boolean existsUsuarioActivoEnEmpresa(@Param("usuarioId") Long usuarioId, @Param("empresaId") Long empresaId);

    /**
     * Verifica si un usuario tiene un rol específico en una empresa
     */
    @Query("SELECT COUNT(ue) > 0 FROM UsuarioEmpresa ue WHERE ue.usuario.id = :usuarioId AND ue.empresa.id = :empresaId AND ue.rol = :rol AND ue.estado = 'ACTIVO'")
    boolean existsUsuarioConRolEnEmpresa(@Param("usuarioId") Long usuarioId, @Param("empresaId") Long empresaId, @Param("rol") RolEmpresa rol);

    /**
     * Busca relaciones que están próximas a vencer
     */
    @Query("SELECT ue FROM UsuarioEmpresa ue WHERE ue.fechaFin IS NOT NULL AND ue.fechaFin <= :fechaLimite AND ue.estado = 'ACTIVO'")
    List<UsuarioEmpresa> findRelacionesProximasAVencer(@Param("fechaLimite") LocalDate fechaLimite);

    /**
     * Busca relaciones que han vencido
     */
    @Query("SELECT ue FROM UsuarioEmpresa ue WHERE ue.fechaFin IS NOT NULL AND ue.fechaFin < :fechaActual AND ue.estado = 'ACTIVO'")
    List<UsuarioEmpresa> findRelacionesVencidas(@Param("fechaActual") LocalDate fechaActual);

    /**
     * Obtiene estadísticas de usuarios por empresa
     */
    @Query("SELECT " +
           "e.id as empresaId, " +
           "e.nombre as empresaNombre, " +
           "COUNT(ue) as totalUsuarios, " +
           "COUNT(CASE WHEN ue.estado = 'ACTIVO' THEN 1 END) as usuariosActivos, " +
           "COUNT(CASE WHEN ue.estado = 'PENDIENTE' THEN 1 END) as usuariosPendientes, " +
           "COUNT(CASE WHEN ue.estado = 'INACTIVO' THEN 1 END) as usuariosInactivos, " +
           "COUNT(CASE WHEN ue.rol = 'ADMINISTRADOR' THEN 1 END) as administradores, " +
           "COUNT(CASE WHEN ue.rol = 'ASESOR' THEN 1 END) as asesores, " +
           "COUNT(CASE WHEN ue.rol = 'OPERARIO' THEN 1 END) as operarios, " +
           "COUNT(CASE WHEN ue.rol = 'CONTADOR' THEN 1 END) as contadores, " +
           "COUNT(CASE WHEN ue.rol = 'TECNICO' THEN 1 END) as tecnicos, " +
           "COUNT(CASE WHEN ue.rol = 'LECTURA' THEN 1 END) as soloLectura " +
           "FROM UsuarioEmpresa ue JOIN ue.empresa e " +
           "GROUP BY e.id, e.nombre")
    List<Object[]> obtenerEstadisticasUsuariosPorEmpresa();

    /**
     * Busca usuarios con paginación y filtros
     */
    @Query("SELECT ue FROM UsuarioEmpresa ue WHERE " +
           "(:empresaId IS NULL OR ue.empresa.id = :empresaId) AND " +
           "(:usuarioId IS NULL OR ue.usuario.id = :usuarioId) AND " +
           "(:rol IS NULL OR ue.rol = :rol) AND " +
           "(:estado IS NULL OR ue.estado = :estado)")
    Page<UsuarioEmpresa> findUsuariosEmpresasConFiltros(
            @Param("empresaId") Long empresaId,
            @Param("usuarioId") Long usuarioId,
            @Param("rol") RolEmpresa rol,
            @Param("estado") EstadoUsuarioEmpresa estado,
            Pageable pageable);

    /**
     * Busca administradores de una empresa
     */
    @Query("SELECT ue FROM UsuarioEmpresa ue WHERE ue.empresa.id = :empresaId AND ue.rol = 'ADMINISTRADOR' AND ue.estado = 'ACTIVO'")
    List<UsuarioEmpresa> findAdministradoresByEmpresaId(@Param("empresaId") Long empresaId);

    /**
     * Busca el primer administrador de una empresa
     */
    @Query("SELECT ue FROM UsuarioEmpresa ue WHERE ue.empresa.id = :empresaId AND ue.rol = 'ADMINISTRADOR' AND ue.estado = 'ACTIVO' ORDER BY ue.fechaCreacion ASC")
    Optional<UsuarioEmpresa> findPrimerAdministradorByEmpresaId(@Param("empresaId") Long empresaId);
}
