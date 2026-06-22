package com.agrocloud.feedlot.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FeedlotConsumoTeoricoRespuesta {

    private Long loteId;
    private Long dietaId;
    private String dietaNombre;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    private List<DiaConsumo> dias = new ArrayList<>();

    public Long getLoteId() {
        return loteId;
    }

    public void setLoteId(Long loteId) {
        this.loteId = loteId;
    }

    public Long getDietaId() {
        return dietaId;
    }

    public void setDietaId(Long dietaId) {
        this.dietaId = dietaId;
    }

    public String getDietaNombre() {
        return dietaNombre;
    }

    public void setDietaNombre(String dietaNombre) {
        this.dietaNombre = dietaNombre;
    }

    public LocalDate getFechaDesde() {
        return fechaDesde;
    }

    public void setFechaDesde(LocalDate fechaDesde) {
        this.fechaDesde = fechaDesde;
    }

    public LocalDate getFechaHasta() {
        return fechaHasta;
    }

    public void setFechaHasta(LocalDate fechaHasta) {
        this.fechaHasta = fechaHasta;
    }

    public List<DiaConsumo> getDias() {
        return dias;
    }

    public void setDias(List<DiaConsumo> dias) {
        this.dias = dias;
    }

    public static class DiaConsumo {
        private LocalDate fecha;
        private String faseNombre;
        private BigDecimal kgMsTeorico;
        private BigDecimal kgReal;
        private BigDecimal diferenciaKg;

        public LocalDate getFecha() {
            return fecha;
        }

        public void setFecha(LocalDate fecha) {
            this.fecha = fecha;
        }

        public String getFaseNombre() {
            return faseNombre;
        }

        public void setFaseNombre(String faseNombre) {
            this.faseNombre = faseNombre;
        }

        public BigDecimal getKgMsTeorico() {
            return kgMsTeorico;
        }

        public void setKgMsTeorico(BigDecimal kgMsTeorico) {
            this.kgMsTeorico = kgMsTeorico;
        }

        public BigDecimal getKgReal() {
            return kgReal;
        }

        public void setKgReal(BigDecimal kgReal) {
            this.kgReal = kgReal;
        }

        public BigDecimal getDiferenciaKg() {
            return diferenciaKg;
        }

        public void setDiferenciaKg(BigDecimal diferenciaKg) {
            this.diferenciaKg = diferenciaKg;
        }
    }
}
