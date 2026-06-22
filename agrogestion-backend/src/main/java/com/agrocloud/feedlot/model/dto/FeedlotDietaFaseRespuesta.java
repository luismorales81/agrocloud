package com.agrocloud.feedlot.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FeedlotDietaFaseRespuesta {

    private Long id;
    private Long dietaId;
    private String nombreFase;
    private Integer diasDesdeIngreso;
    private BigDecimal kgMsCabezaDia;
    private Long insumoId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDietaId() {
        return dietaId;
    }

    public void setDietaId(Long dietaId) {
        this.dietaId = dietaId;
    }

    public String getNombreFase() {
        return nombreFase;
    }

    public void setNombreFase(String nombreFase) {
        this.nombreFase = nombreFase;
    }

    public Integer getDiasDesdeIngreso() {
        return diasDesdeIngreso;
    }

    public void setDiasDesdeIngreso(Integer diasDesdeIngreso) {
        this.diasDesdeIngreso = diasDesdeIngreso;
    }

    public BigDecimal getKgMsCabezaDia() {
        return kgMsCabezaDia;
    }

    public void setKgMsCabezaDia(BigDecimal kgMsCabezaDia) {
        this.kgMsCabezaDia = kgMsCabezaDia;
    }

    public Long getInsumoId() {
        return insumoId;
    }

    public void setInsumoId(Long insumoId) {
        this.insumoId = insumoId;
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
}
