package com.agrocloud.avicola.huevos.model.dto;

import java.time.LocalDate;
import java.util.List;

public class AvicolaHuevoLoteSolicitud {

    private Long establecimientoId;
    private Long razaId;
    private List<Long> razaIds;
    private String nombre;
    private LocalDate fechaInicio;
    private Integer cantidadAvesInicial;
    private Integer cantidadAvesActual;
    private String observaciones;

    public Long getEstablecimientoId() {
        return establecimientoId;
    }

    public void setEstablecimientoId(Long establecimientoId) {
        this.establecimientoId = establecimientoId;
    }

    public Long getRazaId() {
        return razaId;
    }

    public void setRazaId(Long razaId) {
        this.razaId = razaId;
    }

    public List<Long> getRazaIds() {
        return razaIds;
    }

    public void setRazaIds(List<Long> razaIds) {
        this.razaIds = razaIds;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Integer getCantidadAvesInicial() {
        return cantidadAvesInicial;
    }

    public void setCantidadAvesInicial(Integer cantidadAvesInicial) {
        this.cantidadAvesInicial = cantidadAvesInicial;
    }

    public Integer getCantidadAvesActual() {
        return cantidadAvesActual;
    }

    public void setCantidadAvesActual(Integer cantidadAvesActual) {
        this.cantidadAvesActual = cantidadAvesActual;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
