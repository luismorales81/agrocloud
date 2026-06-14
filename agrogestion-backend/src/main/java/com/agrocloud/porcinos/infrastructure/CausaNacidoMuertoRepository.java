package com.agrocloud.porcinos.infrastructure;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.porcinos.domain.CausaNacidoMuerto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CausaNacidoMuertoRepository extends JpaRepository<CausaNacidoMuerto, Long> {

    List<CausaNacidoMuerto> findByEmpresaAndActivoTrue(Empresa empresa);

    Optional<CausaNacidoMuerto> findByIdAndEmpresaAndActivoTrue(Long id, Empresa empresa);

    boolean existsByNombreAndEmpresaAndActivoTrue(String nombre, Empresa empresa);

    Optional<CausaNacidoMuerto> findByEmpresaAndNombreAndActivoTrue(Empresa empresa, String nombre);
}
