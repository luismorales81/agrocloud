package com.agrocloud.dto;

import com.agrocloud.model.entity.HistorialCosecha;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para transferir datos de cosecha sin referencias circulares
 */
public class CosechaDTO {
    private Long id;
    private Long loteId;
    private String loteNombre;
    private Long cultivoId;
    private String cultivoNombre;
    private String variedadSemilla;
    private LocalDate fechaSiembra;
    private LocalDate fechaCosecha;
    private BigDecimal superficieHectareas;
    private BigDecimal cantidadCosechada;
    private String unidadCosecha;
    private BigDecimal rendimientoReal;
    private BigDecimal rendimientoEsperado;
    private BigDecimal diferenciaRendimiento;
    private BigDecimal porcentajeCumplimiento;
    private String observaciones;
    
    // Constructor por defecto
    public CosechaDTO() {}
    
    // Constructor desde entidad
    public CosechaDTO(HistorialCosecha historial) {
        this.id = historial.getId();
        
        // Información del lote
        if (historial.getLote() != null) {
            this.loteId = historial.getLote().getId();
            this.loteNombre = historial.getLote().getNombre();
        } else {
            this.loteNombre = "Lote no encontrado";
        }
        
        // Información del cultivo
        if (historial.getCultivo() != null) {
            this.cultivoId = historial.getCultivo().getId();
            this.cultivoNombre = historial.getCultivo().getNombre();
            this.variedadSemilla = historial.getCultivo().getVariedad();
            this.rendimientoEsperado = historial.getCultivo().getRendimientoEsperado();
        } else {
            this.cultivoNombre = "Cultivo no encontrado";
            this.variedadSemilla = "";
            this.rendimientoEsperado = BigDecimal.ZERO;
        }
        
        this.fechaSiembra = historial.getFechaSiembra();
        this.fechaCosecha = historial.getFechaCosecha();
        this.superficieHectareas = historial.getSuperficieHectareas();
        this.cantidadCosechada = historial.getCantidadCosechada();
        this.unidadCosecha = historial.getUnidadCosecha();
        this.rendimientoReal = historial.getRendimientoReal();
        
        // Calcular diferencia de rendimiento
        if (historial.getRendimientoReal() != null && historial.getRendimientoEsperado() != null) {
            this.diferenciaRendimiento = historial.getRendimientoReal().subtract(historial.getRendimientoEsperado());
        } else {
            this.diferenciaRendimiento = BigDecimal.ZERO;
        }
        
        // Usar el método calculado de porcentaje de cumplimiento
        this.porcentajeCumplimiento = historial.getPorcentajeCumplimiento();
        this.observaciones = historial.getObservaciones();
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getLoteId() {
        return loteId;
    }
    
    public void setLoteId(Long loteId) {
        this.loteId = loteId;
    }
    
    public String getLoteNombre() {
        return loteNombre;
    }
    
    public void setLoteNombre(String loteNombre) {
        this.loteNombre = loteNombre;
    }
    
    public Long getCultivoId() {
        return cultivoId;
    }
    
    public void setCultivoId(Long cultivoId) {
        this.cultivoId = cultivoId;
    }
    
    public String getCultivoNombre() {
        return cultivoNombre;
    }
    
    public void setCultivoNombre(String cultivoNombre) {
        this.cultivoNombre = cultivoNombre;
    }
    
    public String getVariedadSemilla() {
        return variedadSemilla;
    }
    
    public void setVariedadSemilla(String variedadSemilla) {
        this.variedadSemilla = variedadSemilla;
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
    
    public BigDecimal getDiferenciaRendimiento() {
        return diferenciaRendimiento;
    }
    
    public void setDiferenciaRendimiento(BigDecimal diferenciaRendimiento) {
        this.diferenciaRendimiento = diferenciaRendimiento;
    }
    
    public BigDecimal getPorcentajeCumplimiento() {
        return porcentajeCumplimiento;
    }
    
    public void setPorcentajeCumplimiento(BigDecimal porcentajeCumplimiento) {
        this.porcentajeCumplimiento = porcentajeCumplimiento;
    }
    
    public String getObservaciones() {
        return observaciones;
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
