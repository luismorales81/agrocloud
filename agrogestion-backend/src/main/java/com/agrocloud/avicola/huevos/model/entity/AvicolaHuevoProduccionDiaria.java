package com.agrocloud.avicola.huevos.model.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "avicola_huevo_produccion_diaria")
@EntityListeners(AuditingEntityListener.class)
public class AvicolaHuevoProduccionDiaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", nullable = false)
    private AvicolaHuevoLote lote;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "cantidad_huevos", nullable = false)
    private Integer cantidadHuevos;

    @Column(name = "huevos_tam1", nullable = false)
    private Integer huevosTam1 = 0;

    @Column(name = "huevos_tam2", nullable = false)
    private Integer huevosTam2 = 0;

    @Column(name = "huevos_tam3", nullable = false)
    private Integer huevosTam3 = 0;

    @Column(name = "huevos_tam4", nullable = false)
    private Integer huevosTam4 = 0;

    @Column(name = "huevos_rotos", nullable = false)
    private Integer huevosRotos = 0;

    @Column(name = "total_huevos_dia", nullable = false)
    private Integer totalHuevosDia = 0;

    @Column(name = "temperatura_dia", precision = 5, scale = 2)
    private BigDecimal temperaturaDia;

    @Column(name = "humedad_dia", precision = 6, scale = 2)
    private BigDecimal humedadDia;

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

    public AvicolaHuevoLote getLote() {
        return lote;
    }

    public void setLote(AvicolaHuevoLote lote) {
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

    public Integer getCantidadHuevos() {
        return cantidadHuevos;
    }

    public void setCantidadHuevos(Integer cantidadHuevos) {
        this.cantidadHuevos = cantidadHuevos;
    }

    public Integer getHuevosTam1() {
        return huevosTam1;
    }

    public void setHuevosTam1(Integer huevosTam1) {
        this.huevosTam1 = huevosTam1;
    }

    public Integer getHuevosTam2() {
        return huevosTam2;
    }

    public void setHuevosTam2(Integer huevosTam2) {
        this.huevosTam2 = huevosTam2;
    }

    public Integer getHuevosTam3() {
        return huevosTam3;
    }

    public void setHuevosTam3(Integer huevosTam3) {
        this.huevosTam3 = huevosTam3;
    }

    public Integer getHuevosTam4() {
        return huevosTam4;
    }

    public void setHuevosTam4(Integer huevosTam4) {
        this.huevosTam4 = huevosTam4;
    }

    public Integer getHuevosRotos() {
        return huevosRotos;
    }

    public void setHuevosRotos(Integer huevosRotos) {
        this.huevosRotos = huevosRotos;
    }

    public Integer getTotalHuevosDia() {
        return totalHuevosDia;
    }

    public void setTotalHuevosDia(Integer totalHuevosDia) {
        this.totalHuevosDia = totalHuevosDia;
    }

    public BigDecimal getTemperaturaDia() {
        return temperaturaDia;
    }

    public void setTemperaturaDia(BigDecimal temperaturaDia) {
        this.temperaturaDia = temperaturaDia;
    }

    public BigDecimal getHumedadDia() {
        return humedadDia;
    }

    public void setHumedadDia(BigDecimal humedadDia) {
        this.humedadDia = humedadDia;
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
