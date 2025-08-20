package com.agrocloud.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa los lotes dentro de los campos.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@Entity
@Table(name = "lotes")
@EntityListeners(AuditingEntityListener.class)
public class Plot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del lote es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre del lote debe tener entre 2 y 100 caracteres")
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @NotNull(message = "La superficie es obligatoria")
    @Positive(message = "La superficie debe ser un valor positivo")
    @Column(name = "area_hectareas", nullable = false, precision = 10, scale = 2)
    private BigDecimal areaHectareas;

    @Size(max = 100, message = "El estado del lote no puede exceder 100 caracteres")
    @Column(name = "estado", length = 100)
    private String estado = "DISPONIBLE";

    @Size(max = 100, message = "El tipo de suelo no puede exceder 100 caracteres")
    @Column(name = "tipo_suelo", length = 100)
    private String tipoSuelo;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campo_id")
    private Field campo;

    @OneToMany(mappedBy = "lote", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Labor> labores = new ArrayList<>();

    // Constructors
    public Plot() {}

    public Plot(String nombre, BigDecimal areaHectareas, Field campo) {
        this.nombre = nombre;
        this.areaHectareas = areaHectareas;
        this.campo = campo;
        this.activo = true;
        this.estado = "DISPONIBLE";
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getAreaHectareas() {
        return areaHectareas;
    }

    public void setAreaHectareas(BigDecimal areaHectareas) {
        this.areaHectareas = areaHectareas;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getTipoSuelo() {
        return tipoSuelo;
    }

    public void setTipoSuelo(String tipoSuelo) {
        this.tipoSuelo = tipoSuelo;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public Field getCampo() {
        return campo;
    }

    public void setCampo(Field campo) {
        this.campo = campo;
    }

    public List<Labor> getLabores() {
        return labores;
    }

    public void setLabores(List<Labor> labores) {
        this.labores = labores;
    }

    // Helper methods
    public void addLabor(Labor labor) {
        labores.add(labor);
        labor.setLote(this);
    }

    public void removeLabor(Labor labor) {
        labores.remove(labor);
        labor.setLote(null);
    }

    @Override
    public String toString() {
        return "Plot{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", areaHectareas=" + areaHectareas +
                ", estado='" + estado + '\'' +
                ", activo=" + activo +
                '}';
    }
}
