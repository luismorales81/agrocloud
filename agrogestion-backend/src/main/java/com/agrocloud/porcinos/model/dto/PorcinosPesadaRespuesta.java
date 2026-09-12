package com.agrocloud.porcinos.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PorcinosPesadaRespuesta {

    private Long id;
    private Long loteId;
    private Long empresaId;
    private LocalDate fecha;
    private BigDecimal pesoPromedioKg;
    private Integer cabezasMuestreadas;
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

    public BigDecimal getPesoPromedioKg() {
        return pesoPromedioKg;
    }

    public void setPesoPromedioKg(BigDecimal pesoPromedioKg) {
        this.pesoPromedioKg = pesoPromedioKg;
    }

    public Integer getCabezasMuestreadas() {
        return cabezasMuestreadas;
    }

    public void setCabezasMuestreadas(Integer cabezasMuestreadas) {
        this.cabezasMuestreadas = cabezasMuestreadas;
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
