package com.agrocloud.avicola.huevos.model.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "avicola_huevo_venta")
@EntityListeners(AuditingEntityListener.class)
public class AvicolaHuevoVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", nullable = false)
    private AvicolaHuevoLote lote;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", precision = 12, scale = 4)
    private BigDecimal precioUnitario;

    @Column(name = "total", precision = 14, scale = 2)
    private BigDecimal total;

    @Column(name = "comprador", length = 180)
    private String comprador;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "campana_id")
    private Long campanaId;

    @Column(name = "ingreso_id")
    private Long ingresoId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AvicolaHuevoLote getLote() { return lote; }
    public void setLote(AvicolaHuevoLote lote) { this.lote = lote; }
    public Long getEmpresaId() { return empresaId; }
    public void setEmpresaId(Long empresaId) { this.empresaId = empresaId; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public String getComprador() { return comprador; }
    public void setComprador(String comprador) { this.comprador = comprador; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public Long getCampanaId() { return campanaId; }
    public void setCampanaId(Long campanaId) { this.campanaId = campanaId; }
    public Long getIngresoId() { return ingresoId; }
    public void setIngresoId(Long ingresoId) { this.ingresoId = ingresoId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
