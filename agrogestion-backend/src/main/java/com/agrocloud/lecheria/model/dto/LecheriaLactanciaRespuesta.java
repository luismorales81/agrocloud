package com.agrocloud.lecheria.model.dto;

import java.time.LocalDate;

public class LecheriaLactanciaRespuesta {

    private Long id;
    private Long animalId;
    private Integer numeroLactancia;
    private LocalDate fechaParto;
    private LocalDate fechaSecado;
    private Boolean activa;
    private Integer dim;
    private String observaciones;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAnimalId() {
        return animalId;
    }

    public void setAnimalId(Long animalId) {
        this.animalId = animalId;
    }

    public Integer getNumeroLactancia() {
        return numeroLactancia;
    }

    public void setNumeroLactancia(Integer numeroLactancia) {
        this.numeroLactancia = numeroLactancia;
    }

    public LocalDate getFechaParto() {
        return fechaParto;
    }

    public void setFechaParto(LocalDate fechaParto) {
        this.fechaParto = fechaParto;
    }

    public LocalDate getFechaSecado() {
        return fechaSecado;
    }

    public void setFechaSecado(LocalDate fechaSecado) {
        this.fechaSecado = fechaSecado;
    }

    public Boolean getActiva() {
        return activa;
    }

    public void setActiva(Boolean activa) {
        this.activa = activa;
    }

    public Integer getDim() {
        return dim;
    }

    public void setDim(Integer dim) {
        this.dim = dim;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
