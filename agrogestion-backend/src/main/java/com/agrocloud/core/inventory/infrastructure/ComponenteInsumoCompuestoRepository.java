package com.agrocloud.core.inventory.infrastructure;

import com.agrocloud.core.inventory.domain.ComponenteInsumoCompuesto;
import com.agrocloud.core.inventory.domain.InsumoCompuesto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("componenteInsumoCompuestoRepositoryInventario")
public interface ComponenteInsumoCompuestoRepository extends JpaRepository<ComponenteInsumoCompuesto, Long> {

    List<ComponenteInsumoCompuesto> findByInsumoCompuesto(InsumoCompuesto insumoCompuesto);

    void deleteByInsumoCompuesto(InsumoCompuesto insumoCompuesto);
}
