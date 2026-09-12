package com.agrocloud.porcinos.model.dto;

public class PorcinosReproduccionResumen {

    private Long madresActivas;
    private Long gestacionesEnCurso;
    private Long madresEnLactancia;
    private Long partosPeriodo;
    private Long destetesPeriodo;

    public Long getMadresActivas() {
        return madresActivas;
    }

    public void setMadresActivas(Long madresActivas) {
        this.madresActivas = madresActivas;
    }

    public Long getGestacionesEnCurso() {
        return gestacionesEnCurso;
    }

    public void setGestacionesEnCurso(Long gestacionesEnCurso) {
        this.gestacionesEnCurso = gestacionesEnCurso;
    }

    public Long getMadresEnLactancia() {
        return madresEnLactancia;
    }

    public void setMadresEnLactancia(Long madresEnLactancia) {
        this.madresEnLactancia = madresEnLactancia;
    }

    public Long getPartosPeriodo() {
        return partosPeriodo;
    }

    public void setPartosPeriodo(Long partosPeriodo) {
        this.partosPeriodo = partosPeriodo;
    }

    public Long getDestetesPeriodo() {
        return destetesPeriodo;
    }

    public void setDestetesPeriodo(Long destetesPeriodo) {
        this.destetesPeriodo = destetesPeriodo;
    }
}
