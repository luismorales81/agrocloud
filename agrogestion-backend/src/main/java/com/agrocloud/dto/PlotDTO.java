package com.agrocloud.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO para la entidad Plot (Lote) sin relaciones lazy.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
public class PlotDTO {
    
    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal areaHectareas;
    private String estado;
    private String tipoSuelo;
    private String cultivoActual;
    private LocalDate fechaSiembra;
    private LocalDate fechaCosechaEsperada;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private Long campoId;
    private Long userId;
    
    // Constructores
    public PlotDTO() {}
    
    public PlotDTO(Long id, String nombre, String descripcion, BigDecimal areaHectareas, 
                   String estado, String tipoSuelo, Boolean activo, 
                   LocalDateTime fechaCreacion, LocalDateTime fechaActualizacion,
                   Long campoId, Long userId) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.areaHectareas = areaHectareas;
        this.estado = estado;
        this.tipoSuelo = tipoSuelo;
        this.activo = activo;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
        this.campoId = campoId;
        this.userId = userId;
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
    
    public BigDecimal getAreaHectareas() {
        return areaHectareas;
    }
    
    public void setAreaHectareas(BigDecimal areaHectareas) {
        this.areaHectareas = areaHectareas;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    public String getTipoSuelo() {
        return tipoSuelo;
    }
    
    public void setTipoSuelo(String tipoSuelo) {
        this.tipoSuelo = tipoSuelo;
    }
    
    public String getCultivoActual() {
        return cultivoActual;
    }
    
    public void setCultivoActual(String cultivoActual) {
        this.cultivoActual = cultivoActual;
    }
    
    public LocalDate getFechaSiembra() {
        return fechaSiembra;
    }
    
    public void setFechaSiembra(LocalDate fechaSiembra) {
        this.fechaSiembra = fechaSiembra;
    }
    
    public LocalDate getFechaCosechaEsperada() {
        return fechaCosechaEsperada;
    }
    
    public void setFechaCosechaEsperada(LocalDate fechaCosechaEsperada) {
        this.fechaCosechaEsperada = fechaCosechaEsperada;
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
    
    public Long getCampoId() {
        return campoId;
    }
    
    public void setCampoId(Long campoId) {
        this.campoId = campoId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
}

