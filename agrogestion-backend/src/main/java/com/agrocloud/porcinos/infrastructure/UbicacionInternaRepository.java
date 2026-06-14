package com.agrocloud.porcinos.infrastructure;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.porcinos.domain.UbicacionInterna;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UbicacionInternaRepository extends JpaRepository<UbicacionInterna, Long> {

    List<UbicacionInterna> findByEmpresaAndActivoTrue(Empresa empresa);

    List<UbicacionInterna> findByEmpresaAndNivelAndActivoTrue(Empresa empresa, UbicacionInterna.NivelUbicacion nivel);

    List<UbicacionInterna> findByUbicacionPadreAndActivoTrue(UbicacionInterna padre);

    List<UbicacionInterna> findByEmpresaAndTipoUbicacionAndActivoTrue(Empresa empresa, UbicacionInterna.TipoUbicacion tipoUbicacion);

    Optional<UbicacionInterna> findByIdAndEmpresaAndActivoTrue(Long id, Empresa empresa);

    boolean existsByNombreAndEmpresaAndActivoTrue(String nombre, Empresa empresa);
}
