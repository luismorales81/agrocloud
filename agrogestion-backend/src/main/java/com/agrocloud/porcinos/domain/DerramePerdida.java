package com.agrocloud.porcinos.domain;

import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.inventory.domain.Insumo;
import com.agrocloud.core.inventory.domain.InsumoCompuesto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Derrames, pérdidas o accidentes de insumos. Usa loteId y cultivoId para no depender de Cultivos.
 */
@Entity
@Table(name = "porcinos_derrames_perdidas")
@EntityListeners(AuditingEntityListener.class)
public class DerramePerdida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnore
    private Empresa empresa;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 50)
    private TipoPerdida tipo;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dia_alimentacion_id")
    @JsonIgnore
    private DiaAlimentacion diaAlimentacion;

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

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a cero")
    @Column(name = "cantidad", precision = 10, scale = 2, nullable = false)
    private BigDecimal cantidad;

    @NotBlank(message = "El motivo es obligatorio")
    @Column(name = "motivo", nullable = false, length = 200)
    private String motivo;

    @NotBlank(message = "Las observaciones son obligatorias")
    @Column(name = "observaciones", columnDefinition = "TEXT", nullable = false)
    private String observaciones;

    @Column(name = "ubicacion", length = 200)
    private String ubicacion;

    @Column(name = "lote_id")
    private Long loteId;

    @Column(name = "campana_id")
    private Long campanaId;

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

    @Transient
    @JsonProperty("nombreInsumo")
    private String nombreInsumo;

    @Transient
    @JsonProperty("unidadMedida")
    private String unidadMedida;

    @Transient
    @JsonProperty("loteNombre")
    private String loteNombre;

    @Transient
    @JsonProperty("stockAnterior")
    private BigDecimal stockAnterior;

    @Transient
    @JsonProperty("stockPosterior")
    private BigDecimal stockPosterior;

    public enum TipoPerdida {
        DERRAME, PERDIDA, ACCIDENTE, OTRO
    }

    public enum TipoInsumo {
        INSUMO, GRANO_PROPIO, INSUMO_COMPUESTO
    }

    public DerramePerdida() {}

    public DerramePerdida(Empresa empresa, TipoPerdida tipo, LocalDate fecha,
                          TipoInsumo tipoInsumo, BigDecimal cantidad,
                          String motivo, String observaciones, User usuario) {
        this.empresa = empresa;
        this.tipo = tipo;
        this.fecha = fecha;
        this.tipoInsumo = tipoInsumo;
        this.cantidad = cantidad;
        this.motivo = motivo;
        this.observaciones = observaciones;
        this.usuario = usuario;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }
    public TipoPerdida getTipo() { return tipo; }
    public void setTipo(TipoPerdida tipo) { this.tipo = tipo; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public DiaAlimentacion getDiaAlimentacion() { return diaAlimentacion; }
    public void setDiaAlimentacion(DiaAlimentacion diaAlimentacion) { this.diaAlimentacion = diaAlimentacion; }
    public TipoInsumo getTipoInsumo() { return tipoInsumo; }
    public void setTipoInsumo(TipoInsumo tipoInsumo) { this.tipoInsumo = tipoInsumo; }
    public Insumo getInsumo() { return insumo; }
    public void setInsumo(Insumo insumo) {
        this.insumo = insumo;
        if (insumo != null) {
            this.tipoInsumo = TipoInsumo.INSUMO;
            this.nombreInsumo = insumo.getNombre();
            this.unidadMedida = insumo.getUnidadMedida();
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
            this.unidadMedida = insumoCompuesto.getUnidadMedida();
        }
    }
    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public Long getLoteId() { return loteId; }
    public void setLoteId(Long loteId) { this.loteId = loteId; }
    public Long getCampanaId() { return campanaId; }
    public void setCampanaId(Long campanaId) { this.campanaId = campanaId; }
    public User getUsuario() { return usuario; }
    public void setUsuario(User usuario) { this.usuario = usuario; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    public String getNombreInsumo() { return nombreInsumo; }
    public void setNombreInsumo(String nombreInsumo) { this.nombreInsumo = nombreInsumo; }
    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }
    public String getLoteNombre() { return loteNombre; }
    public void setLoteNombre(String loteNombre) { this.loteNombre = loteNombre; }
    public BigDecimal getStockAnterior() { return stockAnterior; }
    public void setStockAnterior(BigDecimal stockAnterior) { this.stockAnterior = stockAnterior; }
    public BigDecimal getStockPosterior() { return stockPosterior; }
    public void setStockPosterior(BigDecimal stockPosterior) { this.stockPosterior = stockPosterior; }
}
