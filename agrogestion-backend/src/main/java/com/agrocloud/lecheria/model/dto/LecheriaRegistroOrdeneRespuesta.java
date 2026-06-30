package com.agrocloud.lecheria.model.dto;

import com.agrocloud.lecheria.model.enums.LecheriaTurnoOrdene;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class LecheriaRegistroOrdeneRespuesta {

    private Long id;
    private Long animalId;
    private Long lactanciaId;
    private LocalDate fecha;
    private LecheriaTurnoOrdene turno;
    private BigDecimal litros;
    private BigDecimal grasaPct;
    private BigDecimal proteinaPct;
    private Long rcs;
    private BigDecimal temperaturaAmbiente;
    private BigDecimal humedadAmbiente;
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

    public Long getLactanciaId() {
        return lactanciaId;
    }

    public void setLactanciaId(Long lactanciaId) {
        this.lactanciaId = lactanciaId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LecheriaTurnoOrdene getTurno() {
        return turno;
    }

    public void setTurno(LecheriaTurnoOrdene turno) {
        this.turno = turno;
    }

    public BigDecimal getLitros() {
        return litros;
    }

    public void setLitros(BigDecimal litros) {
        this.litros = litros;
    }

    public BigDecimal getGrasaPct() {
        return grasaPct;
    }

    public void setGrasaPct(BigDecimal grasaPct) {
        this.grasaPct = grasaPct;
    }

    public BigDecimal getProteinaPct() {
        return proteinaPct;
    }

    public void setProteinaPct(BigDecimal proteinaPct) {
        this.proteinaPct = proteinaPct;
    }

    public Long getRcs() {
        return rcs;
    }

    public void setRcs(Long rcs) {
        this.rcs = rcs;
    }

    public BigDecimal getTemperaturaAmbiente() {
        return temperaturaAmbiente;
    }

    public void setTemperaturaAmbiente(BigDecimal temperaturaAmbiente) {
        this.temperaturaAmbiente = temperaturaAmbiente;
    }

    public BigDecimal getHumedadAmbiente() {
        return humedadAmbiente;
    }

    public void setHumedadAmbiente(BigDecimal humedadAmbiente) {
        this.humedadAmbiente = humedadAmbiente;
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
