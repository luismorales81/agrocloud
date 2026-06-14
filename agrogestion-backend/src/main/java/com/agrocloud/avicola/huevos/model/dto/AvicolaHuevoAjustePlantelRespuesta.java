package com.agrocloud.avicola.huevos.model.dto;

import java.time.LocalDateTime;

public class AvicolaHuevoAjustePlantelRespuesta {

    private Long id;
    private Long loteId;
    private LocalDateTime fechaHora;
    private Integer cantidadAvesAnterior;
    private Integer cantidadAvesNueva;
    private String motivo;
    private Long usuarioId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLoteId() {
        return loteId;
    }

    public void setLoteId(Long loteId) {
        this.loteId = loteId;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public Integer getCantidadAvesAnterior() {
        return cantidadAvesAnterior;
    }

    public void setCantidadAvesAnterior(Integer cantidadAvesAnterior) {
        this.cantidadAvesAnterior = cantidadAvesAnterior;
    }

    public Integer getCantidadAvesNueva() {
        return cantidadAvesNueva;
    }

    public void setCantidadAvesNueva(Integer cantidadAvesNueva) {
        this.cantidadAvesNueva = cantidadAvesNueva;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }
}
