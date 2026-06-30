package com.agrocloud.lecheria.model.dto;

import com.agrocloud.lecheria.model.enums.LecheriaTipoEventoReproductivo;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class LecheriaEventoReproductivoRespuesta {

    private Long id;
    private Long animalId;
    private LecheriaTipoEventoReproductivo tipo;
    private LocalDate fecha;
    private String resultado;
    private String toroPajuela;
    private LocalDate fechaPrevistaParto;
    private String observaciones;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAnimalId() {
        return animalId;
    }

    public void setAnimalId(Long animalId) {
        this.animalId = animalId;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
