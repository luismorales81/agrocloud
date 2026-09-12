package com.agrocloud.porcinos.domain;

import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.Empresa;
import com.agrocloud.porcinos.domain.MotivoBajaPorcino;
import com.agrocloud.porcinos.domain.RazaPorcino;
import com.agrocloud.porcinos.domain.UbicacionInterna;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa una Madre / Unidad productiva en el módulo de Porcinos
 */
@Entity
@Table(name = "porcinos_legacy_madres")
@EntityListeners(AuditingEntityListener.class)
public class Madre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "identificacion", nullable = false, unique = true, length = 100)
    private String identificacion;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(name = "cantidad_tetas", nullable = false)
    private Integer cantidadTetas = 14;

    @Enumerated(EnumType.STRING)
    @Column(name = "origen", nullable = false)
    private OrigenMadre origen = OrigenMadre.EXTERNA;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_actual", nullable = false)
    private EstadoMadre estadoActual = EstadoMadre.CACHORRA;

    @Column(name = "fecha_ingreso_granja", nullable = false)
    private LocalDate fechaIngresoGranja;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raza_id")
    @JsonIgnore
    private RazaPorcino raza;

    @Column(name = "numero_partos", nullable = false)
    private Integer numeroPartos = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ubicacion_interna_id")
    @JsonIgnore
    private UbicacionInterna ubicacionInterna;

    @Column(name = "fecha_baja")
    private LocalDate fechaBaja;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "motivo_baja_id")
    @JsonIgnore
    private MotivoBajaPorcino motivoBaja;

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

    @OneToMany(mappedBy = "madre", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<HistorialEstadoMadre> historialEstados = new ArrayList<>();

    @Transient
    private Gestacion gestacionActivaTransient;

    @Transient
    private Servicio ultimoServicioTransient;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    public enum OrigenMadre {
        EXTERNA, INTERNA
    }

    public enum EstadoMadre {
        CACHORRA, ADULTA, GESTACION, LACTANCIA, RECRIA, DESCARTE
    }

    public Madre() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getIdentificacion() { return identificacion; }
    public void setIdentificacion(String identificacion) { this.identificacion = identificacion; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public Integer getCantidadTetas() { return cantidadTetas; }
    public void setCantidadTetas(Integer cantidadTetas) { this.cantidadTetas = cantidadTetas; }
    public OrigenMadre getOrigen() { return origen; }
    public void setOrigen(OrigenMadre origen) { this.origen = origen; }
    public EstadoMadre getEstadoActual() { return estadoActual; }
    public void setEstadoActual(EstadoMadre estadoActual) { this.estadoActual = estadoActual; }
    public LocalDate getFechaIngresoGranja() { return fechaIngresoGranja; }
    public void setFechaIngresoGranja(LocalDate fechaIngresoGranja) { this.fechaIngresoGranja = fechaIngresoGranja; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }
    public User getUsuario() { return usuario; }
    public void setUsuario(User usuario) { this.usuario = usuario; }
    public List<HistorialEstadoMadre> getHistorialEstados() { return historialEstados; }
    public void setHistorialEstados(List<HistorialEstadoMadre> historialEstados) { this.historialEstados = historialEstados; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public RazaPorcino getRaza() { return raza; }
    public void setRaza(RazaPorcino raza) { this.raza = raza; }
    public Integer getNumeroPartos() { return numeroPartos; }
    public void setNumeroPartos(Integer numeroPartos) { this.numeroPartos = numeroPartos; }
    public UbicacionInterna getUbicacionInterna() { return ubicacionInterna; }
    public void setUbicacionInterna(UbicacionInterna ubicacionInterna) { this.ubicacionInterna = ubicacionInterna; }
    public LocalDate getFechaBaja() { return fechaBaja; }
    public void setFechaBaja(LocalDate fechaBaja) { this.fechaBaja = fechaBaja; }
    public MotivoBajaPorcino getMotivoBaja() { return motivoBaja; }
    public void setMotivoBaja(MotivoBajaPorcino motivoBaja) { this.motivoBaja = motivoBaja; }

    @JsonProperty("gestacionActiva")
    public Gestacion getGestacionActiva() { return gestacionActivaTransient; }
    public void setGestacionActivaTransient(Gestacion gestacionActivaTransient) { this.gestacionActivaTransient = gestacionActivaTransient; }

    @JsonProperty("ultimoServicio")
    public Servicio getUltimoServicio() { return ultimoServicioTransient; }
    public void setUltimoServicioTransient(Servicio ultimoServicioTransient) { this.ultimoServicioTransient = ultimoServicioTransient; }
}
