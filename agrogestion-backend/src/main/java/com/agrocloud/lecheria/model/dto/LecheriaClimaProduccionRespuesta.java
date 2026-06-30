package com.agrocloud.lecheria.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LecheriaClimaProduccionRespuesta {

    private List<PuntoClima> puntos = new ArrayList<>();

    public List<PuntoClima> getPuntos() {
        return puntos;
    }

    public void setPuntos(List<PuntoClima> puntos) {
        this.puntos = puntos;
    }

    public static class PuntoClima {

        private LocalDate fecha;
        private BigDecimal temperatura;
        private BigDecimal humedad;
        private BigDecimal litros;

        public PuntoClima() {
        }

        public PuntoClima(LocalDate fecha, BigDecimal temperatura, BigDecimal humedad, BigDecimal litros) {
            this.fecha = fecha;
            this.temperatura = temperatura;
            this.humedad = humedad;
            this.litros = litros;
        }

        public LocalDate getFecha() {
            return fecha;
        }

        public void setFecha(LocalDate fecha) {
            this.fecha = fecha;
        }

        public BigDecimal getTemperatura() {
            return temperatura;
        }

        public void setTemperatura(BigDecimal temperatura) {
            this.temperatura = temperatura;
        }

        public BigDecimal getHumedad() {
            return humedad;
        }

        public void setHumedad(BigDecimal humedad) {
            this.humedad = humedad;
        }

        public BigDecimal getLitros() {
            return litros;
        }

        public void setLitros(BigDecimal litros) {
            this.litros = litros;
        }
    }
}
