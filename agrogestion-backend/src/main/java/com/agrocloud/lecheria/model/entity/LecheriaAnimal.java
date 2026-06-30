package com.agrocloud.lecheria.model.entity;

import com.agrocloud.lecheria.model.enums.LecheriaEspecie;
import com.agrocloud.lecheria.model.enums.LecheriaEstadoAnimal;
import com.agrocloud.lecheria.model.enums.LecheriaSexoAnimal;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "lecheria_animal")
@EntityListeners(AuditingEntityListener.class)
public class LecheriaAnimal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "identificacion", nullable = false, length = 100)
    private String identificacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "especie", nullable = false, length = 20)
    private LecheriaEspecie especie;

    @Enumerated(EnumType.STRING)
    @Column(name = "sexo", nullable = false, length = 10)
    private LecheriaSexoAnimal sexo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private LecheriaEstadoAnimal estado = LecheriaEstadoAnimal.VAQUILLONA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raza_id")
    private LecheriaRaza raza;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rodeo_id")
    private LecheriaRodeo rodeo;

    @Column(name = "campana_id")
    private Long campanaId;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso;

    @Column(name = "fecha_baja")
    private LocalDate fechaBaja;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEmpresaId() { return empresaId; }
    public void setEmpresaId(Long empresaId) { this.empresaId = empresaId; }
    public String getIdentificacion() { return identificacion; }
    public void setIdentificacion(String identificacion) { this.identificacion = identificacion; }
    public LecheriaEspecie getEspecie() { return especie; }
    public void setEspecie(LecheriaEspecie especie) { this.especie = especie; }
    public LecheriaSexoAnimal getSexo() { return sexo; }
    public void setSexo(LecheriaSexoAnimal sexo) { this.sexo = sexo; }
    public LecheriaEstadoAnimal getEstado() { return estado; }
    public void setEstado(LecheriaEstadoAnimal estado) { this.estado = estado; }
    public LecheriaRaza getRaza() { return raza; }
    public void setRaza(LecheriaRaza raza) { this.raza = raza; }
    public LecheriaRodeo getRodeo() { return rodeo; }
    public void setRodeo(LecheriaRodeo rodeo) { this.rodeo = rodeo; }
    public Long getCampanaId() { return campanaId; }
    public void setCampanaId(Long campanaId) { this.campanaId = campanaId; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public LocalDate getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDate fechaIngreso) { this.fechaIngreso = fechaIngreso; }
    public LocalDate getFechaBaja() { return fechaBaja; }
    public void setFechaBaja(LocalDate fechaBaja) { this.fechaBaja = fechaBaja; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
