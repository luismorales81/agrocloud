package com.agrocloud.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para transferir datos de campos entre frontend y backend
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FieldDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private String ubicacion;
    private BigDecimal areaHectareas;
    private String tipoSuelo;
    private String estado;
    private Boolean activo;
    private String poligono;
    private List<CoordinateDTO> coordenadas;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    // Constructors
    public FieldDTO() {}

    public FieldDTO(Long id, String nombre, String ubicacion, BigDecimal areaHectareas) {
        this.id = id;
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.areaHectareas = areaHectareas;
        this.activo = true;
        this.estado = "ACTIVO";
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public BigDecimal getAreaHectareas() {
        return areaHectareas;
    }

    public void setAreaHectareas(BigDecimal areaHectareas) {
        this.areaHectareas = areaHectareas;
    }

    public String getTipoSuelo() {
        return tipoSuelo;
    }

    public void setTipoSuelo(String tipoSuelo) {
        this.tipoSuelo = tipoSuelo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getPoligono() {
        return poligono;
    }

    public void setPoligono(String poligono) {
        this.poligono = poligono;
    }

    public List<CoordinateDTO> getCoordenadas() {
        return coordenadas;
    }

    public void setCoordenadas(List<CoordinateDTO> coordenadas) {
        this.coordenadas = coordenadas;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    @Override
    public String toString() {
        return "FieldDTO{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", areaHectareas=" + areaHectareas +
                ", estado='" + estado + '\'' +
                ", activo=" + activo +
                '}';
    }

    /**
     * DTO interno para coordenadas
     */
    public static class CoordinateDTO {
        private Double lat;
        private Double lng;

        public CoordinateDTO() {}

        public CoordinateDTO(Double lat, Double lng) {
            this.lat = lat;
            this.lng = lng;
        }

        public Double getLat() {
            return lat;
        }

        public void setLat(Double lat) {
            this.lat = lat;
        }

        public Double getLng() {
            return lng;
        }

        public void setLng(Double lng) {
            this.lng = lng;
        }
    }
}
