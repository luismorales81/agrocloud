package com.agrocloud.chatia.dto;

import java.util.ArrayList;
import java.util.List;

public class ChatIaMensajeRespuesta {

    private String respuesta;
    private List<String> herramientasUsadas = new ArrayList<>();
    private boolean iaDisponible = true;
    private String error;

    public String getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(String respuesta) {
        this.respuesta = respuesta;
    }

    public List<String> getHerramientasUsadas() {
        return herramientasUsadas;
    }

    public void setHerramientasUsadas(List<String> herramientasUsadas) {
        this.herramientasUsadas = herramientasUsadas;
    }

    public boolean isIaDisponible() {
        return iaDisponible;
    }

    public void setIaDisponible(boolean iaDisponible) {
        this.iaDisponible = iaDisponible;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
