package com.agrocloud.porcinos.infrastructure;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.porcinos.domain.RecetaAlimentacionPorEtapa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecetaAlimentacionPorEtapaRepository extends JpaRepository<RecetaAlimentacionPorEtapa, Long> {

    List<RecetaAlimentacionPorEtapa> findByEmpresaAndActivoTrue(Empresa empresa);

    List<RecetaAlimentacionPorEtapa> findByEmpresaAndEtapaAndActivoTrue(
        Empresa empresa, RecetaAlimentacionPorEtapa.EtapaAlimentacion etapa);

    Optional<RecetaAlimentacionPorEtapa> findByEmpresaAndEtapaAndEsPorDefectoTrueAndActivoTrue(
        Empresa empresa, RecetaAlimentacionPorEtapa.EtapaAlimentacion etapa);

    Optional<RecetaAlimentacionPorEtapa> findByIdAndEmpresaAndActivoTrue(Long id, Empresa empresa);
}
