package com.agrocloud.model.dto;

import com.agrocloud.model.entity.Insumo;
import com.agrocloud.model.entity.Insumo.TipoInsumo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO para transferir datos de Insumo sin referencias circulares
 */
public class InsumoDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private TipoInsumo tipo;
    private String unidad;
    private BigDecimal precioUnitario;
    private BigDecimal stockMinimo;
    private BigDecimal stockActual;
    private String proveedor;
    private LocalDate fechaVencimiento;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private Long userId;
    private String userName;

    // Constructor por defecto
    public InsumoDTO() {}

    // Constructor desde entidad
    public InsumoDTO(Insumo insumo) {
        this.id = insumo.getId();
        this.nombre = insumo.getNombre();
        this.descripcion = insumo.getDescripcion();
        this.tipo = insumo.getTipo();
        this.unidad = insumo.getUnidadMedida();
        this.precioUnitario = insumo.getPrecioUnitario();
        this.stockMinimo = insumo.getStockMinimo();
        this.stockActual = insumo.getStockActual();
        this.proveedor = insumo.getProveedor();
        this.fechaVencimiento = insumo.getFechaVencimiento();
        this.activo = insumo.getActivo();
        this.fechaCreacion = insumo.getFechaCreacion();
        this.fechaActualizacion = insumo.getFechaActualizacion();
        
        // Solo información básica del usuario para evitar referencias circulares
        if (insumo.getUser() != null) {
            this.userId = insumo.getUser().getId();
            this.userName = insumo.getUser().getUsername();
        }
    }

    // Getters y Setters
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

    public TipoInsumo getTipo() {
        return tipo;
    }

    public void setTipo(TipoInsumo tipo) {
        this.tipo = tipo;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
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
}
