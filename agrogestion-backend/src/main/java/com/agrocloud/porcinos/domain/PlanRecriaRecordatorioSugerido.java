package com.agrocloud.porcinos.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "porcinos_legacy_plan_recria_recordatorio")
public class PlanRecriaRecordatorioSugerido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_recria_id", nullable = false)
    @JsonIgnore
    private PlanRecria planRecria;

    @NotBlank
    @Size(max = 50)
    @Column(name = "tipo", nullable = false, length = 50)
    private String tipo;

    @NotNull
    @Column(name = "dias_desde_ingreso", nullable = false)
    private Integer diasDesdeIngreso;

    @NotBlank
    @Size(max = 500)
    @Column(name = "descripcion", nullable = false, length = 500)
    private String descripcion;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public PlanRecria getPlanRecria() { return planRecria; }
    public void setPlanRecria(PlanRecria planRecria) { this.planRecria = planRecria; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public Integer getDiasDesdeIngreso() { return diasDesdeIngreso; }
    public void setDiasDesdeIngreso(Integer diasDesdeIngreso) { this.diasDesdeIngreso = diasDesdeIngreso; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
