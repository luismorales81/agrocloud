package com.agrocloud.avicola.ponedoras.model.dto;

import com.agrocloud.avicola.ponedoras.model.enums.AvicolaPonedorasGalponEstado;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AvicolaPonedorasGalponRespuesta {

    private Long id;
    private Long empresaId;
    private Long establecimientoId;
    private String nombre;
    private String raza;
    private LocalDate fechaIngreso;
    private Integer cantidadInicial;
    private Integer cantidadAves;
    private AvicolaPonedorasGalponEstado estado;
    private LocalDate fechaCierre;
    private String observaciones;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String establecimientoNombre;
    private Double climaLatitud;
    private Double climaLongitud;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }

    public Long getEstablecimientoId() {
        return establecimientoId;
    }

    public void setEstablecimientoId(Long establecimientoId) {
        this.establecimientoId = establecimientoId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRaza() {
        return raza;
    }

    public void setRaza(String raza) {
        this.raza = raza;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public Integer getCantidadInicial() {
        return cantidadInicial;
    }

    public void setCantidadInicial(Integer cantidadInicial) {
        this.cantidadInicial = cantidadInicial;
    }

    public Integer getCantidadAves() {
        return cantidadAves;
    }

    public void setCantidadAves(Integer cantidadAves) {
        this.cantidadAves = cantidadAves;
    }

    public AvicolaPonedorasGalponEstado getEstado() {
        return estado;
    }

    public void setEstado(AvicolaPonedorasGalponEstado estado) {
        this.estado = estado;
    }

    public LocalDate getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(LocalDate fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getEstablecimientoNombre() {
        return establecimientoNombre;
    }

    public void setEstablecimientoNombre(String establecimientoNombre) {
        this.establecimientoNombre = establecimientoNombre;
    }

    public Double getClimaLatitud() {
        return climaLatitud;
    }

    public void setClimaLatitud(Double climaLatitud) {
        this.climaLatitud = climaLatitud;
    }

    public Double getClimaLongitud() {
        return climaLongitud;
    }

    public void setClimaLongitud(Double climaLongitud) {
        this.climaLongitud = climaLongitud;
    }
}
