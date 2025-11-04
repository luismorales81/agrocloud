package com.agrocloud.dto;

import com.agrocloud.model.enums.FormaAplicacion;
import com.agrocloud.model.enums.TipoAgroquimico;
import com.agrocloud.model.enums.UnidadMedida;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para request de agroquímicos
 * 
 * @author AgroGestion Team
 * @version 2.0.0
 */
public class AgroquimicoRequest {

    @NotBlank(message = "El nombre comercial es obligatorio")
    @Size(max = 100, message = "El nombre comercial no puede exceder 100 caracteres")
    private String nombreComercial;

    @NotBlank(message = "El principio activo es obligatorio")
    @Size(max = 100, message = "El principio activo no puede exceder 100 caracteres")
    private String principioActivo;

    @NotNull(message = "El tipo de agroquímico es obligatorio")
    private TipoAgroquimico tipo;

    @NotNull(message = "La forma de aplicación es obligatoria")
    private FormaAplicacion formaAplicacion;

    @NotNull(message = "La dosis por hectárea es obligatoria")
    @DecimalMin(value = "0.01", message = "La dosis por hectárea debe ser mayor a 0")
    private BigDecimal dosisPorHa;

    @NotNull(message = "La unidad de medida es obligatoria")
    private UnidadMedida unidadDeMedida;

    @NotNull(message = "La fecha de vencimiento es obligatoria")
    @Future(message = "La fecha de vencimiento debe ser futura")
    private LocalDate fechaVencimiento;

    @NotNull(message = "El stock actual es obligatorio")
    @DecimalMin(value = "0.0", message = "El stock actual no puede ser negativo")
    private BigDecimal stockActual;

    @DecimalMin(value = "0.0", message = "El stock mínimo no puede ser negativo")
    private BigDecimal stockMinimo;

    // Constructors
    public AgroquimicoRequest() {}

    // Getters and Setters
    public String getNombreComercial() {
        return nombreComercial;
    }

    public void setNombreComercial(String nombreComercial) {
        this.nombreComercial = nombreComercial;
    }

    public String getPrincipioActivo() {
        return principioActivo;
    }

    public void setPrincipioActivo(String principioActivo) {
        this.principioActivo = principioActivo;
    }

    public TipoAgroquimico getTipo() {
        return tipo;
    }

    public void setTipo(TipoAgroquimico tipo) {
        this.tipo = tipo;
    }

    public FormaAplicacion getFormaAplicacion() {
        return formaAplicacion;
    }

    public void setFormaAplicacion(FormaAplicacion formaAplicacion) {
        this.formaAplicacion = formaAplicacion;
    }

    public BigDecimal getDosisPorHa() {
        return dosisPorHa;
    }

    public void setDosisPorHa(BigDecimal dosisPorHa) {
        this.dosisPorHa = dosisPorHa;
    }

    public UnidadMedida getUnidadDeMedida() {
        return unidadDeMedida;
    }

    public void setUnidadDeMedida(UnidadMedida unidadDeMedida) {
        this.unidadDeMedida = unidadDeMedida;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public BigDecimal getStockActual() {
        return stockActual;
    }

    public void setStockActual(BigDecimal stockActual) {
        this.stockActual = stockActual;
    }

    public BigDecimal getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(BigDecimal stockMinimo) {
        this.stockMinimo = stockMinimo;
    }
}