package com.agrocloud.porcinos.model.entity;

import com.agrocloud.porcinos.model.enums.PorcinosGalponEstado;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "porcinos_galpon")
@EntityListeners(AuditingEntityListener.class)
public class PorcinosGalpon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "establecimiento_id", nullable = false)
    private PorcinosEstablecimiento establecimiento;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Column(name = "capacidad_cabezas")
    private Integer capacidadCabezas;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private PorcinosGalponEstado estado = PorcinosGalponEstado.DISPONIBLE;

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

    public PorcinosEstablecimiento getEstablecimiento() {
        return establecimiento;
    }

    public void setEstablecimiento(PorcinosEstablecimiento establecimiento) {
        this.establecimiento = establecimiento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getCapacidadCabezas() {
        return capacidadCabezas;
    }

    public void setCapacidadCabezas(Integer capacidadCabezas) {
        this.capacidadCabezas = capacidadCabezas;
    }

    public PorcinosGalponEstado getEstado() {
        return estado;
    }

    public void setEstado(PorcinosGalponEstado estado) {
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
