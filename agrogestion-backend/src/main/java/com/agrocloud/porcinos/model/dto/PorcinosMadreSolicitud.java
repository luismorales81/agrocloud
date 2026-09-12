package com.agrocloud.porcinos.model.dto;

import com.agrocloud.porcinos.model.enums.PorcinosMadreEstado;

import java.time.LocalDate;

public class PorcinosMadreSolicitud {

    private String caravana;
    private Long razaId;
    private Long galponId;
    private PorcinosMadreEstado estado;
    private LocalDate fechaIngreso;
    private LocalDate fechaNacimiento;
    private Boolean activo;

    public String getCaravana() {
        return caravana;
    }

    public void setCaravana(String caravana) {
        this.caravana = caravana;
    }

    public Long getRazaId() {
        return razaId;
    }

    public void setRazaId(Long razaId) {
        this.razaId = razaId;
    }

    public Long getGalponId() {
        return galponId;
    }

    public void setGalponId(Long galponId) {
        this.galponId = galponId;
    }

    public PorcinosMadreEstado getEstado() {
        return estado;
    }

    public void setEstado(PorcinosMadreEstado estado) {
        this.estado = estado;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
