package com.agrocloud.porcinos.infrastructure;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.porcinos.domain.DatosEconomicosPorcino;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DatosEconomicosPorcinoRepository extends JpaRepository<DatosEconomicosPorcino, Long> {

    Optional<DatosEconomicosPorcino> findByEmpresa(Empresa empresa);
}
