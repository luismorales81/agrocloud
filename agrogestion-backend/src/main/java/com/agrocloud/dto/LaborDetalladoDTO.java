package com.agrocloud.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class LaborDetalladoDTO {
    private Long id;
    private String tipo;
    private String nombre;
    private String descripcion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estado;
    private BigDecimal costoBase;
    private String observaciones;
    private Long loteId;
    private String loteNombre;
    private String responsable;
    private BigDecimal horasTrabajo;
    private BigDecimal costoTotal;
    private Integer progreso;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private Boolean activo;
    private Long empresaId;
    private Long userId;
    
    // Nuevos campos para costos detallados
    private BigDecimal costoMaquinaria;
    private BigDecimal costoManoObra;
    private BigDecimal costoInsumos;
    private List<LaborMaquinariaDTO> maquinarias;
    private List<LaborManoObraDTO> manoObra;
    private List<LaborInsumoDTO> insumosUsados;
    
    // Constructores
    public LaborDetalladoDTO() {}
    
    public LaborDetalladoDTO(Long id, String tipo, String nombre, String descripcion, 
                           LocalDate fechaInicio, LocalDate fechaFin, String estado,
                           BigDecimal costoBase, String observaciones, Long loteId, 
                           String loteNombre, String responsable, BigDecimal horasTrabajo,
                           BigDecimal costoTotal, Integer progreso, LocalDateTime fechaCreacion,
                           LocalDateTime fechaActualizacion, Boolean activo, Long empresaId, 
                           Long userId, BigDecimal costoMaquinaria, BigDecimal costoManoObra,
                           BigDecimal costoInsumos, List<LaborMaquinariaDTO> maquinarias, 
                           List<LaborManoObraDTO> manoObra, List<LaborInsumoDTO> insumosUsados) {
        this.id = id;
        this.tipo = tipo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;
        this.costoBase = costoBase;
        this.observaciones = observaciones;
        this.loteId = loteId;
        this.loteNombre = loteNombre;
        this.responsable = responsable;
        this.horasTrabajo = horasTrabajo;
        this.costoTotal = costoTotal;
        this.progreso = progreso;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
        this.activo = activo;
        this.empresaId = empresaId;
        this.userId = userId;
        this.costoMaquinaria = costoMaquinaria;
        this.costoManoObra = costoManoObra;
        this.costoInsumos = costoInsumos;
        this.maquinarias = maquinarias;
        this.manoObra = manoObra;
        this.insumosUsados = insumosUsados;
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public BigDecimal getCostoBase() { return costoBase; }
    public void setCostoBase(BigDecimal costoBase) { this.costoBase = costoBase; }
    
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    
    public Long getLoteId() { return loteId; }
    public void setLoteId(Long loteId) { this.loteId = loteId; }
    
    public String getLoteNombre() { return loteNombre; }
    public void setLoteNombre(String loteNombre) { this.loteNombre = loteNombre; }
    
    public String getResponsable() { return responsable; }
    public void setResponsable(String responsable) { this.responsable = responsable; }
    
    public BigDecimal getHorasTrabajo() { return horasTrabajo; }
    public void setHorasTrabajo(BigDecimal horasTrabajo) { this.horasTrabajo = horasTrabajo; }
    
    public BigDecimal getCostoTotal() { return costoTotal; }
    public void setCostoTotal(BigDecimal costoTotal) { this.costoTotal = costoTotal; }
    
    public Integer getProgreso() { return progreso; }
    public void setProgreso(Integer progreso) { this.progreso = progreso; }
    
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    
    public Long getEmpresaId() { return empresaId; }
    public void setEmpresaId(Long empresaId) { this.empresaId = empresaId; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public BigDecimal getCostoMaquinaria() { return costoMaquinaria; }
    public void setCostoMaquinaria(BigDecimal costoMaquinaria) { this.costoMaquinaria = costoMaquinaria; }
    
    public BigDecimal getCostoManoObra() { return costoManoObra; }
    public void setCostoManoObra(BigDecimal costoManoObra) { this.costoManoObra = costoManoObra; }
    
    public BigDecimal getCostoInsumos() { return costoInsumos; }
    public void setCostoInsumos(BigDecimal costoInsumos) { this.costoInsumos = costoInsumos; }
    
    public List<LaborMaquinariaDTO> getMaquinarias() { return maquinarias; }
    public void setMaquinarias(List<LaborMaquinariaDTO> maquinarias) { this.maquinarias = maquinarias; }
    
    public List<LaborManoObraDTO> getManoObra() { return manoObra; }
    public void setManoObra(List<LaborManoObraDTO> manoObra) { this.manoObra = manoObra; }
    
    public List<LaborInsumoDTO> getInsumosUsados() { return insumosUsados; }
    public void setInsumosUsados(List<LaborInsumoDTO> insumosUsados) { this.insumosUsados = insumosUsados; }
}
