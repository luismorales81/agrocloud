package com.agrocloud.porcinos.domain;

import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.Empresa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa la muerte de animales en recría/engorde
 */
@Entity
@Table(name = "porcinos_muertes_recria")
@EntityListeners(AuditingEntityListener.class)
public class MuerteRecria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recria_id", nullable = false)
    private Recria recria;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "causa", nullable = false)
    private CausaMuerteRecria causa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "causa_mortalidad_id")
    @JsonIgnoreProperties({"empresa"})
    private CausaMortalidadPorcino causaMortalidad;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad = 1;

    @Column(name = "peso_promedio", precision = 10, scale = 2)
    private BigDecimal pesoPromedioSiAplica;

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

    public enum CausaMuerteRecria {
        NEUMONIA, DIARREA, GOLPE, ACCIDENTE, CANIBALISMO, APLASTAMIENTO, INANICION, DEBILIDAD, INTOXICACION, GOLPE_CALOR, DESCONOCIDA
    }

    public MuerteRecria() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Recria getRecria() { return recria; }
    public void setRecria(Recria recria) { this.recria = recria; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public CausaMuerteRecria getCausa() { return causa; }
    public void setCausa(CausaMuerteRecria causa) { this.causa = causa; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public BigDecimal getPesoPromedioSiAplica() { return pesoPromedioSiAplica; }
    public void setPesoPromedioSiAplica(BigDecimal pesoPromedioSiAplica) { this.pesoPromedioSiAplica = pesoPromedioSiAplica; }
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
