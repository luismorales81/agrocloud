package com.agrocloud.porcinos.infrastructure;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.porcinos.domain.TipoServicioPorcino;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipoServicioPorcinoRepository extends JpaRepository<TipoServicioPorcino, Long> {

    List<TipoServicioPorcino> findByEmpresaAndActivoTrue(Empresa empresa);

    Optional<TipoServicioPorcino> findByEmpresaAndNombreAndActivoTrue(Empresa empresa, String nombre);
}
