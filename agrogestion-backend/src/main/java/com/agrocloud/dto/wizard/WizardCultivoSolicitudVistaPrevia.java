package com.agrocloud.dto.wizard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/**
 * Entrada del wizard de cultivo para generar vista previa con IA.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WizardCultivoSolicitudVistaPrevia {

    private String nombreCultivo;
    private String region;
    private String objetivoProductivo;
    /** Superficie del lote en hectáreas (para escenarios de producción). */
    private Double superficieHectareas;
    /** Rendimiento esperado del cultivo guardado (referencia de negocio). */
    private BigDecimal rendimientoEsperadoReferencia;
    private String unidadRendimientoReferencia;
    private Integer cicloDiasReferencia;
    private String variedadReferencia;
    private String descripcionCultivoReferencia;

    public String getNombreCultivo() { return nombreCultivo; }
    public void setNombreCultivo(String nombreCultivo) { this.nombreCultivo = nombreCultivo; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getObjetivoProductivo() { return objetivoProductivo; }
    public void setObjetivoProductivo(String objetivoProductivo) { this.objetivoProductivo = objetivoProductivo; }
    public Double getSuperficieHectareas() { return superficieHectareas; }
    public void setSuperficieHectareas(Double superficieHectareas) { this.superficieHectareas = superficieHectareas; }
    public BigDecimal getRendimientoEsperadoReferencia() { return rendimientoEsperadoReferencia; }
    public void setRendimientoEsperadoReferencia(BigDecimal rendimientoEsperadoReferencia) { this.rendimientoEsperadoReferencia = rendimientoEsperadoReferencia; }
    public String getUnidadRendimientoReferencia() { return unidadRendimientoReferencia; }
    public void setUnidadRendimientoReferencia(String unidadRendimientoReferencia) { this.unidadRendimientoReferencia = unidadRendimientoReferencia; }
    public Integer getCicloDiasReferencia() { return cicloDiasReferencia; }
    public void setCicloDiasReferencia(Integer cicloDiasReferencia) { this.cicloDiasReferencia = cicloDiasReferencia; }
    public String getVariedadReferencia() { return variedadReferencia; }
    public void setVariedadReferencia(String variedadReferencia) { this.variedadReferencia = variedadReferencia; }
    public String getDescripcionCultivoReferencia() { return descripcionCultivoReferencia; }
    public void setDescripcionCultivoReferencia(String descripcionCultivoReferencia) { this.descripcionCultivoReferencia = descripcionCultivoReferencia; }
}
