package com.agrocloud.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO que representa el balance de costos y beneficios.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
public class BalanceDTO {

    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private BigDecimal totalIngresos;
    private BigDecimal totalCostos;
    private BigDecimal balanceNeto;
    private BigDecimal margenBeneficio;
    private List<DetalleBalanceDTO> detalles;

    // Constructors
    public BalanceDTO() {}

    public BalanceDTO(LocalDate fechaInicio, LocalDate fechaFin, BigDecimal totalIngresos, BigDecimal totalCostos) {
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.totalIngresos = totalIngresos != null ? totalIngresos : BigDecimal.ZERO;
        this.totalCostos = totalCostos != null ? totalCostos : BigDecimal.ZERO;
        this.calcularBalanceNeto();
        this.calcularMargenBeneficio();
    }

    // Métodos de cálculo
    private void calcularBalanceNeto() {
        this.balanceNeto = this.totalIngresos.subtract(this.totalCostos);
    }

    private void calcularMargenBeneficio() {
        if (this.totalIngresos.compareTo(BigDecimal.ZERO) > 0) {
            this.margenBeneficio = this.balanceNeto.divide(this.totalIngresos, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        } else {
            this.margenBeneficio = BigDecimal.ZERO;
        }
    }

    // Getters and Setters
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

    public BigDecimal getTotalIngresos() {
        return totalIngresos;
    }

    public void setTotalIngresos(BigDecimal totalIngresos) {
        this.totalIngresos = totalIngresos != null ? totalIngresos : BigDecimal.ZERO;
        this.calcularBalanceNeto();
        this.calcularMargenBeneficio();
    }

    public BigDecimal getTotalCostos() {
        return totalCostos;
    }

    public void setTotalCostos(BigDecimal totalCostos) {
        this.totalCostos = totalCostos != null ? totalCostos : BigDecimal.ZERO;
        this.calcularBalanceNeto();
        this.calcularMargenBeneficio();
    }

    public BigDecimal getBalanceNeto() {
        return balanceNeto;
    }

    public void setBalanceNeto(BigDecimal balanceNeto) {
        this.balanceNeto = balanceNeto;
    }

    public BigDecimal getMargenBeneficio() {
        return margenBeneficio;
    }

    public void setMargenBeneficio(BigDecimal margenBeneficio) {
        this.margenBeneficio = margenBeneficio;
    }

    public List<DetalleBalanceDTO> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleBalanceDTO> detalles) {
        this.detalles = detalles;
    }
}
