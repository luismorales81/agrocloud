package com.agrocloud.lecheria.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LecheriaCurvaLactanciaRespuesta {

    private Long animalId;
    private String identificacion;
    private List<PuntoDimLitros> puntos = new ArrayList<>();

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

    public List<PuntoDimLitros> getPuntos() {
        return puntos;
    }

    public void setPuntos(List<PuntoDimLitros> puntos) {
        this.puntos = puntos;
    }

    public static class PuntoDimLitros {

        private Integer dim;
        private BigDecimal litros;
        private LocalDate fecha;

        public PuntoDimLitros() {
        }

        public PuntoDimLitros(Integer dim, BigDecimal litros, LocalDate fecha) {
            this.dim = dim;
            this.litros = litros;
            this.fecha = fecha;
        }

        public Integer getDim() {
            return dim;
        }

        public void setDim(Integer dim) {
            this.dim = dim;
        }

        public BigDecimal getLitros() {
            return litros;
        }

        public void setLitros(BigDecimal litros) {
            this.litros = litros;
        }

        public LocalDate getFecha() {
            return fecha;
        }

        public void setFecha(LocalDate fecha) {
            this.fecha = fecha;
        }
    }
}
