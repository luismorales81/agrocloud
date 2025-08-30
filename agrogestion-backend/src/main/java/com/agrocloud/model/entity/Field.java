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
 * Entidad que representa los campos en el sistema.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@Entity
@Table(name = "campos")
@EntityListeners(AuditingEntityListener.class)
public class Field {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del campo es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre del campo debe tener entre 2 y 100 caracteres")
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @NotBlank(message = "La ubicación es obligatoria")
    @Size(max = 200, message = "La ubicación no puede exceder 200 caracteres")
    @Column(name = "ubicacion", nullable = false, length = 200)
    private String ubicacion;

    @NotNull(message = "La superficie es obligatoria")
    @Positive(message = "La superficie debe ser un valor positivo")
    @Column(name = "area_hectareas", nullable = false, precision = 10, scale = 2)
    private BigDecimal areaHectareas;

    @Size(max = 100, message = "El tipo de suelo no puede exceder 100 caracteres")
    @Column(name = "tipo_suelo", length = 100)
    private String tipoSuelo;

    @Size(max = 100, message = "El estado del campo no puede exceder 100 caracteres")
    @Column(name = "estado", length = 100)
    private String estado = "ACTIVO";

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "poligono", columnDefinition = "TEXT")
    private String poligono;

    @Column(name = "coordenadas", columnDefinition = "JSON")
    private String coordenadas;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "campo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Plot> lotes = new ArrayList<>();

    // Relación con el usuario propietario
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Constructors
    public Field() {}

    public Field(String nombre, String ubicacion, BigDecimal areaHectareas) {
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.areaHectareas = areaHectareas;
        this.activo = true;
        this.estado = "ACTIVO";
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

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public BigDecimal getAreaHectareas() {
        return areaHectareas;
    }

    public void setAreaHectareas(BigDecimal areaHectareas) {
        this.areaHectareas = areaHectareas;
    }

    public String getTipoSuelo() {
        return tipoSuelo;
    }

    public void setTipoSuelo(String tipoSuelo) {
        this.tipoSuelo = tipoSuelo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getPoligono() {
        return poligono;
    }

    public void setPoligono(String poligono) {
        this.poligono = poligono;
    }

    public String getCoordenadas() {
        return coordenadas;
    }

    public void setCoordenadas(String coordenadas) {
        this.coordenadas = coordenadas;
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

    public List<Plot> getLotes() {
        return lotes;
    }

    public void setLotes(List<Plot> lotes) {
        this.lotes = lotes;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // Helper methods
    public void addLote(Plot lote) {
        lotes.add(lote);
        lote.setCampo(this);
    }

    public void removeLote(Plot lote) {
        lotes.remove(lote);
        lote.setCampo(null);
    }

    @Override
    public String toString() {
        return "Field{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", areaHectareas=" + areaHectareas +
                ", estado='" + estado + '\'' +
                ", activo=" + activo +
                '}';
    }
}
