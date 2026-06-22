package com.agrocloud.feedlot.model.dto;

import java.math.BigDecimal;

public class FeedlotDietaFaseSolicitud {

    private String nombreFase;
    private Integer diasDesdeIngreso;
    private BigDecimal kgMsCabezaDia;
    private Long insumoId;

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
}
