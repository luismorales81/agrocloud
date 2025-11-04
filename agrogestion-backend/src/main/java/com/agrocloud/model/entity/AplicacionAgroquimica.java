package com.agrocloud.model.entity;

import com.agrocloud.model.enums.TipoAplicacion;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa una aplicación de agroquímico en una tarea (labor)
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@Entity
@Table(name = "aplicaciones_agroquimicas")
@EntityListeners(AuditingEntityListener.class)
public class AplicacionAgroquimica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La tarea es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "labor_id", nullable = false)
    private Labor labor;

    @NotNull(message = "El insumo es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insumo_id", nullable = false)
    private Insumo insumo;

    @NotNull(message = "El tipo de aplicación es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_aplicacion", nullable = false, length = 20)
    private TipoAplicacion tipoAplicacion;

    @NotNull(message = "La cantidad total a aplicar es obligatoria")
    @Positive(message = "La cantidad total a aplicar debe ser un valor positivo")
    @Column(name = "cantidad_total_aplicar", nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidadTotalAplicar;

    @Column(name = "dosis_aplicada_por_ha", precision = 10, scale = 2)
    private BigDecimal dosisAplicadaPorHa;

    @Column(name = "superficie_aplicada_ha", precision = 10, scale = 2)
    private BigDecimal superficieAplicadaHa;

    @Column(name = "unidad_medida", length = 50)
    private String unidadMedida; // Ej: "litros", "kilogramos", "ml"

    @Column(name = "observaciones", length = 1000)
    private String observaciones;

    @Column(name = "fecha_aplicacion")
    private LocalDateTime fechaAplicacion;

    @CreatedDate
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    // Constructors
    public AplicacionAgroquimica() {}

    public AplicacionAgroquimica(Labor labor, Insumo insumo, TipoAplicacion tipoAplicacion, 
                                  BigDecimal cantidadTotalAplicar) {
        this.labor = labor;
        this.insumo = insumo;
        this.tipoAplicacion = tipoAplicacion;
        this.cantidadTotalAplicar = cantidadTotalAplicar;
        this.activo = true;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Labor getLabor() {
        return labor;
    }

    public void setLabor(Labor labor) {
        this.labor = labor;
    }

    public Insumo getInsumo() {
        return insumo;
    }

    public void setInsumo(Insumo insumo) {
        this.insumo = insumo;
    }

    public TipoAplicacion getTipoAplicacion() {
        return tipoAplicacion;
    }

    public void setTipoAplicacion(TipoAplicacion tipoAplicacion) {
        this.tipoAplicacion = tipoAplicacion;
    }

    public BigDecimal getCantidadTotalAplicar() {
        return cantidadTotalAplicar;
    }

    public void setCantidadTotalAplicar(BigDecimal cantidadTotalAplicar) {
        this.cantidadTotalAplicar = cantidadTotalAplicar;
    }

    public BigDecimal getDosisAplicadaPorHa() {
        return dosisAplicadaPorHa;
    }

    public void setDosisAplicadaPorHa(BigDecimal dosisAplicadaPorHa) {
        this.dosisAplicadaPorHa = dosisAplicadaPorHa;
    }

    public BigDecimal getSuperficieAplicadaHa() {
        return superficieAplicadaHa;
    }

    public void setSuperficieAplicadaHa(BigDecimal superficieAplicadaHa) {
        this.superficieAplicadaHa = superficieAplicadaHa;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public LocalDateTime getFechaAplicacion() {
        return fechaAplicacion;
    }

    public void setFechaAplicacion(LocalDateTime fechaAplicacion) {
        this.fechaAplicacion = fechaAplicacion;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return "AplicacionAgroquimica{" +
                "id=" + id +
                ", tipoAplicacion=" + tipoAplicacion +
                ", cantidadTotalAplicar=" + cantidadTotalAplicar +
                ", unidadMedida='" + unidadMedida + '\'' +
                ", activo=" + activo +
                '}';
    }
}

