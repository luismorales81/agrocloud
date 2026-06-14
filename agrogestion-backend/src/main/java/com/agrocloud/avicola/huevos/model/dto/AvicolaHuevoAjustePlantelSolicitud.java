package com.agrocloud.avicola.huevos.model.dto;

public class AvicolaHuevoAjustePlantelSolicitud {
    private Integer cantidadAvesNueva;

    private String motivo;

    public Integer getCantidadAvesNueva() {
        return cantidadAvesNueva;
    }

    public void setCantidadAvesNueva(Integer cantidadAvesNueva) {
        this.cantidadAvesNueva = cantidadAvesNueva;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
