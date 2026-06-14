package com.agrocloud.trazabilidad.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;

/**
 * Cuerpo JSON para {@code POST /api/trazabilidad/reportes}
 */
public class SolicitudGeneracionReporteTrazabilidad {

    @NotBlank
    @Pattern(regexp = "LOTE|COSECHA|RECRIA")
    private String entidadTipo;

    @NotNull
    @Positive
    private Long entidadId;

    @NotBlank
    private String certificacion;

    public String getEntidadTipo() { return entidadTipo; }
    public void setEntidadTipo(String entidadTipo) { this.entidadTipo = entidadTipo; }
    public Long getEntidadId() { return entidadId; }
    public void setEntidadId(Long entidadId) { this.entidadId = entidadId; }
    public String getCertificacion() { return certificacion; }
    public void setCertificacion(String certificacion) { this.certificacion = certificacion; }
}
