package com.agrocloud.avicola.huevos.model.entity;

import com.agrocloud.avicola.huevos.model.enums.AvicolaHuevoLoteEstado;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "avicola_huevo_lote")
@EntityListeners(AuditingEntityListener.class)
public class AvicolaHuevoLote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "establecimiento_id", nullable = false)
    private AvicolaHuevoEstablecimiento establecimiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raza_id", nullable = false)
    private AvicolaHuevoRaza raza;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "cantidad_aves_inicial", nullable = false)
    private Integer cantidadAvesInicial;

    @Column(name = "cantidad_aves_actual", nullable = false)
    private Integer cantidadAvesActual;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private AvicolaHuevoLoteEstado estado = AvicolaHuevoLoteEstado.ACTIVO;

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

    public AvicolaHuevoEstablecimiento getEstablecimiento() {
        return establecimiento;
    }

    public void setEstablecimiento(AvicolaHuevoEstablecimiento establecimiento) {
        this.establecimiento = establecimiento;
    }

    public AvicolaHuevoRaza getRaza() {
        return raza;
    }

    public void setRaza(AvicolaHuevoRaza raza) {
        this.raza = raza;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Integer getCantidadAvesInicial() {
        return cantidadAvesInicial;
    }

    public void setCantidadAvesInicial(Integer cantidadAvesInicial) {
        this.cantidadAvesInicial = cantidadAvesInicial;
    }

    public Integer getCantidadAvesActual() {
        return cantidadAvesActual;
    }

    public void setCantidadAvesActual(Integer cantidadAvesActual) {
        this.cantidadAvesActual = cantidadAvesActual;
    }

    public AvicolaHuevoLoteEstado getEstado() {
        return estado;
    }

    public void setEstado(AvicolaHuevoLoteEstado estado) {
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
