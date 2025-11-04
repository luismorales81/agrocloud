package com.agrocloud.dto;

import java.math.BigDecimal;

/**
 * DTO para insumos con propiedades específicas de agroquímicos
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
public class InsumoDTO {
    
    private Long id;
    private String nombre;
    private String descripcion;
    private String tipo;
    private String unidadMedida;
    private BigDecimal precioUnitario;
    private BigDecimal stockActual;
    private BigDecimal stockMinimo;
    private String proveedor;
    private String fechaVencimiento;
    private Boolean activo;
    
    // Campos específicos para agroquímicos
    private String principioActivo;
    private String concentracion;
    private String claseQuimica;
    private String categoriaToxicologica;
    private Integer periodoCarenciaDias;
    private BigDecimal dosisMinimaPorHa;
    private BigDecimal dosisMaximaPorHa;
    private String unidadDosis;
    
    // Indicadores de tipo
    private Boolean esAgroquimico;
    private Boolean tienePropiedadesAgroquimicas;

    // Constructors
    public InsumoDTO() {}

    public InsumoDTO(Long id, String nombre, String tipo) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
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

    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
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

    public String getCategoriaToxicologica() {
        return categoriaToxicologica;
    }

    public void setCategoriaToxicologica(String categoriaToxicologica) {
        this.categoriaToxicologica = categoriaToxicologica;
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

    public String getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(String fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public Boolean getEsAgroquimico() {
        return esAgroquimico;
    }

    public void setEsAgroquimico(Boolean esAgroquimico) {
        this.esAgroquimico = esAgroquimico;
    }

    public Boolean getTienePropiedadesAgroquimicas() {
        return tienePropiedadesAgroquimicas;
    }

    public void setTienePropiedadesAgroquimicas(Boolean tienePropiedadesAgroquimicas) {
        this.tienePropiedadesAgroquimicas = tienePropiedadesAgroquimicas;
    }
}
