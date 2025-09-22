package com.agrocloud.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para transferir datos de insumos utilizados en labores.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
public class LaborInsumoDTO {
    private Long idLaborInsumo;
    private Long idLabor;
    private Long idInsumo;
    private String insumoNombre;
    private String insumoTipo;
    private BigDecimal cantidadUsada;
    private BigDecimal cantidadPlanificada;
    private BigDecimal costoUnitario;
    private BigDecimal costoTotal;
    private String observaciones;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructores
    public LaborInsumoDTO() {}
    
    public LaborInsumoDTO(Long idLaborInsumo, Long idLabor, Long idInsumo, String insumoNombre, 
                         String insumoTipo, BigDecimal cantidadUsada, BigDecimal cantidadPlanificada,
                         BigDecimal costoUnitario, BigDecimal costoTotal, String observaciones,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.idLaborInsumo = idLaborInsumo;
        this.idLabor = idLabor;
        this.idInsumo = idInsumo;
        this.insumoNombre = insumoNombre;
        this.insumoTipo = insumoTipo;
        this.cantidadUsada = cantidadUsada;
        this.cantidadPlanificada = cantidadPlanificada;
        this.costoUnitario = costoUnitario;
        this.costoTotal = costoTotal;
        this.observaciones = observaciones;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters y Setters
    public Long getIdLaborInsumo() { 
        return idLaborInsumo; 
    }
    
    public void setIdLaborInsumo(Long idLaborInsumo) { 
        this.idLaborInsumo = idLaborInsumo; 
    }
    
    public Long getIdLabor() { 
        return idLabor; 
    }
    
    public void setIdLabor(Long idLabor) { 
        this.idLabor = idLabor; 
    }
    
    public Long getIdInsumo() { 
        return idInsumo; 
    }
    
    public void setIdInsumo(Long idInsumo) { 
        this.idInsumo = idInsumo; 
    }
    
    public String getInsumoNombre() { 
        return insumoNombre; 
    }
    
    public void setInsumoNombre(String insumoNombre) { 
        this.insumoNombre = insumoNombre; 
    }
    
    public String getInsumoTipo() { 
        return insumoTipo; 
    }
    
    public void setInsumoTipo(String insumoTipo) { 
        this.insumoTipo = insumoTipo; 
    }
    
    public BigDecimal getCantidadUsada() { 
        return cantidadUsada; 
    }
    
    public void setCantidadUsada(BigDecimal cantidadUsada) { 
        this.cantidadUsada = cantidadUsada; 
    }
    
    public BigDecimal getCantidadPlanificada() { 
        return cantidadPlanificada; 
    }
    
    public void setCantidadPlanificada(BigDecimal cantidadPlanificada) { 
        this.cantidadPlanificada = cantidadPlanificada; 
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
    
    public String getObservaciones() { 
        return observaciones; 
    }
    
    public void setObservaciones(String observaciones) { 
        this.observaciones = observaciones; 
    }
    
    public LocalDateTime getCreatedAt() { 
        return createdAt; 
    }
    
    public void setCreatedAt(LocalDateTime createdAt) { 
        this.createdAt = createdAt; 
    }
    
    public LocalDateTime getUpdatedAt() { 
        return updatedAt; 
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) { 
        this.updatedAt = updatedAt; 
    }
}
