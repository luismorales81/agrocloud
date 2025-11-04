package com.agrocloud.dto;

import com.agrocloud.model.entity.Insumo;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO para Insumo con sus dosis de aplicación incluidas
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
public class InsumoConDosisDTO {
    
    private Long id;
    private String nombre;
    private String descripcion;
    private Insumo.TipoInsumo tipo;
    private String unidadMedida;
    private BigDecimal precioUnitario;
    private BigDecimal stockMinimo;
    private BigDecimal stockActual;
    private String proveedor;
    private LocalDate fechaVencimiento;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    
    // Dosis de aplicación
    private List<DosisAplicacionDTO> dosisAplicaciones = new ArrayList<>();
    
    // Sugerencia de configuración
    private Boolean tieneDosisConfiguradas = false;
    private String mensajeSugerencia;
    
    // Constructors
    public InsumoConDosisDTO() {}
    
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
    
    public Insumo.TipoInsumo getTipo() {
        return tipo;
    }
    
    public void setTipo(Insumo.TipoInsumo tipo) {
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
    
    public BigDecimal getStockMinimo() {
        return stockMinimo;
    }
    
    public void setStockMinimo(BigDecimal stockMinimo) {
        this.stockMinimo = stockMinimo;
    }
    
    public BigDecimal getStockActual() {
        return stockActual;
    }
    
    public void setStockActual(BigDecimal stockActual) {
        this.stockActual = stockActual;
    }
    
    public String getProveedor() {
        return proveedor;
    }
    
    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }
    
    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }
    
    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
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
    
    public List<DosisAplicacionDTO> getDosisAplicaciones() {
        return dosisAplicaciones;
    }
    
    public void setDosisAplicaciones(List<DosisAplicacionDTO> dosisAplicaciones) {
        this.dosisAplicaciones = dosisAplicaciones;
    }
    
    public Boolean getTieneDosisConfiguradas() {
        return tieneDosisConfiguradas;
    }
    
    public void setTieneDosisConfiguradas(Boolean tieneDosisConfiguradas) {
        this.tieneDosisConfiguradas = tieneDosisConfiguradas;
    }
    
    public String getMensajeSugerencia() {
        return mensajeSugerencia;
    }
    
    public void setMensajeSugerencia(String mensajeSugerencia) {
        this.mensajeSugerencia = mensajeSugerencia;
    }
}











