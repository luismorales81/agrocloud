package com.agrocloud.trazabilidad.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Snapshot estructurado para expediente de ciclo de vida (serializado en reporte).
 */
public class HechosExpedienteTrazabilidad {

    private String titulo;
    private String subtitulo;
    private String modulo;
    private String entidadTipo;
    private Long entidadId;
    private String nombreEmpresa;
    private LocalDate fechaCorteDesde;
    private LocalDate fechaCorteHasta;
    private List<Seccion> secciones = new ArrayList<>();
    private List<EventoLinea> lineaTiempo = new ArrayList<>();

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getSubtitulo() { return subtitulo; }
    public void setSubtitulo(String subtitulo) { this.subtitulo = subtitulo; }
    public String getModulo() { return modulo; }
    public void setModulo(String modulo) { this.modulo = modulo; }
    public String getEntidadTipo() { return entidadTipo; }
    public void setEntidadTipo(String entidadTipo) { this.entidadTipo = entidadTipo; }
    public Long getEntidadId() { return entidadId; }
    public void setEntidadId(Long entidadId) { this.entidadId = entidadId; }
    public String getNombreEmpresa() { return nombreEmpresa; }
    public void setNombreEmpresa(String nombreEmpresa) { this.nombreEmpresa = nombreEmpresa; }
    public LocalDate getFechaCorteDesde() { return fechaCorteDesde; }
    public void setFechaCorteDesde(LocalDate fechaCorteDesde) { this.fechaCorteDesde = fechaCorteDesde; }
    public LocalDate getFechaCorteHasta() { return fechaCorteHasta; }
    public void setFechaCorteHasta(LocalDate fechaCorteHasta) { this.fechaCorteHasta = fechaCorteHasta; }
    public List<Seccion> getSecciones() { return secciones; }
    public void setSecciones(List<Seccion> secciones) { this.secciones = secciones; }
    public List<EventoLinea> getLineaTiempo() { return lineaTiempo; }
    public void setLineaTiempo(List<EventoLinea> lineaTiempo) { this.lineaTiempo = lineaTiempo; }

    public static class Seccion {
        private String titulo;
        private List<Fila> filas = new ArrayList<>();

        public String getTitulo() { return titulo; }
        public void setTitulo(String titulo) { this.titulo = titulo; }
        public List<Fila> getFilas() { return filas; }
        public void setFilas(List<Fila> filas) { this.filas = filas; }
    }

    public static class Fila {
        private String etiqueta;
        private String valor;

        public Fila() {}
        public Fila(String etiqueta, String valor) {
            this.etiqueta = etiqueta;
            this.valor = valor;
        }
        public String getEtiqueta() { return etiqueta; }
        public void setEtiqueta(String etiqueta) { this.etiqueta = etiqueta; }
        public String getValor() { return valor; }
        public void setValor(String valor) { this.valor = valor; }
    }

    public static class EventoLinea {
        private LocalDate fecha;
        private String categoria;
        private String descripcion;

        public EventoLinea() {}
        public EventoLinea(LocalDate fecha, String categoria, String descripcion) {
            this.fecha = fecha;
            this.categoria = categoria;
            this.descripcion = descripcion;
        }
        public LocalDate getFecha() { return fecha; }
        public void setFecha(LocalDate fecha) { this.fecha = fecha; }
        public String getCategoria() { return categoria; }
        public void setCategoria(String categoria) { this.categoria = categoria; }
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    }
}
