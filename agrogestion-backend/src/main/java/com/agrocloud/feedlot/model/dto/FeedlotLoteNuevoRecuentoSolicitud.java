package com.agrocloud.feedlot.model.dto;

public class FeedlotLoteNuevoRecuentoSolicitud {

    private Long corralId;
    private String nombre;
    private Long categoriaId;
    private Long razaId;

    public Long getCorralId() { return corralId; }
    public void setCorralId(Long corralId) { this.corralId = corralId; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }
    public Long getRazaId() { return razaId; }
    public void setRazaId(Long razaId) { this.razaId = razaId; }
}
