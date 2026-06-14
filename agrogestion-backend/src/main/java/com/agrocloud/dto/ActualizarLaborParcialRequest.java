package com.agrocloud.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

/**
 * Request para PATCH de labores (spec SDD). Solo se envían los campos a actualizar.
 * Transiciones permitidas: planificada → realizada | cancelada; cancelada → planificada (opcional).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActualizarLaborParcialRequest {

    @JsonProperty("estado")
    private String estado;

    @JsonProperty("fecha_planificada")
    private LocalDate fechaPlanificada;

    @JsonProperty("fecha_realizacion")
    private LocalDate fechaRealizacion;

    @JsonProperty("observaciones")
    private String observaciones;

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDate getFechaPlanificada() {
        return fechaPlanificada;
    }

    public void setFechaPlanificada(LocalDate fechaPlanificada) {
        this.fechaPlanificada = fechaPlanificada;
    }

    public LocalDate getFechaRealizacion() {
        return fechaRealizacion;
    }

    public void setFechaRealizacion(LocalDate fechaRealizacion) {
        this.fechaRealizacion = fechaRealizacion;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
