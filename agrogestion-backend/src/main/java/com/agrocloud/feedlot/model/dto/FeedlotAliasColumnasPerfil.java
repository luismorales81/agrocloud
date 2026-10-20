package com.agrocloud.feedlot.model.dto;

import java.util.ArrayList;
import java.util.List;

public class FeedlotAliasColumnasPerfil {

    private List<String> eid = new ArrayList<>();
    private List<String> vid = new ArrayList<>();
    private List<String> peso = new ArrayList<>();
    private List<String> fecha = new ArrayList<>();
    private List<String> hora = new ArrayList<>();
    private List<String> dosis = new ArrayList<>();
    private List<String> loteProducto = new ArrayList<>();
    private List<String> retiro = new ArrayList<>();
    private List<String> esi = new ArrayList<>();

    public List<String> getEid() { return eid; }
    public void setEid(List<String> eid) { this.eid = eid == null ? new ArrayList<>() : eid; }
    public List<String> getVid() { return vid; }
    public void setVid(List<String> vid) { this.vid = vid == null ? new ArrayList<>() : vid; }
    public List<String> getPeso() { return peso; }
    public void setPeso(List<String> peso) { this.peso = peso == null ? new ArrayList<>() : peso; }
    public List<String> getFecha() { return fecha; }
    public void setFecha(List<String> fecha) { this.fecha = fecha == null ? new ArrayList<>() : fecha; }
    public List<String> getHora() { return hora; }
    public void setHora(List<String> hora) { this.hora = hora == null ? new ArrayList<>() : hora; }
    public List<String> getDosis() { return dosis; }
    public void setDosis(List<String> dosis) { this.dosis = dosis == null ? new ArrayList<>() : dosis; }
    public List<String> getLoteProducto() { return loteProducto; }
    public void setLoteProducto(List<String> loteProducto) { this.loteProducto = loteProducto == null ? new ArrayList<>() : loteProducto; }
    public List<String> getRetiro() { return retiro; }
    public void setRetiro(List<String> retiro) { this.retiro = retiro == null ? new ArrayList<>() : retiro; }
    public List<String> getEsi() { return esi; }
    public void setEsi(List<String> esi) { this.esi = esi == null ? new ArrayList<>() : esi; }
}
