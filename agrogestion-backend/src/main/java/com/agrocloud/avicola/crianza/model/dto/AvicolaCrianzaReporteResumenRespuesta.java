package com.agrocloud.avicola.crianza.model.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AvicolaCrianzaReporteResumenRespuesta {

    private int lotesActivos;
    private int lotesCerrados;
    private BigDecimal mortalidadPromedioPct;
    private BigDecimal conversionPromedio;
    private List<FilaLote> porLote = new ArrayList<>();

    public static class FilaLote {
        private Long loteId;
        private String nombreLote;
        private String estado;
        private Integer cantidadDisponible;
        private BigDecimal mortalidadPct;
        private BigDecimal conversionAlimenticia;
        private Long diasEnProduccion;

        public Long getLoteId() {
            return loteId;
        }

        public void setLoteId(Long loteId) {
            this.loteId = loteId;
        }

        public String getNombreLote() {
            return nombreLote;
        }

        public void setNombreLote(String nombreLote) {
            this.nombreLote = nombreLote;
        }

        public String getEstado() {
            return estado;
        }

        public void setEstado(String estado) {
            this.estado = estado;
        }

        public Integer getCantidadDisponible() {
            return cantidadDisponible;
        }

        public void setCantidadDisponible(Integer cantidadDisponible) {
            this.cantidadDisponible = cantidadDisponible;
        }

        public BigDecimal getMortalidadPct() {
            return mortalidadPct;
        }

        public void setMortalidadPct(BigDecimal mortalidadPct) {
            this.mortalidadPct = mortalidadPct;
        }

        public BigDecimal getConversionAlimenticia() {
            return conversionAlimenticia;
        }

        public void setConversionAlimenticia(BigDecimal conversionAlimenticia) {
            this.conversionAlimenticia = conversionAlimenticia;
        }

        public Long getDiasEnProduccion() {
            return diasEnProduccion;
        }

        public void setDiasEnProduccion(Long diasEnProduccion) {
            this.diasEnProduccion = diasEnProduccion;
        }
    }

    public int getLotesActivos() {
        return lotesActivos;
    }

    public void setLotesActivos(int lotesActivos) {
        this.lotesActivos = lotesActivos;
    }

    public int getLotesCerrados() {
        return lotesCerrados;
    }

    public void setLotesCerrados(int lotesCerrados) {
        this.lotesCerrados = lotesCerrados;
    }

    public BigDecimal getMortalidadPromedioPct() {
        return mortalidadPromedioPct;
    }

    public void setMortalidadPromedioPct(BigDecimal mortalidadPromedioPct) {
        this.mortalidadPromedioPct = mortalidadPromedioPct;
    }

    public BigDecimal getConversionPromedio() {
        return conversionPromedio;
    }

    public void setConversionPromedio(BigDecimal conversionPromedio) {
        this.conversionPromedio = conversionPromedio;
    }

    public List<FilaLote> getPorLote() {
        return porLote;
    }

    public void setPorLote(List<FilaLote> porLote) {
        this.porLote = porLote;
    }
}
