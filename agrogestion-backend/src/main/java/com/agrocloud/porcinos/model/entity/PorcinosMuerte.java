package com.agrocloud.porcinos.model.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "porcinos_muerte")
@EntityListeners(AuditingEntityListener.class)
public class PorcinosMuerte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", nullable = false)
    private PorcinosLote lote;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "cabezas", nullable = false)
    private Integer cabezas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "causa_mortalidad_id")
    private PorcinosCausaMortalidad causaMortalidad;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PorcinosLote getLote() {
        return lote;
    }

    public void setLote(PorcinosLote lote) {
        this.lote = lote;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Integer getCabezas() {
        return cabezas;
    }

    public void setCabezas(Integer cabezas) {
        this.cabezas = cabezas;
    }

    public PorcinosCausaMortalidad getCausaMortalidad() {
        return causaMortalidad;
    }

    public void setCausaMortalidad(PorcinosCausaMortalidad causaMortalidad) {
        this.causaMortalidad = causaMortalidad;
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
}
