package com.agrocloud.porcinos.infrastructure;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.porcinos.domain.ProveedorGenetica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProveedorGeneticaRepository extends JpaRepository<ProveedorGenetica, Long> {

    List<ProveedorGenetica> findByEmpresaAndActivoTrue(Empresa empresa);

    Optional<ProveedorGenetica> findByIdAndEmpresaAndActivoTrue(Long id, Empresa empresa);

    List<ProveedorGenetica> findByEmpresaAndTipoAndActivoTrue(Empresa empresa, ProveedorGenetica.TipoProveedor tipo);

    boolean existsByNombreAndEmpresaAndActivoTrue(String nombre, Empresa empresa);

    Optional<ProveedorGenetica> findByEmpresaAndNombreAndActivoTrue(Empresa empresa, String nombre);
}
