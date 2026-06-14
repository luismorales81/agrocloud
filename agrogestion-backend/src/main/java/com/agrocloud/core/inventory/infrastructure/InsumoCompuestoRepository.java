package com.agrocloud.core.inventory.infrastructure;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.inventory.domain.InsumoCompuesto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("insumoCompuestoRepositoryInventario")
public interface InsumoCompuestoRepository extends JpaRepository<InsumoCompuesto, Long> {

    @Query("SELECT DISTINCT ic FROM InsumoCompuesto ic " +
           "LEFT JOIN FETCH ic.componentes c " +
           "LEFT JOIN FETCH c.insumo " +
           "LEFT JOIN FETCH c.cultivo " +
           "LEFT JOIN FETCH c.insumoCompuestoPadre " +
           "WHERE ic.empresa = :empresa AND ic.activo = true")
    List<InsumoCompuesto> findByEmpresaAndActivoTrue(@Param("empresa") Empresa empresa);

    @Query("SELECT DISTINCT ic FROM InsumoCompuesto ic " +
           "LEFT JOIN FETCH ic.componentes c " +
           "LEFT JOIN FETCH c.insumo " +
           "LEFT JOIN FETCH c.cultivo " +
           "LEFT JOIN FETCH c.insumoCompuestoPadre " +
           "WHERE ic.id = :id AND ic.empresa = :empresa AND ic.activo = true")
    Optional<InsumoCompuesto> findByIdAndEmpresaAndActivoTrue(@Param("id") Long id, @Param("empresa") Empresa empresa);

    List<InsumoCompuesto> findByEmpresaAndTipoAndActivoTrue(Empresa empresa, InsumoCompuesto.TipoInsumoCompuesto tipo);

    boolean existsByNombreAndEmpresaAndActivoTrue(String nombre, Empresa empresa);

    Optional<InsumoCompuesto> findFirstByEmpresaAndNombreIgnoreCaseAndActivoTrue(Empresa empresa, String nombre);
}
