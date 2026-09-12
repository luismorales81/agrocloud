package com.agrocloud.chatia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ChatIaMensajeHistorialItem {

    @NotBlank
    @Pattern(regexp = "usuario|asistente", flags = Pattern.Flag.CASE_INSENSITIVE, message = "Rol inválido")
    private String rol;

    @NotBlank
    private String contenido;

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }
}
