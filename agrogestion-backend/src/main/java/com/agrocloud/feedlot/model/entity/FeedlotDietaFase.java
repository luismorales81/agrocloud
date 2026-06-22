package com.agrocloud.feedlot.model.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedlot_dieta_fase")
@EntityListeners(AuditingEntityListener.class)
public class FeedlotDietaFase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dieta_id", nullable = false)
    private FeedlotDieta dieta;

    @Column(name = "nombre_fase", nullable = false, length = 120)
    private String nombreFase;

    @Column(name = "dias_desde_ingreso", nullable = false)
    private Integer diasDesdeIngreso = 0;

    @Column(name = "kg_ms_cabeza_dia", nullable = false, precision = 10, scale = 3)
    private BigDecimal kgMsCabezaDia;

    @Column(name = "insumo_id")
    private Long insumoId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FeedlotDieta getDieta() {
        return dieta;
    }

    public void setDieta(FeedlotDieta dieta) {
        this.dieta = dieta;
    }

    public String getNombreFase() {
        return nombreFase;
    }

    public void setNombreFase(String nombreFase) {
        this.nombreFase = nombreFase;
    }

    public Integer getDiasDesdeIngreso() {
        return diasDesdeIngreso;
    }

    public void setDiasDesdeIngreso(Integer diasDesdeIngreso) {
        this.diasDesdeIngreso = diasDesdeIngreso;
    }

    public BigDecimal getKgMsCabezaDia() {
        return kgMsCabezaDia;
    }

    public void setKgMsCabezaDia(BigDecimal kgMsCabezaDia) {
        this.kgMsCabezaDia = kgMsCabezaDia;
    }

    public Long getInsumoId() {
        return insumoId;
    }

    public void setInsumoId(Long insumoId) {
        this.insumoId = insumoId;
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
