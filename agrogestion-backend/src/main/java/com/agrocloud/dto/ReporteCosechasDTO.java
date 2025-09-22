package com.agrocloud.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para reportes de cosechas.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
public class ReporteCosechasDTO {
    
    private Long cosechaId;
    private Long loteId;
    private String nombreLote;
    private Long cultivoId;
    private String nombreCultivo;
    private String variedad;
    private BigDecimal superficieHectareas;
    private LocalDate fechaSiembra;
    private LocalDate fechaCosecha;
    private BigDecimal cantidadCosechada;
    private String unidadCosecha;
    private BigDecimal rendimientoReal;
    private BigDecimal rendimientoEsperado;
    private BigDecimal porcentajeCumplimiento;
    private BigDecimal precioPorTonelada;
    private BigDecimal costoTotal;
    private BigDecimal ingresoTotal;
    private BigDecimal rentabilidad;
    private String ubicacionCampo;
    private String observaciones;

    // Constructores
    public ReporteCosechasDTO() {}

    public ReporteCosechasDTO(Long cosechaId, Long loteId, String nombreLote, Long cultivoId, String nombreCultivo,
                             String variedad, BigDecimal superficieHectareas, LocalDate fechaSiembra,
                             LocalDate fechaCosecha, BigDecimal cantidadCosechada, String unidadCosecha,
                             BigDecimal rendimientoReal, BigDecimal rendimientoEsperado, BigDecimal porcentajeCumplimiento,
                             BigDecimal precioPorTonelada,
                             BigDecimal costoTotal, BigDecimal ingresoTotal, BigDecimal rentabilidad,
                             String ubicacionCampo, String observaciones) {
        this.cosechaId = cosechaId;
        this.loteId = loteId;
        this.nombreLote = nombreLote;
        this.cultivoId = cultivoId;
        this.nombreCultivo = nombreCultivo;
        this.variedad = variedad;
        this.superficieHectareas = superficieHectareas;
        this.fechaSiembra = fechaSiembra;
        this.fechaCosecha = fechaCosecha;
        this.cantidadCosechada = cantidadCosechada;
        this.unidadCosecha = unidadCosecha;
        this.rendimientoReal = rendimientoReal;
        this.rendimientoEsperado = rendimientoEsperado;
        this.porcentajeCumplimiento = porcentajeCumplimiento;
        this.precioPorTonelada = precioPorTonelada;
        this.costoTotal = costoTotal;
        this.ingresoTotal = ingresoTotal;
        this.rentabilidad = rentabilidad;
        this.ubicacionCampo = ubicacionCampo;
        this.observaciones = observaciones;
    }

    // Getters y Setters
    public Long getCosechaId() { return cosechaId; }
    public void setCosechaId(Long cosechaId) { this.cosechaId = cosechaId; }

    public Long getLoteId() { return loteId; }
    public void setLoteId(Long loteId) { this.loteId = loteId; }

    public String getNombreLote() { return nombreLote; }
    public void setNombreLote(String nombreLote) { this.nombreLote = nombreLote; }

    public Long getCultivoId() { return cultivoId; }
    public void setCultivoId(Long cultivoId) { this.cultivoId = cultivoId; }

    public String getNombreCultivo() { return nombreCultivo; }
    public void setNombreCultivo(String nombreCultivo) { this.nombreCultivo = nombreCultivo; }

    public String getVariedad() { return variedad; }
    public void setVariedad(String variedad) { this.variedad = variedad; }

    public BigDecimal getSuperficieHectareas() { return superficieHectareas; }
    public void setSuperficieHectareas(BigDecimal superficieHectareas) { this.superficieHectareas = superficieHectareas; }

    public LocalDate getFechaSiembra() { return fechaSiembra; }
    public void setFechaSiembra(LocalDate fechaSiembra) { this.fechaSiembra = fechaSiembra; }

    public LocalDate getFechaCosecha() { return fechaCosecha; }
    public void setFechaCosecha(LocalDate fechaCosecha) { this.fechaCosecha = fechaCosecha; }

    public BigDecimal getCantidadCosechada() { return cantidadCosechada; }
    public void setCantidadCosechada(BigDecimal cantidadCosechada) { this.cantidadCosechada = cantidadCosechada; }

    public String getUnidadCosecha() { return unidadCosecha; }
    public void setUnidadCosecha(String unidadCosecha) { this.unidadCosecha = unidadCosecha; }

    public BigDecimal getRendimientoReal() { return rendimientoReal; }
    public void setRendimientoReal(BigDecimal rendimientoReal) { this.rendimientoReal = rendimientoReal; }

    public BigDecimal getRendimientoEsperado() { return rendimientoEsperado; }
    public void setRendimientoEsperado(BigDecimal rendimientoEsperado) { this.rendimientoEsperado = rendimientoEsperado; }

    public BigDecimal getPorcentajeCumplimiento() { return porcentajeCumplimiento; }
    public void setPorcentajeCumplimiento(BigDecimal porcentajeCumplimiento) { this.porcentajeCumplimiento = porcentajeCumplimiento; }


    public BigDecimal getPrecioPorTonelada() { return precioPorTonelada; }
    public void setPrecioPorTonelada(BigDecimal precioPorTonelada) { this.precioPorTonelada = precioPorTonelada; }

    public BigDecimal getCostoTotal() { return costoTotal; }
    public void setCostoTotal(BigDecimal costoTotal) { this.costoTotal = costoTotal; }

    public BigDecimal getIngresoTotal() { return ingresoTotal; }
    public void setIngresoTotal(BigDecimal ingresoTotal) { this.ingresoTotal = ingresoTotal; }

    public BigDecimal getRentabilidad() { return rentabilidad; }
    public void setRentabilidad(BigDecimal rentabilidad) { this.rentabilidad = rentabilidad; }

    public String getUbicacionCampo() { return ubicacionCampo; }
    public void setUbicacionCampo(String ubicacionCampo) { this.ubicacionCampo = ubicacionCampo; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
