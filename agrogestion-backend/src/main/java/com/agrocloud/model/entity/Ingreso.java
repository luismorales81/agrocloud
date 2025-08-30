package com.agrocloud.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa los ingresos/beneficios en el sistema.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@Entity
@Table(name = "ingresos")
@EntityListeners(AuditingEntityListener.class)
public class Ingreso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El concepto del ingreso es obligatorio")
    @Size(min = 2, max = 200, message = "El concepto debe tener entre 2 y 200 caracteres")
    @Column(name = "concepto", nullable = false, length = 200)
    private String concepto;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @NotNull(message = "El tipo de ingreso es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_ingreso", nullable = false)
    private TipoIngreso tipoIngreso;

    @NotNull(message = "La fecha del ingreso es obligatoria")
    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso;

    @Positive(message = "El monto debe ser un valor positivo")
    @Column(name = "monto", nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    @Size(max = 100, message = "La unidad de medida no puede exceder 100 caracteres")
    @Column(name = "unidad_medida", length = 100)
    private String unidadMedida;

    @Positive(message = "La cantidad debe ser un valor positivo")
    @Column(name = "cantidad", precision = 10, scale = 2)
    private BigDecimal cantidad;

    @Size(max = 200, message = "El cliente/comprador no puede exceder 200 caracteres")
    @Column(name = "cliente_comprador", length = 200)
    private String clienteComprador;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoIngreso estado = EstadoIngreso.REGISTRADO;

    @Size(max = 1000, message = "Las observaciones no pueden exceder 1000 caracteres")
    @Column(name = "observaciones", length = 1000)
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id")
    private Plot lote;

    @NotNull(message = "El usuario es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Enums
    public enum TipoIngreso {
        VENTA_CULTIVO, VENTA_ANIMAL, SERVICIOS_AGRICOLAS, SUBSIDIOS, OTROS_INGRESOS
    }

    public enum EstadoIngreso {
        REGISTRADO, CONFIRMADO, CANCELADO
    }

    // Constructors
    public Ingreso() {}

    public Ingreso(String concepto, TipoIngreso tipoIngreso, LocalDate fechaIngreso, BigDecimal monto, User usuario) {
        this.concepto = concepto;
        this.tipoIngreso = tipoIngreso;
        this.fechaIngreso = fechaIngreso;
        this.monto = monto;
        this.usuario = usuario;
        this.estado = EstadoIngreso.REGISTRADO;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public TipoIngreso getTipoIngreso() {
        return tipoIngreso;
    }

    public void setTipoIngreso(TipoIngreso tipoIngreso) {
        this.tipoIngreso = tipoIngreso;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public String getClienteComprador() {
        return clienteComprador;
    }

    public void setClienteComprador(String clienteComprador) {
        this.clienteComprador = clienteComprador;
    }

    public EstadoIngreso getEstado() {
        return estado;
    }

    public void setEstado(EstadoIngreso estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Plot getLote() {
        return lote;
    }

    public void setLote(Plot lote) {
        this.lote = lote;
    }

    public User getUsuario() {
        return usuario;
    }

    public void setUsuario(User usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
}
