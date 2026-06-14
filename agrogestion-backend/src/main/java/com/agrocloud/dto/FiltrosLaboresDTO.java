package com.agrocloud.dto;

import com.agrocloud.cultivos.domain.Labor;

/**
 * Filtros opcionales para listado paginado de labores.
 */
public class FiltrosLaboresDTO {

    private Long loteId;
    private Labor.EstadoLabor estado;
    private Boolean soloVencidas;
    private String busqueda;

    public FiltrosLaboresDTO() {}

    public FiltrosLaboresDTO(Long loteId, Labor.EstadoLabor estado, Boolean soloVencidas, String busqueda) {
        this.loteId = loteId;
        this.estado = estado;
        this.soloVencidas = soloVencidas;
        this.busqueda = busqueda;
    }

    public Long getLoteId() {
        return loteId;
    }

    public void setLoteId(Long loteId) {
        this.loteId = loteId;
    }

    public Labor.EstadoLabor getEstado() {
        return estado;
    }

    public void setEstado(Labor.EstadoLabor estado) {
        this.estado = estado;
    }

    public Boolean getSoloVencidas() {
        return soloVencidas;
    }

    public void setSoloVencidas(Boolean soloVencidas) {
        this.soloVencidas = soloVencidas;
    }

    public String getBusqueda() {
        return busqueda;
    }

    public void setBusqueda(String busqueda) {
        this.busqueda = busqueda;
    }

    public boolean tieneFiltros() {
        return loteId != null
                || estado != null
                || Boolean.TRUE.equals(soloVencidas)
                || (busqueda != null && !busqueda.isBlank());
    }
}
