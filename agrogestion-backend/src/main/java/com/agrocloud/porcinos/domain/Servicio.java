package com.agrocloud.porcinos.domain;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.domain.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "porcinos_servicios")
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({"madre", "empresa", "usuario"})
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "madre_id", nullable = false)
    @JsonIgnore
    private Madre madre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoServicio tipo;

    @Column(name = "fecha_servicio", nullable = false)
    private LocalDate fechaServicio;

    @Column(name = "macho_id")
    private Long machoId;

    @Column(name = "macho_nombre", length = 255)
    private String machoNombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "origen_semen")
    private OrigenSemen origenSemen;

    @Column(name = "numero_intento", nullable = false)
    private Integer numeroIntento = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_servicio", nullable = false)
    private EstadoServicio estadoServicio = EstadoServicio.PENDIENTE_CONTROL;

    @Column(name = "fecha_control_celo", nullable = false)
    private LocalDate fechaControlCelo;

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

    public enum TipoServicio {
        MONTA_NATURAL, IA
    }

    public enum OrigenSemen {
        INTERNO, EXTERNO
    }

    public enum EstadoServicio {
        PENDIENTE_CONTROL, FALLIDO, PREÑEZ_CONFIRMADA
    }

    public Servicio() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Madre getMadre() { return madre; }
    public void setMadre(Madre madre) { this.madre = madre; }
    public TipoServicio getTipo() { return tipo; }
    public void setTipo(TipoServicio tipo) { this.tipo = tipo; }
    public LocalDate getFechaServicio() { return fechaServicio; }
    public void setFechaServicio(LocalDate fechaServicio) { this.fechaServicio = fechaServicio; }
    public Long getMachoId() { return machoId; }
    public void setMachoId(Long machoId) { this.machoId = machoId; }
    public String getMachoNombre() { return machoNombre; }
    public void setMachoNombre(String machoNombre) { this.machoNombre = machoNombre; }
    public OrigenSemen getOrigenSemen() { return origenSemen; }
    public void setOrigenSemen(OrigenSemen origenSemen) { this.origenSemen = origenSemen; }
    public Integer getNumeroIntento() { return numeroIntento; }
    public void setNumeroIntento(Integer numeroIntento) { this.numeroIntento = numeroIntento; }
    public EstadoServicio getEstadoServicio() { return estadoServicio; }
    public void setEstadoServicio(EstadoServicio estadoServicio) { this.estadoServicio = estadoServicio; }
    public LocalDate getFechaControlCelo() { return fechaControlCelo; }
    public void setFechaControlCelo(LocalDate fechaControlCelo) { this.fechaControlCelo = fechaControlCelo; }
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

    @JsonProperty("madreId")
    public Long getMadreId() {
        return madre != null ? madre.getId() : null;
    }
}
