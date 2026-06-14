package com.agrocloud.cultivos.domain;

import com.agrocloud.core.domain.Empresa;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entidad que representa una transición permitida entre dos estados
 */
@Entity
@Table(name = "cultivo_transiciones_estado",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_transicion",
           columnNames = {"empresa_id", "estado_origen_id", "estado_destino_id"}
       ))
@EntityListeners(AuditingEntityListener.class)
public class TransicionEstadoConfig {

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

    @NotNull(message = "El estado origen es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estado_origen_id", nullable = false)
    @JsonIgnore
    private EstadoLoteConfig estadoOrigen;

    @NotNull(message = "El estado destino es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estado_destino_id", nullable = false)
    @JsonIgnore
    private EstadoLoteConfig estadoDestino;

    @Column(name = "requiere_motivo", nullable = false)
    private Boolean requiereMotivo = false;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    public TransicionEstadoConfig() {}

    public TransicionEstadoConfig(EstadoLoteConfig estadoOrigen, EstadoLoteConfig estadoDestino) {
        this.estadoOrigen = estadoOrigen;
        this.estadoDestino = estadoDestino;
        this.requiereMotivo = false;
        this.activo = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public TipoCultivo getTipoCultivo() { return tipoCultivo; }
    public void setTipoCultivo(TipoCultivo tipoCultivo) { this.tipoCultivo = tipoCultivo; }
    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }
    public EstadoLoteConfig getEstadoOrigen() { return estadoOrigen; }
    public void setEstadoOrigen(EstadoLoteConfig estadoOrigen) { this.estadoOrigen = estadoOrigen; }
    public EstadoLoteConfig getEstadoDestino() { return estadoDestino; }
    public void setEstadoDestino(EstadoLoteConfig estadoDestino) { this.estadoDestino = estadoDestino; }
    public Boolean getRequiereMotivo() { return requiereMotivo; }
    public void setRequiereMotivo(Boolean requiereMotivo) { this.requiereMotivo = requiereMotivo; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    public Long getTipoCultivoId() { return tipoCultivo != null ? tipoCultivo.getId() : null; }
    public Long getEmpresaId() { return empresa != null ? empresa.getId() : null; }
    public Long getEstadoOrigenId() { return estadoOrigen != null ? estadoOrigen.getId() : null; }
    public Long getEstadoDestinoId() { return estadoDestino != null ? estadoDestino.getId() : null; }
    public String getEstadoOrigenNombre() { return estadoOrigen != null ? estadoOrigen.getNombre() : null; }
    public String getEstadoDestinoNombre() { return estadoDestino != null ? estadoDestino.getNombre() : null; }
}
