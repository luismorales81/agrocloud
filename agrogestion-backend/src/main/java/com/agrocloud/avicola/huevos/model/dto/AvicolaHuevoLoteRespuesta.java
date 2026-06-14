package com.agrocloud.avicola.huevos.model.dto;

import com.agrocloud.avicola.huevos.model.enums.AvicolaHuevoLoteEstado;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AvicolaHuevoLoteRespuesta {

    private Long id;
    private Long empresaId;
    private Long establecimientoId;
    private String establecimientoNombre;
    private Long razaId;
    private String razaNombre;
    private String nombre;
    private LocalDate fechaInicio;
    private Integer cantidadAvesInicial;
    private Integer cantidadAvesActual;
    private AvicolaHuevoLoteEstado estado;
    private LocalDate fechaCierre;
    private String observaciones;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** Centro del polígono del establecimiento (para clima). Nulos si no hay coordenadas. */
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

    public String getEstablecimientoNombre() {
        return establecimientoNombre;
    }

    public void setEstablecimientoNombre(String establecimientoNombre) {
        this.establecimientoNombre = establecimientoNombre;
    }

    public Long getRazaId() {
        return razaId;
    }

    public void setRazaId(Long razaId) {
        this.razaId = razaId;
    }

    public String getRazaNombre() {
        return razaNombre;
    }

    public void setRazaNombre(String razaNombre) {
        this.razaNombre = razaNombre;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Integer getCantidadAvesInicial() {
        return cantidadAvesInicial;
    }

    public void setCantidadAvesInicial(Integer cantidadAvesInicial) {
        this.cantidadAvesInicial = cantidadAvesInicial;
    }

    public Integer getCantidadAvesActual() {
        return cantidadAvesActual;
    }

    public void setCantidadAvesActual(Integer cantidadAvesActual) {
        this.cantidadAvesActual = cantidadAvesActual;
    }

    public AvicolaHuevoLoteEstado getEstado() {
        return estado;
    }

    public void setEstado(AvicolaHuevoLoteEstado estado) {
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
