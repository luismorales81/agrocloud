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
 * Entidad que representa una reabsorción embrionaria.
 * Evento reproductivo que indica la pérdida de embriones durante la gestación.
 */
@Entity
@Table(name = "porcinos_legacy_reabsorciones")
@EntityListeners(AuditingEntityListener.class)
public class Reabsorcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gestacion_id", nullable = false)
    private Gestacion gestacion;

    @Column(name = "fecha_deteccion", nullable = false)
    private LocalDate fechaDeteccion;

    @Column(name = "dias_gestacion")
    private Integer diasGestacion;

    @Column(name = "cantidad_embriones")
    private Integer cantidadEmbriones;

    @Column(name = "causa_probable", length = 200)
    private String causaProbable;

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

    public Reabsorcion() {}

    public Reabsorcion(Gestacion gestacion, LocalDate fechaDeteccion, Empresa empresa, User usuario) {
        this.gestacion = gestacion;
        this.fechaDeteccion = fechaDeteccion;
        this.empresa = empresa;
        this.usuario = usuario;
        this.activo = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Gestacion getGestacion() { return gestacion; }
    public void setGestacion(Gestacion gestacion) { this.gestacion = gestacion; }
    public LocalDate getFechaDeteccion() { return fechaDeteccion; }
    public void setFechaDeteccion(LocalDate fechaDeteccion) { this.fechaDeteccion = fechaDeteccion; }
    public Integer getDiasGestacion() { return diasGestacion; }
    public void setDiasGestacion(Integer diasGestacion) { this.diasGestacion = diasGestacion; }
    public Integer getCantidadEmbriones() { return cantidadEmbriones; }
    public void setCantidadEmbriones(Integer cantidadEmbriones) { this.cantidadEmbriones = cantidadEmbriones; }
    public String getCausaProbable() { return causaProbable; }
    public void setCausaProbable(String causaProbable) { this.causaProbable = causaProbable; }
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
