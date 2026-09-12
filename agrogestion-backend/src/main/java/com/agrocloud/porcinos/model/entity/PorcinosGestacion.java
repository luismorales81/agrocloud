package com.agrocloud.porcinos.model.entity;

import com.agrocloud.porcinos.model.enums.PorcinosGestacionEstado;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "porcinos_gestacion")
@EntityListeners(AuditingEntityListener.class)
public class PorcinosGestacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "madre_id", nullable = false)
    private PorcinosMadre madre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicio_id")
    private PorcinosServicio servicio;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_probable_parto", nullable = false)
    private LocalDate fechaProbableParto;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private PorcinosGestacionEstado estado = PorcinosGestacionEstado.EN_CURSO;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

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

    public PorcinosMadre getMadre() {
        return madre;
    }

    public void setMadre(PorcinosMadre madre) {
        this.madre = madre;
    }

    public PorcinosServicio getServicio() {
        return servicio;
    }

    public void setServicio(PorcinosServicio servicio) {
        this.servicio = servicio;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaProbableParto() {
        return fechaProbableParto;
    }

    public void setFechaProbableParto(LocalDate fechaProbableParto) {
        this.fechaProbableParto = fechaProbableParto;
    }

    public PorcinosGestacionEstado getEstado() {
        return estado;
    }

    public void setEstado(PorcinosGestacionEstado estado) {
        this.estado = estado;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
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
