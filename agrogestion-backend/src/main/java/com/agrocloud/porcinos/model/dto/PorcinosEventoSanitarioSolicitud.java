package com.agrocloud.porcinos.model.dto;

import com.agrocloud.porcinos.model.enums.PorcinosEventoSanitarioTipo;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PorcinosEventoSanitarioSolicitud {

    private LocalDate fecha;
    private PorcinosEventoSanitarioTipo tipo;
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

    public PorcinosEventoSanitarioTipo getTipo() {
        return tipo;
    }

    public void setTipo(PorcinosEventoSanitarioTipo tipo) {
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
