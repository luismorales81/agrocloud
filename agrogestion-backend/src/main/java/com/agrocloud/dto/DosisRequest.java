package com.agrocloud.dto;

import com.agrocloud.model.enums.TipoAplicacion;
import java.math.BigDecimal;

/**
 * DTO para crear dosis de agroquímicos por tipo de aplicación
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
public class DosisRequest {
    
    private TipoAplicacion tipoAplicacion;
    private BigDecimal dosisRecomendada;
    private BigDecimal dosisMinima;
    private BigDecimal dosisMaxima;
    private String unidadMedida;
    private String descripcion;
    private String condicionesAplicacion;

    // Constructors
    public DosisRequest() {}

    public DosisRequest(TipoAplicacion tipoAplicacion, BigDecimal dosisRecomendada) {
        this.tipoAplicacion = tipoAplicacion;
        this.dosisRecomendada = dosisRecomendada;
    }

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
