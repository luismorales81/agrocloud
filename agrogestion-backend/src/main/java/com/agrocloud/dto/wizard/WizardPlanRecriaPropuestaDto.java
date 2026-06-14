package com.agrocloud.dto.wizard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WizardPlanRecriaPropuestaDto {

    private PlanRecriaInfo planRecria;
    private List<EtapaPropuesta> etapas = new ArrayList<>();
    private List<RecetaPorEtapaPropuesta> recetasPorEtapa = new ArrayList<>();
    private List<RecordatorioPropuesta> recordatorios = new ArrayList<>();

    public PlanRecriaInfo getPlanRecria() { return planRecria; }
    public void setPlanRecria(PlanRecriaInfo planRecria) { this.planRecria = planRecria; }
    public List<EtapaPropuesta> getEtapas() { return etapas; }
    public void setEtapas(List<EtapaPropuesta> etapas) { this.etapas = etapas; }
    public List<RecetaPorEtapaPropuesta> getRecetasPorEtapa() { return recetasPorEtapa; }
    public void setRecetasPorEtapa(List<RecetaPorEtapaPropuesta> recetasPorEtapa) { this.recetasPorEtapa = recetasPorEtapa; }
    public List<RecordatorioPropuesta> getRecordatorios() { return recordatorios; }
    public void setRecordatorios(List<RecordatorioPropuesta> recordatorios) { this.recordatorios = recordatorios; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PlanRecriaInfo {
        private String nombre;
        private String descripcion;
        private String razaObjetivo;
        private String proposito;

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
        public String getRazaObjetivo() { return razaObjetivo; }
        public void setRazaObjetivo(String razaObjetivo) { this.razaObjetivo = razaObjetivo; }
        public String getProposito() { return proposito; }
        public void setProposito(String proposito) { this.proposito = proposito; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EtapaPropuesta {
        private String nombre;
        private Integer orden;
        private Integer duracionEstimadaDias;
        private BigDecimal pesoIngresoMinimoKg;
        private BigDecimal pesoObjetivoKg;
        private BigDecimal gananciaDiariaEsperadaKg;
        private BigDecimal umbralMortalidadPct;

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public Integer getOrden() { return orden; }
        public void setOrden(Integer orden) { this.orden = orden; }
        public Integer getDuracionEstimadaDias() { return duracionEstimadaDias; }
        public void setDuracionEstimadaDias(Integer duracionEstimadaDias) { this.duracionEstimadaDias = duracionEstimadaDias; }
        public BigDecimal getPesoIngresoMinimoKg() { return pesoIngresoMinimoKg; }
        public void setPesoIngresoMinimoKg(BigDecimal pesoIngresoMinimoKg) { this.pesoIngresoMinimoKg = pesoIngresoMinimoKg; }
        public BigDecimal getPesoObjetivoKg() { return pesoObjetivoKg; }
        public void setPesoObjetivoKg(BigDecimal pesoObjetivoKg) { this.pesoObjetivoKg = pesoObjetivoKg; }
        public BigDecimal getGananciaDiariaEsperadaKg() { return gananciaDiariaEsperadaKg; }
        public void setGananciaDiariaEsperadaKg(BigDecimal gananciaDiariaEsperadaKg) { this.gananciaDiariaEsperadaKg = gananciaDiariaEsperadaKg; }
        public BigDecimal getUmbralMortalidadPct() { return umbralMortalidadPct; }
        public void setUmbralMortalidadPct(BigDecimal umbralMortalidadPct) { this.umbralMortalidadPct = umbralMortalidadPct; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RecetaPorEtapaPropuesta {
        private String etapaNombre;
        private List<InsumoRecetaPropuesta> insumos = new ArrayList<>();

        public String getEtapaNombre() { return etapaNombre; }
        public void setEtapaNombre(String etapaNombre) { this.etapaNombre = etapaNombre; }
        public List<InsumoRecetaPropuesta> getInsumos() { return insumos; }
        public void setInsumos(List<InsumoRecetaPropuesta> insumos) { this.insumos = insumos; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InsumoRecetaPropuesta {
        private String nombreInsumo;
        private BigDecimal kgPorAnimalDia;

        public String getNombreInsumo() { return nombreInsumo; }
        public void setNombreInsumo(String nombreInsumo) { this.nombreInsumo = nombreInsumo; }
        public BigDecimal getKgPorAnimalDia() { return kgPorAnimalDia; }
        public void setKgPorAnimalDia(BigDecimal kgPorAnimalDia) { this.kgPorAnimalDia = kgPorAnimalDia; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RecordatorioPropuesta {
        private String tipo;
        private Integer diasDesdeIngreso;
        private String descripcion;

        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }
        public Integer getDiasDesdeIngreso() { return diasDesdeIngreso; }
        public void setDiasDesdeIngreso(Integer diasDesdeIngreso) { this.diasDesdeIngreso = diasDesdeIngreso; }
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    }
}
