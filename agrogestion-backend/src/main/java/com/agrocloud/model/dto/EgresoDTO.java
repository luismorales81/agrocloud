package com.agrocloud.model.dto;

import com.agrocloud.config.serializer.MontoMaskingSerializer;
import com.agrocloud.model.entity.Egreso;
import com.agrocloud.model.entity.Egreso.TipoEgreso;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO para transferir datos de Egreso sin referencias circulares
 * Los montos se enmascaran automáticamente en las respuestas
 */
public class EgresoDTO {
    private Long id;
    private String concepto;
    private String descripcion;
    private TipoEgreso tipo;
    private Long referenciaId;
    private BigDecimal cantidad;
    private String unidadMedida;
    
    @JsonSerialize(using = MontoMaskingSerializer.class)
    private BigDecimal costoUnitario;
    
    @JsonSerialize(using = MontoMaskingSerializer.class)
    private BigDecimal costoTotal;
    private LocalDate fecha;
    private String proveedor;
    private String observaciones;
    private String estado;
    private Long loteId;
    private String loteNombre;
    private Long userId;
    private String userName;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    
    // Constructor por defecto
    public EgresoDTO() {}
    
    // Constructor desde entidad
    public EgresoDTO(Egreso egreso) {
        this.id = egreso.getId();
        this.concepto = egreso.getConcepto();
        this.descripcion = egreso.getDescripcion();
        this.tipo = egreso.getTipo();
        this.referenciaId = egreso.getReferenciaId();
        this.cantidad = egreso.getCantidad();
        this.unidadMedida = egreso.getUnidadMedida();
        this.costoUnitario = egreso.getCostoUnitario();
        this.costoTotal = egreso.getCostoTotal();
        this.fecha = egreso.getFecha();
        this.proveedor = egreso.getProveedor();
        this.observaciones = egreso.getObservaciones();
        this.estado = egreso.getEstado() != null ? egreso.getEstado().name() : null;
        this.fechaCreacion = egreso.getFechaCreacion();
        this.fechaActualizacion = egreso.getFechaActualizacion();
        
        // Información del lote
        if (egreso.getLote() != null) {
            this.loteId = egreso.getLote().getId();
            this.loteNombre = egreso.getLote().getNombre();
        }
        
        // Información del usuario
        if (egreso.getUser() != null) {
            this.userId = egreso.getUser().getId();
            this.userName = egreso.getUser().getUsername();
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
    
    public TipoEgreso getTipo() {
        return tipo;
    }
    
    public void setTipo(TipoEgreso tipo) {
        this.tipo = tipo;
    }
    
    public Long getReferenciaId() {
        return referenciaId;
    }
    
    public void setReferenciaId(Long referenciaId) {
        this.referenciaId = referenciaId;
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
    
    public BigDecimal getCostoUnitario() {
        return costoUnitario;
    }
    
    public void setCostoUnitario(BigDecimal costoUnitario) {
        this.costoUnitario = costoUnitario;
    }
    
    public BigDecimal getCostoTotal() {
        return costoTotal;
    }
    
    public void setCostoTotal(BigDecimal costoTotal) {
        this.costoTotal = costoTotal;
    }
    
    public LocalDate getFecha() {
        return fecha;
    }
    
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
    
    public String getProveedor() {
        return proveedor;
    }
    
    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
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
