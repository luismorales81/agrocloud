package com.agrocloud.feedlot.model.dto;

import com.agrocloud.feedlot.model.enums.FeedlotEstadoAnimal;
import com.agrocloud.feedlot.model.enums.FeedlotSexoAnimal;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FeedlotAnimalRespuesta {

    private Long id;
    private Long loteId;
    private String eid;
    private String caravanaVisual;
    private FeedlotSexoAnimal sexo;
    private FeedlotEstadoAnimal estado;
    private LocalDate fechaAlta;
    private LocalDate fechaBaja;
    private BigDecimal pesoIngresoKg;
    private boolean retiroVigente;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getLoteId() { return loteId; }
    public void setLoteId(Long loteId) { this.loteId = loteId; }
    public String getEid() { return eid; }
    public void setEid(String eid) { this.eid = eid; }
    public String getCaravanaVisual() { return caravanaVisual; }
    public void setCaravanaVisual(String caravanaVisual) { this.caravanaVisual = caravanaVisual; }
    public FeedlotSexoAnimal getSexo() { return sexo; }
    public void setSexo(FeedlotSexoAnimal sexo) { this.sexo = sexo; }
    public FeedlotEstadoAnimal getEstado() { return estado; }
    public void setEstado(FeedlotEstadoAnimal estado) { this.estado = estado; }
    public LocalDate getFechaAlta() { return fechaAlta; }
    public void setFechaAlta(LocalDate fechaAlta) { this.fechaAlta = fechaAlta; }
    public LocalDate getFechaBaja() { return fechaBaja; }
    public void setFechaBaja(LocalDate fechaBaja) { this.fechaBaja = fechaBaja; }
    public BigDecimal getPesoIngresoKg() { return pesoIngresoKg; }
    public void setPesoIngresoKg(BigDecimal pesoIngresoKg) { this.pesoIngresoKg = pesoIngresoKg; }
    public boolean isRetiroVigente() { return retiroVigente; }
    public void setRetiroVigente(boolean retiroVigente) { this.retiroVigente = retiroVigente; }
}
