package com.agrocloud.trazabilidad.infrastructure;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.trazabilidad.domain.TrazabilidadReporte;
import com.agrocloud.trazabilidad.dto.TrazabilidadReporteListado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrazabilidadReporteRepository extends JpaRepository<TrazabilidadReporte, Long> {

    Optional<TrazabilidadReporte> findByIdAndEmpresa(Long id, Empresa empresa);

    @Query("""
            SELECT new com.agrocloud.trazabilidad.dto.TrazabilidadReporteListado(
                r.id, r.entidadTipo, r.entidadId, r.certificacionCodigo, r.resultado,
                r.hashSnapshot, r.hashPdf, r.versionMotor, r.generadoEn)
            FROM TrazabilidadReporte r
            WHERE r.empresa.id = :empresaId
            ORDER BY r.generadoEn DESC
            """)
    List<TrazabilidadReporteListado> listarResumenesPorEmpresaId(@Param("empresaId") Long empresaId);
}
