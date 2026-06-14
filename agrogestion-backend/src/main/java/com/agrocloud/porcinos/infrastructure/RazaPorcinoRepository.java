package com.agrocloud.porcinos.infrastructure;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.porcinos.domain.RazaPorcino;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RazaPorcinoRepository extends JpaRepository<RazaPorcino, Long> {

    List<RazaPorcino> findByEmpresaAndActivoTrue(Empresa empresa);

    List<RazaPorcino> findByEmpresaAndTipoAndActivoTrue(Empresa empresa, RazaPorcino.TipoRaza tipo);

    Optional<RazaPorcino> findByEmpresaAndNombreAndActivoTrue(Empresa empresa, String nombre);
}
