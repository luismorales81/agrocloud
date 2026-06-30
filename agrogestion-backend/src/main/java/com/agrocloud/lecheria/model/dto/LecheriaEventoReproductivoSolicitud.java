package com.agrocloud.lecheria.model.dto;

import com.agrocloud.lecheria.model.enums.LecheriaTipoEventoReproductivo;

import java.time.LocalDate;

public class LecheriaEventoReproductivoSolicitud {

    private LecheriaTipoEventoReproductivo tipo;
    private LocalDate fecha;
    private String resultado;
    private String toroPajuela;
    private LocalDate fechaPrevistaParto;
    private String observaciones;

    public LecheriaTipoEventoReproductivo getTipo() {
        return tipo;
    }

    public void setTipo(LecheriaTipoEventoReproductivo tipo) {
        this.tipo = tipo;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public String getToroPajuela() {
        return toroPajuela;
    }

    public void setToroPajuela(String toroPajuela) {
        this.toroPajuela = toroPajuela;
    }

    public LocalDate getFechaPrevistaParto() {
        return fechaPrevistaParto;
    }

    public void setFechaPrevistaParto(LocalDate fechaPrevistaParto) {
        this.fechaPrevistaParto = fechaPrevistaParto;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
