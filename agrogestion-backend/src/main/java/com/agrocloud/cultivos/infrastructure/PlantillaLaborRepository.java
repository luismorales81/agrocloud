package com.agrocloud.cultivos.infrastructure;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.cultivos.domain.PlantillaLabor;
import com.agrocloud.cultivos.domain.TipoCultivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlantillaLaborRepository extends JpaRepository<PlantillaLabor, Long> {
    List<PlantillaLabor> findByEmpresaAndTipoCultivoAndActivoTrueOrderByDiaRelativoSiembraAsc(
            Empresa empresa, TipoCultivo tipoCultivo);

    @Query("SELECT DISTINCT p.tipoLabor FROM PlantillaLabor p WHERE p.empresa.id = :empresaId AND p.activo = true ORDER BY p.tipoLabor")
    List<String> findDistinctTipoLaborActivoByEmpresaId(@Param("empresaId") Long empresaId);
}
