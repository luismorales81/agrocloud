package com.agrocloud.core.infrastructure;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.UsuarioEmpresa;
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

@Repository("usuarioEmpresaRepositoryCore")
public interface UsuarioEmpresaRepository extends JpaRepository<UsuarioEmpresa, Long> {

    Optional<UsuarioEmpresa> findByUsuarioAndEmpresa(User usuario, Empresa empresa);

    @Query("SELECT ue FROM UsuarioEmpresa ue WHERE ue.usuario.id = :usuarioId AND ue.empresa.id = :empresaId")
    Optional<UsuarioEmpresa> findByUsuarioIdAndEmpresaId(@Param("usuarioId") Long usuarioId, @Param("empresaId") Long empresaId);

    List<UsuarioEmpresa> findByUsuario(User usuario);

    @Query("SELECT ue FROM UsuarioEmpresa ue WHERE ue.usuario.id = :usuarioId")
    List<UsuarioEmpresa> findByUsuarioId(@Param("usuarioId") Long usuarioId);

    @Query("SELECT ue FROM UsuarioEmpresa ue JOIN FETCH ue.empresa JOIN FETCH ue.usuario WHERE ue.usuario.id = :usuarioId AND ue.estado = 'ACTIVO'")
    List<UsuarioEmpresa> findEmpresasActivasByUsuarioId(@Param("usuarioId") Long usuarioId);

    List<UsuarioEmpresa> findByEmpresa(Empresa empresa);

    @Query("SELECT ue FROM UsuarioEmpresa ue WHERE ue.empresa.id = :empresaId")
    List<UsuarioEmpresa> findByEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT ue FROM UsuarioEmpresa ue WHERE ue.empresa.id = :empresaId AND ue.estado = 'ACTIVO'")
    List<UsuarioEmpresa> findUsuariosActivosByEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT ue FROM UsuarioEmpresa ue WHERE ue.empresa.id = :empresaId AND ue.rol = :rol")
    List<UsuarioEmpresa> findByEmpresaIdAndRol(@Param("empresaId") Long empresaId, @Param("rol") RolEmpresa rol);

    @Query("SELECT ue FROM UsuarioEmpresa ue WHERE ue.empresa.id = :empresaId AND ue.rol = :rol AND ue.estado = 'ACTIVO'")
    List<UsuarioEmpresa> findUsuariosActivosByEmpresaIdAndRol(@Param("empresaId") Long empresaId, @Param("rol") RolEmpresa rol);

    List<UsuarioEmpresa> findByEstado(EstadoUsuarioEmpresa estado);
    List<UsuarioEmpresa> findByRol(RolEmpresa rol);
    long countByEmpresa(Empresa empresa);

    @Query("SELECT COUNT(ue) FROM UsuarioEmpresa ue WHERE ue.empresa.id = :empresaId AND ue.estado = 'ACTIVO'")
    long countUsuariosActivosByEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT COUNT(ue) FROM UsuarioEmpresa ue WHERE ue.empresa.id = :empresaId AND ue.rol = :rol")
    long countByEmpresaIdAndRol(@Param("empresaId") Long empresaId, @Param("rol") RolEmpresa rol);

    @Query("SELECT COUNT(ue) FROM UsuarioEmpresa ue WHERE ue.empresa.id = :empresaId AND ue.rol = :rol AND ue.estado = 'ACTIVO'")
    long countUsuariosActivosByEmpresaIdAndRol(@Param("empresaId") Long empresaId, @Param("rol") RolEmpresa rol);

    @Query("SELECT COUNT(ue) > 0 FROM UsuarioEmpresa ue WHERE ue.usuario.id = :usuarioId AND ue.empresa.id = :empresaId")
    boolean existsByUsuarioIdAndEmpresaId(@Param("usuarioId") Long usuarioId, @Param("empresaId") Long empresaId);

    boolean existsByUsuarioAndEmpresa(User usuario, Empresa empresa);
    boolean existsByUsuario(User usuario);

    @Query("SELECT COUNT(ue) > 0 FROM UsuarioEmpresa ue WHERE ue.usuario.id = :usuarioId AND ue.empresa.id = :empresaId AND ue.estado = 'ACTIVO'")
    boolean existsUsuarioActivoEnEmpresa(@Param("usuarioId") Long usuarioId, @Param("empresaId") Long empresaId);

    @Query("SELECT COUNT(ue) > 0 FROM UsuarioEmpresa ue WHERE ue.usuario.id = :usuarioId AND ue.empresa.id = :empresaId AND ue.rol = :rol AND ue.estado = 'ACTIVO'")
    boolean existsUsuarioConRolEnEmpresa(@Param("usuarioId") Long usuarioId, @Param("empresaId") Long empresaId, @Param("rol") RolEmpresa rol);

    @Query("SELECT ue FROM UsuarioEmpresa ue WHERE ue.fechaFin IS NOT NULL AND ue.fechaFin <= :fechaLimite AND ue.estado = 'ACTIVO'")
    List<UsuarioEmpresa> findRelacionesProximasAVencer(@Param("fechaLimite") LocalDate fechaLimite);

    @Query("SELECT ue FROM UsuarioEmpresa ue WHERE ue.fechaFin IS NOT NULL AND ue.fechaFin < :fechaActual AND ue.estado = 'ACTIVO'")
    List<UsuarioEmpresa> findRelacionesVencidas(@Param("fechaActual") LocalDate fechaActual);

    @Query("SELECT e.id as empresaId, e.nombre as empresaNombre, COUNT(ue) as totalUsuarios, COUNT(CASE WHEN ue.estado = 'ACTIVO' THEN 1 END) as usuariosActivos, COUNT(CASE WHEN ue.estado = 'PENDIENTE' THEN 1 END) as usuariosPendientes, COUNT(CASE WHEN ue.estado = 'INACTIVO' THEN 1 END) as usuariosInactivos, COUNT(CASE WHEN ue.rol = 'ADMINISTRADOR' THEN 1 END) as administradores, COUNT(CASE WHEN ue.rol = 'ASESOR' THEN 1 END) as asesores, COUNT(CASE WHEN ue.rol = 'OPERARIO' THEN 1 END) as operarios, COUNT(CASE WHEN ue.rol = 'CONTADOR' THEN 1 END) as contadores, COUNT(CASE WHEN ue.rol = 'TECNICO' THEN 1 END) as tecnicos, COUNT(CASE WHEN ue.rol = 'LECTURA' THEN 1 END) as soloLectura FROM UsuarioEmpresa ue JOIN ue.empresa e GROUP BY e.id, e.nombre")
    List<Object[]> obtenerEstadisticasUsuariosPorEmpresa();

    @Query("SELECT ue FROM UsuarioEmpresa ue WHERE (:empresaId IS NULL OR ue.empresa.id = :empresaId) AND (:usuarioId IS NULL OR ue.usuario.id = :usuarioId) AND (:rol IS NULL OR ue.rol = :rol) AND (:estado IS NULL OR ue.estado = :estado)")
    Page<UsuarioEmpresa> findUsuariosEmpresasConFiltros(@Param("empresaId") Long empresaId, @Param("usuarioId") Long usuarioId, @Param("rol") RolEmpresa rol, @Param("estado") EstadoUsuarioEmpresa estado, Pageable pageable);

    @Query("SELECT ue FROM UsuarioEmpresa ue WHERE ue.empresa.id = :empresaId AND ue.rol = 'ADMINISTRADOR' AND ue.estado = 'ACTIVO'")
    List<UsuarioEmpresa> findAdministradoresByEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT ue FROM UsuarioEmpresa ue WHERE ue.empresa.id = :empresaId AND ue.rol = 'ADMINISTRADOR' AND ue.estado = 'ACTIVO' ORDER BY ue.fechaCreacion ASC")
    Optional<UsuarioEmpresa> findPrimerAdministradorByEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT ue FROM UsuarioEmpresa ue WHERE ue.rol = :rol")
    List<UsuarioEmpresa> findByRolNombre(@Param("rol") RolEmpresa rol);
}
