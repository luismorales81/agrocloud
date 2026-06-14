package com.agrocloud.avicola.ponedoras.model.dto;

import com.agrocloud.avicola.ponedoras.model.enums.AvicolaPonedorasHuevoCategoria;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AvicolaPonedorasVentaHuevosSolicitud {

    private LocalDate fecha;
    private AvicolaPonedorasHuevoCategoria categoriaHuevo;
    private Integer cantidad;
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

    public AvicolaPonedorasHuevoCategoria getCategoriaHuevo() {
        return categoriaHuevo;
    }

    public void setCategoriaHuevo(AvicolaPonedorasHuevoCategoria categoriaHuevo) {
        this.categoriaHuevo = categoriaHuevo;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
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
