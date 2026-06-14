package com.agrocloud.porcinos.infrastructure;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.porcinos.domain.ParametrosProductivosPorcino;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParametrosProductivosPorcinoRepository extends JpaRepository<ParametrosProductivosPorcino, Long> {

    Optional<ParametrosProductivosPorcino> findByEmpresa(Empresa empresa);
}
