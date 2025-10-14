package com.agrocloud.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para solicitar una venta de grano desde el inventario.
 */
public class VentaGranoRequest {
    
    @NotNull(message = "El ID del inventario es obligatorio")
    private Long inventarioId;
    
    @NotNull(message = "La cantidad a vender es obligatoria")
    @Positive(message = "La cantidad debe ser positiva")
    private BigDecimal cantidadVender;
    
    @NotNull(message = "El precio unitario es obligatorio")
    @Positive(message = "El precio debe ser positivo")
    private BigDecimal precioVentaUnitario;
    
    @NotNull(message = "La fecha de venta es obligatoria")
    private LocalDate fechaVenta;
    
    private String clienteComprador;
    
    private String observaciones;

    // Constructors
    public VentaGranoRequest() {}

    public VentaGranoRequest(Long inventarioId, BigDecimal cantidadVender, 
                            BigDecimal precioVentaUnitario, LocalDate fechaVenta, 
                            String clienteComprador) {
        this.inventarioId = inventarioId;
        this.cantidadVender = cantidadVender;
        this.precioVentaUnitario = precioVentaUnitario;
        this.fechaVenta = fechaVenta;
        this.clienteComprador = clienteComprador;
    }

    // Getters y Setters
    public Long getInventarioId() {
        return inventarioId;
    }

    public void setInventarioId(Long inventarioId) {
        this.inventarioId = inventarioId;
    }

    public BigDecimal getCantidadVender() {
        return cantidadVender;
    }

    public void setCantidadVender(BigDecimal cantidadVender) {
        this.cantidadVender = cantidadVender;
    }

    public BigDecimal getPrecioVentaUnitario() {
        return precioVentaUnitario;
    }

    public void setPrecioVentaUnitario(BigDecimal precioVentaUnitario) {
        this.precioVentaUnitario = precioVentaUnitario;
    }

    public LocalDate getFechaVenta() {
        return fechaVenta;
    }

    public void setFechaVenta(LocalDate fechaVenta) {
        this.fechaVenta = fechaVenta;
    }

    public String getClienteComprador() {
        return clienteComprador;
    }

    public void setClienteComprador(String clienteComprador) {
        this.clienteComprador = clienteComprador;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}

