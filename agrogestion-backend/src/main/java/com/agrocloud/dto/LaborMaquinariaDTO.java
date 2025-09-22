package com.agrocloud.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LaborMaquinariaDTO {
    private Long idLaborMaquinaria;
    private Long idLabor;
    private String tipo; // PROPIA, ALQUILADA
    private String descripcion;
    private String proveedor;
    private BigDecimal costo;
    private BigDecimal horasUso;
    private BigDecimal kilometrosRecorridos;
    private String observaciones;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructores
    public LaborMaquinariaDTO() {}
    
    public LaborMaquinariaDTO(Long idLaborMaquinaria, Long idLabor, String tipo, 
                            String descripcion, String proveedor, BigDecimal costo,
                            BigDecimal horasUso, BigDecimal kilometrosRecorridos,
                            String observaciones, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.idLaborMaquinaria = idLaborMaquinaria;
        this.idLabor = idLabor;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.proveedor = proveedor;
        this.costo = costo;
        this.horasUso = horasUso;
        this.kilometrosRecorridos = kilometrosRecorridos;
        this.observaciones = observaciones;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters y Setters
    public Long getIdLaborMaquinaria() { return idLaborMaquinaria; }
    public void setIdLaborMaquinaria(Long idLaborMaquinaria) { this.idLaborMaquinaria = idLaborMaquinaria; }
    
    public Long getIdLabor() { return idLabor; }
    public void setIdLabor(Long idLabor) { this.idLabor = idLabor; }
    
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public String getProveedor() { return proveedor; }
    public void setProveedor(String proveedor) { this.proveedor = proveedor; }
    
    public BigDecimal getCosto() { return costo; }
    public void setCosto(BigDecimal costo) { this.costo = costo; }
    
    public BigDecimal getHorasUso() { return horasUso; }
    public void setHorasUso(BigDecimal horasUso) { this.horasUso = horasUso; }
    
    public BigDecimal getKilometrosRecorridos() { return kilometrosRecorridos; }
    public void setKilometrosRecorridos(BigDecimal kilometrosRecorridos) { this.kilometrosRecorridos = kilometrosRecorridos; }
    
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
