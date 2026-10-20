package com.agrocloud.feedlot.model.dto;

import com.agrocloud.feedlot.model.enums.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FeedlotSesionMangaSolicitud {

    private Long loteId;
    private FeedlotTipoSesionManga tipo;
    private LocalDate fecha;
    private Long perfilLecturaId;
    private Boolean ajustarPlantel;
    private FeedlotTipoEventoSanitario tipoEvento;
    private Long insumoId;
    private BigDecimal dosisPorAnimal;
    private String unidadDosis;
    private BigDecimal cantidadTotal;
    private Integer diasRetiro;
    private FeedlotAplicador aplicador;
    private String aplicadorNombre;
    private FeedlotOrigenInsumo origenInsumo;
    private String descripcion;
    private FeedlotSubtipoBaja subtipoBaja;
    private Long motivoMuerteId;
    private BigDecimal precioKg;

    public Long getLoteId() { return loteId; }
    public void setLoteId(Long loteId) { this.loteId = loteId; }
    public FeedlotTipoSesionManga getTipo() { return tipo; }
    public void setTipo(FeedlotTipoSesionManga tipo) { this.tipo = tipo; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public Long getPerfilLecturaId() { return perfilLecturaId; }
    public void setPerfilLecturaId(Long perfilLecturaId) { this.perfilLecturaId = perfilLecturaId; }
    public Boolean getAjustarPlantel() { return ajustarPlantel; }
    public void setAjustarPlantel(Boolean ajustarPlantel) { this.ajustarPlantel = ajustarPlantel; }
    public FeedlotTipoEventoSanitario getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(FeedlotTipoEventoSanitario tipoEvento) { this.tipoEvento = tipoEvento; }
    public Long getInsumoId() { return insumoId; }
    public void setInsumoId(Long insumoId) { this.insumoId = insumoId; }
    public BigDecimal getDosisPorAnimal() { return dosisPorAnimal; }
    public void setDosisPorAnimal(BigDecimal dosisPorAnimal) { this.dosisPorAnimal = dosisPorAnimal; }
    public String getUnidadDosis() { return unidadDosis; }
    public void setUnidadDosis(String unidadDosis) { this.unidadDosis = unidadDosis; }
    public BigDecimal getCantidadTotal() { return cantidadTotal; }
    public void setCantidadTotal(BigDecimal cantidadTotal) { this.cantidadTotal = cantidadTotal; }
    public Integer getDiasRetiro() { return diasRetiro; }
    public void setDiasRetiro(Integer diasRetiro) { this.diasRetiro = diasRetiro; }
    public FeedlotAplicador getAplicador() { return aplicador; }
    public void setAplicador(FeedlotAplicador aplicador) { this.aplicador = aplicador; }
    public String getAplicadorNombre() { return aplicadorNombre; }
    public void setAplicadorNombre(String aplicadorNombre) { this.aplicadorNombre = aplicadorNombre; }
    public FeedlotOrigenInsumo getOrigenInsumo() { return origenInsumo; }
    public void setOrigenInsumo(FeedlotOrigenInsumo origenInsumo) { this.origenInsumo = origenInsumo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public FeedlotSubtipoBaja getSubtipoBaja() { return subtipoBaja; }
    public void setSubtipoBaja(FeedlotSubtipoBaja subtipoBaja) { this.subtipoBaja = subtipoBaja; }
    public Long getMotivoMuerteId() { return motivoMuerteId; }
    public void setMotivoMuerteId(Long motivoMuerteId) { this.motivoMuerteId = motivoMuerteId; }
    public BigDecimal getPrecioKg() { return precioKg; }
    public void setPrecioKg(BigDecimal precioKg) { this.precioKg = precioKg; }
}
