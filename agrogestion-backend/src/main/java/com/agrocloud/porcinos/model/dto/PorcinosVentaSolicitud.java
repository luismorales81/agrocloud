package com.agrocloud.porcinos.model.dto;

import com.agrocloud.porcinos.model.enums.PorcinosVentaTipo;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PorcinosVentaSolicitud {

    private LocalDate fecha;
    private PorcinosVentaTipo tipo;
    private Integer cabezas;
    private BigDecimal pesoPromedioKg;
    private BigDecimal precioKg;
    private BigDecimal total;
    private String comprador;
    private String observaciones;

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public PorcinosVentaTipo getTipo() {
        return tipo;
    }

    public void setTipo(PorcinosVentaTipo tipo) {
        this.tipo = tipo;
    }

    public Integer getCabezas() {
        return cabezas;
    }

    public void setCabezas(Integer cabezas) {
        this.cabezas = cabezas;
    }

    public BigDecimal getPesoPromedioKg() {
        return pesoPromedioKg;
    }

    public void setPesoPromedioKg(BigDecimal pesoPromedioKg) {
        this.pesoPromedioKg = pesoPromedioKg;
    }

    public BigDecimal getPrecioKg() {
        return precioKg;
    }

    public void setPrecioKg(BigDecimal precioKg) {
        this.precioKg = precioKg;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getComprador() {
        return comprador;
    }

    public void setComprador(String comprador) {
        this.comprador = comprador;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
