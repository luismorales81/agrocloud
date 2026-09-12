package com.agrocloud.porcinos.model.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "porcinos_destete")
@EntityListeners(AuditingEntityListener.class)
public class PorcinosDestete {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parto_id", nullable = false, unique = true)
    private PorcinosParto parto;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "cantidad_destetados", nullable = false)
    private Integer cantidadDestetados;

    @Column(name = "peso_promedio_kg", precision = 10, scale = 2)
    private BigDecimal pesoPromedioKg;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id")
    private PorcinosLote lote;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PorcinosParto getParto() {
        return parto;
    }

    public void setParto(PorcinosParto parto) {
        this.parto = parto;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Integer getCantidadDestetados() {
        return cantidadDestetados;
    }

    public void setCantidadDestetados(Integer cantidadDestetados) {
        this.cantidadDestetados = cantidadDestetados;
    }

    public BigDecimal getPesoPromedioKg() {
        return pesoPromedioKg;
    }

    public void setPesoPromedioKg(BigDecimal pesoPromedioKg) {
        this.pesoPromedioKg = pesoPromedioKg;
    }

    public PorcinosLote getLote() {
        return lote;
    }

    public void setLote(PorcinosLote lote) {
        this.lote = lote;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
