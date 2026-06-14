package com.agrocloud.porcinos.domain;

import com.agrocloud.core.domain.Empresa;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa los datos económicos configurables del módulo porcino
 */
@Entity
@Table(name = "porcinos_datos_economicos_porcinos")
@EntityListeners(AuditingEntityListener.class)
public class DatosEconomicosPorcino {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "costo_madre_gestacion_dia", precision = 10, scale = 2)
    private BigDecimal costoMadreGestacionDia;

    @Column(name = "costo_madre_lactancia_dia", precision = 10, scale = 2)
    private BigDecimal costoMadreLactanciaDia;

    @Column(name = "costo_lechon", precision = 10, scale = 2)
    private BigDecimal costoLechon;

    @Column(name = "costo_engorde_dia", precision = 10, scale = 2)
    private BigDecimal costoEngordeDia;

    @Column(name = "costo_mano_obra_dia", precision = 10, scale = 2)
    private BigDecimal costoManoObraDia;

    @Column(name = "precio_venta_cerdo_terminado_kg", precision = 10, scale = 2)
    private BigDecimal precioVentaCerdoTerminadoKg;

    @Column(name = "porcentaje_merma_transporte", precision = 5, scale = 2)
    private BigDecimal porcentajeMermaTransporte;

    @Column(name = "kg_maiz_por_racion_engorde", precision = 10, scale = 2)
    private BigDecimal kgMaizPorRacionEngorde;

    @Column(name = "porcentaje_mezcla_alimento_propio_balanceado", precision = 5, scale = 2)
    private BigDecimal porcentajeMezclaAlimentoPropioBalanceado;

    @Column(name = "indice_conversion_objetivo", precision = 5, scale = 2)
    private BigDecimal indiceConversionObjetivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_imputacion_costo_cultivo", length = 50)
    private MetodoImputacionCosto metodoImputacionCostoCultivo;

    @Column(name = "precio_manual_cultivo_kg", precision = 10, scale = 2)
    private BigDecimal precioManualCultivoKg;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnore
    private Empresa empresa;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    public enum MetodoImputacionCosto {
        PROMEDIO_PONDERADO, PRECIO_MERCADO, PRECIO_MANUAL
    }

    public DatosEconomicosPorcino() {}

    public DatosEconomicosPorcino(Empresa empresa) {
        this.empresa = empresa;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BigDecimal getCostoMadreGestacionDia() { return costoMadreGestacionDia; }
    public void setCostoMadreGestacionDia(BigDecimal costoMadreGestacionDia) { this.costoMadreGestacionDia = costoMadreGestacionDia; }
    public BigDecimal getCostoMadreLactanciaDia() { return costoMadreLactanciaDia; }
    public void setCostoMadreLactanciaDia(BigDecimal costoMadreLactanciaDia) { this.costoMadreLactanciaDia = costoMadreLactanciaDia; }
    public BigDecimal getCostoLechon() { return costoLechon; }
    public void setCostoLechon(BigDecimal costoLechon) { this.costoLechon = costoLechon; }
    public BigDecimal getCostoEngordeDia() { return costoEngordeDia; }
    public void setCostoEngordeDia(BigDecimal costoEngordeDia) { this.costoEngordeDia = costoEngordeDia; }
    public BigDecimal getCostoManoObraDia() { return costoManoObraDia; }
    public void setCostoManoObraDia(BigDecimal costoManoObraDia) { this.costoManoObraDia = costoManoObraDia; }
    public BigDecimal getPrecioVentaCerdoTerminadoKg() { return precioVentaCerdoTerminadoKg; }
    public void setPrecioVentaCerdoTerminadoKg(BigDecimal precioVentaCerdoTerminadoKg) { this.precioVentaCerdoTerminadoKg = precioVentaCerdoTerminadoKg; }
    public BigDecimal getPorcentajeMermaTransporte() { return porcentajeMermaTransporte; }
    public void setPorcentajeMermaTransporte(BigDecimal porcentajeMermaTransporte) { this.porcentajeMermaTransporte = porcentajeMermaTransporte; }
    public BigDecimal getKgMaizPorRacionEngorde() { return kgMaizPorRacionEngorde; }
    public void setKgMaizPorRacionEngorde(BigDecimal kgMaizPorRacionEngorde) { this.kgMaizPorRacionEngorde = kgMaizPorRacionEngorde; }
    public BigDecimal getPorcentajeMezclaAlimentoPropioBalanceado() { return porcentajeMezclaAlimentoPropioBalanceado; }
    public void setPorcentajeMezclaAlimentoPropioBalanceado(BigDecimal porcentajeMezclaAlimentoPropioBalanceado) { this.porcentajeMezclaAlimentoPropioBalanceado = porcentajeMezclaAlimentoPropioBalanceado; }
    public BigDecimal getIndiceConversionObjetivo() { return indiceConversionObjetivo; }
    public void setIndiceConversionObjetivo(BigDecimal indiceConversionObjetivo) { this.indiceConversionObjetivo = indiceConversionObjetivo; }
    public MetodoImputacionCosto getMetodoImputacionCostoCultivo() { return metodoImputacionCostoCultivo; }
    public void setMetodoImputacionCostoCultivo(MetodoImputacionCosto metodoImputacionCostoCultivo) { this.metodoImputacionCostoCultivo = metodoImputacionCostoCultivo; }
    public BigDecimal getPrecioManualCultivoKg() { return precioManualCultivoKg; }
    public void setPrecioManualCultivoKg(BigDecimal precioManualCultivoKg) { this.precioManualCultivoKg = precioManualCultivoKg; }
    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}
