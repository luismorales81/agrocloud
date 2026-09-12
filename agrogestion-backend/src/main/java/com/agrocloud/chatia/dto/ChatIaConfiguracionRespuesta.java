package com.agrocloud.chatia.dto;

public class ChatIaConfiguracionRespuesta {

    private String modelo;
    private boolean activo;
    private boolean claveConfigurada;
    private String claveEnmascarada;
    private boolean claveInvalida;

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public boolean isClaveConfigurada() {
        return claveConfigurada;
    }

    public void setClaveConfigurada(boolean claveConfigurada) {
        this.claveConfigurada = claveConfigurada;
    }

    public String getClaveEnmascarada() {
        return claveEnmascarada;
    }

    public void setClaveEnmascarada(String claveEnmascarada) {
        this.claveEnmascarada = claveEnmascarada;
    }

    public boolean isClaveInvalida() {
        return claveInvalida;
    }

    public void setClaveInvalida(boolean claveInvalida) {
        this.claveInvalida = claveInvalida;
    }
}
