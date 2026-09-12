package com.agrocloud.porcinos.model.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "porcinos_servicio")
@EntityListeners(AuditingEntityListener.class)
public class PorcinosServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "madre_id", nullable = false)
    private PorcinosMadre madre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "padrillo_id")
    private PorcinosPadrillo padrillo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_servicio_id")
    private PorcinosTipoServicio tipoServicio;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

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

    public PorcinosMadre getMadre() {
        return madre;
    }

    public void setMadre(PorcinosMadre madre) {
        this.madre = madre;
    }

    public PorcinosPadrillo getPadrillo() {
        return padrillo;
    }

    public void setPadrillo(PorcinosPadrillo padrillo) {
        this.padrillo = padrillo;
    }

    public PorcinosTipoServicio getTipoServicio() {
        return tipoServicio;
    }

    public void setTipoServicio(PorcinosTipoServicio tipoServicio) {
        this.tipoServicio = tipoServicio;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
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
