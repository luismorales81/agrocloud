package com.agrocloud.avicola.crianza.model.dto;

import com.agrocloud.avicola.crianza.model.enums.AvicolaVentaTipo;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Datos para registrar una venta o faena.
 */
public class AvicolaVentaSolicitud {

    private LocalDate fecha;
    private AvicolaVentaTipo tipo;
    private Integer cantidad;
    private BigDecimal pesoPromedio;
    private BigDecimal precioUnitario;
    private BigDecimal total;
    private String comprador;
    private String observaciones;

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public AvicolaVentaTipo getTipo() {
        return tipo;
    }

    public void setTipo(AvicolaVentaTipo tipo) {
        this.tipo = tipo;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPesoPromedio() {
        return pesoPromedio;
    }

    public void setPesoPromedio(BigDecimal pesoPromedio) {
        this.pesoPromedio = pesoPromedio;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
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
