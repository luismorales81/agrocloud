package com.agrocloud.dto;

import java.math.BigDecimal;

/**
 * DTO para estadísticas de desviaciones de aplicaciones
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
public class EstadisticasDesviacionesDTO {

    private int totalAplicaciones;
    private BigDecimal desviacionPromedio;
    private BigDecimal desviacionMinima;
    private BigDecimal desviacionMaxima;
    private long aplicacionesOptimas;
    private long aplicacionesAceptables;
    private long aplicacionesAltas;
    private long aplicacionesCriticas;

    public EstadisticasDesviacionesDTO() {}

    public EstadisticasDesviacionesDTO(int totalAplicaciones, BigDecimal desviacionPromedio, 
                                      BigDecimal desviacionMinima, BigDecimal desviacionMaxima,
                                      long aplicacionesOptimas, long aplicacionesAceptables, 
                                      long aplicacionesAltas, long aplicacionesCriticas) {
        this.totalAplicaciones = totalAplicaciones;
        this.desviacionPromedio = desviacionPromedio;
        this.desviacionMinima = desviacionMinima;
        this.desviacionMaxima = desviacionMaxima;
        this.aplicacionesOptimas = aplicacionesOptimas;
        this.aplicacionesAceptables = aplicacionesAceptables;
        this.aplicacionesAltas = aplicacionesAltas;
        this.aplicacionesCriticas = aplicacionesCriticas;
    }

    // Getters and Setters
    public int getTotalAplicaciones() {
        return totalAplicaciones;
    }

    public void setTotalAplicaciones(int totalAplicaciones) {
        this.totalAplicaciones = totalAplicaciones;
    }

    public BigDecimal getDesviacionPromedio() {
        return desviacionPromedio;
    }

    public void setDesviacionPromedio(BigDecimal desviacionPromedio) {
        this.desviacionPromedio = desviacionPromedio;
    }

    public BigDecimal getDesviacionMinima() {
        return desviacionMinima;
    }

    public void setDesviacionMinima(BigDecimal desviacionMinima) {
        this.desviacionMinima = desviacionMinima;
    }

    public BigDecimal getDesviacionMaxima() {
        return desviacionMaxima;
    }

    public void setDesviacionMaxima(BigDecimal desviacionMaxima) {
        this.desviacionMaxima = desviacionMaxima;
    }

    public long getAplicacionesOptimas() {
        return aplicacionesOptimas;
    }

    public void setAplicacionesOptimas(long aplicacionesOptimas) {
        this.aplicacionesOptimas = aplicacionesOptimas;
    }

    public long getAplicacionesAceptables() {
        return aplicacionesAceptables;
    }

    public void setAplicacionesAceptables(long aplicacionesAceptables) {
        this.aplicacionesAceptables = aplicacionesAceptables;
    }

    public long getAplicacionesAltas() {
        return aplicacionesAltas;
    }

    public void setAplicacionesAltas(long aplicacionesAltas) {
        this.aplicacionesAltas = aplicacionesAltas;
    }

    public long getAplicacionesCriticas() {
        return aplicacionesCriticas;
    }

    public void setAplicacionesCriticas(long aplicacionesCriticas) {
        this.aplicacionesCriticas = aplicacionesCriticas;
    }
}













