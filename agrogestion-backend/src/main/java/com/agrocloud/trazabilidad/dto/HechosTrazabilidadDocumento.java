package com.agrocloud.trazabilidad.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Snapshot amigable para JSON del reporte (serializado en {@code snapshotJson}).
 */
public class HechosTrazabilidadDocumento {

    private String descripcionAlcance;
    private String nombreLote;
    private String nombreEmpresa;
    private String nombreCertificacion;
    private String codigoCertificacion;
    private List<LineaInsumoLabor> insumosPorLabor = new ArrayList<>();
    private List<EventoSanitarioResumen> eventosSanitarios = new ArrayList<>();
    private List<LineaHechoLabor> labores = new ArrayList<>();
    private LocalDate fechaCorteDesde;
    private LocalDate fechaCorteHasta;

    public String getDescripcionAlcance() { return descripcionAlcance; }
    public void setDescripcionAlcance(String descripcionAlcance) { this.descripcionAlcance = descripcionAlcance; }
    public String getNombreLote() { return nombreLote; }
    public void setNombreLote(String nombreLote) { this.nombreLote = nombreLote; }
    public String getNombreEmpresa() { return nombreEmpresa; }
    public void setNombreEmpresa(String nombreEmpresa) { this.nombreEmpresa = nombreEmpresa; }
    public String getNombreCertificacion() { return nombreCertificacion; }
    public void setNombreCertificacion(String nombreCertificacion) { this.nombreCertificacion = nombreCertificacion; }
    public String getCodigoCertificacion() { return codigoCertificacion; }
    public void setCodigoCertificacion(String codigoCertificacion) { this.codigoCertificacion = codigoCertificacion; }
    public List<LineaInsumoLabor> getInsumosPorLabor() { return insumosPorLabor; }
    public void setInsumosPorLabor(List<LineaInsumoLabor> insumosPorLabor) { this.insumosPorLabor = insumosPorLabor; }
    public List<EventoSanitarioResumen> getEventosSanitarios() { return eventosSanitarios; }
    public void setEventosSanitarios(List<EventoSanitarioResumen> eventosSanitarios) { this.eventosSanitarios = eventosSanitarios; }
    public List<LineaHechoLabor> getLabores() { return labores; }
    public void setLabores(List<LineaHechoLabor> labores) { this.labores = labores; }
    public LocalDate getFechaCorteDesde() { return fechaCorteDesde; }
    public void setFechaCorteDesde(LocalDate fechaCorteDesde) { this.fechaCorteDesde = fechaCorteDesde; }
    public LocalDate getFechaCorteHasta() { return fechaCorteHasta; }
    public void setFechaCorteHasta(LocalDate fechaCorteHasta) { this.fechaCorteHasta = fechaCorteHasta; }

    public static class LineaInsumoLabor {
        private Long idLabor;
        private LocalDate fechaLabor;
        private String tipoLabor;
        private Long idInsumo;
        private String nombreInsumo;
        private String tipoInsumo;
        private String cantidadUsada;

        public Long getIdLabor() { return idLabor; }
        public void setIdLabor(Long idLabor) { this.idLabor = idLabor; }
        public LocalDate getFechaLabor() { return fechaLabor; }
        public void setFechaLabor(LocalDate fechaLabor) { this.fechaLabor = fechaLabor; }
        public String getTipoLabor() { return tipoLabor; }
        public void setTipoLabor(String tipoLabor) { this.tipoLabor = tipoLabor; }
        public Long getIdInsumo() { return idInsumo; }
        public void setIdInsumo(Long idInsumo) { this.idInsumo = idInsumo; }
        public String getNombreInsumo() { return nombreInsumo; }
        public void setNombreInsumo(String nombreInsumo) { this.nombreInsumo = nombreInsumo; }
        public String getTipoInsumo() { return tipoInsumo; }
        public void setTipoInsumo(String tipoInsumo) { this.tipoInsumo = tipoInsumo; }
        public String getCantidadUsada() { return cantidadUsada; }
        public void setCantidadUsada(String cantidadUsada) { this.cantidadUsada = cantidadUsada; }
    }

    public static class EventoSanitarioResumen {
        private Long id;
        private LocalDate fecha;
        private String categoria;
        private String nombreTipo;
        private Long recriaId;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public LocalDate getFecha() { return fecha; }
        public void setFecha(LocalDate fecha) { this.fecha = fecha; }
        public String getCategoria() { return categoria; }
        public void setCategoria(String categoria) { this.categoria = categoria; }
        public String getNombreTipo() { return nombreTipo; }
        public void setNombreTipo(String nombreTipo) { this.nombreTipo = nombreTipo; }
        public Long getRecriaId() { return recriaId; }
        public void setRecriaId(Long recriaId) { this.recriaId = recriaId; }
    }

    public static class LineaHechoLabor {
        private Long idLabor;
        private LocalDate fecha;
        private String tipoLabor;
        private String estado;

        public Long getIdLabor() { return idLabor; }
        public void setIdLabor(Long idLabor) { this.idLabor = idLabor; }
        public LocalDate getFecha() { return fecha; }
        public void setFecha(LocalDate fecha) { this.fecha = fecha; }
        public String getTipoLabor() { return tipoLabor; }
        public void setTipoLabor(String tipoLabor) { this.tipoLabor = tipoLabor; }
        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }
    }
}
