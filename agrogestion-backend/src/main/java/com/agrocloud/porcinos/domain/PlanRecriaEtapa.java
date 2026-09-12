package com.agrocloud.porcinos.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Entity
@Table(name = "porcinos_legacy_plan_recria_etapa")
public class PlanRecriaEtapa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_recria_id", nullable = false)
    @JsonIgnore
    private PlanRecria planRecria;

    @NotNull
    @Column(name = "orden", nullable = false)
    private Integer orden;

    @NotBlank
    @Size(max = 30)
    @Column(name = "codigo_etapa", nullable = false, length = 30)
    private String codigoEtapa;

    @Column(name = "duracion_estimada_dias")
    private Integer duracionEstimadaDias;

    @Column(name = "peso_ingreso_minimo_kg", precision = 10, scale = 2)
    private BigDecimal pesoIngresoMinimoKg;

    @Column(name = "peso_objetivo_kg", precision = 10, scale = 2)
    private BigDecimal pesoObjetivoKg;

    @Column(name = "ganancia_diaria_esperada_kg", precision = 10, scale = 4)
    private BigDecimal gananciaDiariaEsperadaKg;

    @Column(name = "umbral_mortalidad_pct", precision = 5, scale = 2)
    private BigDecimal umbralMortalidadPct;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public PlanRecria getPlanRecria() { return planRecria; }
    public void setPlanRecria(PlanRecria planRecria) { this.planRecria = planRecria; }
    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }
    public String getCodigoEtapa() { return codigoEtapa; }
    public void setCodigoEtapa(String codigoEtapa) { this.codigoEtapa = codigoEtapa; }
    public Integer getDuracionEstimadaDias() { return duracionEstimadaDias; }
    public void setDuracionEstimadaDias(Integer duracionEstimadaDias) { this.duracionEstimadaDias = duracionEstimadaDias; }
    public BigDecimal getPesoIngresoMinimoKg() { return pesoIngresoMinimoKg; }
    public void setPesoIngresoMinimoKg(BigDecimal pesoIngresoMinimoKg) { this.pesoIngresoMinimoKg = pesoIngresoMinimoKg; }
    public BigDecimal getPesoObjetivoKg() { return pesoObjetivoKg; }
    public void setPesoObjetivoKg(BigDecimal pesoObjetivoKg) { this.pesoObjetivoKg = pesoObjetivoKg; }
    public BigDecimal getGananciaDiariaEsperadaKg() { return gananciaDiariaEsperadaKg; }
    public void setGananciaDiariaEsperadaKg(BigDecimal gananciaDiariaEsperadaKg) { this.gananciaDiariaEsperadaKg = gananciaDiariaEsperadaKg; }
    public BigDecimal getUmbralMortalidadPct() { return umbralMortalidadPct; }
    public void setUmbralMortalidadPct(BigDecimal umbralMortalidadPct) { this.umbralMortalidadPct = umbralMortalidadPct; }
}
