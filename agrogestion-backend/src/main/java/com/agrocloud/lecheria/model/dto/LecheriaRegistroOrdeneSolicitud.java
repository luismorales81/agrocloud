package com.agrocloud.lecheria.model.dto;

import com.agrocloud.lecheria.model.enums.LecheriaTurnoOrdene;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LecheriaRegistroOrdeneSolicitud {

    private LocalDate fecha;
    private LecheriaTurnoOrdene turno;
    private BigDecimal litros;
    private BigDecimal grasaPct;
    private BigDecimal proteinaPct;
    private Long rcs;
    private BigDecimal temperaturaAmbiente;
    private BigDecimal humedadAmbiente;
    private String observaciones;

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LecheriaTurnoOrdene getTurno() {
        return turno;
    }

    public void setTurno(LecheriaTurnoOrdene turno) {
        this.turno = turno;
    }

    public BigDecimal getLitros() {
        return litros;
    }

    public void setLitros(BigDecimal litros) {
        this.litros = litros;
    }

    public BigDecimal getGrasaPct() {
        return grasaPct;
    }

    public void setGrasaPct(BigDecimal grasaPct) {
        this.grasaPct = grasaPct;
    }

    public BigDecimal getProteinaPct() {
        return proteinaPct;
    }

    public void setProteinaPct(BigDecimal proteinaPct) {
        this.proteinaPct = proteinaPct;
    }

    public Long getRcs() {
        return rcs;
    }

    public void setRcs(Long rcs) {
        this.rcs = rcs;
    }

    public BigDecimal getTemperaturaAmbiente() {
        return temperaturaAmbiente;
    }

    public void setTemperaturaAmbiente(BigDecimal temperaturaAmbiente) {
        this.temperaturaAmbiente = temperaturaAmbiente;
    }

    public BigDecimal getHumedadAmbiente() {
        return humedadAmbiente;
    }

    public void setHumedadAmbiente(BigDecimal humedadAmbiente) {
        this.humedadAmbiente = humedadAmbiente;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
