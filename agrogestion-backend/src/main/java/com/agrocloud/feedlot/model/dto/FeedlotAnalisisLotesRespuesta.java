package com.agrocloud.feedlot.model.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class FeedlotAnalisisLotesRespuesta {

    private List<FeedlotAnalisisLoteItemRespuesta> lotes = new ArrayList<>();

    public List<FeedlotAnalisisLoteItemRespuesta> getLotes() {
        return lotes;
    }

    public void setLotes(List<FeedlotAnalisisLoteItemRespuesta> lotes) {
        this.lotes = lotes;
    }

    public static class FeedlotAnalisisLoteItemRespuesta {
        private Long loteId;
        private String loteNombre;
        private BigDecimal gmd;
        private BigDecimal conversionAlimenticia;
        private BigDecimal mortalidadPct;
        private BigDecimal margen;

        public Long getLoteId() {
            return loteId;
        }

        public void setLoteId(Long loteId) {
            this.loteId = loteId;
        }

        public String getLoteNombre() {
            return loteNombre;
        }

        public void setLoteNombre(String loteNombre) {
            this.loteNombre = loteNombre;
        }

        public BigDecimal getGmd() {
            return gmd;
        }

        public void setGmd(BigDecimal gmd) {
            this.gmd = gmd;
        }

        public BigDecimal getConversionAlimenticia() {
            return conversionAlimenticia;
        }

        public void setConversionAlimenticia(BigDecimal conversionAlimenticia) {
            this.conversionAlimenticia = conversionAlimenticia;
        }

        public BigDecimal getMortalidadPct() {
            return mortalidadPct;
        }

        public void setMortalidadPct(BigDecimal mortalidadPct) {
            this.mortalidadPct = mortalidadPct;
        }

        public BigDecimal getMargen() {
            return margen;
        }

        public void setMargen(BigDecimal margen) {
            this.margen = margen;
        }
    }
}
