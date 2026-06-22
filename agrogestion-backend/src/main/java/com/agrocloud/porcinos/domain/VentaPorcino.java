package com.agrocloud.porcinos.domain;

import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.Empresa;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa una venta de cerdos (terminados o reproductores)
 */
@Entity
@Table(name = "porcinos_ventas_porcinos")
@EntityListeners(AuditingEntityListener.class)
public class VentaPorcino {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoVenta tipo;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "peso_promedio", precision = 10, scale = 2, nullable = false)
    private BigDecimal pesoPromedio;

    @Column(name = "precio_kg", precision = 10, scale = 2)
    private BigDecimal precioKg;

    @Column(name = "ingreso_total", precision = 15, scale = 2, nullable = false)
    private BigDecimal ingresoTotal;

    @Column(name = "lote_id")
    private Long loteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recria_id")
    private Recria recria;

    @Column(name = "cliente", length = 200)
    private String cliente;

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

    @Column(name = "campana_id")
    private Long campanaId;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "peso_envio", precision = 10, scale = 2)
    private BigDecimal pesoEnvio;

    @Column(name = "peso_faena", precision = 10, scale = 2)
    private BigDecimal pesoFaena;

    @Column(name = "rendimiento", precision = 5, scale = 2)
    private BigDecimal rendimiento;

    @Column(name = "fecha_envio")
    private LocalDate fechaEnvio;

    @Column(name = "fecha_faena")
    private LocalDate fechaFaena;

    public enum TipoVenta {
        ENGORDE, REPRODUCTOR, FAENA
    }

    public VentaPorcino() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public TipoVenta getTipo() { return tipo; }
    public void setTipo(TipoVenta tipo) { this.tipo = tipo; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public BigDecimal getPesoPromedio() { return pesoPromedio; }
    public void setPesoPromedio(BigDecimal pesoPromedio) { this.pesoPromedio = pesoPromedio; }
    public BigDecimal getPrecioKg() { return precioKg; }
    public void setPrecioKg(BigDecimal precioKg) { this.precioKg = precioKg; }
    public BigDecimal getIngresoTotal() { return ingresoTotal; }
    public void setIngresoTotal(BigDecimal ingresoTotal) { this.ingresoTotal = ingresoTotal; }
    public Long getLoteId() { return loteId; }
    public void setLoteId(Long loteId) { this.loteId = loteId; }
    public Recria getRecria() { return recria; }
    public void setRecria(Recria recria) { this.recria = recria; }
    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }
    public User getUsuario() { return usuario; }
    public void setUsuario(User usuario) { this.usuario = usuario; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public Long getCampanaId() { return campanaId; }
    public void setCampanaId(Long campanaId) { this.campanaId = campanaId; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    public BigDecimal getPesoEnvio() { return pesoEnvio; }
    public void setPesoEnvio(BigDecimal pesoEnvio) { this.pesoEnvio = pesoEnvio; }
    public BigDecimal getPesoFaena() { return pesoFaena; }
    public void setPesoFaena(BigDecimal pesoFaena) { this.pesoFaena = pesoFaena; }
    public BigDecimal getRendimiento() { return rendimiento; }
    public void setRendimiento(BigDecimal rendimiento) { this.rendimiento = rendimiento; }
    public LocalDate getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(LocalDate fechaEnvio) { this.fechaEnvio = fechaEnvio; }
    public LocalDate getFechaFaena() { return fechaFaena; }
    public void setFechaFaena(LocalDate fechaFaena) { this.fechaFaena = fechaFaena; }
}
