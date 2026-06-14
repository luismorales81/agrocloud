package com.agrocloud.trazabilidad.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

/**
 * Cuerpo para {@code POST /api/trazabilidad/expedientes}
 */
public class SolicitudGeneracionExpedienteTrazabilidad {

    @NotBlank
    @Pattern(regexp = "LOTE|COSECHA|RECRIA|VENTA_PORCINO|AVICOLA_HUEVOS|AVICOLA_CRIANZA|AVICOLA_CARNE|AVICOLA_PONEDORAS")
    private String entidadTipo;

    @NotNull
    @Positive
    private Long entidadId;

    public String getEntidadTipo() { return entidadTipo; }
    public void setEntidadTipo(String entidadTipo) { this.entidadTipo = entidadTipo; }
    public Long getEntidadId() { return entidadId; }
    public void setEntidadId(Long entidadId) { this.entidadId = entidadId; }
}
