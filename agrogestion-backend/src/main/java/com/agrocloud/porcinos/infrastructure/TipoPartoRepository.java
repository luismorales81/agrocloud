package com.agrocloud.porcinos.infrastructure;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.porcinos.domain.TipoParto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipoPartoRepository extends JpaRepository<TipoParto, Long> {

    List<TipoParto> findByEmpresaAndActivoTrue(Empresa empresa);

    Optional<TipoParto> findByIdAndEmpresaAndActivoTrue(Long id, Empresa empresa);

    boolean existsByNombreAndEmpresaAndActivoTrue(String nombre, Empresa empresa);

    Optional<TipoParto> findByEmpresaAndNombreAndActivoTrue(Empresa empresa, String nombre);
}
