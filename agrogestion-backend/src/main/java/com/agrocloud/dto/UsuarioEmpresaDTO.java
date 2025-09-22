package com.agrocloud.dto;

import com.agrocloud.model.enums.EstadoUsuarioEmpresa;
import com.agrocloud.model.enums.RolEmpresa;
import java.time.LocalDate;

/**
 * DTO para la relación usuario-empresa
 */
public class UsuarioEmpresaDTO {
    
    private Long id;
    private Long usuarioId;
    private String usuarioEmail;
    private String usuarioNombre;
    private Long empresaId;
    private String empresaNombre;
    private String empresaCuit;
    private String empresaEmail;
    private RolEmpresa rol;
    private EstadoUsuarioEmpresa estado;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Long creadoPorId;
    private String creadoPorEmail;
    
    // Constructor
    public UsuarioEmpresaDTO() {}
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUsuarioId() {
        return usuarioId;
    }
    
    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }
    
    public String getUsuarioEmail() {
        return usuarioEmail;
    }
    
    public void setUsuarioEmail(String usuarioEmail) {
        this.usuarioEmail = usuarioEmail;
    }
    
    public String getUsuarioNombre() {
        return usuarioNombre;
    }
    
    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }
    
    public Long getEmpresaId() {
        return empresaId;
    }
    
    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }
    
    public String getEmpresaNombre() {
        return empresaNombre;
    }
    
    public void setEmpresaNombre(String empresaNombre) {
        this.empresaNombre = empresaNombre;
    }
    
    public String getEmpresaCuit() {
        return empresaCuit;
    }
    
    public void setEmpresaCuit(String empresaCuit) {
        this.empresaCuit = empresaCuit;
    }
    
    public String getEmpresaEmail() {
        return empresaEmail;
    }
    
    public void setEmpresaEmail(String empresaEmail) {
        this.empresaEmail = empresaEmail;
    }
    
    public RolEmpresa getRol() {
        return rol;
    }
    
    public void setRol(RolEmpresa rol) {
        this.rol = rol;
    }
    
    public EstadoUsuarioEmpresa getEstado() {
        return estado;
    }
    
    public void setEstado(EstadoUsuarioEmpresa estado) {
        this.estado = estado;
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
    
    public Long getCreadoPorId() {
        return creadoPorId;
    }
    
    public void setCreadoPorId(Long creadoPorId) {
        this.creadoPorId = creadoPorId;
    }
    
    public String getCreadoPorEmail() {
        return creadoPorEmail;
    }
    
    public void setCreadoPorEmail(String creadoPorEmail) {
        this.creadoPorEmail = creadoPorEmail;
    }
    
    @Override
    public String toString() {
        return "UsuarioEmpresaDTO{" +
                "id=" + id +
                ", usuarioId=" + usuarioId +
                ", usuarioEmail='" + usuarioEmail + '\'' +
                ", usuarioNombre='" + usuarioNombre + '\'' +
                ", empresaId=" + empresaId +
                ", empresaNombre='" + empresaNombre + '\'' +
                ", empresaCuit='" + empresaCuit + '\'' +
                ", empresaEmail='" + empresaEmail + '\'' +
                ", rol=" + rol +
                ", estado=" + estado +
                '}';
    }
}