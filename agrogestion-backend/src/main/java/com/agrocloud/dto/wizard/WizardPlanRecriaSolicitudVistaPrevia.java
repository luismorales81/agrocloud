package com.agrocloud.dto.wizard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WizardPlanRecriaSolicitudVistaPrevia {

    private String raza;
    private String etapaIngreso;
    private Double pesoPromedioKg;
    private String objetivo;

    public String getRaza() { return raza; }
    public void setRaza(String raza) { this.raza = raza; }
    public String getEtapaIngreso() { return etapaIngreso; }
    public void setEtapaIngreso(String etapaIngreso) { this.etapaIngreso = etapaIngreso; }
    public Double getPesoPromedioKg() { return pesoPromedioKg; }
    public void setPesoPromedioKg(Double pesoPromedioKg) { this.pesoPromedioKg = pesoPromedioKg; }
    public String getObjetivo() { return objetivo; }
    public void setObjetivo(String objetivo) { this.objetivo = objetivo; }
}
