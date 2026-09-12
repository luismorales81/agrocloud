package com.agrocloud.chatia.dto;

import java.util.List;

public class ChatIaEstadoRespuesta {

    private boolean habilitado;
    private String modelo;
    private boolean claveConfigurada;
    private boolean claveInvalida;
    private List<String> modelosDisponibles;

    public boolean isHabilitado() {
        return habilitado;
    }

    public void setHabilitado(boolean habilitado) {
        this.habilitado = habilitado;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public boolean isClaveConfigurada() {
        return claveConfigurada;
    }

    public void setClaveConfigurada(boolean claveConfigurada) {
        this.claveConfigurada = claveConfigurada;
    }

    public boolean isClaveInvalida() {
        return claveInvalida;
    }

    public void setClaveInvalida(boolean claveInvalida) {
        this.claveInvalida = claveInvalida;
    }

    public List<String> getModelosDisponibles() {
        return modelosDisponibles;
    }

    public void setModelosDisponibles(List<String> modelosDisponibles) {
        this.modelosDisponibles = modelosDisponibles;
    }
}
