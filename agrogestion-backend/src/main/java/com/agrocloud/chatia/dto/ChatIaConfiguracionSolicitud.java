package com.agrocloud.chatia.dto;

import jakarta.validation.constraints.NotBlank;

public class ChatIaConfiguracionSolicitud {

    /** Vacío al actualizar: se conserva la clave guardada. Obligatoria solo en el primer alta. */
    private String claveApi;

    @NotBlank(message = "El modelo es obligatorio")
    private String modelo;

    public String getClaveApi() {
        return claveApi;
    }

    public void setClaveApi(String claveApi) {
        this.claveApi = claveApi;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }
}
