package com.agrocloud.feedlot.model.dto;

import com.agrocloud.feedlot.model.enums.FeedlotTipoSesionManga;

public class FeedlotPerfilLecturaMangaSolicitud {

    private String nombre;
    private FeedlotTipoSesionManga tipo;
    private Boolean predeterminado;
    private FeedlotAliasColumnasPerfil alias;
    private Long insumoId;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public FeedlotTipoSesionManga getTipo() { return tipo; }
    public void setTipo(FeedlotTipoSesionManga tipo) { this.tipo = tipo; }
    public Boolean getPredeterminado() { return predeterminado; }
    public void setPredeterminado(Boolean predeterminado) { this.predeterminado = predeterminado; }
    public FeedlotAliasColumnasPerfil getAlias() { return alias; }
    public void setAlias(FeedlotAliasColumnasPerfil alias) { this.alias = alias; }
    public Long getInsumoId() { return insumoId; }
    public void setInsumoId(Long insumoId) { this.insumoId = insumoId; }
}
