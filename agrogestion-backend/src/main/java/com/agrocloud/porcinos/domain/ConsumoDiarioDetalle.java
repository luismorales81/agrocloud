package com.agrocloud.porcinos.domain;

import com.agrocloud.core.inventory.domain.Insumo;
import com.agrocloud.core.inventory.domain.InsumoCompuesto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Detalle de un componente (insumo) consumido en un consumo diario automático.
 * Usa cultivoId (Long) para no depender del módulo Cultivos.
 */
@Entity
@Table(name = "porcinos_consumos_diarios_detalle")
@EntityListeners(AuditingEntityListener.class)
public class ConsumoDiarioDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumo_diario_id", nullable = false)
    @JsonIgnore
    private ConsumoDiarioAutomatico consumoDiario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_componente", nullable = false, length = 50)
    private TipoComponente tipoComponente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "insumo_id")
    @JsonIgnore
    private Insumo insumo;

    @Column(name = "cultivo_id")
    private Long cultivoId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "insumo_compuesto_id")
    @JsonIgnore
    private InsumoCompuesto insumoCompuesto;

    @Column(name = "cantidad_requerida", precision = 10, scale = 2, nullable = false)
    private BigDecimal cantidadRequerida;

    @Column(name = "cantidad_disponible", precision = 10, scale = 2, nullable = false)
    private BigDecimal cantidadDisponible;

    @Column(name = "cantidad_descontada", precision = 10, scale = 2, nullable = false)
    private BigDecimal cantidadDescontada;

    @Column(name = "stock_resultante", precision = 10, scale = 2, nullable = false)
    private BigDecimal stockResultante;

    @Column(name = "deficit", precision = 10, scale = 2, nullable = false)
    private BigDecimal deficit = BigDecimal.ZERO;

    @Column(name = "tiene_deficit", nullable = false)
    private Boolean tieneDeficit = false;

    @Column(name = "porcentaje_cobertura", precision = 5, scale = 2, nullable = false)
    private BigDecimal porcentajeCobertura = BigDecimal.valueOf(100.00);

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Transient
    @JsonProperty("nombreComponente")
    private String nombreComponente;

    @Transient
    @JsonProperty("unidadMedida")
    private String unidadMedida;

    public enum TipoComponente {
        INSUMO, GRANO_PROPIO, INSUMO_COMPUESTO
    }

    public ConsumoDiarioDetalle() {}

    public ConsumoDiarioDetalle(ConsumoDiarioAutomatico consumoDiario, TipoComponente tipoComponente,
                                BigDecimal cantidadRequerida, BigDecimal cantidadDisponible) {
        this.consumoDiario = consumoDiario;
        this.tipoComponente = tipoComponente;
        this.cantidadRequerida = cantidadRequerida;
        this.cantidadDisponible = cantidadDisponible;
        this.cantidadDescontada = cantidadDisponible.min(cantidadRequerida);
        this.stockResultante = cantidadDisponible.subtract(cantidadRequerida);
        if (stockResultante.compareTo(BigDecimal.ZERO) < 0) {
            this.deficit = stockResultante.abs();
            this.tieneDeficit = true;
        } else {
            this.deficit = BigDecimal.ZERO;
            this.tieneDeficit = false;
        }
        if (cantidadRequerida.compareTo(BigDecimal.ZERO) > 0) {
            this.porcentajeCobertura = cantidadDisponible
                .divide(cantidadRequerida, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .min(BigDecimal.valueOf(100));
        }
        poblarNombreComponente();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ConsumoDiarioAutomatico getConsumoDiario() { return consumoDiario; }
    public void setConsumoDiario(ConsumoDiarioAutomatico consumoDiario) { this.consumoDiario = consumoDiario; }
    public TipoComponente getTipoComponente() { return tipoComponente; }
    public void setTipoComponente(TipoComponente tipoComponente) { this.tipoComponente = tipoComponente; }
    public Insumo getInsumo() { return insumo; }
    public void setInsumo(Insumo insumo) {
        this.insumo = insumo;
        if (insumo != null) {
            this.tipoComponente = TipoComponente.INSUMO;
            poblarNombreComponente();
        }
    }
    public Long getCultivoId() { return cultivoId; }
    public void setCultivoId(Long cultivoId) {
        this.cultivoId = cultivoId;
        if (cultivoId != null) {
            this.tipoComponente = TipoComponente.GRANO_PROPIO;
            this.unidadMedida = "kg";
        }
    }
    public InsumoCompuesto getInsumoCompuesto() { return insumoCompuesto; }
    public void setInsumoCompuesto(InsumoCompuesto insumoCompuesto) {
        this.insumoCompuesto = insumoCompuesto;
        if (insumoCompuesto != null) {
            this.tipoComponente = TipoComponente.INSUMO_COMPUESTO;
            poblarNombreComponente();
        }
    }
    public BigDecimal getCantidadRequerida() { return cantidadRequerida; }
    public void setCantidadRequerida(BigDecimal cantidadRequerida) { this.cantidadRequerida = cantidadRequerida; }
    public BigDecimal getCantidadDisponible() { return cantidadDisponible; }
    public void setCantidadDisponible(BigDecimal cantidadDisponible) { this.cantidadDisponible = cantidadDisponible; }
    public BigDecimal getCantidadDescontada() { return cantidadDescontada; }
    public void setCantidadDescontada(BigDecimal cantidadDescontada) { this.cantidadDescontada = cantidadDescontada; }
    public BigDecimal getStockResultante() { return stockResultante; }
    public void setStockResultante(BigDecimal stockResultante) { this.stockResultante = stockResultante; }
    public BigDecimal getDeficit() { return deficit; }
    public void setDeficit(BigDecimal deficit) { this.deficit = deficit; }
    public Boolean getTieneDeficit() { return tieneDeficit; }
    public void setTieneDeficit(Boolean tieneDeficit) { this.tieneDeficit = tieneDeficit; }
    public BigDecimal getPorcentajeCobertura() { return porcentajeCobertura; }
    public void setPorcentajeCobertura(BigDecimal porcentajeCobertura) { this.porcentajeCobertura = porcentajeCobertura; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public String getNombreComponente() { return nombreComponente; }
    public void setNombreComponente(String nombreComponente) { this.nombreComponente = nombreComponente; }
    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }

    private void poblarNombreComponente() {
        if (insumo != null) {
            this.nombreComponente = insumo.getNombre();
            this.unidadMedida = insumo.getUnidadMedida();
        } else if (cultivoId != null) {
            this.unidadMedida = "kg";
            // nombre se puede completar desde servicio si se necesita
        } else if (insumoCompuesto != null) {
            this.nombreComponente = insumoCompuesto.getNombre();
            this.unidadMedida = insumoCompuesto.getUnidadMedida();
        }
    }
}
