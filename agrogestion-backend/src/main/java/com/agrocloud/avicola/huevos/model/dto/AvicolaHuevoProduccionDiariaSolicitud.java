package com.agrocloud.avicola.huevos.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AvicolaHuevoProduccionDiariaSolicitud {

    private LocalDate fecha;
    /** Compatibilidad: si no se envían tamaños, se usa solo este valor. */
    private Integer cantidadHuevos;
    private Integer huevosTam1;
    private Integer huevosTam2;
    private Integer huevosTam3;
    private Integer huevosTam4;
    private Integer huevosRotos;
    private BigDecimal temperaturaDia;
    private BigDecimal humedadDia;
    private String observaciones;

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Integer getCantidadHuevos() {
        return cantidadHuevos;
    }

    public void setCantidadHuevos(Integer cantidadHuevos) {
        this.cantidadHuevos = cantidadHuevos;
    }

    public Integer getHuevosTam1() {
        return huevosTam1;
    }

    public void setHuevosTam1(Integer huevosTam1) {
        this.huevosTam1 = huevosTam1;
    }

    public Integer getHuevosTam2() {
        return huevosTam2;
    }

    public void setHuevosTam2(Integer huevosTam2) {
        this.huevosTam2 = huevosTam2;
    }

    public Integer getHuevosTam3() {
        return huevosTam3;
    }

    public void setHuevosTam3(Integer huevosTam3) {
        this.huevosTam3 = huevosTam3;
    }

    public Integer getHuevosTam4() {
        return huevosTam4;
    }

    public void setHuevosTam4(Integer huevosTam4) {
        this.huevosTam4 = huevosTam4;
    }

    public Integer getHuevosRotos() {
        return huevosRotos;
    }

    public void setHuevosRotos(Integer huevosRotos) {
        this.huevosRotos = huevosRotos;
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

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
