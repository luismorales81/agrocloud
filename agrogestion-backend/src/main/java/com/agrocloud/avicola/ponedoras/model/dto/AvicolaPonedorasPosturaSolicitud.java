package com.agrocloud.avicola.ponedoras.model.dto;

import com.agrocloud.avicola.ponedoras.model.enums.AvicolaPonedorasHuevoCategoria;

import java.time.LocalDate;

public class AvicolaPonedorasPosturaSolicitud {

    private LocalDate fecha;
    private AvicolaPonedorasHuevoCategoria categoriaHuevo;
    private Integer cantidad;
    private String observaciones;

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public AvicolaPonedorasHuevoCategoria getCategoriaHuevo() {
        return categoriaHuevo;
    }

    public void setCategoriaHuevo(AvicolaPonedorasHuevoCategoria categoriaHuevo) {
        this.categoriaHuevo = categoriaHuevo;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
