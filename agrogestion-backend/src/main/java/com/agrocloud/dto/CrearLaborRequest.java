package com.agrocloud.dto;

import com.agrocloud.model.entity.Labor;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class CrearLaborRequest {
    
    @JsonProperty("tipoLabor")
    private String tipoLabor;
    
    @JsonProperty("descripcion")
    private String descripcion;
    
    @JsonProperty("fechaInicio")
    private String fechaInicio;
    
    @JsonProperty("fechaFin")
    private String fechaFin;
    
    @JsonProperty("estado")
    private String estado;
    
    @JsonProperty("responsable")
    private String responsable;
    
    @JsonProperty("horasTrabajo")
    private BigDecimal horasTrabajo;
    
    @JsonProperty("costoTotal")
    private BigDecimal costoTotal;
    
    @JsonProperty("lote")
    private Map<String, Object> lote;
    
    @JsonProperty("insumosUsados")
    private List<Map<String, Object>> insumosUsados;
    
    @JsonProperty("maquinariaAsignada")
    private List<Map<String, Object>> maquinariaAsignada;
    
    @JsonProperty("manoObra")
    private List<Map<String, Object>> manoObra;
    
    // Constructores
    public CrearLaborRequest() {}
    
    // Getters y Setters
    public String getTipoLabor() { return tipoLabor; }
    public void setTipoLabor(String tipoLabor) { this.tipoLabor = tipoLabor; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public String getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(String fechaInicio) { this.fechaInicio = fechaInicio; }
    
    public String getFechaFin() { return fechaFin; }
    public void setFechaFin(String fechaFin) { this.fechaFin = fechaFin; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public String getResponsable() { return responsable; }
    public void setResponsable(String responsable) { this.responsable = responsable; }
    
    public BigDecimal getHorasTrabajo() { return horasTrabajo; }
    public void setHorasTrabajo(BigDecimal horasTrabajo) { this.horasTrabajo = horasTrabajo; }
    
    public BigDecimal getCostoTotal() { return costoTotal; }
    public void setCostoTotal(BigDecimal costoTotal) { this.costoTotal = costoTotal; }
    
    public Map<String, Object> getLote() { return lote; }
    public void setLote(Map<String, Object> lote) { this.lote = lote; }
    
    public List<Map<String, Object>> getInsumosUsados() { return insumosUsados; }
    public void setInsumosUsados(List<Map<String, Object>> insumosUsados) { this.insumosUsados = insumosUsados; }
    
    public List<Map<String, Object>> getMaquinariaAsignada() { return maquinariaAsignada; }
    public void setMaquinariaAsignada(List<Map<String, Object>> maquinariaAsignada) { this.maquinariaAsignada = maquinariaAsignada; }
    
    public List<Map<String, Object>> getManoObra() { return manoObra; }
    public void setManoObra(List<Map<String, Object>> manoObra) { this.manoObra = manoObra; }
}
