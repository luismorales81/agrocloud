package com.agrocloud.avicola.crianza.model.dto;

import com.agrocloud.avicola.crianza.model.enums.AvicolaEspecie;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Datos para crear o actualizar un lote avícola.
 */
public class AvicolaLoteSolicitud {

    private Long establecimientoId;
    private Long razaId;
    private String nombre;
    private AvicolaEspecie especie;
    private String origen;
    private LocalDate fechaIngreso;
    private Integer cantidadInicial;
    private Integer cantidadAnimales;
    private BigDecimal pesoPromedioIngreso;
    private String observaciones;

    public Long getEstablecimientoId() {
        return establecimientoId;
    }

    public void setEstablecimientoId(Long establecimientoId) {
        this.establecimientoId = establecimientoId;
    }

    public Long getRazaId() {
        return razaId;
    }

    public void setRazaId(Long razaId) {
        this.razaId = razaId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public AvicolaEspecie getEspecie() {
        return especie;
    }

    public void setEspecie(AvicolaEspecie especie) {
        this.especie = especie;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public Integer getCantidadInicial() {
        return cantidadInicial;
    }

    public void setCantidadInicial(Integer cantidadInicial) {
        this.cantidadInicial = cantidadInicial;
    }

    public Integer getCantidadAnimales() {
        return cantidadAnimales;
    }

    public void setCantidadAnimales(Integer cantidadAnimales) {
        this.cantidadAnimales = cantidadAnimales;
    }

    public BigDecimal getPesoPromedioIngreso() {
        return pesoPromedioIngreso;
    }

    public void setPesoPromedioIngreso(BigDecimal pesoPromedioIngreso) {
        this.pesoPromedioIngreso = pesoPromedioIngreso;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
