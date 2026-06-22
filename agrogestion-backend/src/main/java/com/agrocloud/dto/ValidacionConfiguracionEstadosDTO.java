package com.agrocloud.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Resultado de validación de la configuración de estados/transiciones/tareas.
 */
public class ValidacionConfiguracionEstadosDTO {

    public enum TipoAviso { ERROR, ADVERTENCIA, INFO }

    public static class AvisoConfiguracion {
        private TipoAviso tipo;
        private String codigo;
        private String mensaje;
        private Long estadoId;
        private String estadoNombre;

        public TipoAviso getTipo() { return tipo; }
        public void setTipo(TipoAviso tipo) { this.tipo = tipo; }
        public String getCodigo() { return codigo; }
        public void setCodigo(String codigo) { this.codigo = codigo; }
        public String getMensaje() { return mensaje; }
        public void setMensaje(String mensaje) { this.mensaje = mensaje; }
        public Long getEstadoId() { return estadoId; }
        public void setEstadoId(Long estadoId) { this.estadoId = estadoId; }
        public String getEstadoNombre() { return estadoNombre; }
        public void setEstadoNombre(String estadoNombre) { this.estadoNombre = estadoNombre; }
    }

    private boolean valida;
    private int totalEstados;
    private int totalTransiciones;
    private int totalTareas;
    private List<AvisoConfiguracion> avisos = new ArrayList<>();

    @JsonProperty("valida")
    public boolean isValida() { return valida; }

    public void setValida(boolean valida) { this.valida = valida; }
    public int getTotalEstados() { return totalEstados; }
    public void setTotalEstados(int totalEstados) { this.totalEstados = totalEstados; }
    public int getTotalTransiciones() { return totalTransiciones; }
    public void setTotalTransiciones(int totalTransiciones) { this.totalTransiciones = totalTransiciones; }
    public int getTotalTareas() { return totalTareas; }
    public void setTotalTareas(int totalTareas) { this.totalTareas = totalTareas; }
    public List<AvisoConfiguracion> getAvisos() { return avisos; }
    public void setAvisos(List<AvisoConfiguracion> avisos) { this.avisos = avisos; }
}
