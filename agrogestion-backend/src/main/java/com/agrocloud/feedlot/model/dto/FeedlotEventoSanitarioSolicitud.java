package com.agrocloud.feedlot.model.dto;

import com.agrocloud.feedlot.model.enums.FeedlotTipoEventoSanitario;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FeedlotEventoSanitarioSolicitud {

    private LocalDate fecha;
    private FeedlotTipoEventoSanitario tipo;
    private String descripcion;
    private Long insumoId;
    private BigDecimal cantidadInsumo;
    private Integer diasRetiro;
    private String observaciones;

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public FeedlotTipoEventoSanitario getTipo() {
        return tipo;
    }

    public void setTipo(FeedlotTipoEventoSanitario tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Long getInsumoId() {
        return insumoId;
    }

    public void setInsumoId(Long insumoId) {
        this.insumoId = insumoId;
    }

    public BigDecimal getCantidadInsumo() {
        return cantidadInsumo;
    }

    public void setCantidadInsumo(BigDecimal cantidadInsumo) {
        this.cantidadInsumo = cantidadInsumo;
    }

    public Integer getDiasRetiro() {
        return diasRetiro;
    }

    public void setDiasRetiro(Integer diasRetiro) {
        this.diasRetiro = diasRetiro;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
