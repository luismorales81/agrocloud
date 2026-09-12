package com.agrocloud.porcinos.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Entity
@Table(name = "porcinos_legacy_plan_recria_receta_sugerencia")
public class PlanRecriaRecetaSugerencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_recria_id", nullable = false)
    @JsonIgnore
    private PlanRecria planRecria;

    @NotBlank
    @Size(max = 30)
    @Column(name = "etapa_codigo", nullable = false, length = 30)
    private String etapaCodigo;

    @NotBlank
    @Size(max = 200)
    @Column(name = "nombre_insumo_compuesto", nullable = false, length = 200)
    private String nombreInsumoCompuesto;

    @NotNull
    @Positive
    @Column(name = "kg_por_animal_dia", nullable = false, precision = 12, scale = 4)
    private BigDecimal kgPorAnimalDia;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public PlanRecria getPlanRecria() { return planRecria; }
    public void setPlanRecria(PlanRecria planRecria) { this.planRecria = planRecria; }
    public String getEtapaCodigo() { return etapaCodigo; }
    public void setEtapaCodigo(String etapaCodigo) { this.etapaCodigo = etapaCodigo; }
    public String getNombreInsumoCompuesto() { return nombreInsumoCompuesto; }
    public void setNombreInsumoCompuesto(String nombreInsumoCompuesto) { this.nombreInsumoCompuesto = nombreInsumoCompuesto; }
    public BigDecimal getKgPorAnimalDia() { return kgPorAnimalDia; }
    public void setKgPorAnimalDia(BigDecimal kgPorAnimalDia) { this.kgPorAnimalDia = kgPorAnimalDia; }
}
