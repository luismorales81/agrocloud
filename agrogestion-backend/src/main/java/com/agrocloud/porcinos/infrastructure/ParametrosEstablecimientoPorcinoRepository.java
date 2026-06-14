package com.agrocloud.porcinos.infrastructure;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.porcinos.domain.ParametrosEstablecimientoPorcino;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParametrosEstablecimientoPorcinoRepository extends JpaRepository<ParametrosEstablecimientoPorcino, Long> {

    Optional<ParametrosEstablecimientoPorcino> findByEmpresa(Empresa empresa);
}
