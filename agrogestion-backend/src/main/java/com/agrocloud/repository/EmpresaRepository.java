package com.agrocloud.repository;

import com.agrocloud.model.entity.Empresa;
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

/**
 * Repositorio para la entidad Empresa en el sistema multiempresa.
 * 
 * @author AgroGestion Team
 * @version 2.0.0
 */
@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    /**
     * Busca una empresa por su CUIT
     */
    Optional<Empresa> findByCuit(String cuit);

    /**
     * Busca una empresa por su email de contacto
     */
    Optional<Empresa> findByEmailContacto(String emailContacto);

    /**
     * Busca empresas por estado
     */
    List<Empresa> findByEstado(EstadoEmpresa estado);


    /**
     * Busca empresas activas
     */
    List<Empresa> findByActivoTrue();


    /**
     * Busca empresas cuyo trial está próximo a vencer
     */
    @Query("SELECT e FROM Empresa e WHERE e.estado = 'TRIAL' AND e.fechaFinTrial <= :fechaLimite")
    List<Empresa> findEmpresasTrialProximoVencer(@Param("fechaLimite") LocalDate fechaLimite);

    /**
     * Busca empresas cuyo trial ha vencido
     */
    @Query("SELECT e FROM Empresa e WHERE e.estado = 'TRIAL' AND e.fechaFinTrial < :fechaActual")
    List<Empresa> findEmpresasTrialVencido(@Param("fechaActual") LocalDate fechaActual);

    /**
     * Cuenta empresas por estado
     */
    long countByEstado(EstadoEmpresa estado);


    /**
     * Cuenta empresas activas
     */
    long countByActivoTrue();

    /**
     * Busca empresas con paginación y filtros
     */
    @Query("SELECT e FROM Empresa e WHERE " +
           "(:nombre IS NULL OR LOWER(e.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
           "(:estado IS NULL OR e.estado = :estado) AND " +
           "(:activo IS NULL OR e.activo = :activo)")
    Page<Empresa> findEmpresasConFiltros(
            @Param("nombre") String nombre,
            @Param("estado") EstadoEmpresa estado,
            @Param("activo") Boolean activo,
            Pageable pageable);

    /**
     * Obtiene estadísticas de empresas
     */
    @Query("SELECT " +
           "COUNT(e) as totalEmpresas, " +
           "COUNT(CASE WHEN e.estado = 'ACTIVO' THEN 1 END) as empresasActivas, " +
           "COUNT(CASE WHEN e.estado = 'TRIAL' THEN 1 END) as empresasTrial, " +
           "COUNT(CASE WHEN e.estado = 'INACTIVO' THEN 1 END) as empresasInactivas, " +
           "COUNT(CASE WHEN e.estado = 'SUSPENDIDO' THEN 1 END) as empresasSuspendidas " +
           "FROM Empresa e")
    Object[] obtenerEstadisticasEmpresas();

    /**
     * Busca empresas creadas en un rango de fechas
     */
    @Query("SELECT e FROM Empresa e WHERE e.fechaCreacion BETWEEN :fechaInicio AND :fechaFin")
    List<Empresa> findEmpresasCreadasEnRango(
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin);

    /**
     * Busca empresas por nombre (búsqueda parcial)
     */
    @Query("SELECT e FROM Empresa e WHERE LOWER(e.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Empresa> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);

    /**
     * Verifica si existe una empresa con el CUIT especificado (excluyendo la empresa actual)
     */
    @Query("SELECT COUNT(e) > 0 FROM Empresa e WHERE e.cuit = :cuit AND (:id IS NULL OR e.id != :id)")
    boolean existsByCuitAndIdNot(@Param("cuit") String cuit, @Param("id") Long id);

    /**
     * Verifica si existe una empresa con el email especificado (excluyendo la empresa actual)
     */
    @Query("SELECT COUNT(e) > 0 FROM Empresa e WHERE e.emailContacto = :email AND (:id IS NULL OR e.id != :id)")
    boolean existsByEmailContactoAndIdNot(@Param("email") String email, @Param("id") Long id);

    /**
     * Obtiene empresas con más usuarios
     */
    @Query("SELECT e, COUNT(ue.usuario) as totalUsuarios " +
           "FROM Empresa e LEFT JOIN e.usuariosEmpresas ue " +
           "WHERE ue.estado = 'ACTIVO' " +
           "GROUP BY e " +
           "ORDER BY totalUsuarios DESC")
    List<Object[]> findEmpresasConMasUsuarios(Pageable pageable);

    /**
     * Obtiene empresas con más actividad (más campos, lotes, etc.)
     */
    @Query("SELECT e, " +
           "(SELECT COUNT(c) FROM Field c WHERE c.empresa = e) as totalCampos, " +
           "(SELECT COUNT(l) FROM Plot l WHERE l.campo.empresa = e) as totalLotes, " +
           "(SELECT COUNT(i) FROM Insumo i WHERE i.empresa = e) as totalInsumos " +
           "FROM Empresa e " +
           "ORDER BY totalCampos DESC, totalLotes DESC, totalInsumos DESC")
    List<Object[]> findEmpresasConMasActividad(Pageable pageable);
    
    /**
     * Cuenta empresas creadas después de una fecha específica
     */
    long countByFechaCreacionAfter(java.time.LocalDateTime fecha);
}
