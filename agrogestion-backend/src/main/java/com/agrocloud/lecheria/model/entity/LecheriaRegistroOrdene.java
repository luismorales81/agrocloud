package com.agrocloud.lecheria.model.entity;

import com.agrocloud.lecheria.model.enums.LecheriaTurnoOrdene;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "lecheria_registro_ordene")
@EntityListeners(AuditingEntityListener.class)
public class LecheriaRegistroOrdene {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "animal_id", nullable = false)
    private LecheriaAnimal animal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lactancia_id", nullable = false)
    private LecheriaLactancia lactancia;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "turno", nullable = false, length = 10)
    private LecheriaTurnoOrdene turno;

    @Column(name = "litros", nullable = false, precision = 10, scale = 3)
    private BigDecimal litros;

    @Column(name = "grasa_pct", precision = 5, scale = 2)
    private BigDecimal grasaPct;

    @Column(name = "proteina_pct", precision = 5, scale = 2)
    private BigDecimal proteinaPct;

    @Column(name = "rcs")
    private Long rcs;

    @Column(name = "temperatura_ambiente", precision = 5, scale = 2)
    private BigDecimal temperaturaAmbiente;

    @Column(name = "humedad_ambiente", precision = 5, scale = 2)
    private BigDecimal humedadAmbiente;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LecheriaAnimal getAnimal() { return animal; }
    public void setAnimal(LecheriaAnimal animal) { this.animal = animal; }
    public LecheriaLactancia getLactancia() { return lactancia; }
    public void setLactancia(LecheriaLactancia lactancia) { this.lactancia = lactancia; }
    public Long getEmpresaId() { return empresaId; }
    public void setEmpresaId(Long empresaId) { this.empresaId = empresaId; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public LecheriaTurnoOrdene getTurno() { return turno; }
    public void setTurno(LecheriaTurnoOrdene turno) { this.turno = turno; }
    public BigDecimal getLitros() { return litros; }
    public void setLitros(BigDecimal litros) { this.litros = litros; }
    public BigDecimal getGrasaPct() { return grasaPct; }
    public void setGrasaPct(BigDecimal grasaPct) { this.grasaPct = grasaPct; }
    public BigDecimal getProteinaPct() { return proteinaPct; }
    public void setProteinaPct(BigDecimal proteinaPct) { this.proteinaPct = proteinaPct; }
    public Long getRcs() { return rcs; }
    public void setRcs(Long rcs) { this.rcs = rcs; }
    public BigDecimal getTemperaturaAmbiente() { return temperaturaAmbiente; }
    public void setTemperaturaAmbiente(BigDecimal temperaturaAmbiente) { this.temperaturaAmbiente = temperaturaAmbiente; }
    public BigDecimal getHumedadAmbiente() { return humedadAmbiente; }
    public void setHumedadAmbiente(BigDecimal humedadAmbiente) { this.humedadAmbiente = humedadAmbiente; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
