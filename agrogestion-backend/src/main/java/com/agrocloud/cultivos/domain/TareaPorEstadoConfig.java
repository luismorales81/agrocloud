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

/**
 * Entidad que representa una tarea/labor permitida en un estado específico
 */
@Entity
@Table(name = "cultivo_tareas_por_estado",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_estado_tarea",
           columnNames = {"estado_id", "tipo_labor"}
       ))
@EntityListeners(AuditingEntityListener.class)
public class TareaPorEstadoConfig {

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

    @NotNull(message = "El estado es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estado_id", nullable = false)
    @JsonIgnore
    private EstadoLoteConfig estado;

    @NotBlank(message = "El tipo de labor es obligatorio")
    @Size(max = 50, message = "El tipo de labor no puede exceder 50 caracteres")
    @Column(name = "tipo_labor", nullable = false, length = 50)
    private String tipoLabor;

    @NotBlank(message = "El nombre de la tarea es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    @Column(name = "nombre_tarea", nullable = false, length = 100)
    private String nombreTarea;

    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "es_obligatoria", nullable = false)
    private Boolean esObligatoria = false;

    @Column(name = "orden", nullable = false)
    private Integer orden = 0;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    public TareaPorEstadoConfig() {}

    public TareaPorEstadoConfig(EstadoLoteConfig estado, String tipoLabor, String nombreTarea) {
        this.estado = estado;
        this.tipoLabor = tipoLabor;
        this.nombreTarea = nombreTarea;
        this.esObligatoria = false;
        this.orden = 0;
        this.activo = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public TipoCultivo getTipoCultivo() { return tipoCultivo; }
    public void setTipoCultivo(TipoCultivo tipoCultivo) { this.tipoCultivo = tipoCultivo; }
    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }
    public EstadoLoteConfig getEstado() { return estado; }
    public void setEstado(EstadoLoteConfig estado) { this.estado = estado; }
    public String getTipoLabor() { return tipoLabor; }
    public void setTipoLabor(String tipoLabor) { this.tipoLabor = tipoLabor; }
    public String getNombreTarea() { return nombreTarea; }
    public void setNombreTarea(String nombreTarea) { this.nombreTarea = nombreTarea; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Boolean getEsObligatoria() { return esObligatoria; }
    public void setEsObligatoria(Boolean esObligatoria) { this.esObligatoria = esObligatoria; }
    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    public Long getTipoCultivoId() { return tipoCultivo != null ? tipoCultivo.getId() : null; }
    public Long getEmpresaId() { return empresa != null ? empresa.getId() : null; }
    public Long getEstadoId() { return estado != null ? estado.getId() : null; }
}
