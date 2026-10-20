package com.agrocloud.feedlot.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FeedlotLecturaArchivoManga {

    private String eidCrudo;
    private String eidNormalizado;
    private String caravanaVisual;
    private BigDecimal pesoKg;
    private LocalDateTime fechaHoraLectura;

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
}
