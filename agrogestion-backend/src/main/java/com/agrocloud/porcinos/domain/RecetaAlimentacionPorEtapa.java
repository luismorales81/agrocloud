package com.agrocloud.porcinos.domain;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.inventory.domain.InsumoCompuesto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Entidad que asocia una receta (InsumoCompuesto) a una etapa de alimentación.
 * Compartido entre módulos: InsumoCompuesto en model.entity.
 */
@Entity
@Table(name = "porcinos_recetas_alimentacion_etapa",
       uniqueConstraints = @UniqueConstraint(columnNames = {"empresa_id", "etapa", "insumo_compuesto_id"}))
public class RecetaAlimentacionPorEtapa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "insumo_compuesto_id", nullable = false)
    @JsonIgnore
    private InsumoCompuesto insumoCompuesto;

    @Enumerated(EnumType.STRING)
    @Column(name = "etapa", nullable = false, length = 50)
    private EtapaAlimentacion etapa;

    @NotNull(message = "La cantidad diaria es obligatoria")
    @Positive(message = "La cantidad diaria debe ser positiva")
    @Column(name = "cantidad_diaria_por_animal", precision = 10, scale = 2, nullable = false)
    private BigDecimal cantidadDiariaPorAnimal;

    @Column(name = "cantidad_diaria_minima", precision = 10, scale = 2)
    private BigDecimal cantidadDiariaMinima;

    @Column(name = "cantidad_diaria_maxima", precision = 10, scale = 2)
    private BigDecimal cantidadDiariaMaxima;

    @Column(name = "peso_minimo_animal", precision = 10, scale = 2)
    private BigDecimal pesoMinimoAnimal;

    @Column(name = "peso_maximo_animal", precision = 10, scale = 2)
    private BigDecimal pesoMaximoAnimal;

    @Column(name = "edad_minima_dias")
    private Integer edadMinimaDias;

    @Column(name = "edad_maxima_dias")
    private Integer edadMaximaDias;

    @Column(name = "es_por_defecto", nullable = false)
    private Boolean esPorDefecto = false;

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnore
    private Empresa empresa;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    public enum EtapaAlimentacion {
        GESTACION, LACTANCIA, F1, F2, F3, F4, DESARROLLO, TERMINACION
    }

    public RecetaAlimentacionPorEtapa() {}

    public RecetaAlimentacionPorEtapa(InsumoCompuesto insumoCompuesto, EtapaAlimentacion etapa,
                                     BigDecimal cantidadDiariaPorAnimal, Empresa empresa) {
        this.insumoCompuesto = insumoCompuesto;
        this.etapa = etapa;
        this.cantidadDiariaPorAnimal = cantidadDiariaPorAnimal;
        this.empresa = empresa;
        this.activo = true;
        this.esPorDefecto = false;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public InsumoCompuesto getInsumoCompuesto() { return insumoCompuesto; }
    public void setInsumoCompuesto(InsumoCompuesto insumoCompuesto) { this.insumoCompuesto = insumoCompuesto; }
    public EtapaAlimentacion getEtapa() { return etapa; }
    public void setEtapa(EtapaAlimentacion etapa) { this.etapa = etapa; }
    public BigDecimal getCantidadDiariaPorAnimal() { return cantidadDiariaPorAnimal; }
    public void setCantidadDiariaPorAnimal(BigDecimal cantidadDiariaPorAnimal) { this.cantidadDiariaPorAnimal = cantidadDiariaPorAnimal; }
    public BigDecimal getCantidadDiariaMinima() { return cantidadDiariaMinima; }
    public void setCantidadDiariaMinima(BigDecimal cantidadDiariaMinima) { this.cantidadDiariaMinima = cantidadDiariaMinima; }
    public BigDecimal getCantidadDiariaMaxima() { return cantidadDiariaMaxima; }
    public void setCantidadDiariaMaxima(BigDecimal cantidadDiariaMaxima) { this.cantidadDiariaMaxima = cantidadDiariaMaxima; }
    public BigDecimal getPesoMinimoAnimal() { return pesoMinimoAnimal; }
    public void setPesoMinimoAnimal(BigDecimal pesoMinimoAnimal) { this.pesoMinimoAnimal = pesoMinimoAnimal; }
    public BigDecimal getPesoMaximoAnimal() { return pesoMaximoAnimal; }
    public void setPesoMaximoAnimal(BigDecimal pesoMaximoAnimal) { this.pesoMaximoAnimal = pesoMaximoAnimal; }
    public Integer getEdadMinimaDias() { return edadMinimaDias; }
    public void setEdadMinimaDias(Integer edadMinimaDias) { this.edadMinimaDias = edadMinimaDias; }
    public Integer getEdadMaximaDias() { return edadMaximaDias; }
    public void setEdadMaximaDias(Integer edadMaximaDias) { this.edadMaximaDias = edadMaximaDias; }
    public Boolean getEsPorDefecto() { return esPorDefecto; }
    public void setEsPorDefecto(Boolean esPorDefecto) { this.esPorDefecto = esPorDefecto; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public boolean aplicaParaAnimal(BigDecimal peso, Integer edadDias) {
        if (pesoMinimoAnimal != null && peso.compareTo(pesoMinimoAnimal) < 0) return false;
        if (pesoMaximoAnimal != null && peso.compareTo(pesoMaximoAnimal) > 0) return false;
        if (edadMinimaDias != null && edadDias < edadMinimaDias) return false;
        if (edadMaximaDias != null && edadDias > edadMaximaDias) return false;
        return true;
    }
}
