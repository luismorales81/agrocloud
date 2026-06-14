package com.agrocloud.trazabilidad.dto;

import jakarta.validation.constraints.NotBlank;

public class SolicitudActualizacionParametrosRegla {

    @NotBlank(message = "parametrosJson es obligatorio")
    private String parametrosJson;

    public String getParametrosJson() {
        return parametrosJson;
    }

    public void setParametrosJson(String parametrosJson) {
        this.parametrosJson = parametrosJson;
    }
}
