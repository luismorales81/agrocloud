package com.agrocloud.lecheria.model.dto;

import com.agrocloud.lecheria.model.enums.LecheriaEspecie;
import com.agrocloud.lecheria.model.enums.LecheriaEstadoAnimal;
import com.agrocloud.lecheria.model.enums.LecheriaSexoAnimal;

import java.time.LocalDate;

public class LecheriaAnimalSolicitud {

    private String identificacion;
    private LecheriaEspecie especie;
    private LecheriaSexoAnimal sexo;
    private LecheriaEstadoAnimal estado;
    private Long razaId;
    private Long rodeoId;
    private LocalDate fechaNacimiento;
    private LocalDate fechaIngreso;
    private String observaciones;

    public String getIdentificacion() {
        return identificacion;
    }

    public void setIdentificacion(String identificacion) {
        this.identificacion = identificacion;
    }

    public LecheriaEspecie getEspecie() {
        return especie;
    }

    public void setEspecie(LecheriaEspecie especie) {
        this.especie = especie;
    }

    public LecheriaSexoAnimal getSexo() {
        return sexo;
    }

    public void setSexo(LecheriaSexoAnimal sexo) {
        this.sexo = sexo;
    }

    public LecheriaEstadoAnimal getEstado() {
        return estado;
    }

    public void setEstado(LecheriaEstadoAnimal estado) {
        this.estado = estado;
    }

    public Long getRazaId() {
        return razaId;
    }

    public void setRazaId(Long razaId) {
        this.razaId = razaId;
    }

    public Long getRodeoId() {
        return rodeoId;
    }

    public void setRodeoId(Long rodeoId) {
        this.rodeoId = rodeoId;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
