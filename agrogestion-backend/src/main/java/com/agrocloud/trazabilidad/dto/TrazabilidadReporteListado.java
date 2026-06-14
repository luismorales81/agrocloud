package com.agrocloud.trazabilidad.dto;

import com.agrocloud.trazabilidad.domain.TrazabilidadReporte;

import java.time.LocalDateTime;

/**
 * Resumen de reporte para listados (sin cargar el PDF en memoria).
 */
public record TrazabilidadReporteListado(
        Long id,
        TrazabilidadReporte.TipoEntidadAlcance entidadTipo,
        Long entidadId,
        String certificacionCodigo,
        TrazabilidadReporte.ResultadoReporte resultado,
        String hashSnapshot,
        String hashPdf,
        String versionMotor,
        LocalDateTime generadoEn
) {
}
