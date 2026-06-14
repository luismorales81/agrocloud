package com.agrocloud.porcinos.infrastructure;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.porcinos.domain.TipoEventoSanitario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipoEventoSanitarioRepository extends JpaRepository<TipoEventoSanitario, Long> {

    List<TipoEventoSanitario> findByEmpresaAndActivoTrue(Empresa empresa);

    Optional<TipoEventoSanitario> findByIdAndEmpresaAndActivoTrue(Long id, Empresa empresa);

    List<TipoEventoSanitario> findByEmpresaAndCategoriaAndActivoTrue(
        Empresa empresa, TipoEventoSanitario.CategoriaEvento categoria);

    boolean existsByNombreAndEmpresaAndActivoTrue(String nombre, Empresa empresa);

    Optional<TipoEventoSanitario> findByEmpresaAndNombreAndActivoTrue(Empresa empresa, String nombre);
}
