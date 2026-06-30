package com.agrocloud.lecheria.model.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class LecheriaPanelRespuesta {

    private Long campanaId;
    private BigDecimal litrosTotalesPeriodo;
    private BigDecimal litrosPorAnimalLactante;
    private BigDecimal promedioRcs;
    private Long animalesLactando;
    private Long animalesSecas;
    private Long animalesPrenadas;
    private List<LecheriaAccionDiaRespuesta> acciones = new ArrayList<>();

    public Long getCampanaId() {
        return campanaId;
    }

    public void setCampanaId(Long campanaId) {
        this.campanaId = campanaId;
    }

    public BigDecimal getLitrosTotalesPeriodo() {
        return litrosTotalesPeriodo;
    }

    public void setLitrosTotalesPeriodo(BigDecimal litrosTotalesPeriodo) {
        this.litrosTotalesPeriodo = litrosTotalesPeriodo;
    }

    public BigDecimal getLitrosPorAnimalLactante() {
        return litrosPorAnimalLactante;
    }

    public void setLitrosPorAnimalLactante(BigDecimal litrosPorAnimalLactante) {
        this.litrosPorAnimalLactante = litrosPorAnimalLactante;
    }

    public BigDecimal getPromedioRcs() {
        return promedioRcs;
    }

    public void setPromedioRcs(BigDecimal promedioRcs) {
        this.promedioRcs = promedioRcs;
    }

    public Long getAnimalesLactando() {
        return animalesLactando;
    }

    public void setAnimalesLactando(Long animalesLactando) {
        this.animalesLactando = animalesLactando;
    }

    public Long getAnimalesSecas() {
        return animalesSecas;
    }

    public void setAnimalesSecas(Long animalesSecas) {
        this.animalesSecas = animalesSecas;
    }

    public Long getAnimalesPrenadas() {
        return animalesPrenadas;
    }

    public void setAnimalesPrenadas(Long animalesPrenadas) {
        this.animalesPrenadas = animalesPrenadas;
    }

    public List<LecheriaAccionDiaRespuesta> getAcciones() {
        return acciones;
    }

    public void setAcciones(List<LecheriaAccionDiaRespuesta> acciones) {
        this.acciones = acciones;
    }
}
