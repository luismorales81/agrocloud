package com.agrocloud.dto.calendario;

import com.agrocloud.core.domain.AmbitoCalendarioSerie;
import com.agrocloud.core.domain.TipoRepeticionTareaRecurrente;

import java.time.LocalDate;

public class CrearSerieTareaRecurrenteSolicitud {

    private String titulo;
    private String descripcion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private TipoRepeticionTareaRecurrente tipoRepeticion;
    private AmbitoCalendarioSerie ambitoCalendario;

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public TipoRepeticionTareaRecurrente getTipoRepeticion() {
        return tipoRepeticion;
    }

    public void setTipoRepeticion(TipoRepeticionTareaRecurrente tipoRepeticion) {
        this.tipoRepeticion = tipoRepeticion;
    }

    public AmbitoCalendarioSerie getAmbitoCalendario() {
        return ambitoCalendario;
    }

    public void setAmbitoCalendario(AmbitoCalendarioSerie ambitoCalendario) {
        this.ambitoCalendario = ambitoCalendario;
    }
}
