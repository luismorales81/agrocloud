package com.agrocloud.lecheria.model.dto;

import com.agrocloud.lecheria.model.enums.LecheriaTipoEventoSanitario;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LecheriaEventoSanitarioSolicitud {

    private LecheriaTipoEventoSanitario tipo;
    private LocalDate fecha;
    private String descripcion;
    private Long insumoId;
    private BigDecimal cantidad;
    private Integer diasRetiro;

    public LecheriaTipoEventoSanitario getTipo() {
        return tipo;
    }

    public void setTipo(LecheriaTipoEventoSanitario tipo) {
        this.tipo = tipo;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
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

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public Integer getDiasRetiro() {
        return diasRetiro;
    }

    public void setDiasRetiro(Integer diasRetiro) {
        this.diasRetiro = diasRetiro;
    }
}
