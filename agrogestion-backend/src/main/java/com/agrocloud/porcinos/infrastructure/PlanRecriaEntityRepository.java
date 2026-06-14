package com.agrocloud.porcinos.infrastructure;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.porcinos.domain.PlanRecria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanRecriaEntityRepository extends JpaRepository<PlanRecria, Long> {

    List<PlanRecria> findByEmpresaAndActivoTrueOrderByNombreAsc(Empresa empresa);

    Optional<PlanRecria> findByIdAndEmpresaAndActivoTrue(Long id, Empresa empresa);
}
