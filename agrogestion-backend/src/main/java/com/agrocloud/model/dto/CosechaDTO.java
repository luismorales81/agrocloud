package com.agrocloud.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO para la entidad Cosecha
 */
public class CosechaDTO {

    private Long id;

    @NotNull(message = "El cultivo es obligatorio")
    private Long cultivoId;

    private String cultivoNombre;

    @NotNull(message = "La fecha de cosecha es obligatoria")
    private LocalDate fecha;

    private Long loteId;
    private String loteNombre;

    @Positive(message = "La cantidad de toneladas debe ser un valor positivo")
    private BigDecimal cantidadToneladas;

    private BigDecimal precioPorTonelada;

    private BigDecimal costoTotal;

    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    private String observaciones;

    private Long usuarioId;
    private String usuarioNombre;
    private Long empresaId;
    private String empresaNombre;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructores
    public CosechaDTO() {}

    public CosechaDTO(Long cultivoId, Long loteId, LocalDate fecha, BigDecimal cantidadToneladas, 
                      BigDecimal precioPorTonelada, BigDecimal costoTotal, String observaciones) {
        this.cultivoId = cultivoId;
        this.loteId = loteId;
        this.fecha = fecha;
        this.cantidadToneladas = cantidadToneladas;
        this.precioPorTonelada = precioPorTonelada;
        this.costoTotal = costoTotal;
        this.observaciones = observaciones;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
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

    public BigDecimal getCantidadToneladas() {
        return cantidadToneladas;
    }

    public void setCantidadToneladas(BigDecimal cantidadToneladas) {
        this.cantidadToneladas = cantidadToneladas;
    }

    public BigDecimal getPrecioPorTonelada() {
        return precioPorTonelada;
    }

    public void setPrecioPorTonelada(BigDecimal precioPorTonelada) {
        this.precioPorTonelada = precioPorTonelada;
    }

    public BigDecimal getCostoTotal() {
        return costoTotal;
    }

    public void setCostoTotal(BigDecimal costoTotal) {
        this.costoTotal = costoTotal;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }

    public String getEmpresaNombre() {
        return empresaNombre;
    }

    public void setEmpresaNombre(String empresaNombre) {
        this.empresaNombre = empresaNombre;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Calcula el rendimiento total (cantidad de toneladas)
     */
    public BigDecimal getRendimientoTotal() {
        return cantidadToneladas != null ? cantidadToneladas : BigDecimal.ZERO;
    }

    @Override
    public String toString() {
        return "CosechaDTO{" +
                "id=" + id +
                ", cultivoId=" + cultivoId +
                ", cultivoNombre='" + cultivoNombre + '\'' +
                ", loteId=" + loteId +
                ", loteNombre='" + loteNombre + '\'' +
                ", fecha=" + fecha +
                ", cantidadToneladas=" + cantidadToneladas +
                ", precioPorTonelada=" + precioPorTonelada +
                ", costoTotal=" + costoTotal +
                ", observaciones='" + observaciones + '\'' +
                '}';
    }
}
