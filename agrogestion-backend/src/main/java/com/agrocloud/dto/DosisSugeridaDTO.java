package com.agrocloud.dto;

import com.agrocloud.model.enums.TipoAplicacion;
import java.math.BigDecimal;

/**
 * DTO para representar una sugerencia de dosis de agroquímico
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
public class DosisSugeridaDTO {
    
    private Long agroquimicoId;
    private String nombreAgroquimico;
    private TipoAplicacion tipoAplicacion;
    private BigDecimal dosisSugeridaPorHa;
    private BigDecimal cantidadTotalSugerida;
    private BigDecimal superficieHa;
    private BigDecimal stockDisponible;
    private boolean stockSuficiente;
    private String unidadMedida;
    private String condicionesAplicacion;
    private BigDecimal factorAjuste;
    private String nivelRiesgo;
    private String observaciones;

    // Constructors
    public DosisSugeridaDTO() {}

    public DosisSugeridaDTO(Long agroquimicoId, String nombreAgroquimico, TipoAplicacion tipoAplicacion,
                           BigDecimal dosisSugeridaPorHa, BigDecimal cantidadTotalSugerida, BigDecimal superficieHa) {
        this.agroquimicoId = agroquimicoId;
        this.nombreAgroquimico = nombreAgroquimico;
        this.tipoAplicacion = tipoAplicacion;
        this.dosisSugeridaPorHa = dosisSugeridaPorHa;
        this.cantidadTotalSugerida = cantidadTotalSugerida;
        this.superficieHa = superficieHa;
    }

    // Getters and Setters
    public Long getAgroquimicoId() {
        return agroquimicoId;
    }

    public void setAgroquimicoId(Long agroquimicoId) {
        this.agroquimicoId = agroquimicoId;
    }

    public String getNombreAgroquimico() {
        return nombreAgroquimico;
    }

    public void setNombreAgroquimico(String nombreAgroquimico) {
        this.nombreAgroquimico = nombreAgroquimico;
    }

    public TipoAplicacion getTipoAplicacion() {
        return tipoAplicacion;
    }

    public void setTipoAplicacion(TipoAplicacion tipoAplicacion) {
        this.tipoAplicacion = tipoAplicacion;
    }

    public BigDecimal getDosisSugeridaPorHa() {
        return dosisSugeridaPorHa;
    }

    public void setDosisSugeridaPorHa(BigDecimal dosisSugeridaPorHa) {
        this.dosisSugeridaPorHa = dosisSugeridaPorHa;
    }

    public BigDecimal getCantidadTotalSugerida() {
        return cantidadTotalSugerida;
    }

    public void setCantidadTotalSugerida(BigDecimal cantidadTotalSugerida) {
        this.cantidadTotalSugerida = cantidadTotalSugerida;
    }

    public BigDecimal getSuperficieHa() {
        return superficieHa;
    }

    public void setSuperficieHa(BigDecimal superficieHa) {
        this.superficieHa = superficieHa;
    }

    public BigDecimal getStockDisponible() {
        return stockDisponible;
    }

    public void setStockDisponible(BigDecimal stockDisponible) {
        this.stockDisponible = stockDisponible;
    }

    public boolean isStockSuficiente() {
        return stockSuficiente;
    }

    public void setStockSuficiente(boolean stockSuficiente) {
        this.stockSuficiente = stockSuficiente;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public String getCondicionesAplicacion() {
        return condicionesAplicacion;
    }

    public void setCondicionesAplicacion(String condicionesAplicacion) {
        this.condicionesAplicacion = condicionesAplicacion;
    }

    public BigDecimal getFactorAjuste() {
        return factorAjuste;
    }

    public void setFactorAjuste(BigDecimal factorAjuste) {
        this.factorAjuste = factorAjuste;
    }

    public String getNivelRiesgo() {
        return nivelRiesgo;
    }

    public void setNivelRiesgo(String nivelRiesgo) {
        this.nivelRiesgo = nivelRiesgo;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    @Override
    public String toString() {
        return "DosisSugeridaDTO{" +
                "agroquimicoId=" + agroquimicoId +
                ", nombreAgroquimico='" + nombreAgroquimico + '\'' +
                ", tipoAplicacion=" + tipoAplicacion +
                ", dosisSugeridaPorHa=" + dosisSugeridaPorHa +
                ", cantidadTotalSugerida=" + cantidadTotalSugerida +
                ", stockSuficiente=" + stockSuficiente +
                '}';
    }
}
