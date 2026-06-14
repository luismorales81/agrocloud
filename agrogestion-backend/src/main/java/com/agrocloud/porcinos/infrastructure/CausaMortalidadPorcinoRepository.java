package com.agrocloud.porcinos.infrastructure;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.porcinos.domain.CausaMortalidadPorcino;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CausaMortalidadPorcinoRepository extends JpaRepository<CausaMortalidadPorcino, Long> {

    List<CausaMortalidadPorcino> findByEmpresaAndActivoTrue(Empresa empresa);

    List<CausaMortalidadPorcino> findByEmpresaAndEtapaAndActivoTrue(Empresa empresa, CausaMortalidadPorcino.EtapaMortalidad etapa);

    Optional<CausaMortalidadPorcino> findByEmpresaAndNombreAndEtapaAndActivoTrue(Empresa empresa, String nombre, CausaMortalidadPorcino.EtapaMortalidad etapa);
}
