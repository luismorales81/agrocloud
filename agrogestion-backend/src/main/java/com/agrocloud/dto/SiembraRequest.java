package com.agrocloud.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class SiembraRequest {
    
    @NotNull(message = "El ID del cultivo es obligatorio")
    private Long cultivoId;
    
    @NotNull(message = "La fecha de siembra es obligatoria")
    private LocalDate fechaSiembra;
    
    // Densidad opcional - se puede obtener de los datos del cultivo si no se proporciona
    private BigDecimal densidadSiembra; // plantas por hectárea
    
    private String observaciones;
    
    private List<InsumoUsadoDTO> insumos;
    
    private List<MaquinariaAsignadaDTO> maquinaria;
    
    private List<ManoObraDTO> manoObra;
    
    // Constructores
    public SiembraRequest() {}
    
    public SiembraRequest(Long cultivoId, LocalDate fechaSiembra, BigDecimal densidadSiembra, String observaciones) {
        this.cultivoId = cultivoId;
        this.fechaSiembra = fechaSiembra;
        this.densidadSiembra = densidadSiembra;
        this.observaciones = observaciones;
    }
    
    // Getters y Setters
    public Long getCultivoId() {
        return cultivoId;
    }
    
    public void setCultivoId(Long cultivoId) {
        this.cultivoId = cultivoId;
    }
    
    public LocalDate getFechaSiembra() {
        return fechaSiembra;
    }
    
    public void setFechaSiembra(LocalDate fechaSiembra) {
        this.fechaSiembra = fechaSiembra;
    }
    
    public BigDecimal getDensidadSiembra() {
        return densidadSiembra;
    }
    
    public void setDensidadSiembra(BigDecimal densidadSiembra) {
        this.densidadSiembra = densidadSiembra;
    }
    
    public String getObservaciones() {
        return observaciones;
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
    
    public List<InsumoUsadoDTO> getInsumos() {
        return insumos;
    }
    
    public void setInsumos(List<InsumoUsadoDTO> insumos) {
        this.insumos = insumos;
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
}
