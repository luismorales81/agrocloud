package com.agrocloud.porcinos.domain;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.domain.User;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa la muerte de una madre (cerda reproductora)
 */
@Entity
@Table(name = "porcinos_legacy_muertes_madres")
@EntityListeners(AuditingEntityListener.class)
public class MadreMuerte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "madre_id", nullable = false)
    private Madre madre;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "causa", nullable = false)
    private CausaMuerteMadre causa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "causa_mortalidad_id")
    @JsonIgnoreProperties({"empresa"})
    private CausaMortalidadPorcino causaMortalidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "motivo_baja_id")
    @JsonIgnoreProperties({"empresa"})
    private MotivoBajaPorcino motivoBaja;

    @Enumerated(EnumType.STRING)
    @Column(name = "etapa_momento", nullable = false)
    private EtapaMomento etapaMomento;

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

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    public enum CausaMuerteMadre {
        ENFERMEDAD, APLASTAMIENTO, ACCIDENTE, METRITIS, PROLAPSO, CAIDA, GOLPE, TORSION, INFECCION, EDAD, DESCONOCIDA
    }
    public enum EtapaMomento {
        CACHORRA, GESTACION, LACTANCIA, ADULTA
    }

    public MadreMuerte() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Madre getMadre() { return madre; }
    public void setMadre(Madre madre) { this.madre = madre; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public CausaMuerteMadre getCausa() { return causa; }
    public void setCausa(CausaMuerteMadre causa) { this.causa = causa; }
    public EtapaMomento getEtapaMomento() { return etapaMomento; }
    public void setEtapaMomento(EtapaMomento etapaMomento) { this.etapaMomento = etapaMomento; }
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
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    public CausaMortalidadPorcino getCausaMortalidad() { return causaMortalidad; }
    public void setCausaMortalidad(CausaMortalidadPorcino causaMortalidad) { this.causaMortalidad = causaMortalidad; }
    public MotivoBajaPorcino getMotivoBaja() { return motivoBaja; }
    public void setMotivoBaja(MotivoBajaPorcino motivoBaja) { this.motivoBaja = motivoBaja; }
}
