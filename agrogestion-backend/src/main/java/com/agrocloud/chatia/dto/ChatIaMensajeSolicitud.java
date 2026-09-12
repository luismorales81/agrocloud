package com.agrocloud.chatia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class ChatIaMensajeSolicitud {

    @NotBlank(message = "El mensaje es obligatorio")
    @Size(max = 4000, message = "El mensaje no puede superar 4000 caracteres")
    private String mensaje;

    @NotNull
    private List<ChatIaMensajeHistorialItem> historial = new ArrayList<>();

    private String moduloActivo;

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public List<ChatIaMensajeHistorialItem> getHistorial() {
        return historial;
    }

    public void setHistorial(List<ChatIaMensajeHistorialItem> historial) {
        this.historial = historial != null ? historial : new ArrayList<>();
    }

    public String getModuloActivo() {
        return moduloActivo;
    }

    public void setModuloActivo(String moduloActivo) {
        this.moduloActivo = moduloActivo;
    }
}
