package com.agrocloud.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * DTO para crear agroquímico desde egreso (movimiento de stock)
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
public class CrearAgroquimicoDesdeEgresoRequest {

    @NotNull(message = "El ID del insumo es obligatorio")
    private Long insumoId;

    private String principioActivo;

    private String claseQuimica;

    private List<DosisBasicaRequest> dosisBasicas;

    // Getters and Setters
    public Long getInsumoId() {
        return insumoId;
    }

    public void setInsumoId(Long insumoId) {
        this.insumoId = insumoId;
    }

    public String getPrincipioActivo() {
        return principioActivo;
    }

    public void setPrincipioActivo(String principioActivo) {
        this.principioActivo = principioActivo;
    }

    public String getClaseQuimica() {
        return claseQuimica;
    }

    public void setClaseQuimica(String claseQuimica) {
        this.claseQuimica = claseQuimica;
    }

    public List<DosisBasicaRequest> getDosisBasicas() {
        return dosisBasicas;
    }

    public void setDosisBasicas(List<DosisBasicaRequest> dosisBasicas) {
        this.dosisBasicas = dosisBasicas;
    }
}













