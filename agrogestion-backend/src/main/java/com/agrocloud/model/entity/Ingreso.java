package com.agrocloud.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad para gestionar los ingresos del sistema agropecuario
 */
@Entity
@Table(name = "ingresos")
public class Ingreso {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "concepto", nullable = false, length = 200)
    private String concepto;
    
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_ingreso", nullable = false)
    private TipoIngreso tipoIngreso;
    
    @Column(name = "monto", nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;
    
    @Column(name = "cantidad", precision = 10, scale = 2)
    private BigDecimal cantidad;
    
    @Column(name = "unidad_medida", length = 50)
    private String unidadMedida;
    
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;
    
    @Column(name = "cliente_comprador", length = 200)
    private String clienteComprador;
    
    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoIngreso estado;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id")
    @JsonIgnore
    private Plot lote;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;
    
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
    
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
    
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
    
    // Enum para tipos de ingreso
    public enum TipoIngreso {
        VENTA_CULTIVO,
        VENTA_ANIMAL,
        SERVICIOS_AGRICOLAS,
        SUBSIDIOS,
        OTROS_INGRESOS
    }
    
    // Enum para estados de ingreso
    public enum EstadoIngreso {
        REGISTRADO,
        CONFIRMADO,
        PAGADO,
        CANCELADO
    }
    
    // Constructor por defecto
    public Ingreso() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        this.estado = EstadoIngreso.REGISTRADO;
        this.activo = true;
    }
    
    // Constructor con campos obligatorios
    public Ingreso(String concepto, TipoIngreso tipoIngreso, BigDecimal monto, LocalDate fecha, User user) {
        this();
        this.concepto = concepto;
        this.tipoIngreso = tipoIngreso;
        this.monto = monto;
        this.fecha = fecha;
        this.user = user;
    }
    
    // Getters y Setters
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
    
    public BigDecimal getMonto() {
        return monto;
    }
    
    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }
    
    public BigDecimal getCantidad() {
        return cantidad;
    }
    
    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }
    
    public String getUnidadMedida() {
        return unidadMedida;
    }
    
    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }
    
    public LocalDate getFecha() {
        return fecha;
    }
    
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
    
    public String getClienteComprador() {
        return clienteComprador;
    }
    
    public void setClienteComprador(String clienteComprador) {
        this.clienteComprador = clienteComprador;
    }
    
    public String getObservaciones() {
        return observaciones;
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
    
    public EstadoIngreso getEstado() {
        return estado;
    }
    
    public void setEstado(EstadoIngreso estado) {
        this.estado = estado;
    }
    
    public Plot getLote() {
        return lote;
    }
    
    public void setLote(Plot lote) {
        this.lote = lote;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
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
    
    public Boolean getActivo() {
        return activo;
    }
    
    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
    
    // Método para actualizar fecha de modificación
    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}
