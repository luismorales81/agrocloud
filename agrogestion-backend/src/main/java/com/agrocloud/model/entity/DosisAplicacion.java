package com.agrocloud.model.entity;

import com.agrocloud.model.enums.TipoAplicacion;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa las dosis sugeridas de aplicación de un insumo (agroquímico)
 * según el tipo de aplicación
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@Entity
@Table(name = "dosis_aplicacion")
@EntityListeners(AuditingEntityListener.class)
public class DosisAplicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El insumo es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insumo_id", nullable = false)
    private Insumo insumo;

    @NotNull(message = "El tipo de aplicación es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_aplicacion", nullable = false, length = 20)
    private TipoAplicacion tipoAplicacion;

    @NotNull(message = "La dosis por hectárea es obligatoria")
    @Positive(message = "La dosis por hectárea debe ser un valor positivo")
    @Column(name = "dosis_por_ha", nullable = false, precision = 10, scale = 2)
    private BigDecimal dosisPorHa;

    @Column(name = "unidad_medida", length = 50)
    private String unidadMedida; // Ej: "litros", "kilogramos", "ml"

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Constructors
    public DosisAplicacion() {}

    public DosisAplicacion(Insumo insumo, TipoAplicacion tipoAplicacion, BigDecimal dosisPorHa) {
        this.insumo = insumo;
        this.tipoAplicacion = tipoAplicacion;
        this.dosisPorHa = dosisPorHa;
        this.activo = true;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public BigDecimal getDosisPorHa() {
        return dosisPorHa;
    }

    public void setDosisPorHa(BigDecimal dosisPorHa) {
        this.dosisPorHa = dosisPorHa;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
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

    @Override
    public String toString() {
        return "DosisAplicacion{" +
                "id=" + id +
                ", tipoAplicacion=" + tipoAplicacion +
                ", dosisPorHa=" + dosisPorHa +
                ", unidadMedida='" + unidadMedida + '\'' +
                ", activo=" + activo +
                '}';
    }
}

