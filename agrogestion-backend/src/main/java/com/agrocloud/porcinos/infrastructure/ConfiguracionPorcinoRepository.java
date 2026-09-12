package com.agrocloud.porcinos.infrastructure;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.porcinos.domain.ConfiguracionPorcino;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConfiguracionPorcinoRepository extends JpaRepository<ConfiguracionPorcino, Long> {

    Optional<ConfiguracionPorcino> findByClaveAndEmpresaAndActivoTrue(String clave, Empresa empresa);

    boolean existsByClaveAndActivoTrue(String clave);

    List<ConfiguracionPorcino> findByEmpresaAndActivoTrue(Empresa empresa);

    List<ConfiguracionPorcino> findByEmpresaAndCategoriaAndActivoTrue(Empresa empresa, String categoria);
}
