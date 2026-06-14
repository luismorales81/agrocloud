package com.agrocloud.avicola.ponedoras.model.dto;

import com.agrocloud.avicola.ponedoras.model.enums.AvicolaPonedorasHuevoCategoria;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AvicolaPonedorasVentaHuevosRespuesta {

    private Long id;
    private Long galponId;
    private Long empresaId;
    private LocalDate fecha;
    private AvicolaPonedorasHuevoCategoria categoriaHuevo;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal total;
    private String comprador;
    private String observaciones;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGalponId() {
        return galponId;
    }

    public void setGalponId(Long galponId) {
        this.galponId = galponId;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
