package com.agrocloud.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Progreso del lote en el camino de estados configurados.
 */
public class ProgresoEstadoLoteDTO {

    private Long loteId;
    private String loteNombre;
    private boolean usaConfiguracion;
    private String mensajeAvance;
    private Long diasDesdeSiembra;
    private Long diasParaProximoEstado;

    private EstadoResumenDTO estadoActual;
    private EstadoResumenDTO proximoEstado;
    private List<PasoCaminoDTO> caminoEstados = new ArrayList<>();
    private List<TareaProgresoDTO> tareas = new ArrayList<>();
    private List<TransicionDisponibleDTO> transicionesDisponibles = new ArrayList<>();

    public static class EstadoResumenDTO {
        private Long id;
        private String nombre;
        private String color;
        private String icono;
        private String modoAvance;
        private Integer diasMinimos;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        public String getIcono() { return icono; }
        public void setIcono(String icono) { this.icono = icono; }
        public String getModoAvance() { return modoAvance; }
        public void setModoAvance(String modoAvance) { this.modoAvance = modoAvance; }
        public Integer getDiasMinimos() { return diasMinimos; }
        public void setDiasMinimos(Integer diasMinimos) { this.diasMinimos = diasMinimos; }
    }

    public static class PasoCaminoDTO {
        private Long id;
        private String nombre;
        private String color;
        private String icono;
        private boolean actual;
        private boolean completado;
        private Integer orden;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        public String getIcono() { return icono; }
        public void setIcono(String icono) { this.icono = icono; }
        public boolean isActual() { return actual; }
        public void setActual(boolean actual) { this.actual = actual; }
        public boolean isCompletado() { return completado; }
        public void setCompletado(boolean completado) { this.completado = completado; }
        public Integer getOrden() { return orden; }
        public void setOrden(Integer orden) { this.orden = orden; }
    }

    public static class TareaProgresoDTO {
        private String tipoLabor;
        private String nombreTarea;
        private boolean esObligatoria;
        private boolean completada;

        public String getTipoLabor() { return tipoLabor; }
        public void setTipoLabor(String tipoLabor) { this.tipoLabor = tipoLabor; }
        public String getNombreTarea() { return nombreTarea; }
        public void setNombreTarea(String nombreTarea) { this.nombreTarea = nombreTarea; }
        public boolean isEsObligatoria() { return esObligatoria; }
        public void setEsObligatoria(boolean esObligatoria) { this.esObligatoria = esObligatoria; }
        public boolean isCompletada() { return completada; }
        public void setCompletada(boolean completada) { this.completada = completada; }
    }

    public static class TransicionDisponibleDTO {
        private Long destinoId;
        private String destinoNombre;
        private String destinoColor;
        private boolean requiereMotivo;

        public Long getDestinoId() { return destinoId; }
        public void setDestinoId(Long destinoId) { this.destinoId = destinoId; }
        public String getDestinoNombre() { return destinoNombre; }
        public void setDestinoNombre(String destinoNombre) { this.destinoNombre = destinoNombre; }
        public String getDestinoColor() { return destinoColor; }
        public void setDestinoColor(String destinoColor) { this.destinoColor = destinoColor; }
        public boolean isRequiereMotivo() { return requiereMotivo; }
        public void setRequiereMotivo(boolean requiereMotivo) { this.requiereMotivo = requiereMotivo; }
    }

    public Long getLoteId() { return loteId; }
    public void setLoteId(Long loteId) { this.loteId = loteId; }
    public String getLoteNombre() { return loteNombre; }
    public void setLoteNombre(String loteNombre) { this.loteNombre = loteNombre; }
    public boolean isUsaConfiguracion() { return usaConfiguracion; }
    public void setUsaConfiguracion(boolean usaConfiguracion) { this.usaConfiguracion = usaConfiguracion; }
    public String getMensajeAvance() { return mensajeAvance; }
    public void setMensajeAvance(String mensajeAvance) { this.mensajeAvance = mensajeAvance; }
    public Long getDiasDesdeSiembra() { return diasDesdeSiembra; }
    public void setDiasDesdeSiembra(Long diasDesdeSiembra) { this.diasDesdeSiembra = diasDesdeSiembra; }
    public Long getDiasParaProximoEstado() { return diasParaProximoEstado; }
    public void setDiasParaProximoEstado(Long diasParaProximoEstado) { this.diasParaProximoEstado = diasParaProximoEstado; }
    public EstadoResumenDTO getEstadoActual() { return estadoActual; }
    public void setEstadoActual(EstadoResumenDTO estadoActual) { this.estadoActual = estadoActual; }
    public EstadoResumenDTO getProximoEstado() { return proximoEstado; }
    public void setProximoEstado(EstadoResumenDTO proximoEstado) { this.proximoEstado = proximoEstado; }
    public List<PasoCaminoDTO> getCaminoEstados() { return caminoEstados; }
    public void setCaminoEstados(List<PasoCaminoDTO> caminoEstados) { this.caminoEstados = caminoEstados; }
    public List<TareaProgresoDTO> getTareas() { return tareas; }
    public void setTareas(List<TareaProgresoDTO> tareas) { this.tareas = tareas; }
    public List<TransicionDisponibleDTO> getTransicionesDisponibles() { return transicionesDisponibles; }
    public void setTransicionesDisponibles(List<TransicionDisponibleDTO> transicionesDisponibles) {
        this.transicionesDisponibles = transicionesDisponibles;
    }
}
