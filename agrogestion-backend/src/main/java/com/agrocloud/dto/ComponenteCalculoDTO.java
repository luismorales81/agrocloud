package com.agrocloud.dto;

import java.math.BigDecimal;

public class ComponenteCalculoDTO {
    private String nombreComponente;
    private String tipoComponente;
    private String unidadMedida;
    private BigDecimal cantidadPorKgReceta;
    private BigDecimal cantidadNecesaria;
    private BigDecimal stockDisponible;
    private BigDecimal maximoPreparable;
    private Boolean stockSuficiente;
    private String mensajeStock;

    public String getNombreComponente() { return nombreComponente; }
    public void setNombreComponente(String nombreComponente) { this.nombreComponente = nombreComponente; }
    public String getTipoComponente() { return tipoComponente; }
    public void setTipoComponente(String tipoComponente) { this.tipoComponente = tipoComponente; }
    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }
    public BigDecimal getCantidadPorKgReceta() { return cantidadPorKgReceta; }
    public void setCantidadPorKgReceta(BigDecimal cantidadPorKgReceta) { this.cantidadPorKgReceta = cantidadPorKgReceta; }
    public BigDecimal getCantidadNecesaria() { return cantidadNecesaria; }
    public void setCantidadNecesaria(BigDecimal cantidadNecesaria) { this.cantidadNecesaria = cantidadNecesaria; }
    public BigDecimal getStockDisponible() { return stockDisponible; }
    public void setStockDisponible(BigDecimal stockDisponible) { this.stockDisponible = stockDisponible; }
    public BigDecimal getMaximoPreparable() { return maximoPreparable; }
    public void setMaximoPreparable(BigDecimal maximoPreparable) { this.maximoPreparable = maximoPreparable; }
    public Boolean getStockSuficiente() { return stockSuficiente; }
    public void setStockSuficiente(Boolean stockSuficiente) { this.stockSuficiente = stockSuficiente; }
    public String getMensajeStock() { return mensajeStock; }
    public void setMensajeStock(String mensajeStock) { this.mensajeStock = mensajeStock; }
}
