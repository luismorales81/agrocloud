package com.agrocloud.porcinos.domain;

import com.agrocloud.core.domain.Empresa;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa una ubicación interna de la granja con estructura jerárquica
 * Estructura: Galpón → Sala → Corral
 */
@Entity
@Table(name = "porcinos_legacy_ubicaciones_internas")
@EntityListeners(AuditingEntityListener.class)
public class UbicacionInterna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "codigo", length = 50)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel", nullable = false)
    private NivelUbicacion nivel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ubicacion_padre_id")
    @JsonIgnore
    private UbicacionInterna ubicacionPadre;

    @Transient
    @JsonProperty("ubicacionPadreId")
    private Long ubicacionPadreId;

    @OneToMany(mappedBy = "ubicacionPadre", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<UbicacionInterna> ubicacionesHijas = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_ubicacion")
    private TipoUbicacion tipoUbicacion;

    @Column(name = "capacidad_maxima")
    private Integer capacidadMaxima;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnore
    private Empresa empresa;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    public enum NivelUbicacion {
        GALPON, SALA, CORRAL
    }

    public enum TipoUbicacion {
        MATERNIDAD, GESTACION, RECRIA, ENGORDE, AISLAMIENTO, GENERAL
    }

    public UbicacionInterna() {}

    public UbicacionInterna(String nombre, NivelUbicacion nivel, Empresa empresa) {
        this.nombre = nombre;
        this.nivel = nivel;
        this.empresa = empresa;
        this.activo = true;
    }

    public Long getUbicacionPadreId() {
        return ubicacionPadreId != null ? ubicacionPadreId : (ubicacionPadre != null ? ubicacionPadre.getId() : null);
    }

    public void setUbicacionPadreId(Long ubicacionPadreId) {
        this.ubicacionPadreId = ubicacionPadreId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public NivelUbicacion getNivel() { return nivel; }
    public void setNivel(NivelUbicacion nivel) { this.nivel = nivel; }
    public UbicacionInterna getUbicacionPadre() { return ubicacionPadre; }
    public void setUbicacionPadre(UbicacionInterna ubicacionPadre) { this.ubicacionPadre = ubicacionPadre; }
    public List<UbicacionInterna> getUbicacionesHijas() { return ubicacionesHijas; }
    public void setUbicacionesHijas(List<UbicacionInterna> ubicacionesHijas) { this.ubicacionesHijas = ubicacionesHijas; }
    public TipoUbicacion getTipoUbicacion() { return tipoUbicacion; }
    public void setTipoUbicacion(TipoUbicacion tipoUbicacion) { this.tipoUbicacion = tipoUbicacion; }
    public Integer getCapacidadMaxima() { return capacidadMaxima; }
    public void setCapacidadMaxima(Integer capacidadMaxima) { this.capacidadMaxima = capacidadMaxima; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    public String getRutaCompleta() {
        StringBuilder ruta = new StringBuilder(nombre);
        UbicacionInterna padre = ubicacionPadre;
        while (padre != null) {
            ruta.insert(0, padre.getNombre() + " > ");
            padre = padre.getUbicacionPadre();
        }
        return ruta.toString();
    }
}
