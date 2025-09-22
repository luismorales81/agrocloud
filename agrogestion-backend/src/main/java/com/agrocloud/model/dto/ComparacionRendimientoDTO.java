package com.agrocloud.model.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para mostrar la comparación entre rendimiento proyectado y real
 */
public class ComparacionRendimientoDTO {

    private Long cultivoId;
    private String cultivoNombre;
    private String variedad;
    private BigDecimal rendimientoProyectado;
    private String unidadRendimiento;
    private BigDecimal rendimientoRealPromedio;
    private BigDecimal diferenciaAbsoluta;
    private BigDecimal diferenciaPorcentual;
    private int totalCosechas;
    private BigDecimal superficieTotalCosechada;
    private BigDecimal rendimientoTotalObtenido;
    private List<CosechaDTO> cosechas;

    // Constructores
    public ComparacionRendimientoDTO() {}

    public ComparacionRendimientoDTO(Long cultivoId, String cultivoNombre, String variedad,
                                   BigDecimal rendimientoProyectado, String unidadRendimiento) {
        this.cultivoId = cultivoId;
        this.cultivoNombre = cultivoNombre;
        this.variedad = variedad;
        this.rendimientoProyectado = rendimientoProyectado;
        this.unidadRendimiento = unidadRendimiento;
        this.rendimientoRealPromedio = BigDecimal.ZERO;
        this.diferenciaAbsoluta = BigDecimal.ZERO;
        this.diferenciaPorcentual = BigDecimal.ZERO;
        this.totalCosechas = 0;
        this.superficieTotalCosechada = BigDecimal.ZERO;
        this.rendimientoTotalObtenido = BigDecimal.ZERO;
    }

    // Getters y Setters
    public Long getCultivoId() {
        return cultivoId;
    }

    public void setCultivoId(Long cultivoId) {
        this.cultivoId = cultivoId;
    }

    public String getCultivoNombre() {
        return cultivoNombre;
    }

    public void setCultivoNombre(String cultivoNombre) {
        this.cultivoNombre = cultivoNombre;
    }

    public String getVariedad() {
        return variedad;
    }

    public void setVariedad(String variedad) {
        this.variedad = variedad;
    }

    public BigDecimal getRendimientoProyectado() {
        return rendimientoProyectado;
    }

    public void setRendimientoProyectado(BigDecimal rendimientoProyectado) {
        this.rendimientoProyectado = rendimientoProyectado;
    }

    public String getUnidadRendimiento() {
        return unidadRendimiento;
    }

    public void setUnidadRendimiento(String unidadRendimiento) {
        this.unidadRendimiento = unidadRendimiento;
    }

    public BigDecimal getRendimientoRealPromedio() {
        return rendimientoRealPromedio;
    }

    public void setRendimientoRealPromedio(BigDecimal rendimientoRealPromedio) {
        this.rendimientoRealPromedio = rendimientoRealPromedio;
    }

    public BigDecimal getDiferenciaAbsoluta() {
        return diferenciaAbsoluta;
    }

    public void setDiferenciaAbsoluta(BigDecimal diferenciaAbsoluta) {
        this.diferenciaAbsoluta = diferenciaAbsoluta;
    }

    public BigDecimal getDiferenciaPorcentual() {
        return diferenciaPorcentual;
    }

    public void setDiferenciaPorcentual(BigDecimal diferenciaPorcentual) {
        this.diferenciaPorcentual = diferenciaPorcentual;
    }

    public int getTotalCosechas() {
        return totalCosechas;
    }

    public void setTotalCosechas(int totalCosechas) {
        this.totalCosechas = totalCosechas;
    }

    public BigDecimal getSuperficieTotalCosechada() {
        return superficieTotalCosechada;
    }

    public void setSuperficieTotalCosechada(BigDecimal superficieTotalCosechada) {
        this.superficieTotalCosechada = superficieTotalCosechada;
    }

    public BigDecimal getRendimientoTotalObtenido() {
        return rendimientoTotalObtenido;
    }

    public void setRendimientoTotalObtenido(BigDecimal rendimientoTotalObtenido) {
        this.rendimientoTotalObtenido = rendimientoTotalObtenido;
    }

    public List<CosechaDTO> getCosechas() {
        return cosechas;
    }

    public void setCosechas(List<CosechaDTO> cosechas) {
        this.cosechas = cosechas;
    }

    /**
     * Indica si el rendimiento real superó al proyectado
     */
    public boolean isRendimientoSuperado() {
        return rendimientoRealPromedio != null && rendimientoProyectado != null &&
               rendimientoRealPromedio.compareTo(rendimientoProyectado) > 0;
    }

    /**
     * Obtiene el estado del rendimiento como texto
     */
    public String getEstadoRendimiento() {
        if (totalCosechas == 0) {
            return "Sin cosechas registradas";
        }
        
        if (isRendimientoSuperado()) {
            return "Rendimiento superado";
        } else if (diferenciaAbsoluta != null && diferenciaAbsoluta.compareTo(BigDecimal.ZERO) == 0) {
            return "Rendimiento exacto";
        } else {
            return "Rendimiento por debajo";
        }
    }

    @Override
    public String toString() {
        return "ComparacionRendimientoDTO{" +
                "cultivoId=" + cultivoId +
                ", cultivoNombre='" + cultivoNombre + '\'' +
                ", variedad='" + variedad + '\'' +
                ", rendimientoProyectado=" + rendimientoProyectado +
                ", rendimientoRealPromedio=" + rendimientoRealPromedio +
                ", diferenciaAbsoluta=" + diferenciaAbsoluta +
                ", diferenciaPorcentual=" + diferenciaPorcentual +
                ", totalCosechas=" + totalCosechas +
                '}';
    }
}
