package com.agrocloud.porcinos.model.dto;

import com.agrocloud.porcinos.model.enums.PorcinosGestacionEstado;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PorcinosGestacionRespuesta {

    private Long id;
    private Long madreId;
    private String madreCaravana;
    private Long servicioId;
    private LocalDate fechaInicio;
    private LocalDate fechaProbableParto;
    private PorcinosGestacionEstado estado;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMadreId() {
        return madreId;
    }

    public void setMadreId(Long madreId) {
        this.madreId = madreId;
    }

    public String getMadreCaravana() {
        return madreCaravana;
    }

    public void setMadreCaravana(String madreCaravana) {
        this.madreCaravana = madreCaravana;
    }

    public Long getServicioId() {
        return servicioId;
    }

    public void setServicioId(Long servicioId) {
        this.servicioId = servicioId;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaProbableParto() {
        return fechaProbableParto;
    }

    public void setFechaProbableParto(LocalDate fechaProbableParto) {
        this.fechaProbableParto = fechaProbableParto;
    }

    public PorcinosGestacionEstado getEstado() {
        return estado;
    }

    public void setEstado(PorcinosGestacionEstado estado) {
        this.estado = estado;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
