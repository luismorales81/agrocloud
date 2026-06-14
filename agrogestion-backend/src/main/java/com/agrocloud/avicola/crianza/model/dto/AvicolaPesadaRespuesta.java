package com.agrocloud.avicola.crianza.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Salida de pesada.
 */
public class AvicolaPesadaRespuesta {

    private Long id;
    private Long loteId;
    private Long empresaId;
    private LocalDate fecha;
    private BigDecimal pesoPromedio;
    private Integer cantidadPesada;
    private String observaciones;
    private LocalDateTime createdAt;

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

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getPesoPromedio() {
        return pesoPromedio;
    }

    public void setPesoPromedio(BigDecimal pesoPromedio) {
        this.pesoPromedio = pesoPromedio;
    }

    public Integer getCantidadPesada() {
        return cantidadPesada;
    }

    public void setCantidadPesada(Integer cantidadPesada) {
        this.cantidadPesada = cantidadPesada;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
