package com.agrocloud.core.infrastructure;

import com.agrocloud.core.domain.UsuarioEmpresaRol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("usuarioEmpresaRolRepositoryCore")
public interface UsuarioEmpresaRolRepository extends JpaRepository<UsuarioEmpresaRol, Long> {

    @Query("SELECT uer FROM UsuarioEmpresaRol uer WHERE uer.usuario.id = :usuarioId AND uer.empresa.id = :empresaId")
    List<UsuarioEmpresaRol> findByUsuarioIdAndEmpresaId(@Param("usuarioId") Long usuarioId, @Param("empresaId") Long empresaId);

    @Query("SELECT uer FROM UsuarioEmpresaRol uer WHERE uer.usuario.id = :usuarioId AND uer.empresa.id = :empresaId AND uer.activo = true")
    List<UsuarioEmpresaRol> findRolesActivosByUsuarioIdAndEmpresaId(@Param("usuarioId") Long usuarioId, @Param("empresaId") Long empresaId);

    @Query("SELECT uer FROM UsuarioEmpresaRol uer WHERE uer.usuario.id = :usuarioId AND uer.empresa.id = :empresaId AND uer.rol.id = :rolId")
    Optional<UsuarioEmpresaRol> findByUsuarioIdAndEmpresaIdAndRolId(@Param("usuarioId") Long usuarioId, @Param("empresaId") Long empresaId, @Param("rolId") Long rolId);

    @Query("SELECT uer FROM UsuarioEmpresaRol uer WHERE uer.usuario.id = :usuarioId")
    List<UsuarioEmpresaRol> findByUsuarioId(@Param("usuarioId") Long usuarioId);

    @Query("SELECT uer FROM UsuarioEmpresaRol uer WHERE uer.empresa.id = :empresaId AND uer.rol.id = :rolId AND uer.activo = true")
    List<UsuarioEmpresaRol> findByEmpresaIdAndRolId(@Param("empresaId") Long empresaId, @Param("rolId") Long rolId);

    @Modifying
    @Query("DELETE FROM UsuarioEmpresaRol uer WHERE uer.usuario.id = :usuarioId AND uer.empresa.id = :empresaId")
    void deleteByUsuarioIdAndEmpresaId(@Param("usuarioId") Long usuarioId, @Param("empresaId") Long empresaId);

    @Modifying
    @Query("DELETE FROM UsuarioEmpresaRol uer WHERE uer.usuario.id = :usuarioId AND uer.empresa.id = :empresaId AND uer.rol.id = :rolId")
    void deleteByUsuarioIdAndEmpresaIdAndRolId(@Param("usuarioId") Long usuarioId, @Param("empresaId") Long empresaId, @Param("rolId") Long rolId);

    @Query("SELECT COUNT(uer) > 0 FROM UsuarioEmpresaRol uer WHERE uer.usuario.id = :usuarioId AND uer.empresa.id = :empresaId AND uer.rol.id = :rolId AND uer.activo = true")
    boolean existsByUsuarioIdAndEmpresaIdAndRolId(@Param("usuarioId") Long usuarioId, @Param("empresaId") Long empresaId, @Param("rolId") Long rolId);

    @Query("SELECT uer.rol.nombre, COUNT(DISTINCT uer.usuario.id) FROM UsuarioEmpresaRol uer WHERE uer.activo = true GROUP BY uer.rol.nombre")
    List<Object[]> countUsuariosUnicosPorRol();
}
