package com.agrocloud.avicola.ponedoras.model.entity;

import com.agrocloud.avicola.ponedoras.model.enums.AvicolaPonedorasDescarteMotivo;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Descarte o liquidación de aves al cierre o baja del galpón.
 */
@Entity
@Table(name = "avicola_ponedoras_descarte_aves")
@EntityListeners(AuditingEntityListener.class)
public class AvicolaPonedorasDescarteAves {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "galpon_id", nullable = false)
    private AvicolaPonedorasGalpon galpon;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "motivo", nullable = false, length = 40)
    private AvicolaPonedorasDescarteMotivo motivo;

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

    public AvicolaPonedorasGalpon getGalpon() {
        return galpon;
    }

    public void setGalpon(AvicolaPonedorasGalpon galpon) {
        this.galpon = galpon;
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

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public AvicolaPonedorasDescarteMotivo getMotivo() {
        return motivo;
    }

    public void setMotivo(AvicolaPonedorasDescarteMotivo motivo) {
        this.motivo = motivo;
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
