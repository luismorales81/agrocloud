package com.agrocloud.porcinos.infrastructure;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.porcinos.domain.CausaMomificado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CausaMomificadoRepository extends JpaRepository<CausaMomificado, Long> {

    List<CausaMomificado> findByEmpresaAndActivoTrue(Empresa empresa);

    Optional<CausaMomificado> findByIdAndEmpresaAndActivoTrue(Long id, Empresa empresa);

    boolean existsByNombreAndEmpresaAndActivoTrue(String nombre, Empresa empresa);

    Optional<CausaMomificado> findByEmpresaAndNombreAndActivoTrue(Empresa empresa, String nombre);
}
