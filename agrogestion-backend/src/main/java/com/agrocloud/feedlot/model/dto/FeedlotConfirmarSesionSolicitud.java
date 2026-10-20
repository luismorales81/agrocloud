package com.agrocloud.feedlot.model.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class FeedlotConfirmarSesionSolicitud {

    private Boolean ajustarPlantel;
    private BigDecimal cantidadTotal;
    private List<FeedlotAccionDesconocidoSolicitud> accionesDesconocidos = new ArrayList<>();
    private FeedlotLoteNuevoRecuentoSolicitud loteNuevo;

    public Boolean getAjustarPlantel() { return ajustarPlantel; }
    public void setAjustarPlantel(Boolean ajustarPlantel) { this.ajustarPlantel = ajustarPlantel; }
    public BigDecimal getCantidadTotal() { return cantidadTotal; }
    public void setCantidadTotal(BigDecimal cantidadTotal) { this.cantidadTotal = cantidadTotal; }
    public List<FeedlotAccionDesconocidoSolicitud> getAccionesDesconocidos() { return accionesDesconocidos; }
    public void setAccionesDesconocidos(List<FeedlotAccionDesconocidoSolicitud> accionesDesconocidos) {
        this.accionesDesconocidos = accionesDesconocidos;
    }
    public FeedlotLoteNuevoRecuentoSolicitud getLoteNuevo() { return loteNuevo; }
    public void setLoteNuevo(FeedlotLoteNuevoRecuentoSolicitud loteNuevo) { this.loteNuevo = loteNuevo; }
}
