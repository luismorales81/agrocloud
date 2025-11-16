package com.agrocloud.dto;

import com.agrocloud.config.serializer.CuitMaskingSerializer;
import com.agrocloud.config.serializer.EmailMaskingSerializer;
import com.agrocloud.model.enums.EstadoEmpresa;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO para transferir datos de empresa sin referencias circulares
 * Los datos sensibles se enmascaran automáticamente en las respuestas
 */
public class EmpresaDTO {
    
    private Long id;
    private String nombre;
    
    @JsonSerialize(using = CuitMaskingSerializer.class)
    private String cuit;
    
    @JsonSerialize(using = EmailMaskingSerializer.class)
    private String emailContacto;
    private String telefonoContacto;
    private String direccion;
    private EstadoEmpresa estado;
    private Boolean activo;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaInicioTrial;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaFinTrial;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaCreacion;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaActualizacion;
    
    // Constructores
    public EmpresaDTO() {}
    
    public EmpresaDTO(Long id, String nombre, String cuit, String emailContacto, 
                     String telefonoContacto, String direccion, EstadoEmpresa estado, 
                     Boolean activo, LocalDate fechaInicioTrial, LocalDate fechaFinTrial,
                     LocalDateTime fechaCreacion, LocalDateTime fechaActualizacion) {
        this.id = id;
        this.nombre = nombre;
        this.cuit = cuit;
        this.emailContacto = emailContacto;
        this.telefonoContacto = telefonoContacto;
        this.direccion = direccion;
        this.estado = estado;
        this.activo = activo;
        this.fechaInicioTrial = fechaInicioTrial;
        this.fechaFinTrial = fechaFinTrial;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
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
    
    public String getCuit() {
        return cuit;
    }
    
    public void setCuit(String cuit) {
        this.cuit = cuit;
    }
    
    public String getEmailContacto() {
        return emailContacto;
    }
    
    public void setEmailContacto(String emailContacto) {
        this.emailContacto = emailContacto;
    }
    
    public String getTelefonoContacto() {
        return telefonoContacto;
    }
    
    public void setTelefonoContacto(String telefonoContacto) {
        this.telefonoContacto = telefonoContacto;
    }
    
    public String getDireccion() {
        return direccion;
    }
    
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
    
    public EstadoEmpresa getEstado() {
        return estado;
    }
    
    public void setEstado(EstadoEmpresa estado) {
        this.estado = estado;
    }
    
    public Boolean getActivo() {
        return activo;
    }
    
    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
    
    public LocalDate getFechaInicioTrial() {
        return fechaInicioTrial;
    }
    
    public void setFechaInicioTrial(LocalDate fechaInicioTrial) {
        this.fechaInicioTrial = fechaInicioTrial;
    }
    
    public LocalDate getFechaFinTrial() {
        return fechaFinTrial;
    }
    
    public void setFechaFinTrial(LocalDate fechaFinTrial) {
        this.fechaFinTrial = fechaFinTrial;
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
