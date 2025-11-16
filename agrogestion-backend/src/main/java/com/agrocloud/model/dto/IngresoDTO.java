package com.agrocloud.model.dto;

import com.agrocloud.config.serializer.MontoMaskingSerializer;
import com.agrocloud.model.entity.Ingreso;
import com.agrocloud.model.entity.Ingreso.TipoIngreso;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO para transferir datos de Ingreso sin referencias circulares
 * Los montos se enmascaran automáticamente en las respuestas
 */
public class IngresoDTO {
    private Long id;
    private String concepto;
    private String descripcion;
    private TipoIngreso tipoIngreso;
    
    @JsonSerialize(using = MontoMaskingSerializer.class)
    private BigDecimal monto;
    private BigDecimal cantidad;
    private String unidadMedida;
    private LocalDate fecha;
    private String clienteComprador;
    private String observaciones;
    private String estado;
    private Long loteId;
    private String loteNombre;
    private Long userId;
    private String userName;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    
    // Constructor por defecto
    public IngresoDTO() {}
    
    // Constructor desde entidad
    public IngresoDTO(Ingreso ingreso) {
        this.id = ingreso.getId();
        this.concepto = ingreso.getConcepto();
        this.descripcion = ingreso.getDescripcion();
        this.tipoIngreso = ingreso.getTipoIngreso();
        this.monto = ingreso.getMonto();
        this.cantidad = ingreso.getCantidad();
        this.unidadMedida = ingreso.getUnidadMedida();
        this.fecha = ingreso.getFecha();
        this.clienteComprador = ingreso.getClienteComprador();
        this.observaciones = ingreso.getObservaciones();
        this.estado = ingreso.getEstado() != null ? ingreso.getEstado().name() : null;
        this.fechaCreacion = ingreso.getFechaCreacion();
        this.fechaActualizacion = ingreso.getFechaActualizacion();
        
        // Información del lote
        if (ingreso.getLote() != null) {
            this.loteId = ingreso.getLote().getId();
            this.loteNombre = ingreso.getLote().getNombre();
        }
        
        // Información del usuario
        if (ingreso.getUser() != null) {
            this.userId = ingreso.getUser().getId();
            this.userName = ingreso.getUser().getUsername();
        }
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getConcepto() {
        return concepto;
    }
    
    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public TipoIngreso getTipoIngreso() {
        return tipoIngreso;
    }
    
    public void setTipoIngreso(TipoIngreso tipoIngreso) {
        this.tipoIngreso = tipoIngreso;
    }
    
    public BigDecimal getMonto() {
        return monto;
    }
    
    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }
    
    public BigDecimal getCantidad() {
        return cantidad;
    }
    
    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }
    
    public String getUnidadMedida() {
        return unidadMedida;
    }
    
    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }
    
    public LocalDate getFecha() {
        return fecha;
    }
    
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
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
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
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
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
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
}
