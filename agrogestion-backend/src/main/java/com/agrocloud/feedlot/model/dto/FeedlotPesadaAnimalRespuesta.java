package com.agrocloud.feedlot.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FeedlotPesadaAnimalRespuesta {

    private Long id;
    private LocalDate fecha;
    private BigDecimal pesoKg;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public BigDecimal getPesoKg() { return pesoKg; }
    public void setPesoKg(BigDecimal pesoKg) { this.pesoKg = pesoKg; }
}
