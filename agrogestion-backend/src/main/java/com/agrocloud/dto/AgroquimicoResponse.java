package com.agrocloud.dto;

import com.agrocloud.model.enums.FormaAplicacion;
import com.agrocloud.model.enums.TipoAgroquimico;
import com.agrocloud.model.enums.UnidadMedida;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO para response de agroquímicos
 * 
 * @author AgroGestion Team
 * @version 2.0.0
 */
public class AgroquimicoResponse {

    private Long id;
    private String nombreComercial;
    private String principioActivo;
    private TipoAgroquimico tipo;
    private FormaAplicacion formaAplicacion;
    private BigDecimal dosisPorHa;
    private UnidadMedida unidadDeMedida;
    private LocalDate fechaVencimiento;
    private BigDecimal stockActual;
    private BigDecimal stockMinimo;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    
    // Campos calculados
    private Boolean estaVencido;
    private Boolean estaPorVencer;
    private Boolean warningStock;

    // Constructors
    public AgroquimicoResponse() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreComercial() {
        return nombreComercial;
    }

    public void setNombreComercial(String nombreComercial) {
        this.nombreComercial = nombreComercial;
    }

    public String getPrincipioActivo() {
        return principioActivo;
    }

    public void setPrincipioActivo(String principioActivo) {
        this.principioActivo = principioActivo;
    }

    public TipoAgroquimico getTipo() {
        return tipo;
    }

    public void setTipo(TipoAgroquimico tipo) {
        this.tipo = tipo;
    }

    public FormaAplicacion getFormaAplicacion() {
        return formaAplicacion;
    }

    public void setFormaAplicacion(FormaAplicacion formaAplicacion) {
        this.formaAplicacion = formaAplicacion;
    }

    public BigDecimal getDosisPorHa() {
        return dosisPorHa;
    }

    public void setDosisPorHa(BigDecimal dosisPorHa) {
        this.dosisPorHa = dosisPorHa;
    }

    public UnidadMedida getUnidadDeMedida() {
        return unidadDeMedida;
    }

    public void setUnidadDeMedida(UnidadMedida unidadDeMedida) {
        this.unidadDeMedida = unidadDeMedida;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public BigDecimal getStockActual() {
        return stockActual;
    }

    public void setStockActual(BigDecimal stockActual) {
        this.stockActual = stockActual;
    }

    public BigDecimal getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(BigDecimal stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public Boolean getEstaVencido() {
        return estaVencido;
    }

    public void setEstaVencido(Boolean estaVencido) {
        this.estaVencido = estaVencido;
    }

    public Boolean getEstaPorVencer() {
        return estaPorVencer;
    }

    public void setEstaPorVencer(Boolean estaPorVencer) {
        this.estaPorVencer = estaPorVencer;
    }

    public Boolean getWarningStock() {
        return warningStock;
    }

    public void setWarningStock(Boolean warningStock) {
        this.warningStock = warningStock;
    }
}
