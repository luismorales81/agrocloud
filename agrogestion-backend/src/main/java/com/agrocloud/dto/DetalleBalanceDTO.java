package com.agrocloud.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO que representa el detalle del balance de costos y beneficios.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
public class DetalleBalanceDTO {

    private String tipo;
    private String concepto;
    private LocalDate fecha;
    private BigDecimal monto;
    private String categoria;
    private String lote;
    private String descripcion;

    // Constructors
    public DetalleBalanceDTO() {}

    public DetalleBalanceDTO(String tipo, String concepto, LocalDate fecha, BigDecimal monto, String categoria) {
        this.tipo = tipo;
        this.concepto = concepto;
        this.fecha = fecha;
        this.monto = monto != null ? monto : BigDecimal.ZERO;
        this.categoria = categoria;
    }

    public DetalleBalanceDTO(String tipo, String concepto, LocalDate fecha, BigDecimal monto, String categoria, String lote, String descripcion) {
        this.tipo = tipo;
        this.concepto = concepto;
        this.fecha = fecha;
        this.monto = monto != null ? monto : BigDecimal.ZERO;
        this.categoria = categoria;
        this.lote = lote;
        this.descripcion = descripcion;
    }

    // Getters and Setters
    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto != null ? monto : BigDecimal.ZERO;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
