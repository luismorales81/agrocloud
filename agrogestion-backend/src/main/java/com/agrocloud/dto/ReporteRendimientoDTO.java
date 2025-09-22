package com.agrocloud.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para reportes de rendimiento.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
public class ReporteRendimientoDTO {
    
    private Long loteId;
    private String nombreLote;
    private Long cultivoId;
    private String nombreCultivo;
    private String variedad;
    private BigDecimal superficieHectareas;
    private LocalDate fechaSiembra;
    private LocalDate fechaCosecha;
    private BigDecimal rendimientoEsperado;
    private BigDecimal rendimientoReal;
    private BigDecimal rendimientoCorregido;
    private BigDecimal porcentajeCumplimiento;
    private BigDecimal diferenciaRendimiento;
    private String unidadRendimiento;
    private String observaciones;

    // Constructores
    public ReporteRendimientoDTO() {}

    public ReporteRendimientoDTO(Long loteId, String nombreLote, Long cultivoId, String nombreCultivo,
                                String variedad, BigDecimal superficieHectareas, LocalDate fechaSiembra,
                                LocalDate fechaCosecha, BigDecimal rendimientoEsperado, BigDecimal rendimientoReal,
                                BigDecimal rendimientoCorregido, BigDecimal porcentajeCumplimiento,
                                BigDecimal diferenciaRendimiento, String unidadRendimiento,
                                String observaciones) {
        this.loteId = loteId;
        this.nombreLote = nombreLote;
        this.cultivoId = cultivoId;
        this.nombreCultivo = nombreCultivo;
        this.variedad = variedad;
        this.superficieHectareas = superficieHectareas;
        this.fechaSiembra = fechaSiembra;
        this.fechaCosecha = fechaCosecha;
        this.rendimientoEsperado = rendimientoEsperado;
        this.rendimientoReal = rendimientoReal;
        this.rendimientoCorregido = rendimientoCorregido;
        this.porcentajeCumplimiento = porcentajeCumplimiento;
        this.diferenciaRendimiento = diferenciaRendimiento;
        this.unidadRendimiento = unidadRendimiento;
        this.observaciones = observaciones;
    }

    // Getters y Setters
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

    public BigDecimal getRendimientoEsperado() { return rendimientoEsperado; }
    public void setRendimientoEsperado(BigDecimal rendimientoEsperado) { this.rendimientoEsperado = rendimientoEsperado; }

    public BigDecimal getRendimientoReal() { return rendimientoReal; }
    public void setRendimientoReal(BigDecimal rendimientoReal) { this.rendimientoReal = rendimientoReal; }

    public BigDecimal getRendimientoCorregido() { return rendimientoCorregido; }
    public void setRendimientoCorregido(BigDecimal rendimientoCorregido) { this.rendimientoCorregido = rendimientoCorregido; }

    public BigDecimal getPorcentajeCumplimiento() { return porcentajeCumplimiento; }
    public void setPorcentajeCumplimiento(BigDecimal porcentajeCumplimiento) { this.porcentajeCumplimiento = porcentajeCumplimiento; }

    public BigDecimal getDiferenciaRendimiento() { return diferenciaRendimiento; }
    public void setDiferenciaRendimiento(BigDecimal diferenciaRendimiento) { this.diferenciaRendimiento = diferenciaRendimiento; }

    public String getUnidadRendimiento() { return unidadRendimiento; }
    public void setUnidadRendimiento(String unidadRendimiento) { this.unidadRendimiento = unidadRendimiento; }


    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
