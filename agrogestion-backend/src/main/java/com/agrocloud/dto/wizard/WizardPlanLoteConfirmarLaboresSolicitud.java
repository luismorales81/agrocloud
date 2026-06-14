package com.agrocloud.dto.wizard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WizardPlanLoteConfirmarLaboresSolicitud {

    private Long cultivoId;
    private Long loteId;
    private List<WizardPlanLoteLaborPlanificadaItem> labores = new ArrayList<>();

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

    public List<WizardPlanLoteLaborPlanificadaItem> getLabores() {
        return labores;
    }

    public void setLabores(List<WizardPlanLoteLaborPlanificadaItem> labores) {
        this.labores = labores != null ? labores : new ArrayList<>();
    }
}
