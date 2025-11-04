package com.agrocloud.dto;

import com.agrocloud.model.enums.TipoAplicacion;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para registrar una aplicación de agroquímico
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
public class RegistrarAplicacionRequest {

    @NotNull(message = "El ID de la labor es obligatorio")
    private Long laborId;

    @NotNull(message = "El ID del agroquímico es obligatorio")
    private Long agroquimicoId;

    @NotNull(message = "El tipo de aplicación es obligatorio")
    private TipoAplicacion tipoAplicacion;

    @NotNull(message = "La superficie aplicada es obligatoria")
    @Positive(message = "La superficie aplicada debe ser positiva")
    private BigDecimal superficieAplicadaHa;

    @NotNull(message = "La dosis aplicada por hectárea es obligatoria")
    @Positive(message = "La dosis aplicada por hectárea debe ser positiva")
    private BigDecimal dosisAplicadaPorHa;

    @NotNull(message = "La cantidad total aplicada es obligatoria")
    @Positive(message = "La cantidad total aplicada debe ser positiva")
    private BigDecimal cantidadTotalAplicada;

    private String unidadMedida;

    private String condicionesClimaticas;

    private String equipoAplicacion;

    private String operador;

    private String observaciones;

    private LocalDateTime fechaAplicacion;

    // Getters and Setters
    public Long getLaborId() {
        return laborId;
    }

    public void setLaborId(Long laborId) {
        this.laborId = laborId;
    }

    public Long getAgroquimicoId() {
        return agroquimicoId;
    }

    public void setAgroquimicoId(Long agroquimicoId) {
        this.agroquimicoId = agroquimicoId;
    }

    public TipoAplicacion getTipoAplicacion() {
        return tipoAplicacion;
    }

    public void setTipoAplicacion(TipoAplicacion tipoAplicacion) {
        this.tipoAplicacion = tipoAplicacion;
    }

    public BigDecimal getSuperficieAplicadaHa() {
        return superficieAplicadaHa;
    }

    public void setSuperficieAplicadaHa(BigDecimal superficieAplicadaHa) {
        this.superficieAplicadaHa = superficieAplicadaHa;
    }

    public BigDecimal getDosisAplicadaPorHa() {
        return dosisAplicadaPorHa;
    }

    public void setDosisAplicadaPorHa(BigDecimal dosisAplicadaPorHa) {
        this.dosisAplicadaPorHa = dosisAplicadaPorHa;
    }

    public BigDecimal getCantidadTotalAplicada() {
        return cantidadTotalAplicada;
    }

    public void setCantidadTotalAplicada(BigDecimal cantidadTotalAplicada) {
        this.cantidadTotalAplicada = cantidadTotalAplicada;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public String getCondicionesClimaticas() {
        return condicionesClimaticas;
    }

    public void setCondicionesClimaticas(String condicionesClimaticas) {
        this.condicionesClimaticas = condicionesClimaticas;
    }

    public String getEquipoAplicacion() {
        return equipoAplicacion;
    }

    public void setEquipoAplicacion(String equipoAplicacion) {
        this.equipoAplicacion = equipoAplicacion;
    }

    public String getOperador() {
        return operador;
    }

    public void setOperador(String operador) {
        this.operador = operador;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public LocalDateTime getFechaAplicacion() {
        return fechaAplicacion;
    }

    public void setFechaAplicacion(LocalDateTime fechaAplicacion) {
        this.fechaAplicacion = fechaAplicacion;
    }
}









