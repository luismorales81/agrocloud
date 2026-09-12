package com.agrocloud.porcinos.model.dto;

import java.math.BigDecimal;

public class PorcinosDietaFaseSolicitud {

    private String nombreFase;
    private Integer diasDesdeIngreso;
    private BigDecimal kgCabezaDia;
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

    public BigDecimal getKgCabezaDia() {
        return kgCabezaDia;
    }

    public void setKgCabezaDia(BigDecimal kgCabezaDia) {
        this.kgCabezaDia = kgCabezaDia;
    }

    public Long getInsumoId() {
        return insumoId;
    }

    public void setInsumoId(Long insumoId) {
        this.insumoId = insumoId;
    }
}
