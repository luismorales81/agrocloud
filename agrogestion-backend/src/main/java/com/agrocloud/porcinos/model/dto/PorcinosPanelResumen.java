package com.agrocloud.porcinos.model.dto;

import java.math.BigDecimal;

public class PorcinosPanelResumen {

    private Long campanaId;
    private Long cabezasEnLotes;
    private Long lotesActivos;
    private Long madresActivas;
    private Long gestacionesEnCurso;
    private Long madresEnLactancia;
    private BigDecimal mortalidadPctPromedio;
    private BigDecimal consumoTotalKg;

    public Long getCampanaId() {
        return campanaId;
    }

    public void setCampanaId(Long campanaId) {
        this.campanaId = campanaId;
    }

    public Long getCabezasEnLotes() {
        return cabezasEnLotes;
    }

    public void setCabezasEnLotes(Long cabezasEnLotes) {
        this.cabezasEnLotes = cabezasEnLotes;
    }

    public Long getLotesActivos() {
        return lotesActivos;
    }

    public void setLotesActivos(Long lotesActivos) {
        this.lotesActivos = lotesActivos;
    }

    public Long getMadresActivas() {
        return madresActivas;
    }

    public void setMadresActivas(Long madresActivas) {
        this.madresActivas = madresActivas;
    }

    public Long getGestacionesEnCurso() {
        return gestacionesEnCurso;
    }

    public void setGestacionesEnCurso(Long gestacionesEnCurso) {
        this.gestacionesEnCurso = gestacionesEnCurso;
    }

    public Long getMadresEnLactancia() {
        return madresEnLactancia;
    }

    public void setMadresEnLactancia(Long madresEnLactancia) {
        this.madresEnLactancia = madresEnLactancia;
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
