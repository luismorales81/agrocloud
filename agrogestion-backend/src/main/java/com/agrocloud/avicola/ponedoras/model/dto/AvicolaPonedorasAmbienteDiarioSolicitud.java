package com.agrocloud.avicola.ponedoras.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AvicolaPonedorasAmbienteDiarioSolicitud {

    private LocalDate fecha;
    private BigDecimal temperaturaDia;
    private BigDecimal humedadDia;

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getTemperaturaDia() {
        return temperaturaDia;
    }

    public void setTemperaturaDia(BigDecimal temperaturaDia) {
        this.temperaturaDia = temperaturaDia;
    }

    public BigDecimal getHumedadDia() {
        return humedadDia;
    }

    public void setHumedadDia(BigDecimal humedadDia) {
        this.humedadDia = humedadDia;
    }
}
