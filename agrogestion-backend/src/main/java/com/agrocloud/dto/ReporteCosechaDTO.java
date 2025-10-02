package com.agrocloud.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * DTO para reportes de cosecha enfocado en cantidad esperada vs obtenida
 */
public class ReporteCosechaDTO {
    
    private Long loteId;
    private String nombreLote;
    private String cultivoActual;
    private LocalDate fechaSiembra;
    private LocalDate fechaCosecha;
    private BigDecimal areaHectareas;
    
    // Cantidades
    private BigDecimal cantidadEsperada;
    private BigDecimal cantidadObtenida;
    private String unidadMedida;
    
    // Rendimientos
    private BigDecimal rendimientoEsperado; // kg/ha
    private BigDecimal rendimientoReal; // kg/ha
    
    // Análisis
    private BigDecimal porcentajeCumplimiento;
    private BigDecimal diferenciaAbsoluta;
    private BigDecimal diferenciaPorcentual;
    
    // Estado del lote
    private String estadoLote;
    private String observaciones;
    
    public ReporteCosechaDTO() {}
    
    public ReporteCosechaDTO(Long loteId, String nombreLote, String cultivoActual, 
                           LocalDate fechaSiembra, LocalDate fechaCosecha,
                           BigDecimal areaHectareas, BigDecimal cantidadEsperada, 
                           BigDecimal cantidadObtenida, String unidadMedida,
                           BigDecimal rendimientoEsperado, BigDecimal rendimientoReal,
                           BigDecimal porcentajeCumplimiento, String estadoLote, String observaciones) {
        this.loteId = loteId;
        this.nombreLote = nombreLote;
        this.cultivoActual = cultivoActual;
        this.fechaSiembra = fechaSiembra;
        this.fechaCosecha = fechaCosecha;
        this.areaHectareas = areaHectareas;
        this.cantidadEsperada = cantidadEsperada;
        this.cantidadObtenida = cantidadObtenida;
        this.unidadMedida = unidadMedida;
        this.rendimientoEsperado = rendimientoEsperado;
        this.rendimientoReal = rendimientoReal;
        this.porcentajeCumplimiento = porcentajeCumplimiento;
        this.estadoLote = estadoLote;
        this.observaciones = observaciones;
        
        // Calcular diferencias
        if (cantidadEsperada != null && cantidadObtenida != null) {
            this.diferenciaAbsoluta = cantidadObtenida.subtract(cantidadEsperada);
            if (cantidadEsperada.compareTo(BigDecimal.ZERO) > 0) {
                this.diferenciaPorcentual = diferenciaAbsoluta.divide(cantidadEsperada, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
            }
        }
    }
    
    // Getters y Setters
    public Long getLoteId() { return loteId; }
    public void setLoteId(Long loteId) { this.loteId = loteId; }
    
    public String getNombreLote() { return nombreLote; }
    public void setNombreLote(String nombreLote) { this.nombreLote = nombreLote; }
    
    public String getCultivoActual() { return cultivoActual; }
    public void setCultivoActual(String cultivoActual) { this.cultivoActual = cultivoActual; }
    
    public LocalDate getFechaSiembra() { return fechaSiembra; }
    public void setFechaSiembra(LocalDate fechaSiembra) { this.fechaSiembra = fechaSiembra; }
    
    public LocalDate getFechaCosecha() { return fechaCosecha; }
    public void setFechaCosecha(LocalDate fechaCosecha) { this.fechaCosecha = fechaCosecha; }
    
    public BigDecimal getAreaHectareas() { return areaHectareas; }
    public void setAreaHectareas(BigDecimal areaHectareas) { this.areaHectareas = areaHectareas; }
    
    public BigDecimal getCantidadEsperada() { return cantidadEsperada; }
    public void setCantidadEsperada(BigDecimal cantidadEsperada) { this.cantidadEsperada = cantidadEsperada; }
    
    public BigDecimal getCantidadObtenida() { return cantidadObtenida; }
    public void setCantidadObtenida(BigDecimal cantidadObtenida) { this.cantidadObtenida = cantidadObtenida; }
    
    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }
    
    public BigDecimal getRendimientoEsperado() { return rendimientoEsperado; }
    public void setRendimientoEsperado(BigDecimal rendimientoEsperado) { this.rendimientoEsperado = rendimientoEsperado; }
    
    public BigDecimal getRendimientoReal() { return rendimientoReal; }
    public void setRendimientoReal(BigDecimal rendimientoReal) { this.rendimientoReal = rendimientoReal; }
    
    public BigDecimal getPorcentajeCumplimiento() { return porcentajeCumplimiento; }
    public void setPorcentajeCumplimiento(BigDecimal porcentajeCumplimiento) { this.porcentajeCumplimiento = porcentajeCumplimiento; }
    
    public BigDecimal getDiferenciaAbsoluta() { return diferenciaAbsoluta; }
    public void setDiferenciaAbsoluta(BigDecimal diferenciaAbsoluta) { this.diferenciaAbsoluta = diferenciaAbsoluta; }
    
    public BigDecimal getDiferenciaPorcentual() { return diferenciaPorcentual; }
    public void setDiferenciaPorcentual(BigDecimal diferenciaPorcentual) { this.diferenciaPorcentual = diferenciaPorcentual; }
    
    public String getEstadoLote() { return estadoLote; }
    public void setEstadoLote(String estadoLote) { this.estadoLote = estadoLote; }
    
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    
    // Métodos de análisis
    public boolean isCumplimientoSatisfactorio() {
        return porcentajeCumplimiento != null && porcentajeCumplimiento.compareTo(new BigDecimal("80")) >= 0;
    }
    
    public boolean isSobreCumplimiento() {
        return porcentajeCumplimiento != null && porcentajeCumplimiento.compareTo(new BigDecimal("100")) > 0;
    }
    
    public boolean isSubCumplimiento() {
        return porcentajeCumplimiento != null && porcentajeCumplimiento.compareTo(new BigDecimal("80")) < 0;
    }
    
    public String getEvaluacionCumplimiento() {
        if (porcentajeCumplimiento == null) {
            return "Sin datos";
        }
        
        if (isSobreCumplimiento()) {
            return "Excelente - Sobre cumplimiento";
        } else if (isCumplimientoSatisfactorio()) {
            return "Bueno - Cumplimiento satisfactorio";
        } else {
            return "Requiere atención - Sub cumplimiento";
        }
    }
    
    public String getResumenCantidades() {
        if (cantidadEsperada == null || cantidadObtenida == null) {
            return "Datos incompletos";
        }
        
        return String.format("%.2f %s obtenidos de %.2f %s esperados (%.1f%%)",
                cantidadObtenida, unidadMedida != null ? unidadMedida : "kg",
                cantidadEsperada, unidadMedida != null ? unidadMedida : "kg",
                porcentajeCumplimiento != null ? porcentajeCumplimiento : BigDecimal.ZERO);
    }
}
