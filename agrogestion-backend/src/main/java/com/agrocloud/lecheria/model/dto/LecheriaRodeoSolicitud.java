package com.agrocloud.lecheria.model.dto;

import com.agrocloud.lecheria.model.enums.LecheriaEspecie;

public class LecheriaRodeoSolicitud {

    private String nombre;
    private LecheriaEspecie especie;
    private Boolean activo;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LecheriaEspecie getEspecie() {
        return especie;
    }

    public void setEspecie(LecheriaEspecie especie) {
        this.especie = especie;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
