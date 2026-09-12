package com.agrocloud.porcinos.model.dto;

import com.agrocloud.porcinos.model.enums.PorcinosGalponEstado;
import com.agrocloud.porcinos.model.enums.PorcinosLoteEstado;
import com.agrocloud.porcinos.model.enums.PorcinosLoteEtapa;
import com.agrocloud.porcinos.model.enums.PorcinosLoteOrigen;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PorcinosLoteRespuesta {

    private Long id;
    private Long empresaId;
    private Long galponId;
    private String galponNombre;
    private PorcinosGalponEstado galponEstado;
    private Long establecimientoId;
    private String establecimientoNombre;
    private Long campanaId;
    private String nombre;
    private PorcinosLoteOrigen origen;
    private Long desteteId;
    private LocalDate fechaIngreso;
    private LocalDate fechaCierre;
    private Integer cabezasInicial;
    private Integer cabezasActuales;
    private Integer cabezasDisponibles;
    private BigDecimal pesoPromedioIngresoKg;
    private PorcinosLoteEtapa etapa;
    private PorcinosLoteEstado estado;
    private String observaciones;
    private Integer diasEnLote;
    private BigDecimal mortalidadPct;
    private Double climaLatitud;
    private Double climaLongitud;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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

    public Long getGalponId() {
        return galponId;
    }

    public void setGalponId(Long galponId) {
        this.galponId = galponId;
    }

    public String getGalponNombre() {
        return galponNombre;
    }

    public void setGalponNombre(String galponNombre) {
        this.galponNombre = galponNombre;
    }

    public PorcinosGalponEstado getGalponEstado() {
        return galponEstado;
    }

    public void setGalponEstado(PorcinosGalponEstado galponEstado) {
        this.galponEstado = galponEstado;
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

    public Long getCampanaId() {
        return campanaId;
    }

    public void setCampanaId(Long campanaId) {
        this.campanaId = campanaId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public PorcinosLoteOrigen getOrigen() {
        return origen;
    }

    public void setOrigen(PorcinosLoteOrigen origen) {
        this.origen = origen;
    }

    public Long getDesteteId() {
        return desteteId;
    }

    public void setDesteteId(Long desteteId) {
        this.desteteId = desteteId;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public LocalDate getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(LocalDate fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public Integer getCabezasInicial() {
        return cabezasInicial;
    }

    public void setCabezasInicial(Integer cabezasInicial) {
        this.cabezasInicial = cabezasInicial;
    }

    public Integer getCabezasActuales() {
        return cabezasActuales;
    }

    public void setCabezasActuales(Integer cabezasActuales) {
        this.cabezasActuales = cabezasActuales;
    }

    public Integer getCabezasDisponibles() {
        return cabezasDisponibles;
    }

    public void setCabezasDisponibles(Integer cabezasDisponibles) {
        this.cabezasDisponibles = cabezasDisponibles;
    }

    public BigDecimal getPesoPromedioIngresoKg() {
        return pesoPromedioIngresoKg;
    }

    public void setPesoPromedioIngresoKg(BigDecimal pesoPromedioIngresoKg) {
        this.pesoPromedioIngresoKg = pesoPromedioIngresoKg;
    }

    public PorcinosLoteEtapa getEtapa() {
        return etapa;
    }

    public void setEtapa(PorcinosLoteEtapa etapa) {
        this.etapa = etapa;
    }

    public PorcinosLoteEstado getEstado() {
        return estado;
    }

    public void setEstado(PorcinosLoteEstado estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Integer getDiasEnLote() {
        return diasEnLote;
    }

    public void setDiasEnLote(Integer diasEnLote) {
        this.diasEnLote = diasEnLote;
    }

    public BigDecimal getMortalidadPct() {
        return mortalidadPct;
    }

    public void setMortalidadPct(BigDecimal mortalidadPct) {
        this.mortalidadPct = mortalidadPct;
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
}
