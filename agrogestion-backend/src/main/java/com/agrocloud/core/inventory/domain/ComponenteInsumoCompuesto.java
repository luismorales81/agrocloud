package com.agrocloud.core.inventory.domain;

import com.agrocloud.cultivos.domain.Cultivo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Entidad que representa un componente (ingrediente) de un insumo compuesto
 * 
 * Ejemplo: En "Ración Gestación", un componente sería:
 * - Insumo: Maíz
 * - Porcentaje: 60%
 * - Cantidad por 100kg: 60 kg
 */
@Entity
@Table(name = "componentes_insumo_compuesto")
public class ComponenteInsumoCompuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insumo_compuesto_id", nullable = false)
    @JsonIgnore
    private InsumoCompuesto insumoCompuesto;

    /**
     * Insumo base que puede ser:
     * - Un Insumo (de la tabla cultivo_insumos) - ej: núcleo comprado, antibiótico
     * - Un Cultivo (grano propio) - ej: maíz propio, soja propia
     * - Otro InsumoCompuesto (receta dentro de receta) - ej: premezcla
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "insumo_id")
    @JsonIgnore
    private Insumo insumo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cultivo_id")
    @JsonIgnore
    private Cultivo cultivo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "insumo_compuesto_padre_id")
    @JsonIgnore
    private InsumoCompuesto insumoCompuestoPadre;

    /**
     * Tipo de componente para identificar el origen
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_componente", nullable = false)
    private TipoComponente tipoComponente;

    /**
     * Porcentaje que representa este componente en el total (0-100)
     * Ejemplo: 60% de maíz en la ración
     */
    @NotNull(message = "El porcentaje es obligatorio")
    @Positive(message = "El porcentaje debe ser positivo")
    @Column(name = "porcentaje", precision = 5, scale = 2, nullable = false)
    private BigDecimal porcentaje;

    /**
     * Cantidad fija en la unidad de medida del componente
     * Alternativa al porcentaje, permite especificar cantidad exacta
     * Ejemplo: 2 kg de antibiótico por cada 100 kg de ración
     */
    @Column(name = "cantidad_fija", precision = 10, scale = 2)
    private BigDecimal cantidadFija;

    /**
     * Unidad de medida del componente (kg, litros, etc.)
     */
    @Column(name = "unidad_medida", length = 50)
    private String unidadMedida = "kg";

    /**
     * Orden de mezcla (para indicar el orden en que se deben agregar los componentes)
     */
    @Column(name = "orden_mezcla")
    private Integer ordenMezcla = 0;

    /**
     * Observaciones específicas para este componente
     */
    @Column(name = "observaciones", length = 500)
    private String observaciones;

    // Campos transient para exponer información al frontend sin serializar objetos completos
    @Transient
    @JsonProperty
    private String nombreComponente;

    @Transient
    @JsonProperty
    private Long insumoId;

    @Transient
    @JsonProperty
    private Long cultivoId;

    @Transient
    @JsonProperty
    private Long insumoCompuestoPadreId;

    public enum TipoComponente {
        INSUMO,           // Insumo de la tabla cultivo_insumos
        GRANO_PROPIO,     // Grano de cultivo propio
        INSUMO_COMPUESTO  // Otro insumo compuesto (receta dentro de receta)
    }

    // Constructors
    public ComponenteInsumoCompuesto() {}

    public ComponenteInsumoCompuesto(InsumoCompuesto insumoCompuesto, Insumo insumo, BigDecimal porcentaje) {
        this.insumoCompuesto = insumoCompuesto;
        this.insumo = insumo;
        this.tipoComponente = TipoComponente.INSUMO;
        this.porcentaje = porcentaje;
    }

    public ComponenteInsumoCompuesto(InsumoCompuesto insumoCompuesto, Cultivo cultivo, BigDecimal porcentaje) {
        this.insumoCompuesto = insumoCompuesto;
        this.cultivo = cultivo;
        this.tipoComponente = TipoComponente.GRANO_PROPIO;
        this.porcentaje = porcentaje;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public InsumoCompuesto getInsumoCompuesto() { return insumoCompuesto; }
    public void setInsumoCompuesto(InsumoCompuesto insumoCompuesto) { this.insumoCompuesto = insumoCompuesto; }
    public Insumo getInsumo() { return insumo; }
    public void setInsumo(Insumo insumo) { this.insumo = insumo; }
    public Cultivo getCultivo() { return cultivo; }
    public void setCultivo(Cultivo cultivo) { this.cultivo = cultivo; }
    public InsumoCompuesto getInsumoCompuestoPadre() { return insumoCompuestoPadre; }
    public void setInsumoCompuestoPadre(InsumoCompuesto insumoCompuestoPadre) { this.insumoCompuestoPadre = insumoCompuestoPadre; }
    public TipoComponente getTipoComponente() { return tipoComponente; }
    public void setTipoComponente(TipoComponente tipoComponente) { this.tipoComponente = tipoComponente; }
    public BigDecimal getPorcentaje() { return porcentaje; }
    public void setPorcentaje(BigDecimal porcentaje) { this.porcentaje = porcentaje; }
    public BigDecimal getCantidadFija() { return cantidadFija; }
    public void setCantidadFija(BigDecimal cantidadFija) { this.cantidadFija = cantidadFija; }
    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }
    public Integer getOrdenMezcla() { return ordenMezcla; }
    public void setOrdenMezcla(Integer ordenMezcla) { this.ordenMezcla = ordenMezcla; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    // Getters y setters para campos transient
    public String getNombreComponente() {
        if (nombreComponente != null) {
            return nombreComponente;
        }
        // Si no está poblado, calcularlo
        if (insumo != null) return insumo.getNombre();
        if (cultivo != null) return cultivo.getNombre();
        if (insumoCompuestoPadre != null) return insumoCompuestoPadre.getNombre();
        return "Componente sin nombre";
    }

    public void setNombreComponente(String nombreComponente) {
        this.nombreComponente = nombreComponente;
    }

    public Long getInsumoId() {
        if (insumoId != null) {
            return insumoId;
        }
        return insumo != null ? insumo.getId() : null;
    }

    public void setInsumoId(Long insumoId) {
        this.insumoId = insumoId;
    }

    public Long getCultivoId() {
        if (cultivoId != null) {
            return cultivoId;
        }
        return cultivo != null ? cultivo.getId() : null;
    }

    public void setCultivoId(Long cultivoId) {
        this.cultivoId = cultivoId;
    }

    public Long getInsumoCompuestoPadreId() {
        if (insumoCompuestoPadreId != null) {
            return insumoCompuestoPadreId;
        }
        return insumoCompuestoPadre != null ? insumoCompuestoPadre.getId() : null;
    }

    public void setInsumoCompuestoPadreId(Long insumoCompuestoPadreId) {
        this.insumoCompuestoPadreId = insumoCompuestoPadreId;
    }

    /**
     * Calcular cantidad necesaria del componente para una cantidad dada del insumo compuesto
     */
    public BigDecimal calcularCantidadNecesaria(BigDecimal cantidadInsumoCompuesto) {
        if (cantidadFija != null) {
            // Si tiene cantidad fija, usar esa
            return cantidadFija;
        }
        if (porcentaje != null) {
            // Calcular según porcentaje
            return cantidadInsumoCompuesto.multiply(porcentaje).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }
}





