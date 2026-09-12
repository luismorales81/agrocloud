package com.agrocloud.porcinos.domain;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.domain.User;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa una transferencia de animales entre corrales/ubicaciones
 */
@Entity
@Table(name = "porcinos_legacy_transferencias_corral")
@EntityListeners(AuditingEntityListener.class)
public class TransferenciaCorral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recria_id", nullable = false)
    private Recria recria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ubicacion_origen_id")
    private UbicacionInterna ubicacionOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ubicacion_destino_id", nullable = false)
    private UbicacionInterna ubicacionDestino;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "cantidad_animales", nullable = false)
    private Integer cantidadAnimales;

    @Column(name = "motivo", length = 200)
    private String motivo;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnore
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnore
    private User usuario;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    public TransferenciaCorral() {}

    public TransferenciaCorral(Recria recria, UbicacionInterna ubicacionDestino,
                              LocalDate fecha, Integer cantidadAnimales, Empresa empresa, User usuario) {
        this.recria = recria;
        this.ubicacionDestino = ubicacionDestino;
        this.fecha = fecha;
        this.cantidadAnimales = cantidadAnimales;
        this.empresa = empresa;
        this.usuario = usuario;
        this.activo = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Recria getRecria() { return recria; }
    public void setRecria(Recria recria) { this.recria = recria; }
    public UbicacionInterna getUbicacionOrigen() { return ubicacionOrigen; }
    public void setUbicacionOrigen(UbicacionInterna ubicacionOrigen) { this.ubicacionOrigen = ubicacionOrigen; }
    public UbicacionInterna getUbicacionDestino() { return ubicacionDestino; }
    public void setUbicacionDestino(UbicacionInterna ubicacionDestino) { this.ubicacionDestino = ubicacionDestino; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public Integer getCantidadAnimales() { return cantidadAnimales; }
    public void setCantidadAnimales(Integer cantidadAnimales) { this.cantidadAnimales = cantidadAnimales; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }
    public User getUsuario() { return usuario; }
    public void setUsuario(User usuario) { this.usuario = usuario; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}
