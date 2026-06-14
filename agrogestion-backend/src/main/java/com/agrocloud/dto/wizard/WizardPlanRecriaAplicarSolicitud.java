package com.agrocloud.dto.wizard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WizardPlanRecriaAplicarSolicitud {

    private Long planRecriaId;

    public Long getPlanRecriaId() { return planRecriaId; }
    public void setPlanRecriaId(Long planRecriaId) { this.planRecriaId = planRecriaId; }
}
