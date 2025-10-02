package com.agrocloud.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO para registrar una cosecha completa.
 * Incluye todos los campos necesarios para historial_cosechas.
 */
public class CosechaRequest {
    
    @NotNull(message = "La fecha de cosecha es obligatoria")
    private LocalDate fechaCosecha;
    
    @Positive(message = "La cantidad cosechada debe ser positiva")
    private BigDecimal cantidadCosechada;
    
    @NotNull(message = "La unidad de medida es obligatoria")
    private String unidadMedida; // "kg", "ton", "qq"
    
    // Campos para historial_cosechas
    private String variedadSemilla;
    
    private String estadoSuelo; // BUENO, DESCANSANDO, AGOTADO
    
    private Boolean requiereDescanso;
    
    private Integer diasDescansoRecomendados;
    
    private BigDecimal precioVenta; // precio por unidad
    
    private String observaciones;
    
    private List<MaquinariaAsignadaDTO> maquinaria;
    
    private List<ManoObraDTO> manoObra;
    
    // Constructores
    public CosechaRequest() {}
    
    public CosechaRequest(LocalDate fechaCosecha, BigDecimal cantidadCosechada, String unidadMedida) {
        this.fechaCosecha = fechaCosecha;
        this.cantidadCosechada = cantidadCosechada;
        this.unidadMedida = unidadMedida;
    }
    
    // Getters y Setters
    public LocalDate getFechaCosecha() {
        return fechaCosecha;
    }
    
    public void setFechaCosecha(LocalDate fechaCosecha) {
        this.fechaCosecha = fechaCosecha;
    }
    
    public BigDecimal getCantidadCosechada() {
        return cantidadCosechada;
    }
    
    public void setCantidadCosechada(BigDecimal cantidadCosechada) {
        this.cantidadCosechada = cantidadCosechada;
    }
    
    public String getUnidadMedida() {
        return unidadMedida;
    }
    
    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }
    
    public BigDecimal getPrecioVenta() {
        return precioVenta;
    }
    
    public void setPrecioVenta(BigDecimal precioVenta) {
        this.precioVenta = precioVenta;
    }
    
    public String getObservaciones() {
        return observaciones;
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
    
    public List<MaquinariaAsignadaDTO> getMaquinaria() {
        return maquinaria;
    }
    
    public void setMaquinaria(List<MaquinariaAsignadaDTO> maquinaria) {
        this.maquinaria = maquinaria;
    }
    
    public List<ManoObraDTO> getManoObra() {
        return manoObra;
    }
    
    public void setManoObra(List<ManoObraDTO> manoObra) {
        this.manoObra = manoObra;
    }
    
    public String getVariedadSemilla() {
        return variedadSemilla;
    }
    
    public void setVariedadSemilla(String variedadSemilla) {
        this.variedadSemilla = variedadSemilla;
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
}
