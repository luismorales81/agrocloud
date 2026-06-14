package com.agrocloud.dto.wizard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WizardPlanLoteEstadosVistaPreviaSolicitud {

    private Long cultivoId;
    private Long loteId;
    private String objetivoProductivo;

    public Long getCultivoId() {
        return cultivoId;
    }

    public void setCultivoId(Long cultivoId) {
        this.cultivoId = cultivoId;
    }

    public Long getLoteId() {
        return loteId;
    }

    public void setLoteId(Long loteId) {
        this.loteId = loteId;
    }

    public String getObjetivoProductivo() {
        return objetivoProductivo;
    }

    public void setObjetivoProductivo(String objetivoProductivo) {
        this.objetivoProductivo = objetivoProductivo;
    }
}
