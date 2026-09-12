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
 * Entidad que representa un registro de faena.
 * @deprecated Unificado con VentaPorcino (tipo FAENA). Mantenido por datos históricos.
 */
@Deprecated
@Entity
@Table(name = "porcinos_legacy_faena")
@EntityListeners(AuditingEntityListener.class)
public class Faena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recria_id", nullable = false)
    private Recria recria;

    @Column(name = "fecha_envio", nullable = false)
    private LocalDate fechaEnvio;

    @Column(name = "fecha_faena")
    private LocalDate fechaFaena;

    @Column(name = "peso_envio", precision = 10, scale = 2, nullable = false)
    private BigDecimal pesoEnvio;

    @Column(name = "peso_faena", precision = 10, scale = 2)
    private BigDecimal pesoFaena;

    @Column(name = "rendimiento", precision = 5, scale = 2)
    private BigDecimal rendimiento;

    @Column(name = "precio_kg", precision = 10, scale = 2)
    private BigDecimal precioKg;

    @Column(name = "ingreso_total", precision = 12, scale = 2)
    private BigDecimal ingresoTotal;

    @Column(name = "cantidad_animales", nullable = false)
    private Integer cantidadAnimales;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnoreProperties({"userCompanyRoles", "usuarioEmpresas", "userRoles", "password", "resetPasswordToken", "resetPasswordTokenExpiry", "verificationToken"})
    private User usuario;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    public Faena() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Recria getRecria() { return recria; }
    public void setRecria(Recria recria) { this.recria = recria; }
    public LocalDate getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(LocalDate fechaEnvio) { this.fechaEnvio = fechaEnvio; }
    public LocalDate getFechaFaena() { return fechaFaena; }
    public void setFechaFaena(LocalDate fechaFaena) { this.fechaFaena = fechaFaena; }
    public BigDecimal getPesoEnvio() { return pesoEnvio; }
    public void setPesoEnvio(BigDecimal pesoEnvio) { this.pesoEnvio = pesoEnvio; }
    public BigDecimal getPesoFaena() { return pesoFaena; }
    public void setPesoFaena(BigDecimal pesoFaena) { this.pesoFaena = pesoFaena; }
    public BigDecimal getRendimiento() { return rendimiento; }
    public void setRendimiento(BigDecimal rendimiento) { this.rendimiento = rendimiento; }
    public BigDecimal getPrecioKg() { return precioKg; }
    public void setPrecioKg(BigDecimal precioKg) { this.precioKg = precioKg; }
    public BigDecimal getIngresoTotal() { return ingresoTotal; }
    public void setIngresoTotal(BigDecimal ingresoTotal) { this.ingresoTotal = ingresoTotal; }
    public Integer getCantidadAnimales() { return cantidadAnimales; }
    public void setCantidadAnimales(Integer cantidadAnimales) { this.cantidadAnimales = cantidadAnimales; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }
    public User getUsuario() { return usuario; }
    public void setUsuario(User usuario) { this.usuario = usuario; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
