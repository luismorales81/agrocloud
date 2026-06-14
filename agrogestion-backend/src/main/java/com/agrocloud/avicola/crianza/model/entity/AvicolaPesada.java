package com.agrocloud.avicola.crianza.model.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Pesada de un lote para seguimiento de peso.
 */
@Entity
@Table(name = "avicola_pesada")
@EntityListeners(AuditingEntityListener.class)
public class AvicolaPesada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", nullable = false)
    private AvicolaLote lote;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "peso_promedio", nullable = false, precision = 8, scale = 3)
    private BigDecimal pesoPromedio;

    @Column(name = "cantidad_pesada")
    private Integer cantidadPesada;

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

    public AvicolaLote getLote() {
        return lote;
    }

    public void setLote(AvicolaLote lote) {
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

    public BigDecimal getPesoPromedio() {
        return pesoPromedio;
    }

    public void setPesoPromedio(BigDecimal pesoPromedio) {
        this.pesoPromedio = pesoPromedio;
    }

    public Integer getCantidadPesada() {
        return cantidadPesada;
    }

    public void setCantidadPesada(Integer cantidadPesada) {
        this.cantidadPesada = cantidadPesada;
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
