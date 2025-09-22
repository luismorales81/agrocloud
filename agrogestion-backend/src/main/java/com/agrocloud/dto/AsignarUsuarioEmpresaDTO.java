package com.agrocloud.dto;

import com.agrocloud.model.enums.RolEmpresa;

/**
 * DTO para asignar un usuario a una empresa
 */
public class AsignarUsuarioEmpresaDTO {
    
    private Long usuarioId;
    private Long empresaId;
    private RolEmpresa rol;
    
    // Constructors
    public AsignarUsuarioEmpresaDTO() {}
    
    public AsignarUsuarioEmpresaDTO(Long usuarioId, Long empresaId, RolEmpresa rol) {
        this.usuarioId = usuarioId;
        this.empresaId = empresaId;
        this.rol = rol;
    }
    
    // Getters and Setters
    public Long getUsuarioId() {
        return usuarioId;
    }
    
    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }
    
    public Long getEmpresaId() {
        return empresaId;
    }
    
    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }
    
    public RolEmpresa getRol() {
        return rol;
    }
    
    public void setRol(RolEmpresa rol) {
        this.rol = rol;
    }
    
    @Override
    public String toString() {
        return "AsignarUsuarioEmpresaDTO{" +
                "usuarioId=" + usuarioId +
                ", empresaId=" + empresaId +
                ", rol=" + rol +
                '}';
    }
}
