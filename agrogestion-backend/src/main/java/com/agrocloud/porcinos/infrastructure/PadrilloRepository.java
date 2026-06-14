package com.agrocloud.porcinos.infrastructure;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.porcinos.domain.Padrillo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PadrilloRepository extends JpaRepository<Padrillo, Long> {

    List<Padrillo> findByEmpresaAndActivoTrue(Empresa empresa);

    Optional<Padrillo> findByIdentificacionAndEmpresaAndActivoTrue(String identificacion, Empresa empresa);

    Optional<Padrillo> findByIdAndActivoTrue(Long id);

    Optional<Padrillo> findByIdAndEmpresa(Long id, Empresa empresa);
}
