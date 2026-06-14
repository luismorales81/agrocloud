package com.agrocloud.trazabilidad.dto;

public class IncidenciaValidacionTrazabilidad {
    private String codigo;
    private String detalle;
    private String reglaAplicada;

    public IncidenciaValidacionTrazabilidad() {}
    public IncidenciaValidacionTrazabilidad(String codigo, String detalle, String reglaAplicada) {
        this.codigo = codigo;
        this.detalle = detalle;
        this.reglaAplicada = reglaAplicada;
    }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
    public String getReglaAplicada() { return reglaAplicada; }
    public void setReglaAplicada(String reglaAplicada) { this.reglaAplicada = reglaAplicada; }
}
