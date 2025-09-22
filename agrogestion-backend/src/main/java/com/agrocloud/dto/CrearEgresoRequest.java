package com.agrocloud.dto;

import com.agrocloud.model.entity.Egreso;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para la creación de egresos con integración automática
 */
public class CrearEgresoRequest {
    
    private Long userId;
    private Egreso.TipoEgreso tipoEgreso;
    private LocalDate fechaEgreso;
    private String observaciones;
    
    // Campos para INSUMO
    private Long insumoId;
    private BigDecimal cantidad;
    
    // Campos para MAQUINARIA_COMPRA
    private String concepto;
    private String marca;
    private String modelo;
    private BigDecimal monto;
    
    // Campos para MAQUINARIA_ALQUILER
    private Long maquinariaId;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private BigDecimal costoDia;
    
    // Campo común para lote
    private Long loteId;

    // Constructor por defecto
    public CrearEgresoRequest() {}

    // Constructor para egreso de insumo
    public CrearEgresoRequest(Long userId, Long insumoId, BigDecimal cantidad, 
                             LocalDate fechaEgreso, String observaciones, Long loteId) {
        this.userId = userId;
        this.tipoEgreso = Egreso.TipoEgreso.INSUMO;
        this.insumoId = insumoId;
        this.cantidad = cantidad;
        this.fechaEgreso = fechaEgreso;
        this.observaciones = observaciones;
        this.loteId = loteId;
    }

    // Constructor para egreso de maquinaria compra
    public CrearEgresoRequest(Long userId, String concepto, String marca, String modelo,
                             BigDecimal monto, LocalDate fechaEgreso, String observaciones, Long loteId) {
        this.userId = userId;
        this.tipoEgreso = Egreso.TipoEgreso.MAQUINARIA_COMPRA;
        this.concepto = concepto;
        this.marca = marca;
        this.modelo = modelo;
        this.monto = monto;
        this.fechaEgreso = fechaEgreso;
        this.observaciones = observaciones;
        this.loteId = loteId;
    }

    // Constructor para egreso de maquinaria alquiler
    public CrearEgresoRequest(Long userId, Long maquinariaId, LocalDate fechaInicio,
                             LocalDate fechaFin, BigDecimal costoDia, BigDecimal monto,
                             LocalDate fechaEgreso, String observaciones, Long loteId) {
        this.userId = userId;
        this.tipoEgreso = Egreso.TipoEgreso.MAQUINARIA_ALQUILER;
        this.maquinariaId = maquinariaId;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.costoDia = costoDia;
        this.monto = monto;
        this.fechaEgreso = fechaEgreso;
        this.observaciones = observaciones;
        this.loteId = loteId;
    }

    // Constructor para egreso general
    public CrearEgresoRequest(Long userId, Egreso.TipoEgreso tipoEgreso, BigDecimal monto,
                             LocalDate fechaEgreso, String observaciones, Long loteId) {
        this.userId = userId;
        this.tipoEgreso = tipoEgreso;
        this.monto = monto;
        this.fechaEgreso = fechaEgreso;
        this.observaciones = observaciones;
        this.loteId = loteId;
    }

    // Getters y Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Egreso.TipoEgreso getTipoEgreso() {
        return tipoEgreso;
    }

    public void setTipoEgreso(Egreso.TipoEgreso tipoEgreso) {
        this.tipoEgreso = tipoEgreso;
    }

    public LocalDate getFechaEgreso() {
        return fechaEgreso;
    }

    public void setFechaEgreso(LocalDate fechaEgreso) {
        this.fechaEgreso = fechaEgreso;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Long getInsumoId() {
        return insumoId;
    }

    public void setInsumoId(Long insumoId) {
        this.insumoId = insumoId;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public Long getMaquinariaId() {
        return maquinariaId;
    }

    public void setMaquinariaId(Long maquinariaId) {
        this.maquinariaId = maquinariaId;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public BigDecimal getCostoDia() {
        return costoDia;
    }

    public void setCostoDia(BigDecimal costoDia) {
        this.costoDia = costoDia;
    }

    public Long getLoteId() {
        return loteId;
    }

    public void setLoteId(Long loteId) {
        this.loteId = loteId;
    }
}
