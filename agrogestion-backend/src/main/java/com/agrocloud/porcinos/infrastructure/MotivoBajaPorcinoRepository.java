package com.agrocloud.porcinos.infrastructure;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.porcinos.domain.MotivoBajaPorcino;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MotivoBajaPorcinoRepository extends JpaRepository<MotivoBajaPorcino, Long> {

    List<MotivoBajaPorcino> findByEmpresaAndActivoTrue(Empresa empresa);

    Optional<MotivoBajaPorcino> findByEmpresaAndNombreAndActivoTrue(Empresa empresa, String nombre);
}
