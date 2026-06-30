package com.agrocloud.lecheria.model.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class LecheriaRankingProduccionRespuesta {

    private List<ItemRanking> items = new ArrayList<>();

    public List<ItemRanking> getItems() {
        return items;
    }

    public void setItems(List<ItemRanking> items) {
        this.items = items;
    }

    public static class ItemRanking {

        private Long animalId;
        private String identificacion;
        private BigDecimal litrosTotales;

        public ItemRanking() {
        }

        public ItemRanking(Long animalId, String identificacion, BigDecimal litrosTotales) {
            this.animalId = animalId;
            this.identificacion = identificacion;
            this.litrosTotales = litrosTotales;
        }

        public Long getAnimalId() {
            return animalId;
        }

        public void setAnimalId(Long animalId) {
            this.animalId = animalId;
        }

        public String getIdentificacion() {
            return identificacion;
        }

        public void setIdentificacion(String identificacion) {
            this.identificacion = identificacion;
        }

        public BigDecimal getLitrosTotales() {
            return litrosTotales;
        }

        public void setLitrosTotales(BigDecimal litrosTotales) {
            this.litrosTotales = litrosTotales;
        }
    }
}
