package com.agrocloud.porcinos.infrastructure;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.porcinos.domain.LechonNN;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LechonNNRepository extends JpaRepository<LechonNN, Long> {

    List<LechonNN> findByEmpresaAndActivoTrue(Empresa empresa);
}
