package com.agrocloud.porcinos.model.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "porcinos_establecimiento")
@EntityListeners(AuditingEntityListener.class)
public class PorcinosEstablecimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "ubicacion", length = 255)
    private String ubicacion;

    @Column(name = "coordenadas", columnDefinition = "TEXT")
    private String coordenadas;

    @Column(name = "dias_gestacion", nullable = false)
    private Integer diasGestacion = 114;

    @Column(name = "dias_lactancia", nullable = false)
    private Integer diasLactancia = 21;

    @Column(name = "dias_entre_celos", nullable = false)
    private Integer diasEntreCelos = 21;

    @Column(name = "faena_habilitada", nullable = false)
    private Boolean faenaHabilitada = true;

    @Column(name = "capacidad_cabezas")
    private Integer capacidadCabezas;

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

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getCoordenadas() {
        return coordenadas;
    }

    public void setCoordenadas(String coordenadas) {
        this.coordenadas = coordenadas;
    }

    public Integer getDiasGestacion() {
        return diasGestacion;
    }

    public void setDiasGestacion(Integer diasGestacion) {
        this.diasGestacion = diasGestacion;
    }

    public Integer getDiasLactancia() {
        return diasLactancia;
    }

    public void setDiasLactancia(Integer diasLactancia) {
        this.diasLactancia = diasLactancia;
    }

    public Integer getDiasEntreCelos() {
        return diasEntreCelos;
    }

    public void setDiasEntreCelos(Integer diasEntreCelos) {
        this.diasEntreCelos = diasEntreCelos;
    }

    public Boolean getFaenaHabilitada() {
        return faenaHabilitada;
    }

    public void setFaenaHabilitada(Boolean faenaHabilitada) {
        this.faenaHabilitada = faenaHabilitada;
    }

    public Integer getCapacidadCabezas() {
        return capacidadCabezas;
    }

    public void setCapacidadCabezas(Integer capacidadCabezas) {
        this.capacidadCabezas = capacidadCabezas;
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
