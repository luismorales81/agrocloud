package com.agrocloud.porcinos.model.entity;

import com.agrocloud.porcinos.model.enums.PorcinosLoteEstado;
import com.agrocloud.porcinos.model.enums.PorcinosLoteEtapa;
import com.agrocloud.porcinos.model.enums.PorcinosLoteOrigen;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "porcinos_lote")
@EntityListeners(AuditingEntityListener.class)
public class PorcinosLote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "galpon_id", nullable = false)
    private PorcinosGalpon galpon;

    @Column(name = "campana_id", nullable = false)
    private Long campanaId;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "origen", nullable = false, length = 20)
    private PorcinosLoteOrigen origen;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destete_id")
    private PorcinosDestete destete;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso;

    @Column(name = "fecha_cierre")
    private LocalDate fechaCierre;

    @Column(name = "cabezas_inicial", nullable = false)
    private Integer cabezasInicial;

    @Column(name = "cabezas_actuales", nullable = false)
    private Integer cabezasActuales;

    @Column(name = "peso_promedio_ingreso_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal pesoPromedioIngresoKg;

    @Enumerated(EnumType.STRING)
    @Column(name = "etapa", nullable = false, length = 20)
    private PorcinosLoteEtapa etapa = PorcinosLoteEtapa.RECRIA;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private PorcinosLoteEstado estado = PorcinosLoteEstado.ACTIVO;

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

    public PorcinosGalpon getGalpon() {
        return galpon;
    }

    public void setGalpon(PorcinosGalpon galpon) {
        this.galpon = galpon;
    }

    public Long getCampanaId() {
        return campanaId;
    }

    public void setCampanaId(Long campanaId) {
        this.campanaId = campanaId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public PorcinosLoteOrigen getOrigen() {
        return origen;
    }

    public void setOrigen(PorcinosLoteOrigen origen) {
        this.origen = origen;
    }

    public PorcinosDestete getDestete() {
        return destete;
    }

    public void setDestete(PorcinosDestete destete) {
        this.destete = destete;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public LocalDate getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(LocalDate fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public Integer getCabezasInicial() {
        return cabezasInicial;
    }

    public void setCabezasInicial(Integer cabezasInicial) {
        this.cabezasInicial = cabezasInicial;
    }

    public Integer getCabezasActuales() {
        return cabezasActuales;
    }

    public void setCabezasActuales(Integer cabezasActuales) {
        this.cabezasActuales = cabezasActuales;
    }

    public BigDecimal getPesoPromedioIngresoKg() {
        return pesoPromedioIngresoKg;
    }

    public void setPesoPromedioIngresoKg(BigDecimal pesoPromedioIngresoKg) {
        this.pesoPromedioIngresoKg = pesoPromedioIngresoKg;
    }

    public PorcinosLoteEtapa getEtapa() {
        return etapa;
    }

    public void setEtapa(PorcinosLoteEtapa etapa) {
        this.etapa = etapa;
    }

    public PorcinosLoteEstado getEstado() {
        return estado;
    }

    public void setEstado(PorcinosLoteEstado estado) {
        this.estado = estado;
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
