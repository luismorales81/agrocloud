package com.agrocloud.dto;

import com.agrocloud.model.entity.Insumo;
import com.agrocloud.model.enums.TipoAplicacion;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Request DTO para crear un insumo con sus dosis de aplicación
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
public class CrearInsumoConDosisRequest {
    
    // Campos del insumo
    @NotNull(message = "El nombre del insumo es obligatorio")
    private String nombre;
    
    private String descripcion;
    
    @NotNull(message = "El tipo de insumo es obligatorio")
    private Insumo.TipoInsumo tipo;
    
    @NotNull(message = "La unidad de medida es obligatoria")
    private String unidadMedida;
    
    @NotNull(message = "El precio unitario es obligatorio")
    @Positive(message = "El precio unitario debe ser positivo")
    private BigDecimal precioUnitario;
    
    private BigDecimal stockMinimo = BigDecimal.ZERO;
    
    private BigDecimal stockActual = BigDecimal.ZERO;
    
    private String proveedor;
    
    private LocalDate fechaVencimiento;
    
    // Dosis de aplicación (opcional)
    private List<DosisInsumoRequest> dosisAplicaciones = new ArrayList<>();
    
    // Clase interna para las dosis
    public static class DosisInsumoRequest {
        @NotNull(message = "El tipo de aplicación es obligatorio")
        private TipoAplicacion tipoAplicacion;
        
        @NotNull(message = "La dosis por hectárea es obligatoria")
        @Positive(message = "La dosis por hectárea debe ser positiva")
        private BigDecimal dosisPorHa;
        
        private String unidadMedida;
        
        private String descripcion;
        
        // Getters and Setters
        public TipoAplicacion getTipoAplicacion() {
            return tipoAplicacion;
        }
        
        public void setTipoAplicacion(TipoAplicacion tipoAplicacion) {
            this.tipoAplicacion = tipoAplicacion;
        }
        
        public BigDecimal getDosisPorHa() {
            return dosisPorHa;
        }
        
        public void setDosisPorHa(BigDecimal dosisPorHa) {
            this.dosisPorHa = dosisPorHa;
        }
        
        public String getUnidadMedida() {
            return unidadMedida;
        }
        
        public void setUnidadMedida(String unidadMedida) {
            this.unidadMedida = unidadMedida;
        }
        
        public String getDescripcion() {
            return descripcion;
        }
        
        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }
    }
    
    // Getters and Setters
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
    
    public List<DosisInsumoRequest> getDosisAplicaciones() {
        return dosisAplicaciones;
    }
    
    public void setDosisAplicaciones(List<DosisInsumoRequest> dosisAplicaciones) {
        this.dosisAplicaciones = dosisAplicaciones;
    }
}















