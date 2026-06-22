package com.agrocloud.feedlot.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FeedlotCurvaPesoRespuesta {

    private Long loteId;
    private String loteNombre;
    private BigDecimal pesoIngresoKg;
    private BigDecimal gmd;
    private List<PuntoPeso> serie = new ArrayList<>();
    private List<PuntoPeso> proyeccion = new ArrayList<>();
    private BigDecimal pesoObjetivoKg;
    private LocalDate fechaProyeccionObjetivo;

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

    public BigDecimal getPesoIngresoKg() {
        return pesoIngresoKg;
    }

    public void setPesoIngresoKg(BigDecimal pesoIngresoKg) {
        this.pesoIngresoKg = pesoIngresoKg;
    }

    public BigDecimal getGmd() {
        return gmd;
    }

    public void setGmd(BigDecimal gmd) {
        this.gmd = gmd;
    }

    public List<PuntoPeso> getSerie() {
        return serie;
    }

    public void setSerie(List<PuntoPeso> serie) {
        this.serie = serie;
    }

    public List<PuntoPeso> getProyeccion() {
        return proyeccion;
    }

    public void setProyeccion(List<PuntoPeso> proyeccion) {
        this.proyeccion = proyeccion;
    }

    public BigDecimal getPesoObjetivoKg() {
        return pesoObjetivoKg;
    }

    public void setPesoObjetivoKg(BigDecimal pesoObjetivoKg) {
        this.pesoObjetivoKg = pesoObjetivoKg;
    }

    public LocalDate getFechaProyeccionObjetivo() {
        return fechaProyeccionObjetivo;
    }

    public void setFechaProyeccionObjetivo(LocalDate fechaProyeccionObjetivo) {
        this.fechaProyeccionObjetivo = fechaProyeccionObjetivo;
    }

    public static class PuntoPeso {
        private LocalDate fecha;
        private BigDecimal pesoKg;
        private boolean proyectado;

        public PuntoPeso() {
        }

        public PuntoPeso(LocalDate fecha, BigDecimal pesoKg, boolean proyectado) {
            this.fecha = fecha;
            this.pesoKg = pesoKg;
            this.proyectado = proyectado;
        }

        public LocalDate getFecha() {
            return fecha;
        }

        public void setFecha(LocalDate fecha) {
            this.fecha = fecha;
        }

        public BigDecimal getPesoKg() {
            return pesoKg;
        }

        public void setPesoKg(BigDecimal pesoKg) {
            this.pesoKg = pesoKg;
        }

        public boolean isProyectado() {
            return proyectado;
        }

        public void setProyectado(boolean proyectado) {
            this.proyectado = proyectado;
        }
    }
}
