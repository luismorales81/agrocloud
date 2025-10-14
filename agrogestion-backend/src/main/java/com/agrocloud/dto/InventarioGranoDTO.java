package com.agrocloud.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para transferir datos de inventario de granos.
 */
public class InventarioGranoDTO {
    
    private Long id;
    private Long cosechaId;
    private Long cultivoId;
    private String cultivoNombre;
    private Long loteId;
    private String loteNombre;
    private BigDecimal cantidadInicial;
    private BigDecimal cantidadDisponible;
    private String unidadMedida;
    private BigDecimal costoProduccionTotal;
    private BigDecimal costoUnitario;
    private LocalDate fechaIngreso;
    private String estado;
    private String variedad;
    private String calidad;
    private String ubicacionAlmacenamiento;
    private String observaciones;
    
    // Campos calculados
    private BigDecimal valorTotal; // cantidadDisponible * costoUnitario
    private BigDecimal cantidadVendida; // cantidadInicial - cantidadDisponible
    private BigDecimal porcentajeDisponible; // (disponible / inicial) * 100

    // Constructors
    public InventarioGranoDTO() {}

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCosechaId() {
        return cosechaId;
    }

    public void setCosechaId(Long cosechaId) {
        this.cosechaId = cosechaId;
    }

    public Long getCultivoId() {
        return cultivoId;
    }

    public void setCultivoId(Long cultivoId) {
        this.cultivoId = cultivoId;
    }

    public String getCultivoNombre() {
        return cultivoNombre;
    }

    public void setCultivoNombre(String cultivoNombre) {
        this.cultivoNombre = cultivoNombre;
    }

    public Long getLoteId() {
        return loteId;
    }

    public void setLoteId(Long loteId) {
        this.loteId = loteId;
    }

    public String getLoteNombre() {
        return loteNombre;
    }

    public void setLoteNombre(String loteNombre) {
        this.loteNombre = loteNombre;
    }

    public BigDecimal getCantidadInicial() {
        return cantidadInicial;
    }

    public void setCantidadInicial(BigDecimal cantidadInicial) {
        this.cantidadInicial = cantidadInicial;
    }

    public BigDecimal getCantidadDisponible() {
        return cantidadDisponible;
    }

    public void setCantidadDisponible(BigDecimal cantidadDisponible) {
        this.cantidadDisponible = cantidadDisponible;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public BigDecimal getCostoProduccionTotal() {
        return costoProduccionTotal;
    }

    public void setCostoProduccionTotal(BigDecimal costoProduccionTotal) {
        this.costoProduccionTotal = costoProduccionTotal;
    }

    public BigDecimal getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(BigDecimal costoUnitario) {
        this.costoUnitario = costoUnitario;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getVariedad() {
        return variedad;
    }

    public void setVariedad(String variedad) {
        this.variedad = variedad;
    }

    public String getCalidad() {
        return calidad;
    }

    public void setCalidad(String calidad) {
        this.calidad = calidad;
    }

    public String getUbicacionAlmacenamiento() {
        return ubicacionAlmacenamiento;
    }

    public void setUbicacionAlmacenamiento(String ubicacionAlmacenamiento) {
        this.ubicacionAlmacenamiento = ubicacionAlmacenamiento;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public BigDecimal getCantidadVendida() {
        return cantidadVendida;
    }

    public void setCantidadVendida(BigDecimal cantidadVendida) {
        this.cantidadVendida = cantidadVendida;
    }

    public BigDecimal getPorcentajeDisponible() {
        return porcentajeDisponible;
    }

    public void setPorcentajeDisponible(BigDecimal porcentajeDisponible) {
        this.porcentajeDisponible = porcentajeDisponible;
    }
}

