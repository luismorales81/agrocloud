package com.agrocloud.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa los movimientos de inventario de insumos.
 * Permite auditoría completa de todos los cambios de stock.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@Entity
@Table(name = "movimientos_inventario")
public class MovimientoInventario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insumo_id", nullable = false)
    private Insumo insumo;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "labor_id")
    @JsonIgnore
    private Labor labor;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", nullable = false)
    private TipoMovimiento tipoMovimiento;
    
    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.0", message = "La cantidad debe ser mayor o igual a 0")
    @Column(name = "cantidad", nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidad;
    
    @Size(max = 255, message = "El motivo no puede exceder 255 caracteres")
    @Column(name = "motivo", length = 255)
    private String motivo;
    
    @Column(name = "fecha_movimiento")
    private LocalDateTime fechaMovimiento;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    @JsonIgnore
    private User usuario;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Enums
    public enum TipoMovimiento {
        ENTRADA, SALIDA, AJUSTE
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (fechaMovimiento == null) {
            fechaMovimiento = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructores
    public MovimientoInventario() {}
    
    public MovimientoInventario(Insumo insumo, Labor labor, TipoMovimiento tipoMovimiento, 
                               BigDecimal cantidad, String motivo, User usuario) {
        this.insumo = insumo;
        this.labor = labor;
        this.tipoMovimiento = tipoMovimiento;
        this.cantidad = cantidad;
        this.motivo = motivo;
        this.usuario = usuario;
        this.fechaMovimiento = LocalDateTime.now();
    }
    
    // Getters y Setters
    public Long getId() { 
        return id; 
    }
    
    public void setId(Long id) { 
        this.id = id; 
    }
    
    public Insumo getInsumo() { 
        return insumo; 
    }
    
    public void setInsumo(Insumo insumo) { 
        this.insumo = insumo; 
    }
    
    public Labor getLabor() { 
        return labor; 
    }
    
    public void setLabor(Labor labor) { 
        this.labor = labor; 
    }
    
    public TipoMovimiento getTipoMovimiento() { 
        return tipoMovimiento; 
    }
    
    public void setTipoMovimiento(TipoMovimiento tipoMovimiento) { 
        this.tipoMovimiento = tipoMovimiento; 
    }
    
    public BigDecimal getCantidad() { 
        return cantidad; 
    }
    
    public void setCantidad(BigDecimal cantidad) { 
        this.cantidad = cantidad; 
    }
    
    public String getMotivo() { 
        return motivo; 
    }
    
    public void setMotivo(String motivo) { 
        this.motivo = motivo; 
    }
    
    public LocalDateTime getFechaMovimiento() { 
        return fechaMovimiento; 
    }
    
    public void setFechaMovimiento(LocalDateTime fechaMovimiento) { 
        this.fechaMovimiento = fechaMovimiento; 
    }
    
    public User getUsuario() { 
        return usuario; 
    }
    
    public void setUsuario(User usuario) { 
        this.usuario = usuario; 
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
