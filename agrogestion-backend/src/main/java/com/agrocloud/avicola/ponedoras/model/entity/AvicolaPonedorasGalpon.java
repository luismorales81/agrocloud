package com.agrocloud.avicola.ponedoras.model.entity;

import com.agrocloud.avicola.crianza.model.entity.AvicolaEstablecimiento;
import com.agrocloud.avicola.ponedoras.model.enums.AvicolaPonedorasGalponEstado;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Galpón o unidad productiva de ponedoras (equivalente a "lote" del módulo).
 */
@Entity
@Table(name = "avicola_ponedoras_galpon")
@EntityListeners(AuditingEntityListener.class)
public class AvicolaPonedorasGalpon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "establecimiento_id")
    private AvicolaEstablecimiento establecimiento;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Column(name = "raza", nullable = false, length = 120)
    private String raza;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso;

    @Column(name = "cantidad_inicial", nullable = false)
    private Integer cantidadInicial;

    @Column(name = "cantidad_aves")
    private Integer cantidadAves;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private AvicolaPonedorasGalponEstado estado = AvicolaPonedorasGalponEstado.ACTIVO;

    @Column(name = "fecha_cierre")
    private LocalDate fechaCierre;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

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

    public AvicolaEstablecimiento getEstablecimiento() {
        return establecimiento;
    }

    public void setEstablecimiento(AvicolaEstablecimiento establecimiento) {
        this.establecimiento = establecimiento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRaza() {
        return raza;
    }

    public void setRaza(String raza) {
        this.raza = raza;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public Integer getCantidadInicial() {
        return cantidadInicial;
    }

    public void setCantidadInicial(Integer cantidadInicial) {
        this.cantidadInicial = cantidadInicial;
    }

    public Integer getCantidadAves() {
        return cantidadAves;
    }

    public void setCantidadAves(Integer cantidadAves) {
        this.cantidadAves = cantidadAves;
    }

    public AvicolaPonedorasGalponEstado getEstado() {
        return estado;
    }

    public void setEstado(AvicolaPonedorasGalponEstado estado) {
        this.estado = estado;
    }

    public LocalDate getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(LocalDate fechaCierre) {
        this.fechaCierre = fechaCierre;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
