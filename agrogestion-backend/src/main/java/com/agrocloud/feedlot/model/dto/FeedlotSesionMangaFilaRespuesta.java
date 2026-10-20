package com.agrocloud.feedlot.model.dto;

import com.agrocloud.feedlot.model.enums.FeedlotAccionDesconocido;
import com.agrocloud.feedlot.model.enums.FeedlotOrigenFilaRecuento;
import com.agrocloud.feedlot.model.enums.FeedlotResultadoFilaManga;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FeedlotSesionMangaFilaRespuesta {

    private Long id;
    private Integer numeroLinea;
    private String eidCrudo;
    private String eidNormalizado;
    private String caravanaVisual;
    private BigDecimal pesoKg;
    private LocalDateTime fechaHoraLectura;
    private Long animalId;
    private FeedlotResultadoFilaManga resultado;
    private String mensaje;
    private FeedlotOrigenFilaRecuento origenRecuento;
    private FeedlotAccionDesconocido accionDesconocido;
    private Long loteDestinoId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getNumeroLinea() { return numeroLinea; }
    public void setNumeroLinea(Integer numeroLinea) { this.numeroLinea = numeroLinea; }
    public String getEidCrudo() { return eidCrudo; }
    public void setEidCrudo(String eidCrudo) { this.eidCrudo = eidCrudo; }
    public String getEidNormalizado() { return eidNormalizado; }
    public void setEidNormalizado(String eidNormalizado) { this.eidNormalizado = eidNormalizado; }
    public String getCaravanaVisual() { return caravanaVisual; }
    public void setCaravanaVisual(String caravanaVisual) { this.caravanaVisual = caravanaVisual; }
    public BigDecimal getPesoKg() { return pesoKg; }
    public void setPesoKg(BigDecimal pesoKg) { this.pesoKg = pesoKg; }
    public LocalDateTime getFechaHoraLectura() { return fechaHoraLectura; }
    public void setFechaHoraLectura(LocalDateTime fechaHoraLectura) { this.fechaHoraLectura = fechaHoraLectura; }
    public Long getAnimalId() { return animalId; }
    public void setAnimalId(Long animalId) { this.animalId = animalId; }
    public FeedlotResultadoFilaManga getResultado() { return resultado; }
    public void setResultado(FeedlotResultadoFilaManga resultado) { this.resultado = resultado; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public FeedlotOrigenFilaRecuento getOrigenRecuento() { return origenRecuento; }
    public void setOrigenRecuento(FeedlotOrigenFilaRecuento origenRecuento) { this.origenRecuento = origenRecuento; }
    public FeedlotAccionDesconocido getAccionDesconocido() { return accionDesconocido; }
    public void setAccionDesconocido(FeedlotAccionDesconocido accionDesconocido) { this.accionDesconocido = accionDesconocido; }
    public Long getLoteDestinoId() { return loteDestinoId; }
    public void setLoteDestinoId(Long loteDestinoId) { this.loteDestinoId = loteDestinoId; }
}
