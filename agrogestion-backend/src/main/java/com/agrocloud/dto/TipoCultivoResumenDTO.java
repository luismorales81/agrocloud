package com.agrocloud.dto;

import com.agrocloud.cultivos.domain.TipoCultivo;

/**
 * Vista resumida de un tipo de cultivo para listados en API (sin relaciones JPA).
 */
public class TipoCultivoResumenDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private Boolean esPlantilla;
    private Boolean activo;

    public TipoCultivoResumenDTO() {}

    public TipoCultivoResumenDTO(Long id, String nombre, String descripcion, Boolean esPlantilla, Boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.esPlantilla = esPlantilla;
        this.activo = activo;
    }

    public static TipoCultivoResumenDTO desde(TipoCultivo tipo) {
        if (tipo == null) {
            return null;
        }
        return new TipoCultivoResumenDTO(
            tipo.getId(),
            tipo.getNombre(),
            tipo.getDescripcion(),
            tipo.getEsPlantilla(),
            tipo.getActivo()
        );
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Boolean getEsPlantilla() { return esPlantilla; }
    public void setEsPlantilla(Boolean esPlantilla) { this.esPlantilla = esPlantilla; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
