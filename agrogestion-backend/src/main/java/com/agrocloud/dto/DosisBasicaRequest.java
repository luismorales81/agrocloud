package com.agrocloud.dto;

import com.agrocloud.model.enums.TipoAplicacion;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * DTO para dosis básica de agroquímico
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
public class DosisBasicaRequest {

    @NotNull(message = "El tipo de aplicación es obligatorio")
    private TipoAplicacion tipoAplicacion;

    @NotNull(message = "La dosis recomendada es obligatoria")
    @Positive(message = "La dosis recomendada debe ser positiva")
    private BigDecimal dosisRecomendada;

    private String unidadMedida;

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

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }
}













