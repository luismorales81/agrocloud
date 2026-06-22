package com.agrocloud.feedlot.model.dto;

import com.agrocloud.feedlot.model.enums.FeedlotTipoTenencia;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FeedlotLoteSolicitud {

    private Long corralId;
    private String nombre;
    private Long categoriaId;
    private Long razaId;
    private Long proveedorId;
    private FeedlotTipoTenencia tipoTenencia;
    private LocalDate fechaIngreso;
    private Integer cabezasInicial;
    private BigDecimal pesoPromedioIngresoKg;
    private BigDecimal precioCompraKg;
    private BigDecimal costoHoteleriaDia;
    private Long dietaId;
    private String observaciones;

    public Long getCorralId() {
        return corralId;
    }

    public void setCorralId(Long corralId) {
        this.corralId = corralId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Long categoriaId) {
        this.categoriaId = categoriaId;
    }

    public Long getRazaId() {
        return razaId;
    }

    public void setRazaId(Long razaId) {
        this.razaId = razaId;
    }

    public Long getProveedorId() {
        return proveedorId;
    }

    public void setProveedorId(Long proveedorId) {
        this.proveedorId = proveedorId;
    }

    public FeedlotTipoTenencia getTipoTenencia() {
        return tipoTenencia;
    }

    public void setTipoTenencia(FeedlotTipoTenencia tipoTenencia) {
        this.tipoTenencia = tipoTenencia;
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

    public BigDecimal getPrecioCompraKg() {
        return precioCompraKg;
    }

    public void setPrecioCompraKg(BigDecimal precioCompraKg) {
        this.precioCompraKg = precioCompraKg;
    }

    public BigDecimal getCostoHoteleriaDia() {
        return costoHoteleriaDia;
    }

    public void setCostoHoteleriaDia(BigDecimal costoHoteleriaDia) {
        this.costoHoteleriaDia = costoHoteleriaDia;
    }

    public Long getDietaId() {
        return dietaId;
    }

    public void setDietaId(Long dietaId) {
        this.dietaId = dietaId;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
