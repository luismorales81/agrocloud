package com.agrocloud.feedlot.model.dto;

public class FeedlotIdentificacionAnimal {

    private Long id;
    private Long filaId;
    private String eid;
    private String caravanaVisual;
    private Long loteId;
    private String loteNombre;
    private String corralNombre;
    private String estado;

    public FeedlotIdentificacionAnimal() {
    }

    public FeedlotIdentificacionAnimal(Long id, String eid, String caravanaVisual) {
        this.id = id;
        this.eid = eid;
        this.caravanaVisual = caravanaVisual;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEid() { return eid; }
    public void setEid(String eid) { this.eid = eid; }
    public String getCaravanaVisual() { return caravanaVisual; }
    public void setCaravanaVisual(String caravanaVisual) { this.caravanaVisual = caravanaVisual; }
    public Long getFilaId() { return filaId; }
    public void setFilaId(Long filaId) { this.filaId = filaId; }
    public Long getLoteId() { return loteId; }
    public void setLoteId(Long loteId) { this.loteId = loteId; }
    public String getLoteNombre() { return loteNombre; }
    public void setLoteNombre(String loteNombre) { this.loteNombre = loteNombre; }
    public String getCorralNombre() { return corralNombre; }
    public void setCorralNombre(String corralNombre) { this.corralNombre = corralNombre; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
