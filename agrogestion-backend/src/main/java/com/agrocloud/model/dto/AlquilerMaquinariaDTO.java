package com.agrocloud.model.dto;

import com.agrocloud.model.entity.AlquilerMaquinaria;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO para transferir datos de AlquilerMaquinaria sin referencias circulares
 */
public class AlquilerMaquinariaDTO {
    private Long id;
    private Long maquinariaId;
    private String maquinariaNombre;
    private String maquinariaMarca;
    private String maquinariaModelo;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private BigDecimal costoDia;
    private BigDecimal costoTotal;
    private Long loteId;
    private String loteNombre;
    private String observaciones;
    private Long userId;
    private String userName;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    
    // Constructor por defecto
    public AlquilerMaquinariaDTO() {}
    
    // Constructor desde entidad
    public AlquilerMaquinariaDTO(AlquilerMaquinaria alquiler) {
        this.id = alquiler.getId();
        this.fechaInicio = alquiler.getFechaInicio();
        this.fechaFin = alquiler.getFechaFin();
        this.costoDia = alquiler.getCostoDia();
        this.costoTotal = alquiler.getCostoTotal();
        this.observaciones = alquiler.getObservaciones();
        this.fechaCreacion = alquiler.getFechaCreacion();
        this.fechaActualizacion = alquiler.getFechaActualizacion();
        
        // Información de la maquinaria
        if (alquiler.getMaquinaria() != null) {
            this.maquinariaId = alquiler.getMaquinaria().getId();
            this.maquinariaNombre = alquiler.getMaquinaria().getNombre();
            this.maquinariaMarca = alquiler.getMaquinaria().getMarca();
            this.maquinariaModelo = alquiler.getMaquinaria().getModelo();
        }
        
        // Información del lote
        if (alquiler.getLote() != null) {
            this.loteId = alquiler.getLote().getId();
            this.loteNombre = alquiler.getLote().getNombre();
        }
        
        // Información del usuario
        if (alquiler.getUser() != null) {
            this.userId = alquiler.getUser().getId();
            this.userName = alquiler.getUser().getUsername();
        }
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getMaquinariaId() {
        return maquinariaId;
    }
    
    public void setMaquinariaId(Long maquinariaId) {
        this.maquinariaId = maquinariaId;
    }
    
    public String getMaquinariaNombre() {
        return maquinariaNombre;
    }
    
    public void setMaquinariaNombre(String maquinariaNombre) {
        this.maquinariaNombre = maquinariaNombre;
    }
    
    public String getMaquinariaMarca() {
        return maquinariaMarca;
    }
    
    public void setMaquinariaMarca(String maquinariaMarca) {
        this.maquinariaMarca = maquinariaMarca;
    }
    
    public String getMaquinariaModelo() {
        return maquinariaModelo;
    }
    
    public void setMaquinariaModelo(String maquinariaModelo) {
        this.maquinariaModelo = maquinariaModelo;
    }
    
    public LocalDate getFechaInicio() {
        return fechaInicio;
    }
    
    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }
    
    public LocalDate getFechaFin() {
        return fechaFin;
    }
    
    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }
    
    public BigDecimal getCostoDia() {
        return costoDia;
    }
    
    public void setCostoDia(BigDecimal costoDia) {
        this.costoDia = costoDia;
    }
    
    public BigDecimal getCostoTotal() {
        return costoTotal;
    }
    
    public void setCostoTotal(BigDecimal costoTotal) {
        this.costoTotal = costoTotal;
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
    
    public String getObservaciones() {
        return observaciones;
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
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
    
    // Método para obtener nombre completo de la maquinaria
    public String getMaquinariaNombreCompleto() {
        StringBuilder nombreCompleto = new StringBuilder(maquinariaNombre);
        if (maquinariaMarca != null && !maquinariaMarca.trim().isEmpty()) {
            nombreCompleto.append(" - ").append(maquinariaMarca);
        }
        if (maquinariaModelo != null && !maquinariaModelo.trim().isEmpty()) {
            nombreCompleto.append(" ").append(maquinariaModelo);
        }
        return nombreCompleto.toString();
    }
    
    // Método para obtener la duración en días
    public long getDuracionDias() {
        if (this.fechaInicio != null && this.fechaFin != null) {
            return java.time.temporal.ChronoUnit.DAYS.between(this.fechaInicio, this.fechaFin) + 1;
        }
        return 0;
    }
    
    // Método para verificar si el alquiler está activo
    public boolean isActivo() {
        LocalDate hoy = LocalDate.now();
        return this.fechaInicio != null && this.fechaFin != null &&
               !hoy.isBefore(this.fechaInicio) && !hoy.isAfter(this.fechaFin);
    }
}
