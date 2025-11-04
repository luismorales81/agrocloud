package com.agrocloud.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para crear un nuevo agroquímico con sus dosis por tipo de aplicación
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
public class CrearAgroquimicoRequest {

    @NotBlank(message = "El nombre del insumo es obligatorio")
    private String nombreInsumo;

    private String descripcionInsumo;

    @NotBlank(message = "La unidad de medida es obligatoria")
    private String unidadMedida;

    @NotNull(message = "El precio unitario es obligatorio")
    @Positive(message = "El precio unitario debe ser positivo")
    private BigDecimal precioUnitario;

    @NotNull(message = "El stock mínimo es obligatorio")
    @Positive(message = "El stock mínimo debe ser positivo")
    private BigDecimal stockMinimo;

    @NotNull(message = "El stock inicial es obligatorio")
    @Positive(message = "El stock inicial debe ser positivo")
    private BigDecimal stockInicial;

    private String proveedor;

    // Datos específicos del agroquímico
    @NotBlank(message = "El principio activo es obligatorio")
    private String principioActivo;

    private String concentracion;

    @NotBlank(message = "La clase química es obligatoria")
    private String claseQuimica;

    private String modoAccion;

    private Integer periodoCarenciaDias;

    private BigDecimal dosisMinimaPorHa;

    private BigDecimal dosisMaximaPorHa;

    private String unidadDosis;

    private String observaciones;

    // Dosis por tipo de aplicación
    private List<DosisTipoAplicacionRequest> dosisPorTipoAplicacion;

    // Getters and Setters
    public String getNombreInsumo() {
        return nombreInsumo;
    }

    public void setNombreInsumo(String nombreInsumo) {
        this.nombreInsumo = nombreInsumo;
    }

    public String getDescripcionInsumo() {
        return descripcionInsumo;
    }

    public void setDescripcionInsumo(String descripcionInsumo) {
        this.descripcionInsumo = descripcionInsumo;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public BigDecimal getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(BigDecimal stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public BigDecimal getStockInicial() {
        return stockInicial;
    }

    public void setStockInicial(BigDecimal stockInicial) {
        this.stockInicial = stockInicial;
    }

    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }

    public String getPrincipioActivo() {
        return principioActivo;
    }

    public void setPrincipioActivo(String principioActivo) {
        this.principioActivo = principioActivo;
    }

    public String getConcentracion() {
        return concentracion;
    }

    public void setConcentracion(String concentracion) {
        this.concentracion = concentracion;
    }

    public String getClaseQuimica() {
        return claseQuimica;
    }

    public void setClaseQuimica(String claseQuimica) {
        this.claseQuimica = claseQuimica;
    }

    public String getModoAccion() {
        return modoAccion;
    }

    public void setModoAccion(String modoAccion) {
        this.modoAccion = modoAccion;
    }

    public Integer getPeriodoCarenciaDias() {
        return periodoCarenciaDias;
    }

    public void setPeriodoCarenciaDias(Integer periodoCarenciaDias) {
        this.periodoCarenciaDias = periodoCarenciaDias;
    }

    public BigDecimal getDosisMinimaPorHa() {
        return dosisMinimaPorHa;
    }

    public void setDosisMinimaPorHa(BigDecimal dosisMinimaPorHa) {
        this.dosisMinimaPorHa = dosisMinimaPorHa;
    }

    public BigDecimal getDosisMaximaPorHa() {
        return dosisMaximaPorHa;
    }

    public void setDosisMaximaPorHa(BigDecimal dosisMaximaPorHa) {
        this.dosisMaximaPorHa = dosisMaximaPorHa;
    }

    public String getUnidadDosis() {
        return unidadDosis;
    }

    public void setUnidadDosis(String unidadDosis) {
        this.unidadDosis = unidadDosis;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public List<DosisTipoAplicacionRequest> getDosisPorTipoAplicacion() {
        return dosisPorTipoAplicacion;
    }

    public void setDosisPorTipoAplicacion(List<DosisTipoAplicacionRequest> dosisPorTipoAplicacion) {
        this.dosisPorTipoAplicacion = dosisPorTipoAplicacion;
    }
}



