package com.agrocloud.porcinos.infrastructure;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.porcinos.domain.EsquemaSanitarioPorcino;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EsquemaSanitarioPorcinoRepository extends JpaRepository<EsquemaSanitarioPorcino, Long> {

    List<EsquemaSanitarioPorcino> findByEmpresaAndActivoTrue(Empresa empresa);
}
