package com.agrocloud.dto;

import com.agrocloud.model.enums.FormaAplicacion;
import com.agrocloud.model.enums.TipoAplicacion;
import com.agrocloud.model.enums.UnidadDosis;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class DosisAgroquimicoRequest {

    private Long insumoId;

    @NotNull(message = "El tipo de aplicación es obligatorio")
    private TipoAplicacion tipoAplicacion;

    @NotNull(message = "La forma de aplicación es obligatoria")
    private FormaAplicacion formaAplicacion;

    // Campo unidad eliminado - ahora se deriva automáticamente del insumo relacionado
    // @Deprecated - Ya no se usa, se mantiene por compatibilidad pero se ignora
    private UnidadDosis unidad;

    @NotNull(message = "La dosis recomendada es obligatoria")
    @DecimalMin(value = "0.01", message = "La dosis recomendada debe ser mayor que 0")
    private Double dosisRecomendadaPorHa;

    // Getters y Setters
    public Long getInsumoId() {
        return insumoId;
    }

    public void setInsumoId(Long insumoId) {
        this.insumoId = insumoId;
    }

    public TipoAplicacion getTipoAplicacion() {
        return tipoAplicacion;
    }

    public void setTipoAplicacion(TipoAplicacion tipoAplicacion) {
        this.tipoAplicacion = tipoAplicacion;
    }

    public FormaAplicacion getFormaAplicacion() {
        return formaAplicacion;
    }

    public void setFormaAplicacion(FormaAplicacion formaAplicacion) {
        this.formaAplicacion = formaAplicacion;
    }

    public UnidadDosis getUnidad() {
        return unidad;
    }

    public void setUnidad(UnidadDosis unidad) {
        this.unidad = unidad;
    }

    public Double getDosisRecomendadaPorHa() {
        return dosisRecomendadaPorHa;
    }

    public void setDosisRecomendadaPorHa(Double dosisRecomendadaPorHa) {
        this.dosisRecomendadaPorHa = dosisRecomendadaPorHa;
    }
}
