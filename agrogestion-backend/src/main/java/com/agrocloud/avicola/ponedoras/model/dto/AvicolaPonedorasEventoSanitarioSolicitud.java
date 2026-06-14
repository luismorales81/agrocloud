package com.agrocloud.avicola.ponedoras.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AvicolaPonedorasEventoSanitarioSolicitud {

    private LocalDate fecha;
    private String tipo;
    private String descripcion;
    private Long insumoId;
    private BigDecimal dosis;
    private String observaciones;

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Long getInsumoId() {
        return insumoId;
    }

    public void setInsumoId(Long insumoId) {
        this.insumoId = insumoId;
    }

    public BigDecimal getDosis() {
        return dosis;
    }

    public void setDosis(BigDecimal dosis) {
        this.dosis = dosis;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
