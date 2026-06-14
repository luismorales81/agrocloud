package com.agrocloud.dto.porcinos;

import com.agrocloud.dto.wizard.WizardPlanRecriaPropuestaDto;

/**
 * Detalle de un plan existente para revisión o edición (mismo formato que el wizard).
 */
public class PlanRecriaEdicionRespuestaDto {

    private long id;
    private WizardPlanRecriaPropuestaDto propuesta;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public WizardPlanRecriaPropuestaDto getPropuesta() { return propuesta; }
    public void setPropuesta(WizardPlanRecriaPropuestaDto propuesta) { this.propuesta = propuesta; }
}
