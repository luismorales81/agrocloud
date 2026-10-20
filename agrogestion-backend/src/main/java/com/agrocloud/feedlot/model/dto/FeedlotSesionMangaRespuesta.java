package com.agrocloud.feedlot.model.dto;

import com.agrocloud.feedlot.model.enums.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FeedlotSesionMangaRespuesta {

    private Long id;
    private Long loteId;
    private String loteNombre;
    private FeedlotTipoSesionManga tipo;
    private LocalDate fecha;
    private FeedlotEstadoSesionManga estado;
    private String nombreArchivo;
    private Integer filasOk;
    private Integer filasError;
    private Boolean ajustarPlantel;
    private FeedlotAplicador aplicador;
    private FeedlotOrigenInsumo origenInsumo;
    private FeedlotTipoEventoSanitario tipoEvento;
    private Long insumoId;
    private BigDecimal dosisPorAnimal;
    private BigDecimal cantidadTotal;
    private Integer diasRetiro;
    private FeedlotSubtipoBaja subtipoBaja;
    private List<FeedlotSesionMangaFilaRespuesta> filas = new ArrayList<>();
    private FeedlotResumenRecuento resumenRecuento;

    private List<String> avisos = new ArrayList<>();
    private List<String> bloqueantes = new ArrayList<>();
    private Boolean puedeConfirmar;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getLoteId() { return loteId; }
    public void setLoteId(Long loteId) { this.loteId = loteId; }
    public String getLoteNombre() { return loteNombre; }
    public void setLoteNombre(String loteNombre) { this.loteNombre = loteNombre; }
    public FeedlotTipoSesionManga getTipo() { return tipo; }
    public void setTipo(FeedlotTipoSesionManga tipo) { this.tipo = tipo; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public FeedlotEstadoSesionManga getEstado() { return estado; }
    public void setEstado(FeedlotEstadoSesionManga estado) { this.estado = estado; }
    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }
    public Integer getFilasOk() { return filasOk; }
    public void setFilasOk(Integer filasOk) { this.filasOk = filasOk; }
    public Integer getFilasError() { return filasError; }
    public void setFilasError(Integer filasError) { this.filasError = filasError; }
    public Boolean getAjustarPlantel() { return ajustarPlantel; }
    public void setAjustarPlantel(Boolean ajustarPlantel) { this.ajustarPlantel = ajustarPlantel; }
    public FeedlotAplicador getAplicador() { return aplicador; }
    public void setAplicador(FeedlotAplicador aplicador) { this.aplicador = aplicador; }
    public FeedlotOrigenInsumo getOrigenInsumo() { return origenInsumo; }
    public void setOrigenInsumo(FeedlotOrigenInsumo origenInsumo) { this.origenInsumo = origenInsumo; }
    public FeedlotTipoEventoSanitario getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(FeedlotTipoEventoSanitario tipoEvento) { this.tipoEvento = tipoEvento; }
    public Long getInsumoId() { return insumoId; }
    public void setInsumoId(Long insumoId) { this.insumoId = insumoId; }
    public BigDecimal getDosisPorAnimal() { return dosisPorAnimal; }
    public void setDosisPorAnimal(BigDecimal dosisPorAnimal) { this.dosisPorAnimal = dosisPorAnimal; }
    public BigDecimal getCantidadTotal() { return cantidadTotal; }
    public void setCantidadTotal(BigDecimal cantidadTotal) { this.cantidadTotal = cantidadTotal; }
    public Integer getDiasRetiro() { return diasRetiro; }
    public void setDiasRetiro(Integer diasRetiro) { this.diasRetiro = diasRetiro; }
    public FeedlotSubtipoBaja getSubtipoBaja() { return subtipoBaja; }
    public void setSubtipoBaja(FeedlotSubtipoBaja subtipoBaja) { this.subtipoBaja = subtipoBaja; }
    public List<FeedlotSesionMangaFilaRespuesta> getFilas() { return filas; }
    public void setFilas(List<FeedlotSesionMangaFilaRespuesta> filas) { this.filas = filas; }
    public FeedlotResumenRecuento getResumenRecuento() { return resumenRecuento; }
    public void setResumenRecuento(FeedlotResumenRecuento resumenRecuento) { this.resumenRecuento = resumenRecuento; }
    public List<String> getAvisos() { return avisos; }
    public void setAvisos(List<String> avisos) { this.avisos = avisos; }
    public List<String> getBloqueantes() { return bloqueantes; }
    public void setBloqueantes(List<String> bloqueantes) { this.bloqueantes = bloqueantes; }
    public Boolean getPuedeConfirmar() { return puedeConfirmar; }
    public void setPuedeConfirmar(Boolean puedeConfirmar) { this.puedeConfirmar = puedeConfirmar; }
}
