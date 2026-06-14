package com.agrocloud.porcinos.domain;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.domain.User;
import com.agrocloud.porcinos.domain.CausaMortalidadPorcino;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa la muerte de un lechón (nacimiento o lactancia)
 */
@Entity
@Table(name = "porcinos_muertes_lactancia")
@EntityListeners(AuditingEntityListener.class)
public class MuerteLechon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "madre_id", nullable = false)
    private Madre madre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parto_id", nullable = false)
    private Parto parto;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "etapa", nullable = false)
    private EtapaLechon etapa;

    @Enumerated(EnumType.STRING)
    @Column(name = "causa", nullable = false)
    private CausaMuerteLechon causa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "causa_mortalidad_id")
    @JsonIgnoreProperties({"empresa"})
    private CausaMortalidadPorcino causaMortalidad;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad = 1;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnoreProperties({"userCompanyRoles", "usuarioEmpresas", "userRoles", "password", "resetPasswordToken", "resetPasswordTokenExpiry", "verificationToken"})
    private User usuario;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    public enum EtapaLechon {
        NACIMIENTO, LACTANCIA
    }

    public enum CausaMuerteLechon {
        APLASTAMIENTO, DIARREA_NEONATAL, HIPOTERMIA, INANICION, DEFORMIDAD, INFECCION, DIARREA_POSDESTETE, DESCONOCIDA
    }

    public MuerteLechon() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Madre getMadre() { return madre; }
    public void setMadre(Madre madre) { this.madre = madre; }
    public Parto getParto() { return parto; }
    public void setParto(Parto parto) { this.parto = parto; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public EtapaLechon getEtapa() { return etapa; }
    public void setEtapa(EtapaLechon etapa) { this.etapa = etapa; }
    public CausaMuerteLechon getCausa() { return causa; }
    public void setCausa(CausaMuerteLechon causa) { this.causa = causa; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }
    public User getUsuario() { return usuario; }
    public void setUsuario(User usuario) { this.usuario = usuario; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public CausaMortalidadPorcino getCausaMortalidad() { return causaMortalidad; }
    public void setCausaMortalidad(CausaMortalidadPorcino causaMortalidad) { this.causaMortalidad = causaMortalidad; }
}
