package com.agrocloud.porcinos.domain;

import com.agrocloud.core.domain.Empresa;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Plan reutilizable de recría (etapas, sugerencias de receta y recordatorios) por empresa.
 */
@Entity
@Table(name = "porcinos_legacy_plan_recria")
public class PlanRecria {

    public enum PropositoPlan {
        ENGORDE, REPRODUCCION, MIXTO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnore
    private Empresa empresa;

    @NotBlank
    @Size(max = 200)
    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Size(max = 500)
    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Size(max = 120)
    @Column(name = "raza_objetivo", length = 120)
    private String razaObjetivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "proposito", nullable = false, length = 30)
    private PropositoPlan proposito = PropositoPlan.ENGORDE;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "planRecria", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<PlanRecriaEtapa> etapas = new ArrayList<>();

    @OneToMany(mappedBy = "planRecria", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<PlanRecriaRecetaSugerencia> recetasSugeridas = new ArrayList<>();

    @OneToMany(mappedBy = "planRecria", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<PlanRecriaRecordatorioSugerido> recordatoriosSugeridos = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getRazaObjetivo() { return razaObjetivo; }
    public void setRazaObjetivo(String razaObjetivo) { this.razaObjetivo = razaObjetivo; }
    public PropositoPlan getProposito() { return proposito; }
    public void setProposito(PropositoPlan proposito) { this.proposito = proposito; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    public List<PlanRecriaEtapa> getEtapas() { return etapas; }
    public void setEtapas(List<PlanRecriaEtapa> etapas) { this.etapas = etapas; }
    public List<PlanRecriaRecetaSugerencia> getRecetasSugeridas() { return recetasSugeridas; }
    public void setRecetasSugeridas(List<PlanRecriaRecetaSugerencia> recetasSugeridas) { this.recetasSugeridas = recetasSugeridas; }
    public List<PlanRecriaRecordatorioSugerido> getRecordatoriosSugeridos() { return recordatoriosSugeridos; }
    public void setRecordatoriosSugeridos(List<PlanRecriaRecordatorioSugerido> recordatoriosSugeridos) { this.recordatoriosSugeridos = recordatoriosSugeridos; }
}
