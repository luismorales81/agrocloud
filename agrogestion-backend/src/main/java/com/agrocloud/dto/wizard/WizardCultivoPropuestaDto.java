package com.agrocloud.dto.wizard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Propuesta completa de configuración de tipo de cultivo (IA o manual).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WizardCultivoPropuestaDto {

    private TipoCultivoInfo tipoCultivo;
    private List<EstadoPropuesta> estados = new ArrayList<>();
    private List<CalendarioLaborPropuesta> calendarioLabores = new ArrayList<>();
    /** Escenario de rendimiento y producción (no se persiste en BD; solo orientativo). */
    private EstimacionProduccionPropuesta estimacionProduccion;
    /** Costos orientativos del ciclo (no se persisten en plantillas hasta existir columna en BD). */
    private ResumenCostosPropuesta resumenCostos;

    public TipoCultivoInfo getTipoCultivo() { return tipoCultivo; }
    public void setTipoCultivo(TipoCultivoInfo tipoCultivo) { this.tipoCultivo = tipoCultivo; }
    public List<EstadoPropuesta> getEstados() { return estados; }
    public void setEstados(List<EstadoPropuesta> estados) { this.estados = estados; }
    public List<CalendarioLaborPropuesta> getCalendarioLabores() { return calendarioLabores; }
    public void setCalendarioLabores(List<CalendarioLaborPropuesta> calendarioLabores) { this.calendarioLabores = calendarioLabores; }
    public EstimacionProduccionPropuesta getEstimacionProduccion() { return estimacionProduccion; }
    public void setEstimacionProduccion(EstimacionProduccionPropuesta estimacionProduccion) { this.estimacionProduccion = estimacionProduccion; }
    public ResumenCostosPropuesta getResumenCostos() { return resumenCostos; }
    public void setResumenCostos(ResumenCostosPropuesta resumenCostos) { this.resumenCostos = resumenCostos; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TipoCultivoInfo {
        private String nombre;
        private String descripcion;
        private Integer duracionCicloDias;

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
        public Integer getDuracionCicloDias() { return duracionCicloDias; }
        public void setDuracionCicloDias(Integer duracionCicloDias) { this.duracionCicloDias = duracionCicloDias; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EstadoPropuesta {
        private String nombre;
        private Integer orden;
        private String color;
        private List<String> tareasHabilitadas = new ArrayList<>();
        private List<String> transicionesPermitidas = new ArrayList<>();
        private Boolean esEstadoInicial;
        private Boolean esEstadoFinal;

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public Integer getOrden() { return orden; }
        public void setOrden(Integer orden) { this.orden = orden; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        public List<String> getTareasHabilitadas() { return tareasHabilitadas; }
        public void setTareasHabilitadas(List<String> tareasHabilitadas) { this.tareasHabilitadas = tareasHabilitadas; }
        public List<String> getTransicionesPermitidas() { return transicionesPermitidas; }
        public void setTransicionesPermitidas(List<String> transicionesPermitidas) { this.transicionesPermitidas = transicionesPermitidas; }
        public Boolean getEsEstadoInicial() { return esEstadoInicial; }
        public void setEsEstadoInicial(Boolean esEstadoInicial) { this.esEstadoInicial = esEstadoInicial; }
        public Boolean getEsEstadoFinal() { return esEstadoFinal; }
        public void setEsEstadoFinal(Boolean esEstadoFinal) { this.esEstadoFinal = esEstadoFinal; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CalendarioLaborPropuesta {
        private String nombre;
        private String tipoLabor;
        private Integer diaRelativoSiembra;
        private String estadoEsperado;
        private Integer duracionEstimadaHs;
        private List<String> insumosSugeridos = new ArrayList<>();
        /** Costo directo orientativo en ARS para esa labor sobre el lote de referencia. */
        private Double costoEstimadoArs;

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getTipoLabor() { return tipoLabor; }
        public void setTipoLabor(String tipoLabor) { this.tipoLabor = tipoLabor; }
        public Integer getDiaRelativoSiembra() { return diaRelativoSiembra; }
        public void setDiaRelativoSiembra(Integer diaRelativoSiembra) { this.diaRelativoSiembra = diaRelativoSiembra; }
        public String getEstadoEsperado() { return estadoEsperado; }
        public void setEstadoEsperado(String estadoEsperado) { this.estadoEsperado = estadoEsperado; }
        public Integer getDuracionEstimadaHs() { return duracionEstimadaHs; }
        public void setDuracionEstimadaHs(Integer duracionEstimadaHs) { this.duracionEstimadaHs = duracionEstimadaHs; }
        public List<String> getInsumosSugeridos() { return insumosSugeridos; }
        public void setInsumosSugeridos(List<String> insumosSugeridos) { this.insumosSugeridos = insumosSugeridos; }
        public Double getCostoEstimadoArs() { return costoEstimadoArs; }
        public void setCostoEstimadoArs(Double costoEstimadoArs) { this.costoEstimadoArs = costoEstimadoArs; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResumenCostosPropuesta {
        private Double costoTotalEstimadoArs;
        private Double costoPorHectareaArs;
        private String moneda;
        private String detalle;

        public Double getCostoTotalEstimadoArs() { return costoTotalEstimadoArs; }
        public void setCostoTotalEstimadoArs(Double costoTotalEstimadoArs) { this.costoTotalEstimadoArs = costoTotalEstimadoArs; }
        public Double getCostoPorHectareaArs() { return costoPorHectareaArs; }
        public void setCostoPorHectareaArs(Double costoPorHectareaArs) { this.costoPorHectareaArs = costoPorHectareaArs; }
        public String getMoneda() { return moneda; }
        public void setMoneda(String moneda) { this.moneda = moneda; }
        public String getDetalle() { return detalle; }
        public void setDetalle(String detalle) { this.detalle = detalle; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EstimacionProduccionPropuesta {
        private Double rendimientoEscenarioBajo;
        private Double rendimientoEscenarioAlto;
        /** Promedio entre bajo y alto cuando la IA lo indique o se calcule en servidor. */
        private Double rendimientoEsperadoCentral;
        private String unidadRendimiento;
        private Double produccionTotalEstimadaToneladas;
        private String detalleEscenario;
        private String advertencias;

        public Double getRendimientoEscenarioBajo() { return rendimientoEscenarioBajo; }
        public void setRendimientoEscenarioBajo(Double rendimientoEscenarioBajo) { this.rendimientoEscenarioBajo = rendimientoEscenarioBajo; }
        public Double getRendimientoEscenarioAlto() { return rendimientoEscenarioAlto; }
        public void setRendimientoEscenarioAlto(Double rendimientoEscenarioAlto) { this.rendimientoEscenarioAlto = rendimientoEscenarioAlto; }
        public Double getRendimientoEsperadoCentral() { return rendimientoEsperadoCentral; }
        public void setRendimientoEsperadoCentral(Double rendimientoEsperadoCentral) { this.rendimientoEsperadoCentral = rendimientoEsperadoCentral; }
        public String getUnidadRendimiento() { return unidadRendimiento; }
        public void setUnidadRendimiento(String unidadRendimiento) { this.unidadRendimiento = unidadRendimiento; }
        public Double getProduccionTotalEstimadaToneladas() { return produccionTotalEstimadaToneladas; }
        public void setProduccionTotalEstimadaToneladas(Double produccionTotalEstimadaToneladas) { this.produccionTotalEstimadaToneladas = produccionTotalEstimadaToneladas; }
        public String getDetalleEscenario() { return detalleEscenario; }
        public void setDetalleEscenario(String detalleEscenario) { this.detalleEscenario = detalleEscenario; }
        public String getAdvertencias() { return advertencias; }
        public void setAdvertencias(String advertencias) { this.advertencias = advertencias; }
    }
}
