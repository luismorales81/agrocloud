package com.agrocloud.feedlot.model.dto;

import java.math.BigDecimal;

public class FeedlotPanelRespuesta {

    private Long campanaId;
    private Long cabezasEnFeed;
    private Long lotesActivos;
    private BigDecimal gmdPromedio;
    private BigDecimal mortalidadPctPromedio;
    private BigDecimal consumoTotalKg;

    public Long getCampanaId() {
        return campanaId;
    }

    public void setCampanaId(Long campanaId) {
        this.campanaId = campanaId;
    }

    public Long getCabezasEnFeed() {
        return cabezasEnFeed;
    }

    public void setCabezasEnFeed(Long cabezasEnFeed) {
        this.cabezasEnFeed = cabezasEnFeed;
    }

    public Long getLotesActivos() {
        return lotesActivos;
    }

    public void setLotesActivos(Long lotesActivos) {
        this.lotesActivos = lotesActivos;
    }

    public BigDecimal getGmdPromedio() {
        return gmdPromedio;
    }

    public void setGmdPromedio(BigDecimal gmdPromedio) {
        this.gmdPromedio = gmdPromedio;
    }

    public BigDecimal getMortalidadPctPromedio() {
        return mortalidadPctPromedio;
    }

    public void setMortalidadPctPromedio(BigDecimal mortalidadPctPromedio) {
        this.mortalidadPctPromedio = mortalidadPctPromedio;
    }

    public BigDecimal getConsumoTotalKg() {
        return consumoTotalKg;
    }

    public void setConsumoTotalKg(BigDecimal consumoTotalKg) {
        this.consumoTotalKg = consumoTotalKg;
    }
}
