package com.agrocloud.porcinos.domain;

import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.inventory.domain.Insumo;
import com.agrocloud.core.inventory.domain.InsumoCompuesto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Trazabilidad de movimientos de stock porcinos. Usa cultivoId para no depender de Cultivos.
 */
@Entity
@Table(name = "porcinos_legacy_movimientos_stock")
@EntityListeners(AuditingEntityListener.class)
public class MovimientoStockPorcino {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnore
    private Empresa empresa;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", nullable = false, length = 50)
    private TipoMovimiento tipoMovimiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumo_diario_id")
    @JsonIgnore
    private ConsumoDiarioAutomatico consumoDiario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "derrame_id")
    @JsonIgnore
    private DerramePerdida derrame;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dia_alimentacion_id")
    @JsonIgnore
    private DiaAlimentacion diaAlimentacion;

    @Column(name = "fecha_movimiento", nullable = false)
    private LocalDate fechaMovimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_insumo", nullable = false, length = 50)
    private TipoInsumo tipoInsumo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "insumo_id")
    private Insumo insumo;

    @Column(name = "cultivo_id")
    private Long cultivoId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "insumo_compuesto_id")
    private InsumoCompuesto insumoCompuesto;

    @Column(name = "cantidad", precision = 10, scale = 2, nullable = false)
    private BigDecimal cantidad;

    @Column(name = "stock_anterior", precision = 10, scale = 2, nullable = false)
    private BigDecimal stockAnterior;

    @Column(name = "stock_posterior", precision = 10, scale = 2, nullable = false)
    private BigDecimal stockPosterior;

    @Column(name = "permite_negativo", nullable = false)
    private Boolean permiteNegativo = false;

    @Column(name = "motivo", length = 200)
    private String motivo;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "lote_id")
    private Long loteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recria_id")
    @JsonIgnore
    private Recria recria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    @JsonIgnore
    private User usuario;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Transient
    @JsonProperty("nombreInsumo")
    private String nombreInsumo;

    @Transient
    @JsonProperty("loteNombre")
    private String loteNombre;

    @Transient
    @JsonProperty("recriaNombre")
    private String recriaNombre;

    public enum TipoMovimiento {
        CONSUMO_AUTOMATICO, DERRAME, PERDIDA, AJUSTE_INVENTARIO, INGRESO, TRANSFERENCIA
    }

    public enum TipoInsumo {
        INSUMO, GRANO_PROPIO, INSUMO_COMPUESTO
    }

    public MovimientoStockPorcino() {}

    public MovimientoStockPorcino(Empresa empresa, TipoMovimiento tipoMovimiento,
                                  LocalDate fechaMovimiento, TipoInsumo tipoInsumo,
                                  BigDecimal cantidad, BigDecimal stockAnterior,
                                  BigDecimal stockPosterior, Boolean permiteNegativo) {
        this.empresa = empresa;
        this.tipoMovimiento = tipoMovimiento;
        this.fechaMovimiento = fechaMovimiento;
        this.tipoInsumo = tipoInsumo;
        this.cantidad = cantidad;
        this.stockAnterior = stockAnterior;
        this.stockPosterior = stockPosterior;
        this.permiteNegativo = permiteNegativo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }
    public TipoMovimiento getTipoMovimiento() { return tipoMovimiento; }
    public void setTipoMovimiento(TipoMovimiento tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; }
    public ConsumoDiarioAutomatico getConsumoDiario() { return consumoDiario; }
    public void setConsumoDiario(ConsumoDiarioAutomatico consumoDiario) { this.consumoDiario = consumoDiario; }
    public DerramePerdida getDerrame() { return derrame; }
    public void setDerrame(DerramePerdida derrame) { this.derrame = derrame; }
    public DiaAlimentacion getDiaAlimentacion() { return diaAlimentacion; }
    public void setDiaAlimentacion(DiaAlimentacion diaAlimentacion) { this.diaAlimentacion = diaAlimentacion; }
    public LocalDate getFechaMovimiento() { return fechaMovimiento; }
    public void setFechaMovimiento(LocalDate fechaMovimiento) { this.fechaMovimiento = fechaMovimiento; }
    public TipoInsumo getTipoInsumo() { return tipoInsumo; }
    public void setTipoInsumo(TipoInsumo tipoInsumo) { this.tipoInsumo = tipoInsumo; }
    public Insumo getInsumo() { return insumo; }
    public void setInsumo(Insumo insumo) {
        this.insumo = insumo;
        if (insumo != null) {
            this.tipoInsumo = TipoInsumo.INSUMO;
            this.nombreInsumo = insumo.getNombre();
        }
    }
    public Long getCultivoId() { return cultivoId; }
    public void setCultivoId(Long cultivoId) {
        this.cultivoId = cultivoId;
        if (cultivoId != null) this.tipoInsumo = TipoInsumo.GRANO_PROPIO;
    }
    public InsumoCompuesto getInsumoCompuesto() { return insumoCompuesto; }
    public void setInsumoCompuesto(InsumoCompuesto insumoCompuesto) {
        this.insumoCompuesto = insumoCompuesto;
        if (insumoCompuesto != null) {
            this.tipoInsumo = TipoInsumo.INSUMO_COMPUESTO;
            this.nombreInsumo = insumoCompuesto.getNombre();
        }
    }
    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }
    public BigDecimal getStockAnterior() { return stockAnterior; }
    public void setStockAnterior(BigDecimal stockAnterior) { this.stockAnterior = stockAnterior; }
    public BigDecimal getStockPosterior() { return stockPosterior; }
    public void setStockPosterior(BigDecimal stockPosterior) { this.stockPosterior = stockPosterior; }
    public Boolean getPermiteNegativo() { return permiteNegativo; }
    public void setPermiteNegativo(Boolean permiteNegativo) { this.permiteNegativo = permiteNegativo; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public Long getLoteId() { return loteId; }
    public void setLoteId(Long loteId) { this.loteId = loteId; }
    public Recria getRecria() { return recria; }
    public void setRecria(Recria recria) { this.recria = recria; }
    public User getUsuario() { return usuario; }
    public void setUsuario(User usuario) { this.usuario = usuario; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public String getNombreInsumo() { return nombreInsumo; }
    public void setNombreInsumo(String nombreInsumo) { this.nombreInsumo = nombreInsumo; }
    public String getLoteNombre() { return loteNombre; }
    public void setLoteNombre(String loteNombre) { this.loteNombre = loteNombre; }
    public String getRecriaNombre() { return recriaNombre; }
    public void setRecriaNombre(String recriaNombre) { this.recriaNombre = recriaNombre; }
}
