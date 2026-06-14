package com.agrocloud.porcinos.domain;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.domain.User;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "porcinos_gestacion")
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({"madre", "servicio"})
public class Gestacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "madre_id", nullable = false)
    @JsonIgnore
    private Madre madre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicio_id")
    @JsonIgnore
    private Servicio servicio;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_probable_parto", nullable = false)
    private LocalDate fechaProbableParto;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoGestacion estado = EstadoGestacion.EN_CURSO;

    @Column(name = "fecha_aborto")
    private LocalDate fechaAborto;

    @Column(name = "causa_aborto", length = 200)
    private String causaAborto;

    @Column(name = "fecha_sala_maternidad")
    private LocalDate fechaSalaMaternidad;

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

    @OneToMany(mappedBy = "gestacion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ChequeoGestacion> chequeos = new ArrayList<>();

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    public enum EstadoGestacion {
        EN_CURSO, ABORTO, FINALIZADA
    }

    public Gestacion() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Madre getMadre() { return madre; }
    public void setMadre(Madre madre) { this.madre = madre; }
    public Servicio getServicio() { return servicio; }
    public void setServicio(Servicio servicio) { this.servicio = servicio; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDate getFechaProbableParto() { return fechaProbableParto; }
    public void setFechaProbableParto(LocalDate fechaProbableParto) { this.fechaProbableParto = fechaProbableParto; }
    public EstadoGestacion getEstado() { return estado; }
    public void setEstado(EstadoGestacion estado) { this.estado = estado; }
    public LocalDate getFechaAborto() { return fechaAborto; }
    public void setFechaAborto(LocalDate fechaAborto) { this.fechaAborto = fechaAborto; }
    public String getCausaAborto() { return causaAborto; }
    public void setCausaAborto(String causaAborto) { this.causaAborto = causaAborto; }
    public LocalDate getFechaSalaMaternidad() { return fechaSalaMaternidad; }
    public void setFechaSalaMaternidad(LocalDate fechaSalaMaternidad) { this.fechaSalaMaternidad = fechaSalaMaternidad; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }
    public User getUsuario() { return usuario; }
    public void setUsuario(User usuario) { this.usuario = usuario; }
    public List<ChequeoGestacion> getChequeos() { return chequeos; }
    public void setChequeos(List<ChequeoGestacion> chequeos) { this.chequeos = chequeos; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    @JsonGetter("madreId")
    public Long getMadreId() {
        return madre != null ? madre.getId() : null;
    }

    @JsonGetter("madreIdentificacion")
    public String getMadreIdentificacion() {
        return madre != null ? madre.getIdentificacion() : null;
    }
}
