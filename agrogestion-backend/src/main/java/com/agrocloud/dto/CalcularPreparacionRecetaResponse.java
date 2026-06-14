package com.agrocloud.dto;

import java.math.BigDecimal;
import java.util.List;

public class CalcularPreparacionRecetaResponse {
    private Long recetaId;
    private String recetaNombre;
    private String unidadMedida;
    private BigDecimal cantidadSolicitada;
    private List<ComponenteCalculoDTO> componentes;
    private BigDecimal maximoPreparableConStock;
    private Boolean stockSuficienteGlobal;
    private String mensaje;

    public Long getRecetaId() { return recetaId; }
    public void setRecetaId(Long recetaId) { this.recetaId = recetaId; }
    public String getRecetaNombre() { return recetaNombre; }
    public void setRecetaNombre(String recetaNombre) { this.recetaNombre = recetaNombre; }
    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }
    public BigDecimal getCantidadSolicitada() { return cantidadSolicitada; }
    public void setCantidadSolicitada(BigDecimal cantidadSolicitada) { this.cantidadSolicitada = cantidadSolicitada; }
    public List<ComponenteCalculoDTO> getComponentes() { return componentes; }
    public void setComponentes(List<ComponenteCalculoDTO> componentes) { this.componentes = componentes; }
    public BigDecimal getMaximoPreparableConStock() { return maximoPreparableConStock; }
    public void setMaximoPreparableConStock(BigDecimal maximoPreparableConStock) { this.maximoPreparableConStock = maximoPreparableConStock; }
    public Boolean getStockSuficienteGlobal() { return stockSuficienteGlobal; }
    public void setStockSuficienteGlobal(Boolean stockSuficienteGlobal) { this.stockSuficienteGlobal = stockSuficienteGlobal; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
