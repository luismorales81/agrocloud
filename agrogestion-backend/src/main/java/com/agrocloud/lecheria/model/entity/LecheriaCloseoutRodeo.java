package com.agrocloud.lecheria.model.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "lecheria_closeout_rodeo")
@EntityListeners(AuditingEntityListener.class)
public class LecheriaCloseoutRodeo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rodeo_id", nullable = false)
    private LecheriaRodeo rodeo;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "campana_id", nullable = false)
    private Long campanaId;

    @Column(name = "fecha_cierre", nullable = false)
    private LocalDate fechaCierre;

    @Column(name = "litros_totales", precision = 14, scale = 3)
    private BigDecimal litrosTotales;

    @Column(name = "costo_alimentacion", precision = 14, scale = 2)
    private BigDecimal costoAlimentacion;

    @Column(name = "ingresos_leche", precision = 14, scale = 2)
    private BigDecimal ingresosLeche;

    @Column(name = "margen", precision = 14, scale = 2)
    private BigDecimal margen;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LecheriaRodeo getRodeo() { return rodeo; }
    public void setRodeo(LecheriaRodeo rodeo) { this.rodeo = rodeo; }
    public Long getEmpresaId() { return empresaId; }
    public void setEmpresaId(Long empresaId) { this.empresaId = empresaId; }
    public Long getCampanaId() { return campanaId; }
    public void setCampanaId(Long campanaId) { this.campanaId = campanaId; }
    public LocalDate getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(LocalDate fechaCierre) { this.fechaCierre = fechaCierre; }
    public BigDecimal getLitrosTotales() { return litrosTotales; }
    public void setLitrosTotales(BigDecimal litrosTotales) { this.litrosTotales = litrosTotales; }
    public BigDecimal getCostoAlimentacion() { return costoAlimentacion; }
    public void setCostoAlimentacion(BigDecimal costoAlimentacion) { this.costoAlimentacion = costoAlimentacion; }
    public BigDecimal getIngresosLeche() { return ingresosLeche; }
    public void setIngresosLeche(BigDecimal ingresosLeche) { this.ingresosLeche = ingresosLeche; }
    public BigDecimal getMargen() { return margen; }
    public void setMargen(BigDecimal margen) { this.margen = margen; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
