package com.agrocloud.dto;

import com.agrocloud.model.enums.FormaAplicacion;
import com.agrocloud.model.enums.TipoAplicacion;
import jakarta.validation.constraints.NotNull;

public class CalcularCantidadRequest {
    
    @NotNull(message = "El ID del insumo es obligatorio")
    private Long insumoId;
    
    @NotNull(message = "El ID del lote es obligatorio")
    private Long loteId;
    
    @NotNull(message = "El tipo de aplicación es obligatorio")
    private TipoAplicacion tipoAplicacion;
    
    @NotNull(message = "La forma de aplicación es obligatoria")
    private FormaAplicacion formaAplicacion;
    
    private Double dosisPersonalizada; // Opcional, para permitir variación del usuario

    // Getters y Setters
    public Long getInsumoId() {
        return insumoId;
    }

    public void setInsumoId(Long insumoId) {
        this.insumoId = insumoId;
    }

    public Long getLoteId() {
        return loteId;
    }

    public void setLoteId(Long loteId) {
        this.loteId = loteId;
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

    public Double getDosisPersonalizada() {
        return dosisPersonalizada;
    }

    public void setDosisPersonalizada(Double dosisPersonalizada) {
        this.dosisPersonalizada = dosisPersonalizada;
    }
}