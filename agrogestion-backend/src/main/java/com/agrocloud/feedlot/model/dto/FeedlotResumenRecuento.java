package com.agrocloud.feedlot.model.dto;

import java.util.ArrayList;
import java.util.List;

public class FeedlotResumenRecuento {

    private int presentes;
    private int cabezasSinEnrolar;
    private List<FeedlotIdentificacionAnimal> faltantes = new ArrayList<>();
    private List<FeedlotIdentificacionAnimal> intrusos = new ArrayList<>();
    private List<FeedlotIdentificacionAnimal> desconocidos = new ArrayList<>();
    private List<FeedlotIdentificacionAnimal> otroLote = new ArrayList<>();
    private List<FeedlotIdentificacionAnimal> inactivos = new ArrayList<>();

    public int getPresentes() { return presentes; }
    public void setPresentes(int presentes) { this.presentes = presentes; }
    public int getCabezasSinEnrolar() { return cabezasSinEnrolar; }
    public void setCabezasSinEnrolar(int cabezasSinEnrolar) { this.cabezasSinEnrolar = cabezasSinEnrolar; }
    public List<FeedlotIdentificacionAnimal> getFaltantes() { return faltantes; }
    public void setFaltantes(List<FeedlotIdentificacionAnimal> faltantes) { this.faltantes = faltantes; }
    public List<FeedlotIdentificacionAnimal> getIntrusos() { return intrusos; }
    public void setIntrusos(List<FeedlotIdentificacionAnimal> intrusos) { this.intrusos = intrusos; }
    public List<FeedlotIdentificacionAnimal> getDesconocidos() { return desconocidos; }
    public void setDesconocidos(List<FeedlotIdentificacionAnimal> desconocidos) { this.desconocidos = desconocidos; }
    public List<FeedlotIdentificacionAnimal> getOtroLote() { return otroLote; }
    public void setOtroLote(List<FeedlotIdentificacionAnimal> otroLote) { this.otroLote = otroLote; }
    public List<FeedlotIdentificacionAnimal> getInactivos() { return inactivos; }
    public void setInactivos(List<FeedlotIdentificacionAnimal> inactivos) { this.inactivos = inactivos; }
}
