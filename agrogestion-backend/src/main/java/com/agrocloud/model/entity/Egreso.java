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
 * Entidad que representa los egresos/gastos en el sistema.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@Entity
@Table(name = "egresos")
@EntityListeners(AuditingEntityListener.class)
public class Egreso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El concepto del egreso es obligatorio")
    @Size(min = 2, max = 200, message = "El concepto debe tener entre 2 y 200 caracteres")
    @Column(name = "concepto", nullable = false, length = 200)
    private String concepto;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @NotNull(message = "El tipo de egreso es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_egreso", nullable = false)
    private TipoEgreso tipoEgreso;

    @NotNull(message = "La fecha del egreso es obligatoria")
    @Column(name = "fecha_egreso", nullable = false)
    private LocalDate fechaEgreso;

    @Positive(message = "El monto debe ser un valor positivo")
    @Column(name = "monto", nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    @Size(max = 100, message = "La unidad de medida no puede exceder 100 caracteres")
    @Column(name = "unidad_medida", length = 100)
    private String unidadMedida;

    @Positive(message = "La cantidad debe ser un valor positivo")
    @Column(name = "cantidad", precision = 10, scale = 2)
    private BigDecimal cantidad;

    @Size(max = 200, message = "El proveedor no puede exceder 200 caracteres")
    @Column(name = "proveedor", length = 200)
    private String proveedor;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoEgreso estado = EstadoEgreso.REGISTRADO;

    @Size(max = 1000, message = "Las observaciones no pueden exceder 1000 caracteres")
    @Column(name = "observaciones", length = 1000)
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id")
    private Plot lote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insumo_id")
    private Insumo insumo;

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
    public enum TipoEgreso {
        INSUMOS, COMBUSTIBLE, MANO_OBRA, MAQUINARIA, SERVICIOS, IMPUESTOS, OTROS_EGRESOS
    }

    public enum EstadoEgreso {
        REGISTRADO, PAGADO, CANCELADO
    }

    // Constructors
    public Egreso() {}

    public Egreso(String concepto, TipoEgreso tipoEgreso, LocalDate fechaEgreso, BigDecimal monto, User usuario) {
        this.concepto = concepto;
        this.tipoEgreso = tipoEgreso;
        this.fechaEgreso = fechaEgreso;
        this.monto = monto;
        this.usuario = usuario;
        this.estado = EstadoEgreso.REGISTRADO;
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

    public TipoEgreso getTipoEgreso() {
        return tipoEgreso;
    }

    public void setTipoEgreso(TipoEgreso tipoEgreso) {
        this.tipoEgreso = tipoEgreso;
    }

    public LocalDate getFechaEgreso() {
        return fechaEgreso;
    }

    public void setFechaEgreso(LocalDate fechaEgreso) {
        this.fechaEgreso = fechaEgreso;
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

    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }

    public EstadoEgreso getEstado() {
        return estado;
    }

    public void setEstado(EstadoEgreso estado) {
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

    public Insumo getInsumo() {
        return insumo;
    }

    public void setInsumo(Insumo insumo) {
        this.insumo = insumo;
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
