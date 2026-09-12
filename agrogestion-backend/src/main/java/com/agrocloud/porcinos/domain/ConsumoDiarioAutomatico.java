package com.agrocloud.porcinos.domain;

import com.agrocloud.core.inventory.domain.InsumoCompuesto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un consumo diario automático generado para un lote/recría.
 * Usa loteId (Long) para no depender del módulo Cultivos.
 */
@Entity
@Table(name = "porcinos_legacy_consumos_diarios_automaticos")
@EntityListeners(AuditingEntityListener.class)
public class ConsumoDiarioAutomatico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dia_alimentacion_id", nullable = false)
    @JsonIgnore
    private DiaAlimentacion diaAlimentacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recria_id")
    @JsonIgnore
    private Recria recria;

    @Column(name = "lote_id")
    private Long loteId;

    @Column(name = "campana_id")
    private Long campanaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "madre_id")
    @JsonIgnore
    private Madre madre;

    @Column(name = "etapa_alimentacion", nullable = false, length = 50)
    private String etapaAlimentacion;

    @Column(name = "cantidad_animales", nullable = false)
    private Integer cantidadAnimales;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "receta_id", nullable = false)
    private InsumoCompuesto receta;

    @Column(name = "cantidad_receta_total", precision = 10, scale = 2, nullable = false)
    private BigDecimal cantidadRecetaTotal;

    /** kg totales de ración informados por el operario; null = usar solo la proyección (cantidadRecetaTotal). */
    @Column(name = "cantidad_receta_real", precision = 10, scale = 2)
    private BigDecimal cantidadRecetaReal;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_registro_consumo", nullable = false, length = 20)
    private TipoRegistroConsumo tipoRegistroConsumo = TipoRegistroConsumo.ESTIMADO;

    @Column(name = "cantidad_diaria_por_animal", precision = 10, scale = 2, nullable = false)
    private BigDecimal cantidadDiariaPorAnimal;

    public enum TipoRegistroConsumo {
        ESTIMADO,
        REAL
    }

    @Column(name = "procesado", nullable = false)
    private Boolean procesado = false;

    @Column(name = "fecha_procesamiento")
    private LocalDateTime fechaProcesamiento;

    @OneToMany(mappedBy = "consumoDiario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ConsumoDiarioDetalle> detalles = new ArrayList<>();

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Transient
    @JsonProperty("recriaNombre")
    private String recriaNombre;

    @Transient
    @JsonProperty("loteNombre")
    private String loteNombre;

    @Transient
    @JsonProperty("madreNombre")
    private String madreNombre;

    @Transient
    @JsonProperty("recetaNombre")
    private String recetaNombre;

    public ConsumoDiarioAutomatico() {}

    public ConsumoDiarioAutomatico(DiaAlimentacion diaAlimentacion, Recria recria,
                                   String etapaAlimentacion, Integer cantidadAnimales,
                                   InsumoCompuesto receta, BigDecimal cantidadDiariaPorAnimal) {
        this.diaAlimentacion = diaAlimentacion;
        this.recria = recria;
        this.etapaAlimentacion = etapaAlimentacion;
        this.cantidadAnimales = cantidadAnimales;
        this.receta = receta;
        this.cantidadDiariaPorAnimal = cantidadDiariaPorAnimal;
        this.cantidadRecetaTotal = cantidadDiariaPorAnimal.multiply(BigDecimal.valueOf(cantidadAnimales));
        this.cantidadRecetaReal = null;
        this.tipoRegistroConsumo = TipoRegistroConsumo.ESTIMADO;
        this.procesado = false;
    }

    /**
     * Cantidad de ración (kg) usada para desglosar receta, alertas y descuento de stock:
     * {@link #cantidadRecetaReal} si está cargada y es positiva; si no, la proyección {@link #cantidadRecetaTotal}.
     */
    public BigDecimal getCantidadRecetaEfectiva() {
        if (cantidadRecetaReal != null && cantidadRecetaReal.compareTo(BigDecimal.ZERO) > 0) {
            return cantidadRecetaReal;
        }
        return cantidadRecetaTotal;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public DiaAlimentacion getDiaAlimentacion() { return diaAlimentacion; }
    public void setDiaAlimentacion(DiaAlimentacion diaAlimentacion) { this.diaAlimentacion = diaAlimentacion; }
    public Recria getRecria() { return recria; }
    public void setRecria(Recria recria) { this.recria = recria; }
    public Long getLoteId() { return loteId; }
    public void setLoteId(Long loteId) { this.loteId = loteId; }
    public Long getCampanaId() { return campanaId; }
    public void setCampanaId(Long campanaId) { this.campanaId = campanaId; }
    public Madre getMadre() { return madre; }
    public void setMadre(Madre madre) { this.madre = madre; }
    public String getEtapaAlimentacion() { return etapaAlimentacion; }
    public void setEtapaAlimentacion(String etapaAlimentacion) { this.etapaAlimentacion = etapaAlimentacion; }
    public Integer getCantidadAnimales() { return cantidadAnimales; }
    public void setCantidadAnimales(Integer cantidadAnimales) { this.cantidadAnimales = cantidadAnimales; }
    public InsumoCompuesto getReceta() { return receta; }
    public void setReceta(InsumoCompuesto receta) { this.receta = receta; }
    public BigDecimal getCantidadRecetaTotal() { return cantidadRecetaTotal; }
    public void setCantidadRecetaTotal(BigDecimal cantidadRecetaTotal) { this.cantidadRecetaTotal = cantidadRecetaTotal; }
    public BigDecimal getCantidadRecetaReal() { return cantidadRecetaReal; }
    public void setCantidadRecetaReal(BigDecimal cantidadRecetaReal) { this.cantidadRecetaReal = cantidadRecetaReal; }
    public TipoRegistroConsumo getTipoRegistroConsumo() { return tipoRegistroConsumo; }
    public void setTipoRegistroConsumo(TipoRegistroConsumo tipoRegistroConsumo) { this.tipoRegistroConsumo = tipoRegistroConsumo; }
    public BigDecimal getCantidadDiariaPorAnimal() { return cantidadDiariaPorAnimal; }
    public void setCantidadDiariaPorAnimal(BigDecimal cantidadDiariaPorAnimal) { this.cantidadDiariaPorAnimal = cantidadDiariaPorAnimal; }
    public Boolean getProcesado() { return procesado; }
    public void setProcesado(Boolean procesado) { this.procesado = procesado; }
    public LocalDateTime getFechaProcesamiento() { return fechaProcesamiento; }
    public void setFechaProcesamiento(LocalDateTime fechaProcesamiento) { this.fechaProcesamiento = fechaProcesamiento; }
    public List<ConsumoDiarioDetalle> getDetalles() { return detalles; }
    public void setDetalles(List<ConsumoDiarioDetalle> detalles) { this.detalles = detalles; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public String getRecriaNombre() { return recriaNombre; }
    public void setRecriaNombre(String recriaNombre) { this.recriaNombre = recriaNombre; }
    public String getLoteNombre() { return loteNombre; }
    public void setLoteNombre(String loteNombre) { this.loteNombre = loteNombre; }
    public String getMadreNombre() { return madreNombre; }
    public void setMadreNombre(String madreNombre) { this.madreNombre = madreNombre; }
    public String getRecetaNombre() { return recetaNombre; }
    public void setRecetaNombre(String recetaNombre) { this.recetaNombre = recetaNombre; }

    public void marcarComoProcesado() {
        this.procesado = true;
        this.fechaProcesamiento = LocalDateTime.now();
    }

    public void agregarDetalle(ConsumoDiarioDetalle detalle) {
        detalle.setConsumoDiario(this);
        this.detalles.add(detalle);
    }
}
