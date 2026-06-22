package com.agrocloud.core.infrastructure;

import com.agrocloud.core.domain.Campana;
import com.agrocloud.model.enums.EstadoCampana;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("campanaRepositoryCore")
public interface CampanaRepository extends JpaRepository<Campana, Long> {

    List<Campana> findByEmpresaIdOrderByFechaInicioDesc(Long empresaId);

    Optional<Campana> findByEmpresaIdAndEstado(Long empresaId, EstadoCampana estado);

    Optional<Campana> findByEmpresaIdAndEsDefaultTrue(Long empresaId);

    boolean existsByEmpresaIdAndCodigo(Long empresaId, String codigo);

    Optional<Campana> findByEmpresaIdAndCodigo(Long empresaId, String codigo);
}
