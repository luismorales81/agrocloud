package com.agrocloud.avicola.ponedoras.model.dto;

import com.agrocloud.avicola.ponedoras.model.enums.AvicolaPonedorasGalponEstado;

import java.time.LocalDate;

/**
 * Alta o edición de galpón (empresa desde contexto de seguridad, no en body).
 */
public class AvicolaPonedorasGalponSolicitud {

    private Long establecimientoId;
    private String nombre;
    private String raza;
    private LocalDate fechaIngreso;
    private Integer cantidadInicial;
    private Integer cantidadAves;
    private AvicolaPonedorasGalponEstado estado;
    private LocalDate fechaCierre;
    private String observaciones;

    public Long getEstablecimientoId() {
        return establecimientoId;
    }

    public void setEstablecimientoId(Long establecimientoId) {
        this.establecimientoId = establecimientoId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRaza() {
        return raza;
    }

    public void setRaza(String raza) {
        this.raza = raza;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public Integer getCantidadInicial() {
        return cantidadInicial;
    }

    public void setCantidadInicial(Integer cantidadInicial) {
        this.cantidadInicial = cantidadInicial;
    }

    public Integer getCantidadAves() {
        return cantidadAves;
    }

    public void setCantidadAves(Integer cantidadAves) {
        this.cantidadAves = cantidadAves;
    }

    public AvicolaPonedorasGalponEstado getEstado() {
        return estado;
    }

    public void setEstado(AvicolaPonedorasGalponEstado estado) {
        this.estado = estado;
    }

    public LocalDate getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(LocalDate fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
