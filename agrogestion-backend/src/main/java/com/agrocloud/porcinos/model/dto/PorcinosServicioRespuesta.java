package com.agrocloud.porcinos.model.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PorcinosServicioRespuesta {

    private Long id;
    private Long madreId;
    private String madreCaravana;
    private Long padrilloId;
    private String padrilloNombre;
    private Long tipoServicioId;
    private String tipoServicioNombre;
    private LocalDate fecha;
    private String observaciones;
    private Long gestacionId;
    private LocalDate fechaProbableParto;
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

    public Long getPadrilloId() {
        return padrilloId;
    }

    public void setPadrilloId(Long padrilloId) {
        this.padrilloId = padrilloId;
    }

    public String getPadrilloNombre() {
        return padrilloNombre;
    }

    public void setPadrilloNombre(String padrilloNombre) {
        this.padrilloNombre = padrilloNombre;
    }

    public Long getTipoServicioId() {
        return tipoServicioId;
    }

    public void setTipoServicioId(Long tipoServicioId) {
        this.tipoServicioId = tipoServicioId;
    }

    public String getTipoServicioNombre() {
        return tipoServicioNombre;
    }

    public void setTipoServicioNombre(String tipoServicioNombre) {
        this.tipoServicioNombre = tipoServicioNombre;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Long getGestacionId() {
        return gestacionId;
    }

    public void setGestacionId(Long gestacionId) {
        this.gestacionId = gestacionId;
    }

    public LocalDate getFechaProbableParto() {
        return fechaProbableParto;
    }

    public void setFechaProbableParto(LocalDate fechaProbableParto) {
        this.fechaProbableParto = fechaProbableParto;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
