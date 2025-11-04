package com.agrocloud.dto;

import com.agrocloud.model.enums.TipoAplicacion;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * DTO para dosis por tipo de aplicación
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
public class DosisTipoAplicacionRequest {

    @NotNull(message = "El tipo de aplicación es obligatorio")
    private TipoAplicacion tipoAplicacion;

    @NotNull(message = "La dosis recomendada es obligatoria")
    @Positive(message = "La dosis recomendada debe ser positiva")
    private BigDecimal dosisRecomendada;

    private BigDecimal dosisMinima;

    private BigDecimal dosisMaxima;

    private String unidadMedida;

    private String descripcion;

    private String condicionesAplicacion;

    // Getters and Setters
    public TipoAplicacion getTipoAplicacion() {
        return tipoAplicacion;
    }

    public void setTipoAplicacion(TipoAplicacion tipoAplicacion) {
        this.tipoAplicacion = tipoAplicacion;
    }

    public BigDecimal getDosisRecomendada() {
        return dosisRecomendada;
    }

    public void setDosisRecomendada(BigDecimal dosisRecomendada) {
        this.dosisRecomendada = dosisRecomendada;
    }

    public BigDecimal getDosisMinima() {
        return dosisMinima;
    }

    public void setDosisMinima(BigDecimal dosisMinima) {
        this.dosisMinima = dosisMinima;
    }

    public BigDecimal getDosisMaxima() {
        return dosisMaxima;
    }

    public void setDosisMaxima(BigDecimal dosisMaxima) {
        this.dosisMaxima = dosisMaxima;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCondicionesAplicacion() {
        return condicionesAplicacion;
    }

    public void setCondicionesAplicacion(String condicionesAplicacion) {
        this.condicionesAplicacion = condicionesAplicacion;
    }
}









