package com.agrocloud.dto.porcinos;

import java.time.LocalDateTime;

/**
 * Resumen de un plan de recría para listados.
 */
public class PlanRecriaResumenDto {

    private Long id;
    private String nombre;
    private String descripcion;
    private String razaObjetivo;
    private String proposito;
    private LocalDateTime fechaCreacion;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getRazaObjetivo() { return razaObjetivo; }
    public void setRazaObjetivo(String razaObjetivo) { this.razaObjetivo = razaObjetivo; }
    public String getProposito() { return proposito; }
    public void setProposito(String proposito) { this.proposito = proposito; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
