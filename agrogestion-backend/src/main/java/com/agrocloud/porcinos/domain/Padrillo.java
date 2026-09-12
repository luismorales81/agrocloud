package com.agrocloud.porcinos.domain;

import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.Empresa;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa un Padrillo (macho reproductor)
 */
@Entity
@Table(name = "porcinos_legacy_padrillos")
@EntityListeners(AuditingEntityListener.class)
public class Padrillo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "identificacion", nullable = false, unique = true, length = 100)
    private String identificacion;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "origen", nullable = false)
    private OrigenPadrillo origen = OrigenPadrillo.EXTERNA;

    @Column(name = "fecha_ingreso_granja", nullable = false)
    private LocalDate fechaIngresoGranja;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raza_id")
    @JsonIgnore
    private RazaPorcino raza;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_genetica_id")
    @JsonIgnore
    private ProveedorGenetica proveedorGenetica;

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

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    public enum OrigenPadrillo {
        EXTERNA, INTERNA
    }

    public Padrillo() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getIdentificacion() { return identificacion; }
    public void setIdentificacion(String identificacion) { this.identificacion = identificacion; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public OrigenPadrillo getOrigen() { return origen; }
    public void setOrigen(OrigenPadrillo origen) { this.origen = origen; }
    public LocalDate getFechaIngresoGranja() { return fechaIngresoGranja; }
    public void setFechaIngresoGranja(LocalDate fechaIngresoGranja) { this.fechaIngresoGranja = fechaIngresoGranja; }
    public LocalDate getFechaBaja() { return fechaBaja; }
    public void setFechaBaja(LocalDate fechaBaja) { this.fechaBaja = fechaBaja; }
    public MotivoBajaPorcino getMotivoBaja() { return motivoBaja; }
    public void setMotivoBaja(MotivoBajaPorcino motivoBaja) { this.motivoBaja = motivoBaja; }
    public RazaPorcino getRaza() { return raza; }
    public void setRaza(RazaPorcino raza) { this.raza = raza; }
    public ProveedorGenetica getProveedorGenetica() { return proveedorGenetica; }
    public void setProveedorGenetica(ProveedorGenetica proveedorGenetica) { this.proveedorGenetica = proveedorGenetica; }
    public UbicacionInterna getUbicacionInterna() { return ubicacionInterna; }
    public void setUbicacionInterna(UbicacionInterna ubicacionInterna) { this.ubicacionInterna = ubicacionInterna; }
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
