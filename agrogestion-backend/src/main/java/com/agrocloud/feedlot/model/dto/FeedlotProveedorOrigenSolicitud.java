package com.agrocloud.feedlot.model.dto;

import com.agrocloud.feedlot.model.enums.FeedlotTipoProveedor;

public class FeedlotProveedorOrigenSolicitud {

    private String nombre;
    private FeedlotTipoProveedor tipo;
    private Boolean activo;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public FeedlotTipoProveedor getTipo() {
        return tipo;
    }

    public void setTipo(FeedlotTipoProveedor tipo) {
        this.tipo = tipo;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
