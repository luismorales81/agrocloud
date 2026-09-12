package com.agrocloud.porcinos.model.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "porcinos_parto")
@EntityListeners(AuditingEntityListener.class)
public class PorcinosParto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gestacion_id", nullable = false)
    private PorcinosGestacion gestacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "madre_id", nullable = false)
    private PorcinosMadre madre;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "nacidos_vivos", nullable = false)
    private Integer nacidosVivos = 0;

    @Column(name = "nacidos_muertos", nullable = false)
    private Integer nacidosMuertos = 0;

    @Column(name = "momificados", nullable = false)
    private Integer momificados = 0;

    @Column(name = "temperatura_ambiente", precision = 5, scale = 2)
    private BigDecimal temperaturaAmbiente;

    @Column(name = "humedad_ambiente", precision = 5, scale = 2)
    private BigDecimal humedadAmbiente;

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

    public PorcinosGestacion getGestacion() {
        return gestacion;
    }

    public void setGestacion(PorcinosGestacion gestacion) {
        this.gestacion = gestacion;
    }

    public PorcinosMadre getMadre() {
        return madre;
    }

    public void setMadre(PorcinosMadre madre) {
        this.madre = madre;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Integer getNacidosVivos() {
        return nacidosVivos;
    }

    public void setNacidosVivos(Integer nacidosVivos) {
        this.nacidosVivos = nacidosVivos;
    }

    public Integer getNacidosMuertos() {
        return nacidosMuertos;
    }

    public void setNacidosMuertos(Integer nacidosMuertos) {
        this.nacidosMuertos = nacidosMuertos;
    }

    public Integer getMomificados() {
        return momificados;
    }

    public void setMomificados(Integer momificados) {
        this.momificados = momificados;
    }

    public BigDecimal getTemperaturaAmbiente() {
        return temperaturaAmbiente;
    }

    public void setTemperaturaAmbiente(BigDecimal temperaturaAmbiente) {
        this.temperaturaAmbiente = temperaturaAmbiente;
    }

    public BigDecimal getHumedadAmbiente() {
        return humedadAmbiente;
    }

    public void setHumedadAmbiente(BigDecimal humedadAmbiente) {
        this.humedadAmbiente = humedadAmbiente;
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
