package com.agrocloud.lecheria.model.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "lecheria_lactancia")
@EntityListeners(AuditingEntityListener.class)
public class LecheriaLactancia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "animal_id", nullable = false)
    private LecheriaAnimal animal;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "numero_lactancia", nullable = false)
    private Integer numeroLactancia = 1;

    @Column(name = "fecha_parto", nullable = false)
    private LocalDate fechaParto;

    @Column(name = "fecha_secado")
    private LocalDate fechaSecado;

    @Column(name = "activa", nullable = false)
    private Boolean activa = true;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LecheriaAnimal getAnimal() { return animal; }
    public void setAnimal(LecheriaAnimal animal) { this.animal = animal; }
    public Long getEmpresaId() { return empresaId; }
    public void setEmpresaId(Long empresaId) { this.empresaId = empresaId; }
    public Integer getNumeroLactancia() { return numeroLactancia; }
    public void setNumeroLactancia(Integer numeroLactancia) { this.numeroLactancia = numeroLactancia; }
    public LocalDate getFechaParto() { return fechaParto; }
    public void setFechaParto(LocalDate fechaParto) { this.fechaParto = fechaParto; }
    public LocalDate getFechaSecado() { return fechaSecado; }
    public void setFechaSecado(LocalDate fechaSecado) { this.fechaSecado = fechaSecado; }
    public Boolean getActiva() { return activa; }
    public void setActiva(Boolean activa) { this.activa = activa; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
