package com.agrocloud.cultivos.domain;

import com.agrocloud.core.domain.Empresa;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un estado configurado para un tipo de cultivo
 */
@Entity
@Table(name = "cultivo_estados_lote",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_tipo_empresa_nombre",
           columnNames = {"tipo_cultivo_id", "empresa_id", "nombre"}
       ))
@EntityListeners(AuditingEntityListener.class)
public class EstadoLoteConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_cultivo_id")
    @JsonIgnore
    private TipoCultivo tipoCultivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id")
    @JsonIgnore
    private Empresa empresa;

    @NotBlank(message = "El nombre del estado es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Size(max = 20, message = "El color no puede exceder 20 caracteres")
    @Column(name = "color", length = 20)
    private String color = "#10b981";

    @Size(max = 10, message = "El icono no puede exceder 10 caracteres")
    @Column(name = "icono", length = 10)
    private String icono;

    @NotNull(message = "El orden es obligatorio")
    @Column(name = "orden", nullable = false)
    private Integer orden;

    @Column(name = "es_estado_inicial", nullable = false)
    private Boolean esEstadoInicial = false;

    @Column(name = "es_estado_final", nullable = false)
    private Boolean esEstadoFinal = false;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "estadoOrigen", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<TransicionEstadoConfig> transicionesOrigen = new ArrayList<>();

    @OneToMany(mappedBy = "estadoDestino", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<TransicionEstadoConfig> transicionesDestino = new ArrayList<>();

    @OneToMany(mappedBy = "estado", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<TareaPorEstadoConfig> tareas = new ArrayList<>();

    public EstadoLoteConfig() {}

    public EstadoLoteConfig(String nombre, String descripcion, Integer orden) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.orden = orden;
        this.activo = true;
        this.esEstadoInicial = false;
        this.esEstadoFinal = false;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public TipoCultivo getTipoCultivo() { return tipoCultivo; }
    public void setTipoCultivo(TipoCultivo tipoCultivo) { this.tipoCultivo = tipoCultivo; }
    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public String getIcono() { return icono; }
    public void setIcono(String icono) { this.icono = icono; }
    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }
    public Boolean getEsEstadoInicial() { return esEstadoInicial; }
    public void setEsEstadoInicial(Boolean esEstadoInicial) { this.esEstadoInicial = esEstadoInicial; }
    public Boolean getEsEstadoFinal() { return esEstadoFinal; }
    public void setEsEstadoFinal(Boolean esEstadoFinal) { this.esEstadoFinal = esEstadoFinal; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    public List<TransicionEstadoConfig> getTransicionesOrigen() { return transicionesOrigen; }
    public void setTransicionesOrigen(List<TransicionEstadoConfig> transicionesOrigen) { this.transicionesOrigen = transicionesOrigen; }
    public List<TransicionEstadoConfig> getTransicionesDestino() { return transicionesDestino; }
    public void setTransicionesDestino(List<TransicionEstadoConfig> transicionesDestino) { this.transicionesDestino = transicionesDestino; }
    public List<TareaPorEstadoConfig> getTareas() { return tareas; }
    public void setTareas(List<TareaPorEstadoConfig> tareas) { this.tareas = tareas; }
    public Long getTipoCultivoId() { return tipoCultivo != null ? tipoCultivo.getId() : null; }
    public Long getEmpresaId() { return empresa != null ? empresa.getId() : null; }
}
