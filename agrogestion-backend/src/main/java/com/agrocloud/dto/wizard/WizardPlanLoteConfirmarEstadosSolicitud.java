package com.agrocloud.dto.wizard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WizardPlanLoteConfirmarEstadosSolicitud {

    private WizardCultivoPropuestaDto propuesta;
    private Long cultivoId;
    private Long loteId;

    public WizardCultivoPropuestaDto getPropuesta() {
        return propuesta;
    }

    public void setPropuesta(WizardCultivoPropuestaDto propuesta) {
        this.propuesta = propuesta;
    }

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
}
