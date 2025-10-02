package com.agrocloud.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad para representar el historial de cosechas de un lote
 * Permite rastrear los ciclos completos de siembra a cosecha
 */
@Entity
@Table(name = "historial_cosechas")
@EntityListeners(AuditingEntityListener.class)
public class HistorialCosecha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El lote es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", nullable = false)
    private Plot lote;

    @NotNull(message = "El cultivo es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cultivo_id", nullable = false)
    private Cultivo cultivo;

    @NotNull(message = "La fecha de siembra es obligatoria")
    @Column(name = "fecha_siembra", nullable = false)
    private LocalDate fechaSiembra;

    @NotNull(message = "La fecha de cosecha es obligatoria")
    @Column(name = "fecha_cosecha", nullable = false)
    private LocalDate fechaCosecha;

    @Positive(message = "La superficie debe ser positiva")
    @Column(name = "superficie_hectareas", nullable = false, precision = 10, scale = 2)
    private BigDecimal superficieHectareas;

    @Positive(message = "La cantidad cosechada debe ser positiva")
    @Column(name = "cantidad_cosechada", nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidadCosechada;

    @Column(name = "unidad_cosecha", length = 10, nullable = false)
    private String unidadCosecha;

    @Column(name = "rendimiento_real", precision = 10, scale = 2)
    private BigDecimal rendimientoReal;

    @Column(name = "rendimiento_esperado", precision = 10, scale = 2)
    private BigDecimal rendimientoEsperado;


    @Column(name = "variedad_semilla", length = 100)
    private String variedadSemilla;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "estado_suelo", length = 50)
    private String estadoSuelo = "BUENO";

    @Column(name = "requiere_descanso", nullable = false)
    private Boolean requiereDescanso = false;

    @Column(name = "dias_descanso_recomendados")
    private Integer diasDescansoRecomendados = 0;

    // Campos para análisis de rentabilidad
    @Column(name = "precio_venta_unitario", precision = 10, scale = 2)
    private BigDecimal precioVentaUnitario = BigDecimal.ZERO;

    @Column(name = "ingreso_total", precision = 15, scale = 2)
    private BigDecimal ingresoTotal = BigDecimal.ZERO;

    @Column(name = "costo_total_produccion", precision = 15, scale = 2)
    private BigDecimal costoTotalProduccion = BigDecimal.ZERO;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;

    // Constructors
    public HistorialCosecha() {}

    public HistorialCosecha(Plot lote, Cultivo cultivo, LocalDate fechaSiembra, 
                           LocalDate fechaCosecha, BigDecimal superficieHectareas,
                           BigDecimal cantidadCosechada, String unidadCosecha,
                           BigDecimal rendimientoReal, BigDecimal rendimientoEsperado,
                           String variedadSemilla, String observaciones, User usuario) {
        this.lote = lote;
        this.cultivo = cultivo;
        this.fechaSiembra = fechaSiembra;
        this.fechaCosecha = fechaCosecha;
        this.superficieHectareas = superficieHectareas;
        this.cantidadCosechada = cantidadCosechada;
        this.unidadCosecha = unidadCosecha;
        this.rendimientoReal = rendimientoReal;
        this.rendimientoEsperado = rendimientoEsperado;
        this.variedadSemilla = variedadSemilla;
        this.observaciones = observaciones;
        this.usuario = usuario;
        this.estadoSuelo = "BUENO";
        this.requiereDescanso = false;
        this.diasDescansoRecomendados = 0;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Plot getLote() {
        return lote;
    }

    public void setLote(Plot lote) {
        this.lote = lote;
    }

    public Cultivo getCultivo() {
        return cultivo;
    }

    public void setCultivo(Cultivo cultivo) {
        this.cultivo = cultivo;
    }

    public LocalDate getFechaSiembra() {
        return fechaSiembra;
    }

    public void setFechaSiembra(LocalDate fechaSiembra) {
        this.fechaSiembra = fechaSiembra;
    }

    public LocalDate getFechaCosecha() {
        return fechaCosecha;
    }

    public void setFechaCosecha(LocalDate fechaCosecha) {
        this.fechaCosecha = fechaCosecha;
    }

    public BigDecimal getSuperficieHectareas() {
        return superficieHectareas;
    }

    public void setSuperficieHectareas(BigDecimal superficieHectareas) {
        this.superficieHectareas = superficieHectareas;
    }

    public BigDecimal getCantidadCosechada() {
        return cantidadCosechada;
    }

    public void setCantidadCosechada(BigDecimal cantidadCosechada) {
        this.cantidadCosechada = cantidadCosechada;
    }

    public String getUnidadCosecha() {
        return unidadCosecha;
    }

    public void setUnidadCosecha(String unidadCosecha) {
        this.unidadCosecha = unidadCosecha;
    }

    public BigDecimal getRendimientoReal() {
        return rendimientoReal;
    }

    public void setRendimientoReal(BigDecimal rendimientoReal) {
        this.rendimientoReal = rendimientoReal;
    }

    public BigDecimal getRendimientoEsperado() {
        return rendimientoEsperado;
    }

    public void setRendimientoEsperado(BigDecimal rendimientoEsperado) {
        this.rendimientoEsperado = rendimientoEsperado;
    }


    public String getVariedadSemilla() {
        return variedadSemilla;
    }

    public void setVariedadSemilla(String variedadSemilla) {
        this.variedadSemilla = variedadSemilla;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getEstadoSuelo() {
        return estadoSuelo;
    }

    public void setEstadoSuelo(String estadoSuelo) {
        this.estadoSuelo = estadoSuelo;
    }

    public Boolean getRequiereDescanso() {
        return requiereDescanso;
    }

    public void setRequiereDescanso(Boolean requiereDescanso) {
        this.requiereDescanso = requiereDescanso;
    }

    public Integer getDiasDescansoRecomendados() {
        return diasDescansoRecomendados;
    }

    public void setDiasDescansoRecomendados(Integer diasDescansoRecomendados) {
        this.diasDescansoRecomendados = diasDescansoRecomendados;
    }

    public BigDecimal getPrecioVentaUnitario() {
        return precioVentaUnitario;
    }

    public void setPrecioVentaUnitario(BigDecimal precioVentaUnitario) {
        this.precioVentaUnitario = precioVentaUnitario;
    }

    public BigDecimal getIngresoTotal() {
        return ingresoTotal;
    }

    public void setIngresoTotal(BigDecimal ingresoTotal) {
        this.ingresoTotal = ingresoTotal;
    }

    public BigDecimal getCostoTotalProduccion() {
        return costoTotalProduccion;
    }

    public void setCostoTotalProduccion(BigDecimal costoTotalProduccion) {
        this.costoTotalProduccion = costoTotalProduccion;
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

    public User getUsuario() {
        return usuario;
    }

    public void setUsuario(User usuario) {
        this.usuario = usuario;
    }

    // Helper methods
    public long getDiasCiclo() {
        if (fechaSiembra != null && fechaCosecha != null) {
            return java.time.temporal.ChronoUnit.DAYS.between(fechaSiembra, fechaCosecha);
        }
        return 0;
    }

    public BigDecimal getPorcentajeCumplimiento() {
        if (rendimientoEsperado != null && rendimientoEsperado.compareTo(BigDecimal.ZERO) > 0) {
            return rendimientoReal.divide(rendimientoEsperado, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }
        return BigDecimal.ZERO;
    }

    @Override
    public String toString() {
        return "HistorialCosecha{" +
                "id=" + id +
                ", lote=" + (lote != null ? lote.getNombre() : "null") +
                ", cultivo=" + (cultivo != null ? cultivo.getNombre() : "null") +
                ", fechaSiembra=" + fechaSiembra +
                ", fechaCosecha=" + fechaCosecha +
                ", rendimientoReal=" + rendimientoReal +
                '}';
    }
}
