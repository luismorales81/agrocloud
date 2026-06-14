package com.agrocloud.core.infrastructure;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.model.enums.EstadoEmpresa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository("empresaRepositoryCore")
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    Optional<Empresa> findByCuit(String cuit);
    Optional<Empresa> findByEmailContacto(String emailContacto);
    List<Empresa> findByEstado(EstadoEmpresa estado);
    List<Empresa> findByActivoTrue();

    @Query("SELECT e FROM Empresa e WHERE e.estado = 'TRIAL' AND e.fechaFinTrial <= :fechaLimite")
    List<Empresa> findEmpresasTrialProximoVencer(@Param("fechaLimite") LocalDate fechaLimite);

    @Query("SELECT e FROM Empresa e WHERE e.estado = 'TRIAL' AND e.fechaFinTrial < :fechaActual")
    List<Empresa> findEmpresasTrialVencido(@Param("fechaActual") LocalDate fechaActual);

    long countByEstado(EstadoEmpresa estado);
    long countByActivoTrue();

    @Query("SELECT e FROM Empresa e WHERE (:nombre IS NULL OR LOWER(e.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND (:estado IS NULL OR e.estado = :estado) AND (:activo IS NULL OR e.activo = :activo)")
    Page<Empresa> findEmpresasConFiltros(@Param("nombre") String nombre, @Param("estado") EstadoEmpresa estado, @Param("activo") Boolean activo, Pageable pageable);

    @Query("SELECT COUNT(e) as totalEmpresas, COUNT(CASE WHEN e.estado = 'ACTIVO' THEN 1 END) as empresasActivas, COUNT(CASE WHEN e.estado = 'TRIAL' THEN 1 END) as empresasTrial, COUNT(CASE WHEN e.estado = 'INACTIVO' THEN 1 END) as empresasInactivas, COUNT(CASE WHEN e.estado = 'SUSPENDIDO' THEN 1 END) as empresasSuspendidas FROM Empresa e")
    Object[] obtenerEstadisticasEmpresas();

    @Query("SELECT e FROM Empresa e WHERE e.fechaCreacion BETWEEN :fechaInicio AND :fechaFin")
    List<Empresa> findEmpresasCreadasEnRango(@Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);

    @Query("SELECT e FROM Empresa e WHERE LOWER(e.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Empresa> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);

    @Query("SELECT COUNT(e) > 0 FROM Empresa e WHERE e.cuit = :cuit AND (:id IS NULL OR e.id != :id)")
    boolean existsByCuitAndIdNot(@Param("cuit") String cuit, @Param("id") Long id);

    @Query("SELECT COUNT(e) > 0 FROM Empresa e WHERE e.emailContacto = :email AND (:id IS NULL OR e.id != :id)")
    boolean existsByEmailContactoAndIdNot(@Param("email") String email, @Param("id") Long id);

    @Query("SELECT e, COUNT(ue.usuario) as totalUsuarios FROM Empresa e LEFT JOIN e.usuariosEmpresas ue WHERE ue.estado = 'ACTIVO' GROUP BY e ORDER BY totalUsuarios DESC")
    List<Object[]> findEmpresasConMasUsuarios(Pageable pageable);

    @Query(value = "SELECT e.nombre, e.cuit, (SELECT COUNT(*) FROM cultivo_campos c WHERE c.empresa_id = e.id) as total_campos, (SELECT COUNT(*) FROM cultivo_lotes l JOIN cultivo_campos c ON l.campo_id = c.id WHERE c.empresa_id = e.id) as total_lotes, (SELECT COUNT(*) FROM cultivo_labores lab JOIN cultivo_lotes l ON lab.lote_id = l.id JOIN cultivo_campos c ON l.campo_id = c.id WHERE c.empresa_id = e.id) as total_labores FROM empresas e ORDER BY total_campos DESC, total_lotes DESC, total_labores DESC", nativeQuery = true)
    List<Object[]> findEmpresasConMasActividad();

    long countByFechaCreacionAfter(java.time.LocalDateTime fecha);
}
