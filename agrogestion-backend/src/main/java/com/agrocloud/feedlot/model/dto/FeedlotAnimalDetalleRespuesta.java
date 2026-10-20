package com.agrocloud.feedlot.model.dto;

import java.util.ArrayList;
import java.util.List;

public class FeedlotAnimalDetalleRespuesta extends FeedlotAnimalRespuesta {

    private List<FeedlotPesadaAnimalRespuesta> pesadas = new ArrayList<>();
    private List<FeedlotEventoSanitarioRespuesta> eventosSanitarios = new ArrayList<>();

    public List<FeedlotPesadaAnimalRespuesta> getPesadas() { return pesadas; }
    public void setPesadas(List<FeedlotPesadaAnimalRespuesta> pesadas) { this.pesadas = pesadas; }
    public List<FeedlotEventoSanitarioRespuesta> getEventosSanitarios() { return eventosSanitarios; }
    public void setEventosSanitarios(List<FeedlotEventoSanitarioRespuesta> eventosSanitarios) {
        this.eventosSanitarios = eventosSanitarios;
    }
}
