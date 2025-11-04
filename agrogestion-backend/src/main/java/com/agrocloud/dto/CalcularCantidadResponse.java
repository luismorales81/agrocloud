package com.agrocloud.dto;

import com.agrocloud.model.enums.UnidadDosis;

public class CalcularCantidadResponse {
    private Double cantidadNecesaria;
    private UnidadDosis unidadDeMedida;
    private Double dosisRecomendadaPorHa;
    private Double dosisUtilizada;
    private Boolean stockSuficiente;
    private String mensajeStock;
    private Boolean dosisModificada;
    private String mensajeDosis;
    private Double variacionPorcentual;

    // Getters y Setters
    public Double getCantidadNecesaria() {
        return cantidadNecesaria;
    }

    public void setCantidadNecesaria(Double cantidadNecesaria) {
        this.cantidadNecesaria = cantidadNecesaria;
    }

    public UnidadDosis getUnidadDeMedida() {
        return unidadDeMedida;
    }

    public void setUnidadDeMedida(UnidadDosis unidadDeMedida) {
        this.unidadDeMedida = unidadDeMedida;
    }

    public Double getDosisRecomendadaPorHa() {
        return dosisRecomendadaPorHa;
    }

    public void setDosisRecomendadaPorHa(Double dosisRecomendadaPorHa) {
        this.dosisRecomendadaPorHa = dosisRecomendadaPorHa;
    }

    public Double getDosisUtilizada() {
        return dosisUtilizada;
    }

    public void setDosisUtilizada(Double dosisUtilizada) {
        this.dosisUtilizada = dosisUtilizada;
    }

    public Boolean getStockSuficiente() {
        return stockSuficiente;
    }

    public void setStockSuficiente(Boolean stockSuficiente) {
        this.stockSuficiente = stockSuficiente;
    }

    public String getMensajeStock() {
        return mensajeStock;
    }

    public void setMensajeStock(String mensajeStock) {
        this.mensajeStock = mensajeStock;
    }

    public Boolean getDosisModificada() {
        return dosisModificada;
    }

    public void setDosisModificada(Boolean dosisModificada) {
        this.dosisModificada = dosisModificada;
    }

    public String getMensajeDosis() {
        return mensajeDosis;
    }

    public void setMensajeDosis(String mensajeDosis) {
        this.mensajeDosis = mensajeDosis;
    }

    public Double getVariacionPorcentual() {
        return variacionPorcentual;
    }

    public void setVariacionPorcentual(Double variacionPorcentual) {
        this.variacionPorcentual = variacionPorcentual;
    }
}