package com.agrocloud.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad para registrar movimientos de inventario de GRANOS cosechados.
 * Separada de MovimientoInventario (insumos) para mayor claridad.
 */
@Entity
@Table(name = "movimientos_inventario_granos")
@EntityListeners(AuditingEntityListener.class)
public class MovimientoInventarioGrano {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El inventario es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventario_id", nullable = false)
    private InventarioGrano inventario;

    @NotNull(message = "El usuario es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;

    @NotNull
    @Column(name = "tipo_movimiento", length = 30, nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoMovimiento tipoMovimiento;

    @NotNull
    @Positive(message = "La cantidad debe ser positiva")
    @Column(name = "cantidad", precision = 10, scale = 2, nullable = false)
    private BigDecimal cantidad;

    @Column(name = "referencia_tipo", length = 30)
    private String referenciaTipo; // INGRESO, EGRESO, LABOR, null

    @Column(name = "referencia_id")
    private Long referenciaId;

    @Column(name = "precio_unitario", precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "monto_total", precision = 15, scale = 2)
    private BigDecimal montoTotal;

    @Column(name = "cliente_destino", length = 200)
    private String clienteDestino;

    @NotNull
    @Column(name = "fecha_movimiento", nullable = false)
    private LocalDate fechaMovimiento;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Enum para tipos de movimiento DE GRANOS
    public enum TipoMovimiento {
        ENTRADA_COSECHA,
        SALIDA_VENTA,
        AJUSTE_INVENTARIO,
        MERMA,
        USO_INTERNO
    }

    // Constructors
    public MovimientoInventarioGrano() {}

    public MovimientoInventarioGrano(InventarioGrano inventario, User usuario, TipoMovimiento tipoMovimiento,
                                    BigDecimal cantidad, LocalDate fechaMovimiento) {
        this.inventario = inventario;
        this.usuario = usuario;
        this.tipoMovimiento = tipoMovimiento;
        this.cantidad = cantidad;
        this.fechaMovimiento = fechaMovimiento;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public InventarioGrano getInventario() {
        return inventario;
    }

    public void setInventario(InventarioGrano inventario) {
        this.inventario = inventario;
    }

    public User getUsuario() {
        return usuario;
    }

    public void setUsuario(User usuario) {
        this.usuario = usuario;
    }

    public TipoMovimiento getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(TipoMovimiento tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public String getReferenciaTipo() {
        return referenciaTipo;
    }

    public void setReferenciaTipo(String referenciaTipo) {
        this.referenciaTipo = referenciaTipo;
    }

    public Long getReferenciaId() {
        return referenciaId;
    }

    public void setReferenciaId(Long referenciaId) {
        this.referenciaId = referenciaId;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }

    public String getClienteDestino() {
        return clienteDestino;
    }

    public void setClienteDestino(String clienteDestino) {
        this.clienteDestino = clienteDestino;
    }

    public LocalDate getFechaMovimiento() {
        return fechaMovimiento;
    }

    public void setFechaMovimiento(LocalDate fechaMovimiento) {
        this.fechaMovimiento = fechaMovimiento;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

