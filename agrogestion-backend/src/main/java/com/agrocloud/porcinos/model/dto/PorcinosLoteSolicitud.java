package com.agrocloud.porcinos.model.dto;

import com.agrocloud.porcinos.model.enums.PorcinosLoteEtapa;
import com.agrocloud.porcinos.model.enums.PorcinosLoteOrigen;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PorcinosLoteSolicitud {

    private Long galponId;
    private String nombre;
    private PorcinosLoteOrigen origen;
    private LocalDate fechaIngreso;
    private Integer cabezasInicial;
    private BigDecimal pesoPromedioIngresoKg;
    private PorcinosLoteEtapa etapa;
    private String observaciones;

    public Long getGalponId() {
        return galponId;
    }

    public void setGalponId(Long galponId) {
        this.galponId = galponId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public PorcinosLoteOrigen getOrigen() {
        return origen;
    }

    public void setOrigen(PorcinosLoteOrigen origen) {
        this.origen = origen;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public Integer getCabezasInicial() {
        return cabezasInicial;
    }

    public void setCabezasInicial(Integer cabezasInicial) {
        this.cabezasInicial = cabezasInicial;
    }

    public BigDecimal getPesoPromedioIngresoKg() {
        return pesoPromedioIngresoKg;
    }

    public void setPesoPromedioIngresoKg(BigDecimal pesoPromedioIngresoKg) {
        this.pesoPromedioIngresoKg = pesoPromedioIngresoKg;
    }

    public PorcinosLoteEtapa getEtapa() {
        return etapa;
    }

    public void setEtapa(PorcinosLoteEtapa etapa) {
        this.etapa = etapa;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
